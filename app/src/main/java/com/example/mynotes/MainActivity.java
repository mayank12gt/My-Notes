package com.example.mynotes;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.mynotes.adapters.NotesAdapter;
import com.example.mynotes.database.NotesDatabase;
import com.example.mynotes.entities.Note;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    ImageView addNotebtn;
    Integer REQUEST_CODE=1;
    List<Note> noteslist;
    RecyclerView notesrv;
    NotesAdapter notesAdapter;
    LinearProgressIndicator loading;

    ActivityResultLauncher<Intent> createNoteActivitylauncher=registerForActivityResult(new ActivityResultContracts
            .StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if(result.getResultCode()==RESULT_OK){
                getNotes();

            }

        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        notesrv =findViewById(R.id.notesrv);
        noteslist = new ArrayList<>();
        loading = findViewById(R.id.loading);

        addNotebtn = findViewById(R.id.addNote);
        addNotebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(),CreateNoteActivity.class);
                createNoteActivitylauncher.launch(intent);
            }
        });


        notesrv.setLayoutManager(new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL));
        notesAdapter = new NotesAdapter(noteslist,this);

        notesrv.setAdapter(notesAdapter);

        getNotes();
    }



    private void getNotes(){
        loading.setVisibility(View.VISIBLE);
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(new Runnable() {
            @Override
            public void run() {


               List<Note>notes= NotesDatabase.getInstance(getApplicationContext()).getDao().getAllNotes();

               runOnUiThread(new Runnable() {
                   @Override
                   public void run() {
                      loading.setVisibility(View.GONE);
                       if(noteslist.size()==0){
                           noteslist.addAll(notes);
                           notesAdapter.notifyDataSetChanged();
                       }
                       else{
                           noteslist.add(0,notes.get(0));
                           notesAdapter.notifyItemInserted(0);
                       }
                       notesrv.smoothScrollToPosition(0);
                   }
               });
            }
        });

    }
}