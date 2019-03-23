package com.example.suvrat.helpdadministrator;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.ResultReceiver;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity" ;
    EditText nameEdittext, dobEditText, genderEditText, phoneNumber1EditText, phoneNumber2EditText,
            address1EditText, address2EditText, landmarkEditText, stateEditText, cityEditText,
            aadharnumberEditText, pannumberEditText, transportationEditText, bankaccountEditText,
            modeofpaymentEditText, testEdit;
    Button submit, autolocate;
    FirebaseDatabase database;
    Intent intent;
    Uri photoUri;
    ImageButton imageButton;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private boolean mLocationPermissionGranted;
    private Location mLastKnownLocation;

    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    double latitude, longitude;
    int flag = 0;

    private Uri filePath;
    private final int PICK_IMAGE_REQUEST = 71;
    private StorageReference mStorageRef;
    StorageReference storageRef;

    protected Location mLastLocation;
    private AddressResultReceiver mResultReceiver;

    String mAddressOutput,currentPhotoPath;
    String imageFileName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        database = FirebaseDatabase.getInstance();
        FirebaseStorage storage= FirebaseStorage.getInstance();
        storageRef= storage.getReferenceFromUrl
                ("gs://helpdadministrator.appspot.com"); //url of firebase app

        //Declaring the edittexts ------
        testEdit= findViewById(R.id.test);

        nameEdittext = findViewById(R.id.name);
        dobEditText = findViewById(R.id.dob);
        genderEditText = findViewById(R.id.gender);
        phoneNumber1EditText = findViewById(R.id.phonenumber1);
        phoneNumber2EditText = findViewById(R.id.phonenumber2);
        address1EditText = findViewById(R.id.address1);
        address2EditText = findViewById(R.id.address2);
        landmarkEditText = findViewById(R.id.landmark);
        stateEditText = findViewById(R.id.state);
        cityEditText = findViewById(R.id.city);
        aadharnumberEditText = findViewById(R.id.aadharnumber);
        pannumberEditText = findViewById(R.id.pannumber);
        transportationEditText = findViewById(R.id.transportation);
        bankaccountEditText = findViewById(R.id.bankaccount);
        modeofpaymentEditText = findViewById(R.id.modeofpayment);
        submit = findViewById(R.id.submit);
        autolocate = findViewById(R.id.autobutton);
        imageButton = findViewById(R.id.user);
        //----------------------

        final String choose[]= {"Take Photo", "Choose Photo from Gallery", "Cancel"};
        //image uplaod icon functionality
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //chooseImage();

                AlertDialog.Builder builder= new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Add a Profile Photo");
                builder.setItems(choose, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(choose[i].equals("Take Photo")){
                            //check for camera permission
                            if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_CALENDAR)!=
                                    PackageManager.PERMISSION_GRANTED){
                                //not granted, requesting
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, Constants.MY_PERMISSIONS_REQUEST_CAM);
                            }
                            else
                                //we already have permission
                                dispatchTakePicturesIntent();
                        }
                        else if(choose[i].equals("Choose Photo from Gallery")){
                            uploadImage();
                        }
                        else if(choose[i].equals("Cancel")){
                            dialogInterface.dismiss();
                        }
                    }
                });
                builder.show();

            }
        });

        //auto locate address button functionality
        autolocate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getLocationPermission();
            }
        });

        //submit button functionalty
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //edittexts value fetch
                String name = nameEdittext.getText().toString();
                String dob = dobEditText.getText().toString();
                String gender = genderEditText.getText().toString();
                String phonenumber1 = phoneNumber1EditText.getText().toString();
                String phonenumber2 = phoneNumber2EditText.getText().toString();
                String addharnumber = aadharnumberEditText.getText().toString();
                String pannumber = pannumberEditText.getText().toString();
                String transportation = transportationEditText.getText().toString();
                String bankaccount = bankaccountEditText.getText().toString();
                String modeofpayment = modeofpaymentEditText.getText().toString();
                //------------------------

                // Required fields-------------
                if (TextUtils.isEmpty(name) || TextUtils.isEmpty(dob) || TextUtils.isEmpty(gender) || TextUtils.isEmpty(phonenumber1)
                        || TextUtils.isEmpty(transportation) || TextUtils.isEmpty(modeofpayment)) {
                    Toast.makeText(MainActivity.this, "Fill all reqd fields", Toast.LENGTH_SHORT).show();
                }else{
                final Maid maid = new Maid();
                maid.setName(name);
                maid.setDob(dob);
                maid.setGender(gender);
                maid.setPhonenumber1(phonenumber1);
                maid.setPhonenumber2(phonenumber2);

                if (flag == 0) {
                    String address1 = address1EditText.getText().toString();
                    String address2 = address2EditText.getText().toString();
                    String landmark = landmarkEditText.getText().toString();
                    String state = stateEditText.getText().toString();
                    String city = cityEditText.getText().toString();
                  //  if (TextUtils.isEmpty(address1) || TextUtils.isEmpty(address2) || TextUtils.isEmpty(state) ||
                   //         TextUtils.isEmpty(city)) {
                    //    Toast.makeText(MainActivity.this, "Complete full address details", Toast.LENGTH_SHORT).show();
                    //} else {
                        maid.setAddress1(address1);
                        maid.setAddress2(address2);
                        maid.setLandmark(landmark);
                        maid.setState(state);
                        maid.setCity(city);

                        String finalAddress = address1 + " " + address2 + " " + landmark + " " + state + " " + city;
                        Geocoder geocoder = new Geocoder(MainActivity.this);
                        try {
                            List<Address> addressList = geocoder.getFromLocationName(finalAddress, 1);
                            Address address = addressList.get(0);
                            latitude = address.getLatitude();
                            longitude = address.getLongitude();
                            maid.setLati(latitude);
                            maid.setLongi(longitude);
                        }catch (IOException e) {
                            //..
                        } catch (Exception e2) {
                            Toast.makeText(MainActivity.this, "Enter Correct Address Details", Toast.LENGTH_LONG).show();
                        }
                } else {
                    maid.setLati(latitude);
                    maid.setLongi(longitude);
                }
                maid.setAadharnumber(addharnumber);
                maid.setPannumber(pannumber);
                maid.setTransportation(transportation);
                maid.setBankaccount(bankaccount);
                maid.setModeofpayment(modeofpayment);

                //saving name of image coresspoding to this object
                //will (hopefully) be used while showing data from database
                //eg: StorageReference islandRef = storageRef.child("photoUrl.jpg");
                //to download the image from storage into app memory via byte array
                maid.setPhotoUrl("images/" + imageFileName);

                    database.getReference().child(phonenumber1 + " " + name).setValue(maid).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (latitude != 0 || longitude != 0) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(MainActivity.this, "The values have been submitted", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(MainActivity.this, submitScreen.class);
                                    startActivity(intent);
                                }
                            }
                        }
                    });
            } //else for reqd fields
            }
        });
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        mStorageRef = FirebaseStorage.getInstance().getReference();
    }


    ////////////////////////// Autolocating location ////////////////////////////////////
    private void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
            getDeviceLocation();
        } else {

            //Android has many ways of asking for permission, the method requestPermissions() being one.
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    //callback from requestPermissions()
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                    getDeviceLocation();
                } else {
                    Toast.makeText(MainActivity.this, "error", Toast.LENGTH_SHORT).show();
                }
                break;
            }
            case Constants.MY_PERMISSIONS_REQUEST_CAM: {
                //call function again now that permission is granted
                dispatchTakePicturesIntent();
            }
        }
    }

    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (mLocationPermissionGranted) {
                Task locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            mLastKnownLocation = (Location) task.getResult();
                            if (mLastKnownLocation != null) {
                                latitude = mLastKnownLocation.getLatitude();
                                longitude = mLastKnownLocation.getLongitude();
                                Toast.makeText(MainActivity.this, "found lat and log automatically", Toast.LENGTH_SHORT).show();
                                flag = 1;
                                startIntentService();
                                //displayAddressOutput();
                            } else {
                                Toast.makeText(MainActivity.this, "last location null", Toast.LENGTH_SHORT).show();
                            }

                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }

    }
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////// Choosing image from gallery ////////////////////////////////
    public void uploadImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            filePath = data.getData();
            //  Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
            if (filePath != null) {
                final ProgressDialog progressDialog = new ProgressDialog(this);
                progressDialog.setTitle("Uploading...");
                progressDialog.show();

                progressDialog.setCanceledOnTouchOutside(false);
                // imageButton.setImageBitmap(bitmap);

                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                imageFileName = "JPEG_" + timeStamp + "_";
                StorageReference ref = mStorageRef.child("images/"+ imageFileName);
                ref.putFile(filePath).
                        addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                progressDialog.dismiss();
                                Toast.makeText(MainActivity.this, "Uploaded", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressDialog.dismiss();
                                e.printStackTrace();
                                Toast.makeText(MainActivity.this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot
                                        .getTotalByteCount());
                                progressDialog.setMessage("Uploaded " + (int) progress + "%");
                            }
                        });
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                    imageButton.setImageBitmap(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        else if(requestCode == Constants.REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK){
            launchUploadActivity();
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    //$$$$$$$$$$$$$$$$$$$$$$$$$$ Reverse Geocoding $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$
    protected void startIntentService() {
        Intent intent = new Intent(this, FetchAddressIntentService.class);
        mResultReceiver = new AddressResultReceiver(null);
        intent.putExtra(Constants.RECEIVER, mResultReceiver);
        intent.putExtra(Constants.LOCATION_DATA_EXTRA, mLastKnownLocation);
        Log.i(TAG,"HELLO LOCATION CHECK: "+ mLastKnownLocation);
        if(mLastKnownLocation!=null)
        startService(intent);
        //displayAddressOutput();
    }

    class AddressResultReceiver extends ResultReceiver {
        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            if(resultData== null){
                Toast.makeText(MainActivity.this,"Error in reverse geocoding", Toast.LENGTH_SHORT).show();
                return;
            }
            // Display the address string
            // or an error message sent from the intent service.
            mAddressOutput = resultData.getString(Constants.RESULT_DATA_KEY);
            if (mAddressOutput == null) {
                Toast.makeText(MainActivity.this,"Error in reverse geocoding", Toast.LENGTH_SHORT).show();
            }
            //displayAddressOutput();
            // Show a toast message if an address was found.
            if (resultCode == Constants.SUCCESS_RESULT) {
                Toast.makeText(MainActivity.this,"address found",Toast.LENGTH_SHORT).show();
               MainActivity.this.runOnUiThread(new Runnable(){
                   @Override
                   public void run() {
                       address1EditText.setVisibility(View.INVISIBLE);
                       address2EditText.setVisibility(View.INVISIBLE);
                       landmarkEditText.setVisibility(View.INVISIBLE);
                       stateEditText.setVisibility(View.INVISIBLE);
                       cityEditText.setVisibility(View.INVISIBLE);
                       testEdit.setVisibility(View.VISIBLE);
                       displayAddressOutput(); //using this runOnUiThread method because otherwise its not working.
                       //as we are trying to touch a UI thread view from another thread.
                   }
                });
            }
        }
    }
    public void displayAddressOutput(){
        testEdit.setText(mAddressOutput);
    }

    //$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$

    /************************** Image from camera ********************************/
    public void dispatchTakePicturesIntent(){

        Intent takePictureIntent= new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(takePictureIntent.resolveActivity(getPackageManager())!= null){
            //create file
            File photoFile= null;
            try{
                photoFile= createImageFile();
            }catch (IOException e){

            }
            //file successfully created?
            if(photoFile!= null){
                photoUri= FileProvider.getUriForFile(this, "com.example.suvrat.helpdadministrator", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityForResult(takePictureIntent, Constants.REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    public void launchUploadActivity(){
      /*  Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(currentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);

        Intent i = new Intent(MainActivity.this, UploadActivity.class);
        i.putExtra("filePath", currentPhotoPath);
        startActivity(i);   */

       final Uri fileUri= Uri.parse(currentPhotoPath);
      /*  FirebaseStorage storage= FirebaseStorage.getInstance();
        final StorageReference storageRef= storage.getReferenceFromUrl
                ("gs://helpdadministrator.appspot.com"); //url of firebase app */
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 8;
        final Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath, options);
        imageButton.setImageBitmap(bitmap);

        StorageReference childRef= storageRef.child("images/"+ fileUri.getLastPathSegment());
        UploadTask uploadTask= childRef.putFile(Uri.parse("file://"+fileUri));

        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(MainActivity.this, "Upload Successful!", Toast.LENGTH_LONG).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this, "Upload unSuccessful!", Toast.LENGTH_LONG).show();
            }
        });
    }

    private File createImageFile()throws IOException {
        //creating image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        //Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    /***************************************************************************/



}


