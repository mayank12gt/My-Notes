package com.example.mynotes.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.mynotes.CreateNoteActivity;
import com.example.mynotes.NotesOnClickListener;
import com.example.mynotes.R;
import com.example.mynotes.entities.Note;

import java.util.List;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.viewholder> {

    private final List<Note> noteList;
    Context context;
    NotesOnClickListener listener;
    public NotesAdapter(List<Note> noteList, Context context, NotesOnClickListener listener, DiffUtil.ItemCallback<Note> itemCallback){

        this.noteList=noteList;
        this.context=context;
        this.listener= listener;
    }

    @NonNull
    @Override
    public NotesAdapter.viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new viewholder(LayoutInflater.from(context).inflate(R.layout.note_item,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull NotesAdapter.viewholder holder, @SuppressLint("RecyclerView") int position) {

        holder.noteTitle.setText(noteList.get(position).getTitle());
        if(noteList.get(position).getSubtitle().isEmpty()){
            holder.notesubTitle.setVisibility(View.GONE);
        }
        else {
            holder.notesubTitle.setText(noteList.get(position).getSubtitle());
        }
        if(noteList.get(position).getImage_path().isEmpty()){
            holder.noteImage.setVisibility(View.GONE);
        }
        else{
            holder.noteImage.setVisibility(View.VISIBLE);
            holder.noteImage.setImageURI(Uri.parse(noteList.get(position).getImage_path()));
            Log.d("uri",noteList.get(position).getImage_path());
        }


        holder.dateTime.setText(noteList.get(position).getDateTime());

        holder.card.setCardBackgroundColor(noteList.get(position).getColor());


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onNoteClicked(noteList.get(position),position);
            }
        });


    }

    @Override
    public int getItemCount() {
        return noteList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public class viewholder extends RecyclerView.ViewHolder {

       TextView noteTitle,notesubTitle, dateTime;
       ImageView noteImage;
       CardView card;
        public viewholder(@NonNull View itemView) {
            super(itemView);

            noteTitle=itemView.findViewById(R.id.noteTitletv);
            notesubTitle =itemView.findViewById(R.id.notesubTitletv);
            dateTime = itemView.findViewById(R.id.textdatetime);
            noteImage = itemView.findViewById(R.id.noteImage);
            card= itemView.findViewById(R.id.layout_card);

        }
    }
}
