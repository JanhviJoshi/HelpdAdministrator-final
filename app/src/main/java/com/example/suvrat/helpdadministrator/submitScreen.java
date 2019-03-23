package com.example.suvrat.helpdadministrator;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class submitScreen extends AppCompatActivity {
    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submit_screen);

        imageView= findViewById(R.id.arrayTest);
       /* final FirebaseDatabase database= FirebaseDatabase.getInstance();
        DatabaseReference ref= database.getReference();
        FirebaseStorage storage= FirebaseStorage.getInstance();
        StorageReference storageRef= storage.getReferenceFromUrl
                ("gs://helpdadministrator.appspot.com");  */

        //final Maid maid= new Maid();
      /*  ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Maid maid = dataSnapshot.child("basmati").getValue(Maid.class)
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        })  */

     /* String imageName= "images/JPEG_20190322_202317_";

      StorageReference refref= storageRef.child("images/JPEG_20190322_202317_");
      refref.getBytes(1024*1024).addOnSuccessListener(new OnSuccessListener<byte[]>() {
          @Override
          public void onSuccess(byte[] bytes) {
             Bitmap bitmap= BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
             imageView.setImageBitmap(bitmap);
          }
      }); */

    }
}
