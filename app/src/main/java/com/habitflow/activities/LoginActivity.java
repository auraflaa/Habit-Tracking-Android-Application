package com.habitflow.activities;

import android.content.Intent;
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

    // UI state
    private boolean isLoginMode = true;

    // Views
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
        til_password= findViewById(R.id.til_password);
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
        tv_skip.setOnClickListener(v -> goToMain());
    }

    // ── Toggle login ↔ register ───────────────────────────────────────────────
    private void toggleMode() {
        isLoginMode = !isLoginMode;
        tv_error.setVisibility(View.GONE);

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

        // Animate title
        tv_title.animate().alpha(0f).setDuration(150).withEndAction(() -> {
            tv_title.animate().alpha(1f).setDuration(150).start();
        }).start();
    }

    // ── Handle sign in / register ─────────────────────────────────────────────
    private void handlePrimaryAction() {
        tv_error.setVisibility(View.GONE);
        String email    = et_email.getText() != null ? et_email.getText().toString().trim() : "";
        String password = et_password.getText() != null ? et_password.getText().toString() : "";

        // Basic validation
        if (TextUtils.isEmpty(email)) {
            til_email.setError("Please enter your email");
            return;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            til_email.setError("Enter a valid email address");
            return;
        }
        til_email.setError(null);

        if (TextUtils.isEmpty(password) || password.length() < 6) {
            til_password.setError("Password must be at least 6 characters");
            return;
        }
        til_password.setError(null);

        if (!isLoginMode) {
            String name = et_name.getText() != null ? et_name.getText().toString().trim() : "";
            if (TextUtils.isEmpty(name)) {
                til_name.setError("Please enter your name");
                return;
            }
            til_name.setError(null);
        }

        // Show progress, simulate auth delay
        progress.setVisibility(View.VISIBLE);
        btn_primary.setEnabled(false);

        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            progress.setVisibility(View.GONE);
            btn_primary.setEnabled(true);
            // TODO: plug in real Firebase / backend auth here
            goToMain();
        }, 1200);
    }

    // ── Navigate to MainActivity ──────────────────────────────────────────────
    private void goToMain() {
        startActivity(new Intent(this, MainActivity.class));
        overridePendingTransition(R.anim.slide_up, R.anim.fade_out);
        finish();
    }
}
