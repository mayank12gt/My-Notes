package com.example.mynotes.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.mynotes.entities.Note;

import java.util.List;

@Dao
public interface NoteDao {

@Insert(onConflict = OnConflictStrategy.REPLACE)
void addNote(Note note);

@Delete
void deleteNote(Note note);

    @Query("select * from notes order by id DESC ")
    List<Note> getAllNotes();

    @Update
    void UpdateNote(Note note);

    @Delete
    void DeleteNote(Note note);
    @Query("select * from notes where  title LIKE '%' || :query || '%' or subtitle LIKE '%' || :query || '%' or note_text LIKE '%' || :query || '%' order by id DESC ")
    List<Note> SearchNotes(String query);


}
