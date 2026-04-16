package com.habitflow.fragments;

import android.app.TimePickerDialog;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.habitflow.R;
import com.habitflow.data.HabitStore;
import com.habitflow.model.Habit;
import com.habitflow.util.ReminderManager;

import java.util.Locale;

public class AddHabitSheet extends BottomSheetDialogFragment {

    private Habit habitToEdit;
    private Runnable onSaveListener;

    private EditText etName, etDesc;
    private TextView tvSheetTitle, tvReminderTime;
    private ChipGroup chipsCategory, chipsPriority, chipsSegment;
    private LinearLayout llEmojis, llColors;
    private SwitchMaterial switchNotify;
    private String selectedEmoji = "🏃";
    private String selectedColor = "#FF5252";
    private String selectedTime = "08:00";

    private final String[] emojis = {"🏃", "📚", "🧘", "🥗", "⚡", "🤝", "💧", "✍️", "🍎", "🚲", "💻", "🎸", "💊", "🚭", "💵", "🧹", "🛌", "🚶", "🏊", "🧠"};
    private final String[] colors = {"#FF5252", "#FFD600", "#00BCD4", "#7AD326", "#728AED", "#9C6AE6", "#FF9800", "#E91E63", "#4CAF50", "#2196F3"};

    public static AddHabitSheet newInstance(Habit habit) {
        AddHabitSheet fragment = new AddHabitSheet();
        fragment.habitToEdit = habit;
        return fragment;
    }

    public void setOnSaveListener(Runnable listener) {
        this.onSaveListener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_add_habit, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        bindViews(view);
        setupEmojiPicker();
        setupColorPicker();
        setupTimePicker();

        if (habitToEdit != null) {
            setupEditMode(view);
        }

        view.findViewById(R.id.btn_close).setOnClickListener(v -> dismiss());
        view.findViewById(R.id.btn_save).setOnClickListener(v -> saveHabit());
        view.findViewById(R.id.btn_delete).setOnClickListener(v -> deleteHabit());
    }

    private void bindViews(View v) {
        tvSheetTitle = v.findViewById(R.id.tv_sheet_title);
        etName = v.findViewById(R.id.et_name);
        etDesc = v.findViewById(R.id.et_desc);
        chipsCategory = v.findViewById(R.id.chips_category);
        chipsPriority = v.findViewById(R.id.chips_priority);
        chipsSegment = v.findViewById(R.id.chips_segment);
        llEmojis = v.findViewById(R.id.ll_emojis);
        llColors = v.findViewById(R.id.ll_colors);
        switchNotify = v.findViewById(R.id.switch_reminder);
        tvReminderTime = v.findViewById(R.id.tv_reminder_time);
    }

    private void setupTimePicker() {
        tvReminderTime.setOnClickListener(v -> {
            String[] parts = selectedTime.split(":");
            int h = Integer.parseInt(parts[0]);
            int m = Integer.parseInt(parts[1]);

            new TimePickerDialog(getContext(), (view, hourOfDay, minute) -> {
                selectedTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
                tvReminderTime.setText(selectedTime);
            }, h, m, true).show();
        });
    }

    private void setupEmojiPicker() {
        for (String emoji : emojis) {
            TextView tv = new TextView(getContext());
            tv.setText(emoji);
            tv.setTextSize(24);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, 0, 16, 0);
            tv.setLayoutParams(lp);
            tv.setPadding(12, 12, 12, 12);
            tv.setOnClickListener(v -> {
                selectedEmoji = emoji;
                updateEmojiSelection();
            });
            llEmojis.addView(tv);
        }
        updateEmojiSelection();
    }

    private void updateEmojiSelection() {
        for (int i = 0; i < llEmojis.getChildCount(); i++) {
            TextView tv = (TextView) llEmojis.getChildAt(i);
            if (tv.getText().toString().equals(selectedEmoji)) {
                tv.setBackgroundResource(R.drawable.bg_icon_btn_selected);
            } else {
                tv.setBackground(null);
            }
        }
    }

    private void setupColorPicker() {
        for (String color : colors) {
            View v = new View(getContext());
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(80, 80);
            lp.setMargins(0, 0, 16, 0);
            v.setLayoutParams(lp);
            
            GradientDrawable gd = new GradientDrawable();
            gd.setShape(GradientDrawable.OVAL);
            gd.setColor(Color.parseColor(color));
            v.setBackground(gd);

            v.setOnClickListener(view -> {
                selectedColor = color;
                updateColorSelection();
            });
            llColors.addView(v);
        }
        updateColorSelection();
    }

    private void updateColorSelection() {
        for (int i = 0; i < llColors.getChildCount(); i++) {
            View v = llColors.getChildAt(i);
            if (colors[i].equals(selectedColor)) {
                v.setScaleX(1.2f);
                v.setScaleY(1.2f);
            } else {
                v.setScaleX(1.0f);
                v.setScaleY(1.0f);
            }
        }
    }

    private void setupEditMode(View view) {
        tvSheetTitle.setText("Edit Habit");
        etName.setText(habitToEdit.name);
        etDesc.setText(habitToEdit.description);
        selectedEmoji = habitToEdit.emoji;
        selectedColor = habitToEdit.colorHex;
        selectedTime = habitToEdit.notifyTime.isEmpty() ? "08:00" : habitToEdit.notifyTime;
        tvReminderTime.setText(selectedTime);
        switchNotify.setChecked(habitToEdit.notifyEnabled);
        
        updateEmojiSelection();
        updateColorSelection();

        setChipSelected(chipsCategory, habitToEdit.category);
        setChipSelected(chipsPriority, habitToEdit.priority);
        setChipSelected(chipsSegment, habitToEdit.segment);

        view.findViewById(R.id.btn_delete).setVisibility(View.VISIBLE);
    }

    private void setChipSelected(ChipGroup group, String text) {
        for (int i = 0; i < group.getChildCount(); i++) {
            Chip chip = (Chip) group.getChildAt(i);
            if (chip.getText().toString().contains(text)) {
                chip.setChecked(true);
                return;
            }
        }
    }

    private void saveHabit() {
        String name = etName.getText().toString().trim();
        if (name.isEmpty()) {
            Toast.makeText(getContext(), "Please enter a name", Toast.LENGTH_SHORT).show();
            return;
        }

        Habit h = (habitToEdit != null) ? habitToEdit : new Habit();
        h.name = name;
        h.description = etDesc.getText().toString().trim();
        h.emoji = selectedEmoji;
        h.colorHex = selectedColor;
        h.category = getSelectedChipText(chipsCategory);
        h.priority = getSelectedChipText(chipsPriority);
        h.segment = getSelectedChipText(chipsSegment);
        h.notifyEnabled = switchNotify.isChecked();
        h.notifyTime = selectedTime;

        if (habitToEdit != null) {
            HabitStore.get(requireContext()).update(requireContext(), h);
        } else {
            HabitStore.get(requireContext()).add(requireContext(), h);
        }

        // Handle Reminder scheduling
        ReminderManager.scheduleReminder(requireContext(), h);

        if (onSaveListener != null) onSaveListener.run();
        dismiss();
    }

    private String getSelectedChipText(ChipGroup group) {
        int id = group.getCheckedChipId();
        if (id == View.NO_ID) return "";
        Chip chip = group.findViewById(id);
        String text = chip.getText().toString();
        return text.replaceAll("[^a-zA-Z]", "").trim();
    }

    private void deleteHabit() {
        if (habitToEdit != null) {
            ReminderManager.cancelReminder(requireContext(), habitToEdit);
            HabitStore.get(requireContext()).delete(requireContext(), habitToEdit.id);
            if (onSaveListener != null) onSaveListener.run();
            dismiss();
        }
    }
}
