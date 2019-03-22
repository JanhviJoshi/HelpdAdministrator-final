package com.example.suvrat.helpdadministrator;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class UploadActivity extends AppCompatActivity {
    ImageView imageView;
    Button uploadButton, cancelButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        imageView= findViewById(R.id.preview);
        uploadButton= findViewById(R.id.upload);
        cancelButton= findViewById(R.id.cancel);

        Intent i= getIntent();
        final String filePath= i.getStringExtra("filePath");
        final Uri fileUri= Uri.parse(filePath); //converting to uri
        FirebaseStorage storage= FirebaseStorage.getInstance();
        final StorageReference storageRef= storage.getReferenceFromUrl
                                            ("gs://helpdadministrator.appspot.com"); //url of firebase app

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 8;
        final Bitmap bitmap = BitmapFactory.decodeFile(filePath, options);
        imageView.setImageBitmap(bitmap);

        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                StorageReference childRef= storageRef.child("images/"+ fileUri.getLastPathSegment());
                UploadTask uploadTask= childRef.putFile(Uri.parse("file://"+fileUri));

                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Toast.makeText(UploadActivity.this, "Upload Successful!", Toast.LENGTH_LONG).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(UploadActivity.this, "Upload unSuccessful!", Toast.LENGTH_LONG).show();
                    }
                });

                //Toast.makeText(UploadActivity.this,"Will upload photo to databse", Toast.LENGTH_SHORT).show();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent= new Intent(UploadActivity.this, MainActivity.class);
                startActivity(intent);

            }
        });

    }
}
