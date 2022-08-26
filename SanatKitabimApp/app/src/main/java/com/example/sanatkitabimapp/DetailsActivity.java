package com.example.sanatkitabimapp;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import com.example.sanatkitabimapp.databinding.ActivityDetailsBinding;
import com.google.android.material.snackbar.Snackbar;

import java.io.ByteArrayOutputStream;

public class DetailsActivity extends AppCompatActivity {

    Bitmap selectedImage;
    SQLiteDatabase database;
    ActivityResultLauncher<Intent> intentLauncher; //galeriye gitmek icin
    ActivityResultLauncher<String> permissionLauncher; //izin istemek icin
    private ActivityDetailsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDetailsBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        registerLauncher();

        database = this.openOrCreateDatabase("MyArtBook", MODE_PRIVATE, null);

        Intent intent = getIntent();
        String info = intent.getStringExtra("info");
        if(info.matches("new")){
            //new art
            binding.artNameET.setText("");
            binding.artistNameET.setText("");
            binding.yearET.setText("");
            binding.saveButton.setVisibility(View.VISIBLE);
            Bitmap selectImage = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.selectimage);
            binding.selectImageView.setImageBitmap(selectImage);
        }else{
            int artId = intent.getIntExtra("artId", 1);
            binding.saveButton.setVisibility(View.INVISIBLE);
            try{
                //id de hangisine bastigimizi bilmedigimiz icin ? isareti koyuyoruz
                //en sonda ki artId, ? isareti yerine geciyor.
                Cursor cursor = database.rawQuery("SELECT * FROM arts WHERE id=?", new String[] {String.valueOf(artId)});
                int artNameIx = cursor.getColumnIndex("artname");
                int artistNameIx = cursor.getColumnIndex("artistname");
                int yearIx = cursor.getColumnIndex("year");
                int imageIx = cursor.getColumnIndex("image");

                while(cursor.moveToNext()){
                    binding.artNameET.setText(cursor.getString(artNameIx));
                    binding.artistNameET.setText(cursor.getString(artistNameIx));
                    binding.yearET.setText(cursor.getString(yearIx));

                    //resim veriydi onu gostermek icin bitmap e cevirmek gerek:
                    byte[] bytes = cursor.getBlob(imageIx);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    binding.selectImageView.setImageBitmap(bitmap);
                }

                cursor.close();

            }catch(Exception e){
                e.printStackTrace();
            }

        }

    }


    public void save(View view){

        String artName = binding.artNameET.getText().toString();
        String artistName = binding.artistNameET.getText().toString();
        String year = binding.yearET.getText().toString();

        Bitmap smallImage = makeSmallerImage(selectedImage, 300);
        //smallImage i db ye kaydetmek icin veri haline yani 1 ve 0 lara cevirmek gerek
        //output stream denilen bir yontem var:
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        smallImage.compress(Bitmap.CompressFormat.PNG, 50, outputStream);
        byte[] byteArrayImage = outputStream.toByteArray();

        //Artik nameleri, year ve byteArrayImage leri db ye kaydedebiliriz. Once db olusturalim.

        try{
            //db ye veriyi kaydetme islemi BLOB ile olur
            database.execSQL("CREATE TABLE IF NOT EXISTS arts(id INTEGER PRIMARY KEY, artname VARCHAR, artistname VARCHAR, year VARCHAR, image BLOB)");
            //VALUES icerisine bir deger yazilmayacak. Sonradan calistirilabilecek SQLite Statement olusturucaz
            //database.execSQL("INSERT INTO arts(artName, artistName, year, image) VALUES()"); bunu string degiskenine yazalim:
            String sqlString = "INSERT INTO arts(artname, artistname, year, image) VALUES(?, ?, ?, ?)";

            //SQLiteStatement - sonradan baglama (binding) islemlerini kolay yapmamızı saglar
            //asagida, o string i alıp db de calıstırıcam anlamina geliyor
            SQLiteStatement sqLiteStatement = database.compileStatement(sqlString);
            //index 1'den basliyor!
            sqLiteStatement.bindString(1, artName);
            sqLiteStatement.bindString(2, artistName);
            sqLiteStatement.bindString(3, year);
            sqLiteStatement.bindBlob(4, byteArrayImage);
            sqLiteStatement.execute();

        }catch(Exception e ){
            e.printStackTrace();
        }

        //Save olduktan sonra main activity e geri donmek icin:
        Intent intent = new Intent(DetailsActivity.this, MainActivity.class);
        //bundan onceki butun activityleri kapatmak ve sadece su an gidecegini acmak icin:
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);

    }

    //Gorseli vertabanina kaydetmek gerek bu yuzden goseli kucultmek icin:
    //Her iki taraftan esit sekilde kucultmek icin makeSmallerImage in parametresine max uzunluk verdim
    public Bitmap makeSmallerImage(Bitmap image, int maxSize){
        int width = image.getWidth(); //guncel genislik
        int height = image.getHeight(); //guncel yukseklik

        float bitmapRadio = (float) width/ (float) height; //1'den buyukse yatay, 1'den kucukse dikey bir gorsel demektir.

        if(bitmapRadio>1){
            //landscape image
            width = maxSize;
            height = (int) (width / bitmapRadio);
        }else{
            //portrait image
            height = maxSize;
            width =(int) (width * bitmapRadio);
        }

        return image.createScaledBitmap(image, width, height, true);
    }


    public void selectImage(View view){
        //if(ContextCompat) diyoruz. Bunu sebebi surum 18 ve altı ise izne gerek yok
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            //Kullanıcı eğer izni vermezse kullanıcıya mantık göstermemiz gerekebilir.
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)){
                Snackbar.make(view, "Permission need for gallery",Snackbar.LENGTH_INDEFINITE).setAction("Give Permission", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //aciklamali request permission
                        permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
                    }
                }).show();
            }else{
                //request permission
                permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
            }


        }else{
            //izin verilmis gallery e git
            Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intentLauncher.launch(intentToGallery);
        }

    }


    private void registerLauncher(){

        intentLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if(result.getResultCode() == RESULT_OK){
                    Intent intentFromResult = result.getData();

                    if(intentFromResult != null){
                        Uri imageData = intentFromResult.getData();
                        //binding.selectImageView.setImageURI(imageData);
                        //Bize resmin uri ı degilde database ye kaydetmek icin resmin kendisi lazim o yuzden yukariyi yorum satirinal al
                        //Bu yuzden bitmap a cevirmek gerek

                        try{
                        if(Build.VERSION.SDK_INT >= 28){
                            ImageDecoder.Source source = ImageDecoder.createSource(getContentResolver(), imageData);
                            selectedImage = ImageDecoder.decodeBitmap(source);
                            binding.selectImageView.setImageBitmap(selectedImage);
                        }else{
                         selectedImage = MediaStore.Images.Media.getBitmap(getContentResolver(), imageData);
                         binding.selectImageView.setImageBitmap(selectedImage);
                        }
                        }catch(Exception e){
                            e.printStackTrace();
                        }
                    }
                }
            }
        });

        permissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
            @Override
            public void onActivityResult(Boolean result) {
                if(result){
                    //permission granted (izin verildi) gallery e git
                    Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intentLauncher.launch(intentToGallery);
                }else{
                    //permission denied (izin verilmedi)
                    Toast.makeText(DetailsActivity.this, "Permission needed!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


}