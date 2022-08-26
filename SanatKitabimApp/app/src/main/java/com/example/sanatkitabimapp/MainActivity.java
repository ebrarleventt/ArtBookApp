package com.example.sanatkitabimapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.example.sanatkitabimapp.databinding.ActivityMainBinding;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    ArrayList<Art> artArrayList;
    ArtAdapter artAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        artArrayList = new ArrayList<>();

        binding.mainActivityRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        artAdapter = new ArtAdapter(artArrayList);
        binding.mainActivityRecyclerView.setAdapter(artAdapter);

        getData();

    }

    //verileri cekme:
    private void getData(){

        try{

            //diger activity deki ile birebir ayni olmali
            SQLiteDatabase sqLiteDatabase = this.openOrCreateDatabase("MyArtBook", MODE_PRIVATE, null);
            Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM arts", null);
            int nameIx = cursor.getColumnIndex("artname");
            int idIx = cursor.getColumnIndex("id");

            while(cursor.moveToNext()){
                String name = cursor.getString(nameIx);
                int id = cursor.getInt(idIx);
                Art art = new Art(name, id);
                artArrayList.add(art);
            }
            artAdapter.notifyDataSetChanged();
            cursor.close();

        }catch(Exception e){
            e.printStackTrace();
        }

    }

    //menuyu activity ye baglamak icin:
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater menuInflater = getMenuInflater();
        //hangi menuyu baglamak istiyorsun, hangi menuyle baglanacak
        menuInflater.inflate(R.menu.art_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //menuye tiklaninca ne olacak?
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        //Birden fazla item olabilir bu nedenle if kullan
        if(item.getItemId() == R.id.addArt){
            Intent intent = new Intent(this, DetailsActivity.class);
            intent.putExtra("info", "new");
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }
}