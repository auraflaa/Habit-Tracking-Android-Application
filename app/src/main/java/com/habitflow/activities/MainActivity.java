package com.habitflow.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.habitflow.R;
import com.habitflow.fragments.AddHabitSheet;
import com.habitflow.fragments.CalendarFragment;
import com.habitflow.fragments.HomeFragment;
import com.habitflow.fragments.ProgressFragment;
import com.habitflow.fragments.SettingsFragment;
import com.habitflow.util.NotificationHelper;
import com.habitflow.util.ThemeManager;

public class MainActivity extends AppCompatActivity {

    private static final int NOTIFICATION_PERMISSION_CODE = 101;

    private BottomNavigationView bottomNav;
    private FloatingActionButton fab;

    private HomeFragment     homeFragment;
    private CalendarFragment calendarFragment;
    private ProgressFragment progressFragment;
    private SettingsFragment settingsFragment;
    private Fragment         activeFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Notification Channel
        NotificationHelper.createNotificationChannel(this);

        bottomNav = findViewById(R.id.bottom_nav);
        fab       = findViewById(R.id.fab_add);

        initFragments(savedInstanceState);
        setupBottomNav();
        setupFab();
        checkNotificationPermission();
    }

    private void checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_PERMISSION_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == NOTIFICATION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, notifications should work now
            }
        }
    }

    // ── Fragment management ───────────────────────────────────────────────────

    private void initFragments(Bundle saved) {
        FragmentManager fm = getSupportFragmentManager();
        
        if (saved != null) {
            homeFragment     = (HomeFragment) fm.findFragmentByTag("home");
            calendarFragment = (CalendarFragment) fm.findFragmentByTag("calendar");
            progressFragment = (ProgressFragment) fm.findFragmentByTag("progress");
            settingsFragment = (SettingsFragment) fm.findFragmentByTag("settings");

            if (homeFragment != null && !homeFragment.isHidden()) activeFragment = homeFragment;
            else if (calendarFragment != null && !calendarFragment.isHidden()) activeFragment = calendarFragment;
            else if (progressFragment != null && !progressFragment.isHidden()) activeFragment = progressFragment;
            else if (settingsFragment != null && !settingsFragment.isHidden()) activeFragment = settingsFragment;
            
            if (activeFragment == null) activeFragment = homeFragment;
            return;
        }

        homeFragment     = new HomeFragment();
        calendarFragment = new CalendarFragment();
        progressFragment = new ProgressFragment();
        settingsFragment = new SettingsFragment();

        fm.beginTransaction()
                .add(R.id.fragment_container, homeFragment,     "home")
                .add(R.id.fragment_container, calendarFragment, "calendar").hide(calendarFragment)
                .add(R.id.fragment_container, progressFragment, "progress").hide(progressFragment)
                .add(R.id.fragment_container, settingsFragment, "settings").hide(settingsFragment)
                .commit();

        activeFragment = homeFragment;
    }

    private void showFragment(Fragment target) {
        if (target == null || target == activeFragment) return;
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                .hide(activeFragment)
                .show(target)
                .commit();
        activeFragment = target;
    }

    // ── Bottom nav ────────────────────────────────────────────────────────────

    private void setupBottomNav() {
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_today) {
                showFragment(homeFragment);
            } else if (id == R.id.nav_calendar) {
                showFragment(calendarFragment);
            } else if (id == R.id.nav_progress) {
                showFragment(progressFragment);
            } else if (id == R.id.nav_settings) {
                showFragment(settingsFragment);
            }
            return true;
        });
    }

    // ── FAB ───────────────────────────────────────────────────────────────────

    private void setupFab() {
        fab.setOnClickListener(v -> {
            AddHabitSheet sheet = AddHabitSheet.newInstance(null);
            sheet.setOnSaveListener(this::notifyDataChanged);
            sheet.show(getSupportFragmentManager(), "add_habit");
        });
    }

    /** Called when data in HabitStore changes (added, edited, or toggled) */
    public void notifyDataChanged() {
        if (homeFragment != null) homeFragment.refreshData();
        if (calendarFragment != null) calendarFragment.onHabitAdded();
        if (progressFragment != null) progressFragment.onHabitAdded();
    }

    public void restartForTheme() {
        recreate();
    }
}
