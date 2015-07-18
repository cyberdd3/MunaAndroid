package com.akraft.muna.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.akraft.muna.R;
import com.akraft.muna.models.Mark;
import com.akraft.muna.models.Profile;
import com.akraft.muna.service.ServiceManager;

import java.util.HashMap;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class CountMarkFragment extends Fragment {

    private Mark mark;

    @InjectView(R.id.codeword)
    EditText codewordEdit;
    @InjectView(R.id.name)
    TextView nameText;
    @InjectView(R.id.switcher)
    ViewSwitcher switcher;
    @InjectView(R.id.count_mark)
    Button countMarkButton;

    private SuccessListener successListener;

    public CountMarkFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_count_mark, container, false);

        mark = (Mark) getArguments().getParcelable("mark");
        ButterKnife.inject(this, rootView);
        nameText.setText(mark.getName());
        codewordEdit.addTextChangedListener(textWatcher);

        countMarkButton.setEnabled(false);
        countMarkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switcher.showNext();
                Map<String, String> data = new HashMap<>();
                data.put("mark_id", String.valueOf(mark.getId()));
                data.put("codeword", codewordEdit.getText().toString());
                ServiceManager.getInstance().service.countMark(data, resultListener);
            }
        });

        return rootView;
    }

    public void setSuccessListener(SuccessListener successListener) {
        this.successListener = successListener;
    }


    Callback<Profile> resultListener = new Callback<Profile>() {
        @Override
        public void success(Profile result, Response response) {
            showToast("You have completed this mark!\n" + result.getExp() + " exp now");
            if (successListener != null)
                successListener.markCompleted();

        }

        @Override
        public void failure(RetrofitError error) {

            switch (error.getResponse().getStatus()) {
                case 400:
                    showToast(R.string.wrong_codeword);
                    break;
                case 500:
                    showToast(R.string.server_error);
                    break;
            }
            switcher.showPrevious();
        }
    };

    private void showToast(int strId) {
        showToast(getParentFragment().getActivity().getResources().getString(strId));
    }

    private void showToast(String string) {
        Toast.makeText(getParentFragment().getActivity(), string, Toast.LENGTH_SHORT).show();
    }


    TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            countMarkButton.setEnabled(s.length() > 0);
        }
    };

    public interface SuccessListener {
        void markCompleted();
    }

}
