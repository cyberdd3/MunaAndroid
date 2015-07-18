package com.akraft.muna.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;

import com.akraft.muna.R;
import com.akraft.muna.Utils;
import com.akraft.muna.models.wrappers.Credentials;
import com.akraft.muna.service.MainService;
import com.akraft.muna.service.ServiceManager;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class RegisterActivity extends AppCompatActivity {

    private static final int PASSWORD_MIN_LENGTH = 6;
    @InjectView(R.id.username) EditText usernameEditText;
    @InjectView(R.id.email) EditText emailEditText;
    @InjectView(R.id.password) EditText passwordEditText;
    @InjectView(R.id.password_again) EditText passwordAgainEditText;
    @InjectView(R.id.attempt_register) Button attemptRegister;
    private MainService service;
    private boolean hasErrors;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ButterKnife.inject(this);

        service = ServiceManager.getInstance().service;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_register, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @OnClick(R.id.attempt_register)
    public void attemptRegister() {
        validateForm();

    }

    private void register() {
        final Credentials credentials = new Credentials(usernameEditText.getText().toString(),
                passwordEditText.getText().toString(), emailEditText.getText().toString());

        service.register(credentials, new Callback<Object>() {
            @Override
            public void success(Object o, Response response) {
                Intent returnIntent = new Intent();
                returnIntent.putExtra("credentials", credentials);
                setResult(RESULT_OK, returnIntent);
                finish();
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }

    private void validateForm() {
        hasErrors = false;

        if (usernameEditText.getText().length() > 2) {
            if (usernameEditText.getText().length() > 30) {
                setError(usernameEditText, getResources().getString(R.string.username_too_long));
            }
            service.usernameExists(usernameEditText.getText().toString(), new Callback<Boolean>() {
                @Override
                public void success(Boolean notExists, Response response) {
                    if (!notExists)
                        setError(usernameEditText, getResources().getString(R.string.username_exists));
                    else if (!hasErrors)
                        register();
                }

                @Override
                public void failure(RetrofitError error) {

                }
            });
        } else {
            setError(usernameEditText, getResources().getString(R.string.username_too_short));
        }

        if (!Utils.isValidEmail(emailEditText.getText().toString())) {
            setError(emailEditText, getResources().getString(R.string.email_error));
        }

        if (emailEditText.getText().length() > 3) {
            if (emailEditText.getText().length() > 254) {
                setError(emailEditText, getResources().getString(R.string.email_too_long));
            }
            service.emailExists(emailEditText.getText().toString(), new Callback<Boolean>() {
                @Override
                public void success(Boolean notExists, Response response) {
                    if (!notExists)
                        emailEditText.setError(getResources().getString(R.string.email_exists));
                    else if (!hasErrors)
                        register();
                }

                @Override
                public void failure(RetrofitError error) {

                }
            });
        }
        if (passwordEditText.getText().length() < PASSWORD_MIN_LENGTH)
            setError(passwordEditText, String.format(getResources().getString(R.string.password_error), PASSWORD_MIN_LENGTH));

        if (!passwordEditText.getText().toString().equals(passwordAgainEditText.getText().toString())) {
            setError(passwordAgainEditText, getResources().getString(R.string.passwords_dont_match));
        }

    }

    private void setError(EditText editText, String error) {
        editText.setError(error);
        hasErrors = true;
    }
}
