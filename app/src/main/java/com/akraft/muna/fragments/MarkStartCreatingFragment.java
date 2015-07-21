package com.akraft.muna.fragments;

import android.animation.Animator;
import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.akraft.muna.Config;
import com.akraft.muna.R;
import com.akraft.muna.callbacks.MarkCreatingCallback;
import com.akraft.muna.models.Mark;
import com.akraft.muna.service.ServiceManager;

import java.util.regex.Pattern;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class MarkStartCreatingFragment extends Fragment{
    private MarkCreatingCallback mCallback;

    @InjectView(R.id.codeword)
    EditText codewordText;
    @InjectView(R.id.start)
    FloatingActionButton startButton;
    @InjectView(R.id.codeword_requirements)
    TextView codewordRequirementsText;

    private Mark mark;
    private int screenHeight;

    private boolean enabledToProceed = false;

    Pattern codewordPattern = Pattern.compile("[\\p{L}\\p{Digit}\\p{Space}]{" + Config.CODEWORD_MIN_LENGTH + ",}");

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_mark_start_creating, container, false);
        ButterKnife.inject(this, rootView);

        screenHeight = getActivity().getResources().getDisplayMetrics().heightPixels;


        codewordText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (isCodewordCorrect(s.toString()))
                    enableProceed();
                else
                    disableProceed();
            }
        });

        mark = getArguments().getParcelable("mark");
        if (mark != null && mark.getCodeword() != null && !mark.getCodeword().equals("")) {
            codewordText.setText(mark.getCodeword());
            codewordRequirementsText.setAlpha(0.0f);
        } else {
            startButton.setAlpha(0.0f);
            startButton.setTranslationY(screenHeight);
            codewordRequirementsText.setAlpha(1.0f);
        }

        return rootView;
    }

    private boolean isCodewordCorrect(String s) {
        s = s.trim();

        return codewordPattern.matcher(s).matches();
    }

    private void enableProceed() {
        if (!enabledToProceed) {
            startButton.setEnabled(true);
            startButton.animate().alpha(1.0f).translationY(0).setInterpolator(new AccelerateDecelerateInterpolator());
            codewordRequirementsText.animate().alpha(0.0f);
        }

        enabledToProceed = true;
    }

    private void disableProceed() {
        if (enabledToProceed) {
            startButton.setEnabled(false);
            startButton.animate().alpha(0.0f).translationY(screenHeight).setInterpolator(new AccelerateDecelerateInterpolator());
            codewordRequirementsText.animate().alpha(1.0f);
        }
        enabledToProceed = false;
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mCallback = (MarkCreatingCallback) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnMarkLocationChosen");
        }
    }

    @OnClick(R.id.start)
    public void startCreating() {
        InputMethodManager inputManager = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(codewordText.getWindowToken(), 0);

        mCallback.started(codewordText.getText().toString().trim());
        mCallback.next();
    }

    @OnClick(R.id.generate)
    public void generateCodeword() {

        ServiceManager.getInstance().service.generateCodeword(new Callback<String>() {
            @Override
            public void success(String s, Response response) {
                codewordText.setText(s);
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }

}
