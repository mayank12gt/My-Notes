package com.example.mynotes.adapters;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;

import com.example.mynotes.entities.Note;

import java.util.Objects;

public class NotesComparator extends DiffUtil.ItemCallback<Note> {
    @Override
    public boolean areItemsTheSame(@NonNull Note oldItem, @NonNull Note newItem) {
        return Objects.equals(oldItem.getID(), newItem.getID());
    }

    @Override
    public boolean areContentsTheSame(@NonNull Note oldItem, @NonNull Note newItem) {
        return Objects.equals(oldItem, newItem);
    }
}
