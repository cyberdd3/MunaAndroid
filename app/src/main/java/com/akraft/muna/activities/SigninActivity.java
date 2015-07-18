package com.akraft.muna.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.akraft.muna.R;
import com.akraft.muna.Utils;
import com.akraft.muna.models.User;
import com.akraft.muna.models.wrappers.Credentials;
import com.akraft.muna.models.wrappers.Token;
import com.akraft.muna.service.ServiceManager;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import java.util.HashMap;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class SigninActivity extends AppCompatActivity {

    private static final int REGISTER = 1;
    @InjectView(R.id.username_email)
    EditText usernameEmailEditText;
    @InjectView(R.id.password)
    EditText passwordEditText;
    @InjectView(R.id.facebook_login)
    LoginButton facebookLoginButton;

    ServiceManager serviceManager = ServiceManager.getInstance();
    private SharedPreferences storage;
    private User user;
    private CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        storage = getSharedPreferences(Utils.AUTH_PREF, 0);
        FacebookSdk.sdkInitialize(getApplicationContext());

        if (storage.contains("token")) {
            serviceManager.setToken(storage.getString("token", null));
            //TODO check login validity if expired
            finishLogin();
        } else {
            initUI();
        }
    }

    private void initUI() {
        setContentView(R.layout.activity_signin);
        ButterKnife.inject(this);

        callbackManager = CallbackManager.Factory.create();

        facebookLoginButton.setReadPermissions("email", "public_profile");
        facebookLoginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Map<String, String> data = new HashMap<String, String>();
                data.put("token", loginResult.getAccessToken().getToken());

                ServiceManager.getInstance().service.facebookLogin(data, new Callback<Token>() {
                    @Override
                    public void success(Token token, Response response) {
                        loadProfile(new Credentials(token.getUser().getUsername(), token.getUser().getPassword(), null));
                        saveToken(token.getToken());
                    }

                    @Override
                    public void failure(RetrofitError error) {

                    }
                });
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException e) {
                Log.d("dbg", e.toString());
            }
        });
    }


    private void finishLogin() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("user", user);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_signin, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        return id == R.id.action_settings || super.onOptionsItemSelected(item);

    }

    @OnClick(R.id.attempt_signin)
    public void signinClick() {
        String usernameEmail = usernameEmailEditText.getText().toString();
        String email = Utils.isValidEmail(usernameEmail) ? usernameEmail : null;
        String username = email == null ? usernameEmail : null;

        Credentials credentials = new Credentials(username, passwordEditText.getText().toString(), email);
        attemptSignin(credentials);
    }

    private void attemptSignin(final Credentials credentials) {
        serviceManager.service.login(credentials, new Callback<Token>() {
            @Override
            public void success(Token o, Response response) {
                loadProfile(credentials);
                saveToken(o.getToken());
            }

            @Override
            public void failure(RetrofitError error) {
                Toast.makeText(getApplicationContext(), R.string.wrong_credentials, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadProfile(Credentials credentials) {
        serviceManager.service.user(credentials.getUsername(), credentials.getEmail(), null, new Callback<User>() {

            @Override
            public void success(User data, Response response) {
                user = data;
                saveMyProfile(data);
                finishLogin();
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }

    private void saveMyProfile(User user) {
        //ORMManager.getInstance().save(this, user);
        user.save();

        SharedPreferences.Editor editor = getSharedPreferences(Utils.AUTH_PREF, 0).edit();
        editor.putLong("id", user.getId());

        editor.apply();
    }

    @OnClick(R.id.register)
    public void register() {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivityForResult(intent, REGISTER);
    }

    private void saveToken(String token) {
        SharedPreferences.Editor editor = getSharedPreferences(Utils.AUTH_PREF, 0).edit();
        editor.putString("token", token);
        editor.apply();

        serviceManager.setToken(token);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REGISTER) {
            if (resultCode == RESULT_OK) {
                attemptSignin((Credentials) data.getParcelableExtra("credentials"));
            }
        } else {
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }

    }

    public String[] getUsernameOrEmail() {
        String text = usernameEmailEditText.getText().toString();
        String[] usernameAndEmail = new String[2];
        usernameAndEmail[Utils.isValidEmail(text) ? 1 : 0] = text;
        return usernameAndEmail;
    }

    @Override
    protected void onResume() {
        super.onResume();
        AppEventsLogger.activateApp(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        AppEventsLogger.deactivateApp(this);
    }
}
