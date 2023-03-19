package com.example.mynotes;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.mynotes.adapters.NotesAdapter;
import com.example.mynotes.adapters.NotesComparator;
import com.example.mynotes.database.NotesDatabase;
import com.example.mynotes.entities.Note;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity implements NotesOnClickListener {

    private static final Integer REQUEST_CODE_SHOW_NOTE = 1;
    private static final Integer REQUEST_CODE_ADD_NOTE = 2;
    private static final Integer REQUEST_CODE_UPDATE_NOTE = 3;
    private static final Integer REQUEST_CODE_DELETE_NOTE = 4;
    private Timer timer;
    ImageView addNotebtn;
    Integer noteclickedposition=-1;

    List<Note> noteslist;
    RecyclerView notesrv;
    NotesAdapter notesAdapter;
    LinearProgressIndicator loading;
    SearchView searchView;

    ActivityResultLauncher<Intent> createNoteActivitylauncher=registerForActivityResult(new ActivityResultContracts
            .StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if(result.getResultCode()==RESULT_OK){
                getNotes(REQUEST_CODE_ADD_NOTE);

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
        searchView  = findViewById(R.id.notesSearch);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if(!newText.trim().isEmpty()){
                    timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {

                                    searchNotes(newText);
                                }
                            });
                        }
                    },800);

                }
                else{
                    noteslist.clear();
                     getNotes(REQUEST_CODE_SHOW_NOTE);
                }

                return true;
            }
        });


        addNotebtn = findViewById(R.id.addNote);
        addNotebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(),CreateNoteActivity.class);
                createNoteActivitylauncher.launch(intent);
            }
        });


        notesrv.setLayoutManager(new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL));
        notesAdapter = new NotesAdapter(noteslist,this,this,new NotesComparator());

        notesrv.setAdapter(notesAdapter);

        getNotes(REQUEST_CODE_SHOW_NOTE);
    }

    private void searchNotes(String newText) {
        loading.setVisibility(View.VISIBLE);
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                List<Note>notes= NotesDatabase.getInstance(getApplicationContext()).getDao().SearchNotes(newText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loading.setVisibility(View.GONE);
                        noteslist.clear();
                        noteslist.addAll(notes);
                        if(notes.isEmpty()){
                            Toast.makeText(MainActivity.this, "empty search", Toast.LENGTH_SHORT).show();
                        }
                        notesAdapter.notifyDataSetChanged();
                    }
                });
            }
        });
    }


    private void getNotes(int requestcode){
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
                       if(requestcode==REQUEST_CODE_SHOW_NOTE){
                           noteslist.addAll(notes);
                           notesAdapter.notifyDataSetChanged();
                       }
                       else if(requestcode==REQUEST_CODE_ADD_NOTE){
                           noteslist.add(0,notes.get(0));
                           notesAdapter.notifyItemInserted(0);
                       }
                       else if(requestcode==REQUEST_CODE_UPDATE_NOTE){
                           noteslist.set(noteclickedposition,notes.get(noteclickedposition));

                           notesAdapter.notifyDataSetChanged();
                       }
                       else if(requestcode==REQUEST_CODE_DELETE_NOTE){
                           Toast.makeText(MainActivity.this, String.valueOf(noteclickedposition), Toast.LENGTH_SHORT).show();
                           noteslist.remove((int)noteclickedposition);

                           notesAdapter.notifyItemRemoved((int)noteclickedposition);
                       }
                       notesrv.smoothScrollToPosition(0);
                   }
               });
            }
        });

    }


    ActivityResultLauncher<Intent> UpdateNoteActivitylauncher=registerForActivityResult(new ActivityResultContracts
            .StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if(result.getResultCode()==RESULT_OK){
                Toast.makeText(MainActivity.this,"Correct",Toast.LENGTH_LONG).show();
                if(result.getData().getBooleanExtra("deleted",false)){
                    getNotes(REQUEST_CODE_DELETE_NOTE);
                    Toast.makeText(MainActivity.this, "deleted", Toast.LENGTH_SHORT).show();
                }
                else {
                    getNotes(REQUEST_CODE_UPDATE_NOTE);
                }

            }

        }
    });


    @Override
    public void onNoteClicked(Note note, int position) {
        Intent i= new Intent(this,CreateNoteActivity.class);
        i.putExtra("isUpdate",true);
        i.putExtra("Note",note);
        UpdateNoteActivitylauncher.launch(i);
        noteclickedposition = position;



    }
}