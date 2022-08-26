package com.example.sanatkitabimapp;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sanatkitabimapp.databinding.RecyclerArtBinding;

import java.util.ArrayList;

public class ArtAdapter extends RecyclerView.Adapter<ArtAdapter.ArtHolder> {

    ArrayList<Art> artArrayList;

    public ArtAdapter(ArrayList<Art> artArrayList){
        this.artArrayList = artArrayList;
    }

    @NonNull
    @Override
    public ArtHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerArtBinding recyclerArtBinding = RecyclerArtBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ArtHolder(recyclerArtBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull ArtHolder holder, int position) {
        holder.binding.recyclerViewTV.setText(artArrayList.get(position).name);
        holder.binding.recyclerViewTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(holder.itemView.getContext(), DetailsActivity.class);
                intent.putExtra("artId", artArrayList.get(holder.getAdapterPosition()).id);
                intent.putExtra("info", "old");
                holder.itemView.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return artArrayList.size();
    }


    public class ArtHolder extends RecyclerView.ViewHolder{

        private RecyclerArtBinding binding;

        public ArtHolder(RecyclerArtBinding binding) {
            super(binding.getRoot());  //gorunum aliniyor
            this.binding = binding;
        }
    }

}
