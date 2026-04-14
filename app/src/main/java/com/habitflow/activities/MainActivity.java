package com.habitflow.activities;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
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
        if (calendarFragment != null) calendarFragment.onHabitAdded(); // Using existing method
        if (progressFragment != null) progressFragment.onHabitAdded(); // Using existing method
    }

    public void restartForTheme() {
        recreate();
    }
}
