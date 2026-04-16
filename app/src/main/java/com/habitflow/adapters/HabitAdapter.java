package com.habitflow.adapters;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.habitflow.R;
import com.habitflow.model.Habit;

import java.util.List;

public class HabitAdapter extends RecyclerView.Adapter<HabitAdapter.HabitVH> {

    public interface OnHabitClick {
        void onCheck(Habit habit, int position);
        void onLongPress(Habit habit, int position);
    }

    private final List<Habit> habits;
    private final OnHabitClick listener;

    public HabitAdapter(List<Habit> habits, OnHabitClick listener) {
        this.habits = habits;
        this.listener = listener;
    }

    @NonNull
    @Override
    public HabitVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_habit, parent, false);
        return new HabitVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull HabitVH holder, int position) {
        Habit h = habits.get(position);
        
        holder.tvName.setText(h.name);
        holder.tvEmoji.setText(h.emoji);
        holder.tvCategory.setText(h.category);
        holder.tvStreak.setText("🔥 " + h.currentStreak);

        // Accent color (optional circle behind emoji)
        int color = Color.parseColor(h.colorHex != null ? h.colorHex : "#728AED");
        
        // Priority dot
        GradientDrawable priorityBg = new GradientDrawable();
        priorityBg.setShape(GradientDrawable.OVAL);
        if (Habit.PRIORITY_HIGH.equals(h.priority)) priorityBg.setColor(Color.parseColor("#FF5252"));
        else if (Habit.PRIORITY_LOW.equals(h.priority)) priorityBg.setColor(Color.parseColor("#7AD326"));
        else priorityBg.setColor(Color.parseColor("#FFD600"));
        holder.viewPriority.setBackground(priorityBg);

        // Completion state
        if (h.completedToday) {
            holder.btnCheck.setBackgroundResource(R.drawable.bg_check_done);
            holder.ivCheckIcon.setVisibility(View.VISIBLE);
            holder.tvName.setAlpha(0.5f);
            holder.tvName.setPaintFlags(holder.tvName.getPaintFlags() | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            holder.btnCheck.setBackgroundResource(R.drawable.bg_check_empty);
            holder.ivCheckIcon.setVisibility(View.GONE);
            holder.tvName.setAlpha(1.0f);
            holder.tvName.setPaintFlags(holder.tvName.getPaintFlags() & (~android.graphics.Paint.STRIKE_THRU_TEXT_FLAG));
        }

        holder.btnCheck.setOnClickListener(v -> {
            if (listener != null) {
                // Perform toggle without notifyDataSetChanged to keep it smooth
                h.completedToday = !h.completedToday;
                listener.onCheck(h, position);
                notifyItemChanged(position);
            }
        });

        holder.itemView.setOnClickListener(v -> {
             if (listener != null) listener.onLongPress(h, position);
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) listener.onLongPress(h, position);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return habits.size();
    }

    static class HabitVH extends RecyclerView.ViewHolder {
        TextView tvName, tvEmoji, tvCategory, tvStreak;
        View viewAccent, viewPriority;
        FrameLayout btnCheck;
        ImageView ivCheckIcon;

        HabitVH(@NonNull View v) {
            super(v);
            tvName = v.findViewById(R.id.tv_name);
            tvEmoji = v.findViewById(R.id.tv_emoji);
            tvCategory = v.findViewById(R.id.tv_category);
            tvStreak = v.findViewById(R.id.tv_streak);
            viewAccent = v.findViewById(R.id.view_accent);
            viewPriority = v.findViewById(R.id.view_priority);
            btnCheck = v.findViewById(R.id.btn_check);
            ivCheckIcon = v.findViewById(R.id.iv_check_icon);
        }
    }
}