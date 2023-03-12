package com.example.mynotes.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.mynotes.entities.Note;

import java.util.List;

@Dao
public interface NoteDao {

@Insert(onConflict = OnConflictStrategy.REPLACE)
    public void addNote(Note note);

@Delete
    public void deleteNote(Note note);

    @Query("select * from notes order by id DESC ")
    public  List<Note> getAllNotes();


}
