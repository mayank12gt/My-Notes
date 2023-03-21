package com.example.mynotes;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.mynotes.database.NotesDatabase;
import com.example.mynotes.entities.Note;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import eltos.simpledialogfragment.SimpleDialog;
import eltos.simpledialogfragment.color.SimpleColorDialog;

public class CreateNoteActivity extends AppCompatActivity implements SimpleDialog.OnDialogResultListener {


    private static final String DIALOG_TAG = "colorDialog";
    public static final int KITKAT_VALUE = 1002;
    ImageView backbtn, addImage, noteImage, addColor, deletebtn, donebtn;
    EditText noteTitle, noteSubtitle, noteDetails;
    TextView dateTimetv, noteURL;

    Uri imageuri;
    Note availableNote = null;
    int note_color =  Color.parseColor("#333333");


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_note);


        noteTitle = findViewById(R.id.noteTitleET);
        noteDetails = findViewById(R.id.noteDetailsET);
        noteSubtitle = findViewById(R.id.notesubTitleET);
        dateTimetv = findViewById(R.id.datetimetv);
        noteImage = findViewById(R.id.noteImage);
        addImage = findViewById(R.id.addimagebtn);
        addColor = findViewById(R.id.addcolorbutton);
        noteURL = findViewById(R.id.noteURL);
        backbtn = findViewById(R.id.backbtn);
        deletebtn = findViewById(R.id.deletebtn);
        donebtn = findViewById(R.id.donebtn);


        if (getIntent().getBooleanExtra("isUpdate", false)) {
            availableNote = (Note) getIntent().getSerializableExtra("Note");
            setView();
        }


        backbtn.setOnClickListener(view -> onBackPressed());

        dateTimetv.setText(new SimpleDateFormat("EEEE, dd MMMM yyyy HH:mm a", Locale.getDefault())
                .format(new Date()));

        GradientDrawable bgShape = (GradientDrawable) addColor.getBackground();
        bgShape.setColor(note_color);

        donebtn.setOnClickListener(view -> {
            if (getIntent().getBooleanExtra("isUpdate", false)) {
                UpdateNote();
            } else {
                saveNote();
            }
        });

        addImage.setOnClickListener(view -> {
            Intent intent;



                intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/*");
                photopickerlauncher.launch(intent);

        });

        addColor.setOnClickListener(view -> SimpleColorDialog.build()
                .title("Choose a color")
                .allowCustom(true)
                .colors(CreateNoteActivity.this, SimpleColorDialog.MATERIAL_COLOR_PALLET_LIGHT)
                .show(CreateNoteActivity.this, DIALOG_TAG));


        noteImage.setOnLongClickListener(view -> {
            registerForContextMenu(noteImage);
            return false;
        });
    }


    private void setView() {
        noteTitle.setText(availableNote.getTitle());
        noteSubtitle.setText(availableNote.getSubtitle());
        noteDetails.setText(availableNote.getNoteText());

        if (!availableNote.getImage_path().isEmpty()) {
            noteImage.setVisibility(View.VISIBLE);
            noteImage.setImageURI(Uri.parse(availableNote.getImage_path()));
            imageuri = Uri.parse(availableNote.getImage_path());
        }
        note_color = availableNote.getColor();

        dateTimetv.setText(availableNote.getDateTime());

        deletebtn.setVisibility(View.VISIBLE);

        deletebtn.setOnClickListener(view -> {
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(CreateNoteActivity.this);
            builder.setTitle("Delete Note");
            builder.setMessage("Are you sure you want to delete this note?");
            builder.setIcon(R.drawable.ic_baseline_delete_forever_24);
            builder.setNegativeButton("No", (dialogInterface, i) -> dialogInterface.dismiss());
            builder.setPositiveButton("Yes", (dialogInterface, i) -> deleteNote(availableNote));
            builder.show();
        });
        GradientDrawable bgShape = (GradientDrawable) addColor.getBackground();
        bgShape.setColor(note_color);

    }


    ActivityResultLauncher<Intent> photopickerlauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if (result.getResultCode() == RESULT_OK) {
                Intent data = result.getData();
                Uri uri = data.getData();
                ContentResolver resolver = CreateNoteActivity.this.getContentResolver();
                resolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                if (uri != null) {
                    noteImage.setVisibility(View.VISIBLE);
                    Glide.with(CreateNoteActivity.this).load(uri).
                            into(noteImage);
                    imageuri = uri;


                } else {
                    Log.d("PhotoPicker", "No media selected");
                }
            }
        }
    });


    private void saveNote() {
        if (noteTitle.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Note Title is empty!", Toast.LENGTH_SHORT).show();
        } else if (noteDetails.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Note Detail is empty!", Toast.LENGTH_SHORT).show();
        } else {
            Note note = new Note();
            note.setTitle(noteTitle.getText().toString());
            note.setSubtitle(noteSubtitle.getText().toString());
            note.setNoteText(noteDetails.getText().toString());
            note.setImage_path(imageuri == null ? "" : imageuri.toString());
            note.setDateTime(dateTimetv.getText().toString());

            note.setColor(note_color);


            ExecutorService executorService = Executors.newSingleThreadExecutor();

            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    NotesDatabase.getInstance(getApplicationContext()).getDao().addNote(note);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(CreateNoteActivity.this, "Note added", Toast.LENGTH_SHORT).show();
                            Intent i = new Intent();
                            setResult(RESULT_OK, i);
                            finish();
                        }
                    });

                }
            });


        }
    }


    private void UpdateNote() {
        if (noteTitle.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Note Title is empty!", Toast.LENGTH_SHORT).show();
        } else if (noteDetails.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Note Detail is empty!", Toast.LENGTH_SHORT).show();
        } else {
            Note note = availableNote;
            note.setTitle(noteTitle.getText().toString());
            note.setSubtitle(noteSubtitle.getText().toString());
            note.setNoteText(noteDetails.getText().toString());
            note.setImage_path(imageuri == null ? "" : imageuri.toString());
            note.setDateTime(dateTimetv.getText().toString());
            note.setColor(note_color);
            ExecutorService executorService = Executors.newSingleThreadExecutor();

            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    NotesDatabase.getInstance(getApplicationContext()).getDao().UpdateNote(note);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(CreateNoteActivity.this, "Note Updated", Toast.LENGTH_SHORT).show();
                            Intent i = new Intent();
                            setResult(RESULT_OK, i);

                            finish();
                        }
                    });

                }
            });


        }
    }

    private void deleteNote(Note note) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();

        executorService.execute(new Runnable() {
            @Override
            public void run() {
                NotesDatabase.getInstance(getApplicationContext()).getDao().DeleteNote(note);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(CreateNoteActivity.this, "Note Deleted", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent();
                        i.putExtra("deleted", true);
                        setResult(RESULT_OK, i);

                        finish();
                    }
                });

            }
        });
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = new MenuInflater(this);
        inflater.inflate(R.menu.image_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.delete_img:
                noteImage.setImageURI(null);
                noteImage.setVisibility(View.GONE);
                imageuri = null;

                Toast.makeText(this, "Image deleted", Toast.LENGTH_SHORT).show();
        }

        return super.onContextItemSelected(item);
    }

    @Override
    public boolean onResult(@NonNull String dialogTag, int which, @NonNull Bundle extras) {
        if (DIALOG_TAG.equals(dialogTag)) {
            note_color = extras.getInt(SimpleColorDialog.COLOR);
            return true;
        }

        return false;
    }
}