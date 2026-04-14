package com.habitflow.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.habitflow.R;
import com.habitflow.activities.LoginActivity;
import com.habitflow.util.ThemeManager;

public class SettingsFragment extends Fragment {

    private androidx.gridlayout.widget.GridLayout gridThemes;
    private SwitchMaterial switchNotifs;
    private static final String PREFS_SETTINGS = "app_settings";
    private static final String KEY_NOTIFS = "notifications_enabled";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        gridThemes = view.findViewById(R.id.grid_themes);
        switchNotifs = view.findViewById(R.id.switch_notifs);
        
        loadSettings();
        setupThemePicker();
        setupClickListeners(view);
    }

    private void loadSettings() {
        SharedPreferences prefs = requireContext().getSharedPreferences(PREFS_SETTINGS, Context.MODE_PRIVATE);
        boolean notifsEnabled = prefs.getBoolean(KEY_NOTIFS, true);
        switchNotifs.setChecked(notifsEnabled);
        
        switchNotifs.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean(KEY_NOTIFS, isChecked).apply();
            String status = isChecked ? "enabled" : "disabled";
            Toast.makeText(getContext(), "Notifications " + status, Toast.LENGTH_SHORT).show();
        });
    }

    private void setupClickListeners(View view) {
        // Reminders
        view.findViewById(R.id.row_reminders).setOnClickListener(v -> {
            Toast.makeText(getContext(), "Reminders feature coming soon!", Toast.LENGTH_SHORT).show();
        });

        // Rate on Play Store
        view.findViewById(R.id.row_rate).setOnClickListener(v -> {
            String packageName = requireContext().getPackageName();
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packageName)));
            } catch (android.content.ActivityNotFoundException anfe) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + packageName)));
            }
        });

        // Share HabitFlow
        view.findViewById(R.id.row_share).setOnClickListener(v -> {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, "Check out HabitFlow! It's helping me build better habits. https://play.google.com/store/apps/details?id=" + requireContext().getPackageName());
            sendIntent.setType("text/plain");
            startActivity(Intent.createChooser(sendIntent, "Share via"));
        });

        // Sign Out
        view.findViewById(R.id.row_logout).setOnClickListener(v -> {
            showLogoutConfirmation();
        });
    }

    private void showLogoutConfirmation() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_logout_confirmation, null);
        
        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext(), R.style.Theme_HabitFlow_Dialog)
                .setView(dialogView)
                .create();

        MaterialButton btnCancel = dialogView.findViewById(R.id.btn_cancel);
        MaterialButton btnLogout = dialogView.findViewById(R.id.btn_logout);

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnLogout.setOnClickListener(v -> {
            dialog.dismiss();
            Toast.makeText(getContext(), "Signed out successfully", Toast.LENGTH_SHORT).show();
            
            Intent intent = new Intent(requireActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            requireActivity().finish();
        });

        dialog.show();
    }

    private void setupThemePicker() {
        String[] themeKeys = {
                ThemeManager.THEME_DARK, ThemeManager.THEME_LIGHT, ThemeManager.THEME_OCEAN,
                ThemeManager.THEME_SUNSET, ThemeManager.THEME_FOREST, ThemeManager.THEME_AMOLED
        };

        gridThemes.removeAllViews();
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int itemWidth = (screenWidth - dpToPx(64)) / 3;

        for (String key : themeKeys) {
            View itemView = createThemeItem(key, itemWidth);
            gridThemes.addView(itemView);
        }
    }

    private View createThemeItem(String key, int width) {
        LinearLayout container = new LinearLayout(getContext());
        container.setOrientation(LinearLayout.VERTICAL);
        container.setGravity(Gravity.CENTER);
        container.setPadding(dpToPx(8), dpToPx(12), dpToPx(8), dpToPx(12));

        // Circle Preview
        View circle = new View(getContext());
        int circleSize = dpToPx(56);
        LinearLayout.LayoutParams lpCircle = new LinearLayout.LayoutParams(circleSize, circleSize);
        lpCircle.bottomMargin = dpToPx(8);
        circle.setLayoutParams(lpCircle);

        GradientDrawable gd = new GradientDrawable();
        gd.setShape(GradientDrawable.OVAL);
        gd.setColor(Color.parseColor(ThemeManager.previewColorFor(key)));
        gd.setStroke(dpToPx(2), Color.parseColor(ThemeManager.accentColorFor(key)));
        circle.setBackground(gd);

        // Label
        TextView label = new TextView(getContext());
        label.setText(ThemeManager.labelFor(key));
        label.setTextSize(12);
        label.setTextColor(getResources().getColor(R.color.text_secondary));
        label.setGravity(Gravity.CENTER);

        container.addView(circle);
        container.addView(label);

        // Selection state
        if (key.equals(ThemeManager.getSavedTheme(requireContext()))) {
            container.setBackgroundResource(R.drawable.bg_card_selected);
        }

        container.setOnClickListener(v -> {
            ThemeManager.saveTheme(requireContext(), key);
            requireActivity().recreate(); // Reapply theme
        });

        return container;
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }
}
