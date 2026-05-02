package com.habitflow.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.habitflow.R;
import com.habitflow.activities.LoginActivity;
import com.habitflow.adapters.ReminderSettingAdapter;
import com.habitflow.data.HabitStore;
import com.habitflow.model.Habit;
import com.habitflow.util.ReminderManager;
import com.habitflow.util.ThemeManager;

import java.util.List;

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
        view.findViewById(R.id.row_reminders).setOnClickListener(v -> showRemindersDialog());

        view.findViewById(R.id.row_rate).setOnClickListener(v -> {
            String packageName = requireContext().getPackageName();
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packageName)));
            } catch (android.content.ActivityNotFoundException anfe) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + packageName)));
            }
        });

        view.findViewById(R.id.row_share).setOnClickListener(v -> {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, "Check out HabitFlow! https://play.google.com/store/apps/details?id=" + requireContext().getPackageName());
            sendIntent.setType("text/plain");
            startActivity(Intent.createChooser(sendIntent, "Share via"));
        });

        view.findViewById(R.id.row_logout).setOnClickListener(v -> showLogoutConfirmation());
    }

    private void showRemindersDialog() {
        View v = LayoutInflater.from(getContext()).inflate(R.layout.dialog_reminders_list, null);
        RecyclerView rv = v.findViewById(R.id.rv_reminders_list);
        
        List<Habit> habits = HabitStore.get(requireContext()).getHabits();
        ReminderSettingAdapter adapter = new ReminderSettingAdapter(habits, h -> {
            HabitStore.get(requireContext()).update(requireContext(), h);
            ReminderManager.scheduleReminder(requireContext(), h);
        });
        
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.setAdapter(adapter);

        new MaterialAlertDialogBuilder(requireContext(), R.style.Theme_HabitFlow_Dialog)
                .setTitle("Habit Reminders")
                .setView(v)
                .setPositiveButton("Done", null)
                .show();
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
            com.google.firebase.auth.FirebaseAuth.getInstance().signOut();
            
            // Clear local cache for the singleton to prevent data leak to next user
            com.habitflow.data.HabitStore.reset();
            
            // Also sign out from Google to allow account switching next time
            com.google.android.gms.auth.api.signin.GoogleSignInOptions gso = new com.google.android.gms.auth.api.signin.GoogleSignInOptions.Builder(com.google.android.gms.auth.api.signin.GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestEmail()
                    .build();
            com.google.android.gms.auth.api.signin.GoogleSignIn.getClient(requireActivity(), gso).signOut();

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
        // Calculate item width based on grid column count
        gridThemes.post(() -> {
            int gridWidth = gridThemes.getWidth() - gridThemes.getPaddingLeft() - gridThemes.getPaddingRight();
            int itemWidth = gridWidth / 3;

            for (String key : themeKeys) {
                gridThemes.addView(createThemeItem(key, itemWidth));
            }
        });
    }

    private View createThemeItem(String key, int width) {
        boolean isSelected = key.equals(ThemeManager.getSavedTheme(requireContext()));
        
        LinearLayout container = new LinearLayout(getContext());
        container.setOrientation(LinearLayout.VERTICAL);
        container.setGravity(Gravity.CENTER);
        container.setPadding(0, dpToPx(12), 0, dpToPx(12));
        
        androidx.gridlayout.widget.GridLayout.LayoutParams lp = new androidx.gridlayout.widget.GridLayout.LayoutParams();
        lp.width = width;
        container.setLayoutParams(lp);

        View preview = new View(getContext());
        int size = dpToPx(48);
        LinearLayout.LayoutParams lpPreview = new LinearLayout.LayoutParams(size, size);
        lpPreview.bottomMargin = dpToPx(8);
        preview.setLayoutParams(lpPreview);

        GradientDrawable gd = new GradientDrawable();
        gd.setCornerRadius(dpToPx(12));
        gd.setColor(Color.parseColor(ThemeManager.previewColorFor(key)));
        
        if (isSelected) {
            gd.setStroke(dpToPx(3), Color.parseColor(ThemeManager.accentColorFor(key)));
        } else {
            // Use a theme-aware border color for unselected items
            int borderColor = 0x338A8880;
            android.util.TypedValue typedValue = new android.util.TypedValue();
            if (getContext().getTheme().resolveAttribute(R.attr.customBorderColor, typedValue, true)) {
                borderColor = typedValue.data;
            }
            gd.setStroke(dpToPx(1), borderColor);
        }
        preview.setBackground(gd);

        TextView label = new TextView(getContext());
        label.setText(ThemeManager.labelFor(key));
        label.setTextSize(12);
        
        int textSecondary = 0xFF8A8880;
        android.util.TypedValue typedValue = new android.util.TypedValue();
        if (getContext().getTheme().resolveAttribute(R.attr.customTextSecondary, typedValue, true)) {
            textSecondary = typedValue.data;
        }

        label.setTextColor(isSelected ? Color.parseColor(ThemeManager.accentColorFor(key)) : textSecondary);
        if (isSelected) label.setTypeface(null, Typeface.BOLD);
        label.setGravity(Gravity.CENTER);

        container.addView(preview);
        container.addView(label);

        container.setOnClickListener(v -> {
            if (!isSelected) {
                ThemeManager.saveTheme(requireContext(), key);
                requireActivity().recreate();
            }
        });

        return container;
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }
}
