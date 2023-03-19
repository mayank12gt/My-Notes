package com.example.mynotes;

import static java.security.AccessController.getContext;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.mynotes.database.NotesDatabase;
import com.example.mynotes.entities.Note;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CreateNoteActivity extends AppCompatActivity {


    ImageView backbtn,addImage,noteImage,addUrl,deletebtn;
    EditText noteTitle, noteSubtitle, noteDetails;
    TextView dateTimetv,noteURL;
    public static final int KITKAT_VALUE = 1002;
     Uri imageuri;
     Dialog addUrldialog;
     String url="";
     Note availableNote=null;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_note);

        noteTitle=findViewById(R.id.noteTitleET);
        noteDetails= findViewById(R.id.noteDetailsET);
        noteSubtitle=findViewById(R.id.notesubTitleET);
        dateTimetv=findViewById(R.id.datetimetv);
        noteImage = findViewById(R.id.noteImage);
        addImage=findViewById(R.id.addimagebtn);
        addUrl = findViewById(R.id.addurlbutton);
        noteURL  =findViewById(R.id.noteURL);
        backbtn = findViewById(R.id.backbtn);
        deletebtn = findViewById(R.id.deletebtn);


        backbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        dateTimetv.setText(new SimpleDateFormat("EEEE, dd MMMM yyyy HH:mm a", Locale.getDefault())
                .format(new Date()));

        if(getIntent().getBooleanExtra("isUpdate",false)){
             availableNote = (Note)getIntent().getSerializableExtra("Note");
            setView();
        }

        ImageView donebtn=findViewById(R.id.donebtn);
        donebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getIntent().getBooleanExtra("isUpdate", false)) {
                    UpdateNote();
                } else {
                    saveNote();
                }
            }
        });

        // Registers a photo picker activity launcher in single-select mode.
        ActivityResultLauncher<PickVisualMediaRequest> pickMedia =
                registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                    // Callback is invoked after the user selects a media item or closes the
                    // photo picker.
                    if (uri != null) {
                        noteImage.setVisibility(View.VISIBLE);
                        Glide.with(this).load(uri).placeholder(R.drawable.ic_baseline_arrow_back_ios_24).
                                into(noteImage);
                        imageuri = uri;

                        ContentResolver resolver = this.getContentResolver();
                        resolver.takePersistableUriPermission(imageuri,Intent.FLAG_GRANT_READ_URI_PERMISSION);



                    } else {
                        Log.d("PhotoPicker", "No media selected");
                    }
                });

        //add image to note
        addImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                //pickMedia.launch(new PickVisualMediaRequest.Builder()
                  //      .setMediaType( (ActivityResultContracts.PickVisualMedia.VisualMediaType) ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                    //



                Intent intent;

                if (Build.VERSION.SDK_INT < 19) {
                    intent = new Intent();
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    intent.setType("image/*");
                    photopickerlauncher.launch(intent);
                } else {
                    Toast.makeText(CreateNoteActivity.this, ">19", Toast.LENGTH_SHORT).show();
                    intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType("image/*");
                    photopickerlauncher.launch(intent);
                }



            }


        });

        addUrl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                    MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(CreateNoteActivity.this);

                    View v= LayoutInflater.from(CreateNoteActivity.this).inflate(R.layout.addurldialoglayout,findViewById(R.id.addurllayout));
                    builder.setView(v);
                    addUrldialog=builder.create();
                    addUrldialog.show();
                    EditText addurlET =v.findViewById(R.id.addurlET);
                    TextView ok = v.findViewById(R.id.addurlokbtn);
                    TextView cancel = v.findViewById(R.id.addurlcancelbtn);

                    ok.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            url = addurlET.getText().toString().trim();
                            if(!url.isEmpty()) {
                                noteURL.setVisibility(View.VISIBLE);
                                noteURL.setText(url);
                            }
                            else{
                                noteURL.setVisibility(View.INVISIBLE);
                            }
                            addUrldialog.dismiss();
                        }
                    });
                    cancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            addUrldialog.dismiss();
                        }
                    });








            }
        });


        noteImage.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                registerForContextMenu(noteImage);
                return false;
            }
        });
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
                        Intent i= new Intent();
                        i.putExtra("deleted",true);
                        setResult(RESULT_OK,i);

                        finish();
                    }
                });

            }
        });
    }

    private void setView() {

        noteTitle.setText(availableNote.getTitle());
        noteSubtitle.setText(availableNote.getSubtitle());
        noteDetails.setText(availableNote.getNoteText());
        if(!availableNote.getImage_path().isEmpty()) {
            noteImage.setVisibility(View.VISIBLE);
            noteImage.setImageURI(Uri.parse(availableNote.getImage_path()));
            imageuri = Uri.parse(availableNote.getImage_path());
        }

        dateTimetv.setText(availableNote.getDateTime());

        deletebtn.setVisibility(View.VISIBLE);

        deletebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(CreateNoteActivity.this);
                builder.setTitle("Delete Note");
                builder.setMessage("Are you sure you want to delete this note?");
                builder.setIcon(R.drawable.ic_baseline_delete_forever_24);
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        deleteNote(availableNote);

                    }
                });
                builder.show();
            }
        });

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == KITKAT_VALUE) {
            if (resultCode == Activity.RESULT_OK) {
                Uri uri = data.getData();
                if (uri != null) {
                    noteImage.setVisibility(View.VISIBLE);
                    noteImage.setImageURI(uri);
                    imageuri = uri;
                    Toast.makeText(this, uri.toString(), Toast.LENGTH_SHORT).show();

                    ContentResolver resolver = this.getContentResolver();
                    resolver.takePersistableUriPermission(imageuri,Intent.FLAG_GRANT_READ_URI_PERMISSION);



                } else {
                    Toast.makeText(this, "error", Toast.LENGTH_SHORT).show();
                    Log.d("PhotoPicker", "No media selected");
                }

            }
        }
    }

    ActivityResultLauncher<Intent> photopickerlauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if(result.getResultCode()==RESULT_OK){
                Intent data = result.getData();
                Uri uri = data.getData();
                ContentResolver resolver = CreateNoteActivity.this.getContentResolver();
                resolver.takePersistableUriPermission(uri,Intent.FLAG_GRANT_READ_URI_PERMISSION| Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                if (uri != null) {
                    noteImage.setVisibility(View.VISIBLE);
                    Glide.with(CreateNoteActivity.this).load(uri).placeholder(R.drawable.ic_baseline_arrow_back_ios_24).
                            into(noteImage);
                    imageuri = uri;





                } else {
                    Log.d("PhotoPicker", "No media selected");
                }
            }
        }
    });

    private void saveNote(){
        if(noteTitle.getText().toString().trim().isEmpty()){
            Toast.makeText(this, "Note Title is empty!", Toast.LENGTH_SHORT).show();
        }

        else if(noteDetails.getText().toString().trim().isEmpty()){
            Toast.makeText(this, "Note Detail is empty!", Toast.LENGTH_SHORT).show();
        }

else {
            Note note = new Note();
            note.setTitle(noteTitle.getText().toString());
            note.setSubtitle(noteSubtitle.getText().toString());
            note.setNoteText(noteDetails.getText().toString());
            note.setImage_path(imageuri==null?"":imageuri.toString());
            note.setDateTime(dateTimetv.getText().toString());
            note.setWebLink(url);



            ExecutorService executorService = Executors.newSingleThreadExecutor();

            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    NotesDatabase.getInstance(getApplicationContext()).getDao().addNote(note);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(CreateNoteActivity.this, "Note added", Toast.LENGTH_SHORT).show();
                            Intent i= new Intent();
                              setResult(RESULT_OK,i);
                            finish();
                        }
                    });

                }
            });


        }
    }


    private void UpdateNote(){
        if(noteTitle.getText().toString().trim().isEmpty()){
            Toast.makeText(this, "Note Title is empty!", Toast.LENGTH_SHORT).show();
        }

        else if(noteDetails.getText().toString().trim().isEmpty()){
            Toast.makeText(this, "Note Detail is empty!", Toast.LENGTH_SHORT).show();
        }

        else {
            Note note = availableNote;
            note.setTitle(noteTitle.getText().toString());
            note.setSubtitle(noteSubtitle.getText().toString());
            note.setNoteText(noteDetails.getText().toString());
            note.setImage_path(imageuri==null?"":imageuri.toString());
            note.setDateTime(dateTimetv.getText().toString());
            note.setWebLink(url==null?"":url.trim());




            ExecutorService executorService = Executors.newSingleThreadExecutor();

                            executorService.execute(new Runnable() {
                                @Override
                                public void run() {
                                    NotesDatabase.getInstance(getApplicationContext()).getDao().UpdateNote(note);

                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(CreateNoteActivity.this, "Note Updated", Toast.LENGTH_SHORT).show();
                                            Intent i= new Intent();
                                            setResult(RESULT_OK,i);

                            finish();
                        }
                    });

                }
            });


        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = new MenuInflater(this);
        inflater.inflate(R.menu.image_menu,menu);
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){
            case R.id.delete_img:
                 noteImage.setImageURI(null);
                 noteImage.setVisibility(View.GONE);
                 imageuri=null;

                Toast.makeText(this, "Image deleted", Toast.LENGTH_SHORT).show();
        }

        return super.onContextItemSelected(item);
    }
}