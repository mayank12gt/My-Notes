package com.example.mynotes;

import com.example.mynotes.entities.Note;

public interface NotesOnClickListener {

    void onNoteClicked(Note note, int position);
}
