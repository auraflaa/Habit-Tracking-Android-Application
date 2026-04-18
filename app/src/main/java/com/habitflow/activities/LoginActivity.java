package com.habitflow.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.habitflow.R;
import com.habitflow.util.ThemeManager;

public class LoginActivity extends AppCompatActivity {

    private boolean isLoginMode = true;
    private TextView tv_title, tv_subtitle, tv_error;
    private TextInputLayout til_name, til_email, til_password;
    private TextInputEditText et_name, et_email, et_password;
    private MaterialButton btn_primary, btn_toggle;
    private TextView tv_skip;
    private LinearProgressIndicator progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        bindViews();
        setListeners();
    }

    private void bindViews() {
        tv_title    = findViewById(R.id.tv_title);
        tv_subtitle = findViewById(R.id.tv_subtitle);
        tv_error    = findViewById(R.id.tv_error);
        til_name    = findViewById(R.id.til_name);
        til_email   = findViewById(R.id.til_email);
        til_password = findViewById(R.id.til_password);
        et_name     = findViewById(R.id.et_name);
        et_email    = findViewById(R.id.et_email);
        et_password = findViewById(R.id.et_password);
        btn_primary = findViewById(R.id.btn_primary_action);
        btn_toggle  = findViewById(R.id.btn_toggle_mode);
        tv_skip     = findViewById(R.id.tv_skip);
        progress    = findViewById(R.id.progress);
    }

    private void setListeners() {
        btn_primary.setOnClickListener(v -> handlePrimaryAction());
        btn_toggle.setOnClickListener(v -> toggleMode());
        tv_skip.setOnClickListener(v -> goToMain("User"));
    }

    private void toggleMode() {
        isLoginMode = !isLoginMode;
        tv_error.setVisibility(View.GONE);
        
        // Clear errors
        til_name.setError(null);
        til_email.setError(null);
        til_password.setError(null);

        if (isLoginMode) {
            tv_title.setText(R.string.login_title);
            tv_subtitle.setText(R.string.login_subtitle);
            btn_primary.setText(R.string.btn_login);
            btn_toggle.setText(R.string.link_register);
            til_name.setVisibility(View.GONE);
        } else {
            tv_title.setText(R.string.register_title);
            tv_subtitle.setText(R.string.register_subtitle);
            btn_primary.setText(R.string.btn_register);
            btn_toggle.setText(R.string.link_login);
            til_name.setVisibility(View.VISIBLE);
        }
    }

    private void handlePrimaryAction() {
        String email    = et_email.getText() != null ? et_email.getText().toString().trim() : "";
        String password = et_password.getText() != null ? et_password.getText().toString() : "";
        String name     = et_name.getText() != null ? et_name.getText().toString().trim() : "";

        boolean hasError = false;

        if (TextUtils.isEmpty(email)) {
            til_email.setError("Email required");
            hasError = true;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            til_email.setError("Enter a valid email");
            hasError = true;
        } else {
            til_email.setError(null);
        }

        if (TextUtils.isEmpty(password)) {
            til_password.setError("Password required");
            hasError = true;
        } else if (password.length() < 6) {
            til_password.setError("Min 6 characters");
            hasError = true;
        } else {
            til_password.setError(null);
        }

        if (!isLoginMode) {
            if (TextUtils.isEmpty(name)) {
                til_name.setError("Name required");
                hasError = true;
            } else {
                til_name.setError(null);
            }
        }

        if (hasError) return;

        progress.setVisibility(View.VISIBLE);
        btn_primary.setEnabled(false);

        String displayName;
        if (isLoginMode) {
            String rawPart = email.split("@")[0];
            String namePart = rawPart.split("\\.")[0];
            displayName = namePart.substring(0, 1).toUpperCase() + namePart.substring(1).toLowerCase();
        } else {
            displayName = name;
        }

        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> goToMain(displayName), 800);
    }

    private void goToMain(String name) {
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        prefs.edit().putString("user_name", name).apply();

        startActivity(new Intent(this, MainActivity.class));
        overridePendingTransition(R.anim.slide_up, R.anim.fade_out);
        finish();
    }
}
