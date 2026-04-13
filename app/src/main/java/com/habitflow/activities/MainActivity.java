package com.habitflow.activities;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.habitflow.R;
import com.habitflow.fragments.AddHabitSheet;
import com.habitflow.fragments.CalendarFragment;
import com.habitflow.fragments.HomeFragment;
import com.habitflow.fragments.ProgressFragment;
import com.habitflow.fragments.SettingsFragment;
import com.habitflow.util.ThemeManager;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;
    private FloatingActionButton fab;

    // Keep fragment instances alive to preserve scroll state
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

        bottomNav = findViewById(R.id.bottom_nav);
        fab       = findViewById(R.id.fab_add);

        initFragments(savedInstanceState);
        setupBottomNav();
        setupFab();
    }

    // ── Fragment management ───────────────────────────────────────────────────

    private void initFragments(Bundle saved) {
        if (saved != null) return; // Fragments already re-created by system

        homeFragment     = new HomeFragment();
        calendarFragment = new CalendarFragment();
        progressFragment = new ProgressFragment();
        settingsFragment = new SettingsFragment();

        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container, homeFragment,     "home")
                .add(R.id.fragment_container, calendarFragment, "calendar").hide(calendarFragment)
                .add(R.id.fragment_container, progressFragment, "progress").hide(progressFragment)
                .add(R.id.fragment_container, settingsFragment, "settings").hide(settingsFragment)
                .commit();

        activeFragment = homeFragment;
    }

    private void showFragment(Fragment target) {
        if (target == activeFragment) return;
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
                fab.show();
            } else if (id == R.id.nav_calendar) {
                showFragment(calendarFragment);
                fab.hide();
            } else if (id == R.id.nav_progress) {
                showFragment(progressFragment);
                fab.hide();
            } else if (id == R.id.nav_settings) {
                showFragment(settingsFragment);
                fab.hide();
            }
            return true;
        });
    }

    // ── FAB ───────────────────────────────────────────────────────────────────

    private void setupFab() {
        fab.setOnClickListener(v -> {
            AddHabitSheet sheet = AddHabitSheet.newInstance(null); // null = add mode
            sheet.show(getSupportFragmentManager(), "add_habit");
        });
    }

    // ── Called by SettingsFragment when theme changes ─────────────────────────

    public void restartForTheme() {
        recreate(); // Re-creates the activity with the new theme applied in onCreate
    }
}
