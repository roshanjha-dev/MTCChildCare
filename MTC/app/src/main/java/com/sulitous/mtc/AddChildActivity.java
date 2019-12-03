package com.sulitous.mtc;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.myhexaville.smartimagepicker.ImagePicker;
import com.myhexaville.smartimagepicker.OnImagePickedListener;

import java.io.ByteArrayOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AddChildActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private TextView mChildDobView;
    private int year, month, day;
    private TextInputLayout mChildNameLayout, mChildFatherNameLayout, mChildMotherNameLayout,mChildAgeLayout,mChildPhoneLayout,mParentSummaryLayout,
            mChildWeightLayout,mChildHeightLayout,mChildAddressLayout,mChildGramPanchayatLayout,mChildDistrictLayout,mChildBlockLayout,mParentVisitLayout;
    private Spinner mChildGenderView;
    private Switch mChildBplView,mParentSupport;
    private FirebaseFirestore mRootRef;
    private boolean isEdit= false,isImageTaken = false;
    private String key,timeStamp;
    private ImagePicker imagePicker;
    private OnImagePickedListener mImage;
    private ImageView mImageView;
    private StorageReference mStorageRootRef;

    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;
    private static final String LOCATION_FINE = android.Manifest.permission.ACCESS_FINE_LOCATION;
    private static final int LOCATION_FINE_CODE = 0x1;

    private GoogleApiClient mGoogleApiClient;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback mLocationCallback;
    private double latitude=0,longitude=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_child);
        mChildDobView =  findViewById(R.id.child_dob);
        mChildGenderView =  findViewById(R.id.child_gender);
        mChildBplView =  findViewById(R.id.child_bpl);
        mChildNameLayout =  findViewById(R.id.child_name_layout);
        mChildFatherNameLayout =  findViewById(R.id.father_layout);
        mChildMotherNameLayout =  findViewById(R.id.mother_layout);
        mChildAgeLayout =  findViewById(R.id.age_layout);
        mChildPhoneLayout =  findViewById(R.id.phone_layout);
        mChildWeightLayout =  findViewById(R.id.weight_layout);
        mChildHeightLayout =  findViewById(R.id.height_layout);
        mChildAddressLayout =  findViewById(R.id.address_layout);
        mChildGramPanchayatLayout =  findViewById(R.id.gram_panchayat_layout);
        mChildBlockLayout =  findViewById(R.id.block_layout);
        mChildDistrictLayout =  findViewById(R.id.district_layout);
        mParentSummaryLayout = findViewById(R.id.summary_layout);
        mParentVisitLayout = findViewById(R.id.visited_time_layout);
        mParentSupport = findViewById(R.id.parentSupport);
        mImageView = findViewById(R.id.add_child_imageView);
        Button mChildSaveView =  findViewById(R.id.child_save);

        Calendar calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);

        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);

        mChildDobView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCreateDialog(999).show();
            }
        });
        mChildSaveView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveChildDetails();
            }
        });
        showGenderSpinner();
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        mRootRef = FirebaseFirestore.getInstance();

        mImage = new OnImagePickedListener() {
            @Override
            public void onImagePicked(Uri imageUri) {
                previewCapturedImage(imageUri);
            }
        };
        mGoogleApiClient = new GoogleApiClient.Builder(AddChildActivity.this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(AddChildActivity.this)
                .addOnConnectionFailedListener(AddChildActivity.this).build();
        mGoogleApiClient.connect();


        if (getIntent().hasExtra("CHILD")){
            if(getSupportActionBar() != null){
                getSupportActionBar().setTitle("Edit Child Details");
            }
            String SChild = getIntent().getStringExtra("CHILD");
            Gson gson = new Gson();
            Child child = gson.fromJson(SChild,Child.class);
            key = child.getKey();
            mChildBplView.setChecked(child.isBpl());
            mChildGenderView.setSelection(child.getGender()+1);
            mChildDobView.setText(child.getDob());
            mChildNameLayout.getEditText().setText(child.getName());
            mChildFatherNameLayout.getEditText().setText(child.getFather());
            mChildMotherNameLayout.getEditText().setText(child.getMother());
            mChildAgeLayout.getEditText().setText(String.valueOf(child.getAge()));
            mChildPhoneLayout.getEditText().setText(child.getPhone());
            mChildWeightLayout.getEditText().setText(String.valueOf(child.getWeight()));
            mChildHeightLayout.getEditText().setText(String.valueOf(child.getHeight()));
            mChildDistrictLayout.getEditText().setText(child.getDistrict());
            mChildBlockLayout.getEditText().setText(child.getBlock());
            mChildGramPanchayatLayout.getEditText().setText(child.getGramPanchayat());
            mChildAddressLayout.getEditText().setText(child.getAddress());

            mChildSaveView.setText(R.string.update);
            isEdit = true;

            mParentSupport.setVisibility(View.VISIBLE);
            mParentSupport.setOnCheckedChangeListener(this);
            mParentSupport.setChecked(!child.isParentSupport());
            if(!child.isParentSupport()){
                mParentVisitLayout.getEditText().setText(String.valueOf(child.getVisitCount()));
                mParentSummaryLayout.getEditText().setText(child.getParentSummary());
            }
        }else{
            mImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    imagePicker = new ImagePicker(AddChildActivity.this, null, mImage);
                    imagePicker.setWithImageCrop(1,1);
                    imagePicker.choosePicture(true);
                }
            });
            mStorageRootRef = FirebaseStorage.getInstance().getReference();
            if(checkPlayServices()){
                if (ActivityCompat.checkSelfPermission(AddChildActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(AddChildActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    askForPermission(LOCATION_FINE, LOCATION_FINE_CODE);
                }else {
                    createLocationRequest();
                }
            }
        }

    }

    private void askForPermission(String location, int locationCode) {
        if (ContextCompat.checkSelfPermission(AddChildActivity.this, location) != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(AddChildActivity.this, location)) {

                //This is called if mUser has denied the location before
                //In this case I am just asking the location again
                ActivityCompat.requestPermissions(AddChildActivity.this, new String[]{location}, locationCode);

            } else {

                ActivityCompat.requestPermissions(AddChildActivity.this, new String[]{location}, locationCode);
            }
        } else {
            Toast.makeText(AddChildActivity.this, "" + location + " is already granted.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (ActivityCompat.checkSelfPermission(AddChildActivity.this, permissions[0]) == PackageManager.PERMISSION_GRANTED) {
            switch (requestCode) {

                case 1:
                    createLocationRequest();
                    break;
            }

            Toast.makeText(AddChildActivity.this, "Permission granted", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(AddChildActivity.this, "Give Location Permission", Toast.LENGTH_SHORT).show();
        }
    }

    protected void createLocationRequest() {
        @SuppressLint("RestrictedApi") LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);

        // **************************
        builder.setAlwaysShow(true);

        Task<LocationSettingsResponse> result =
                LocationServices.getSettingsClient(this).checkLocationSettings(builder.build());
        result.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
            @Override
            public void onComplete(@NonNull Task<LocationSettingsResponse> task) {
                try {
                    LocationSettingsResponse response = task.getResult(ApiException.class);
                    // All location settings are satisfied. The client can initialize location
                    // requests here.
                } catch (ApiException exception) {
                    switch (exception.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            // Location settings are not satisfied. But could be fixed by showing the
                            // user a dialog.
                            try {
                                // Cast to a resolvable exception.
                                ResolvableApiException resolvable = (ResolvableApiException) exception;
                                // Show the dialog by calling startResolutionForResult(),
                                // and check the result in onActivityResult().
                                resolvable.startResolutionForResult(
                                        AddChildActivity.this, 7334);
                            } catch (IntentSender.SendIntentException e) {
                                // Ignore the error.
                            } catch (ClassCastException e) {
                                // Ignore, should be an impossible error.
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            // Location settings are not satisfied. However, we have no way to fix the
                            // settings so we won't show the dialog.
                            break;
                    }
                }
            }
        });

    }

    private boolean checkPlayServices() {
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int resultCode = googleAPI.isGooglePlayServicesAvailable(AddChildActivity.this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (googleAPI.isUserResolvableError(resultCode)) {
                googleAPI.getErrorDialog(AddChildActivity.this, resultCode,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Toast.makeText(AddChildActivity.this.getApplicationContext(),
                        "This device is not supported.", Toast.LENGTH_LONG)
                        .show();
            }
            return false;
        }
        return true;
    }

    private void previewCapturedImage(Uri imageUri) {
        isImageTaken = true;
        mImageView.setImageURI(imageUri);
        timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
    }

    private void saveChildDetails() {
        boolean addChild = true;
        String childName = mChildNameLayout.getEditText().getText().toString().trim();
        String childFather = mChildFatherNameLayout.getEditText().getText().toString().trim();
        String childMother = mChildMotherNameLayout.getEditText().getText().toString().trim();
        String childDob = mChildDobView.getText().toString().trim();
        int childAge;
        if (TextUtils.isEmpty(mChildAgeLayout.getEditText().getText().toString().trim())){
            childAge = 0;
        }else {
            try {
                childAge = Integer.valueOf(mChildAgeLayout.getEditText().getText().toString().trim());
            }catch (NumberFormatException e){
                childAge = -1;
            }
        }
        int childGender = mChildGenderView.getSelectedItemPosition()-1;
        String childPhone = mChildPhoneLayout.getEditText().getText().toString().trim();
        boolean childBpl = mChildBplView.isChecked();
        double childWeight;
        if (TextUtils.isEmpty(mChildWeightLayout.getEditText().getText().toString().trim())){
            childWeight = 0;
        }else {
            try {
                childWeight = Double.valueOf(mChildWeightLayout.getEditText().getText().toString().trim());
            }catch (NumberFormatException e){
                childWeight = -1;
            }
        }
        double childHeight;
        if (TextUtils.isEmpty(mChildHeightLayout.getEditText().getText().toString().trim())){
            childHeight = 0;
        }else {
            try {
                childHeight = Double.valueOf(mChildHeightLayout.getEditText().getText().toString().trim());
            }catch (NumberFormatException e){
                childHeight = -1;
            }
        }
        String childAddress = mChildAddressLayout.getEditText().getText().toString().trim();
        String childGramPanchayat = mChildGramPanchayatLayout.getEditText().getText().toString().trim();
        String childBlock = mChildBlockLayout.getEditText().getText().toString().trim();
        String childDistrict = mChildDistrictLayout.getEditText().getText().toString().trim();
        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.shared_file_user), Context.MODE_PRIVATE);
        String childMTC = sharedPreferences.getString(getString(R.string.centre_key_shared),"");
        int childTreatment = 0;

        if (TextUtils.isEmpty(childName)){
            mChildNameLayout.setError(getString(R.string.field_required));
        }else {
            mChildNameLayout.setError(null);
        }

        if(isNameValid(childName)){
            mChildNameLayout.setError(getString(R.string.invalid_name));
        }else {
            mChildNameLayout.setError(null);
        }

        if (TextUtils.isEmpty(childFather)&&TextUtils.isEmpty(childMother)){
            mChildFatherNameLayout.setError(getString(R.string.field_required));
        }else {
            mChildFatherNameLayout.setError(null);
        }

        if (isNameValid(childFather) && isNameValid(childMother)){
            mChildFatherNameLayout.setError(getString(R.string.invalid_parent));
        }else {
            mChildFatherNameLayout.setError(null);
        }

        if (childAge == 0){
            mChildAgeLayout.setError(getString(R.string.field_required));
        }else {
            mChildAgeLayout.setError(null);
        }

        if (childAge == -1||!isAgeValid(childAge)){
            mChildAgeLayout.setError(getString(R.string.invalid_age));
        }else {
            mChildAgeLayout.setError(null);
        }

        if (childGender == -1){
            addChild = false;
            Toast.makeText(AddChildActivity.this, "Select Gender", Toast.LENGTH_SHORT).show();
        }

        if (TextUtils.isEmpty(childPhone)){
            mChildPhoneLayout.setError(getString(R.string.field_required));
        }else {
            mChildPhoneLayout.setError(null);
        }

        if (!isPhoneNumberValid(childPhone)){
            mChildPhoneLayout.setError(getString(R.string.invalid_phone));
        }else {
            mChildPhoneLayout.setError(null);
        }

        if (childWeight == 0){
            mChildWeightLayout.setError(getString(R.string.field_required));
        }else {
            mChildWeightLayout.setError(null);
        }

        if (childWeight == -1){
            mChildWeightLayout.setError(getString(R.string.invalid_weight));
        }else {
            mChildWeightLayout.setError(null);
        }

        if (childHeight == 0){
            mChildHeightLayout.setError(getString(R.string.field_required));
        }else {
            mChildHeightLayout.setError(null);
        }

        if (childHeight == -1){
            mChildHeightLayout.setError(getString(R.string.invalid_height));
        }else {
            mChildHeightLayout.setError(null);
        }

        if (TextUtils.isEmpty(childAddress)){
            mChildAddressLayout.setError(getString(R.string.field_required));
        }else {
            mChildAddressLayout.setError(null);
        }

        if (TextUtils.isEmpty(childGramPanchayat)){
            mChildGramPanchayatLayout.setError(getString(R.string.field_required));
        }else {
            mChildGramPanchayatLayout.setError(null);
        }

        if (TextUtils.isEmpty(childBlock)){
            mChildBlockLayout.setError(getString(R.string.field_required));
        }else {
            mChildBlockLayout.setError(null);
        }

        if (TextUtils.isEmpty(childDistrict)){
            mChildDistrictLayout.setError(getString(R.string.field_required));
        }else {
            mChildDistrictLayout.setError(null);
        }

        String visitedTime,parentSummary;

        if (mParentSupport.isChecked()) {
            visitedTime = mParentVisitLayout.getEditText().getText().toString().trim();
            parentSummary = mParentSummaryLayout.getEditText().getText().toString().trim();
        }else {
            visitedTime = "0";
            parentSummary = "nah";
        }

        if (mParentSupport.isChecked()){
            if (TextUtils.isEmpty(visitedTime)){
                mParentVisitLayout.setError(getString(R.string.field_required));
            }else if (!TextUtils.isDigitsOnly(visitedTime)){
                mParentVisitLayout.setError(getString(R.string.invalid_number));
            }else {
                mParentVisitLayout.setError(null);
            }

            if (TextUtils.isEmpty(parentSummary)){
                mParentSummaryLayout.setError(getString(R.string.field_required));
            }else if (parentSummary.length() <= 10){
                mParentSummaryLayout.setError(getString(R.string.more_details));
            } else{
                mParentSummaryLayout.setError(null);
            }
        }else {
            mParentVisitLayout.setError(null);
            mParentSummaryLayout.setError(null);
        }

        if (addChild) {
            if (childFather.isEmpty()){
                childFather = "null";
            }
            if (childMother.isEmpty()){
                childMother = "null";
            }
            if (mChildNameLayout.getError() == null && mChildFatherNameLayout.getError() == null && mChildMotherNameLayout.getError() == null &&mChildAgeLayout.getError() == null &&mChildPhoneLayout.getError() == null &&mParentSummaryLayout.getError() ==null &&mParentVisitLayout.getError() == null&&
                    mChildWeightLayout.getError() == null &&mChildHeightLayout.getError() == null &&mChildAddressLayout.getError() == null &&mChildGramPanchayatLayout.getError() == null &&mChildDistrictLayout.getError() == null &&mChildBlockLayout.getError() == null){
                if(isEdit){
                    childAdd(childName, childFather, childMother, childDob, childAge, childGender, childPhone, childBpl, childWeight, childHeight, childAddress, childGramPanchayat, childBlock, childDistrict, childMTC, childTreatment, parentSummary, visitedTime);
                }else {
                    if (isImageTaken) {
                        if (latitude == 0  || longitude == 0){
                            Toast.makeText(this, "Give location permission and turn on GPS", Toast.LENGTH_SHORT).show();
                        }else {
                            childAdd(childName, childFather, childMother, childDob, childAge, childGender, childPhone, childBpl, childWeight, childHeight, childAddress, childGramPanchayat, childBlock, childDistrict, childMTC, childTreatment, parentSummary, visitedTime);
                        }
                    }else {
                        Toast.makeText(this, "Add Child Image also", Toast.LENGTH_SHORT).show();
                    }
                }

            }
        }else {
            Toast.makeText(this, "Check the errors", Toast.LENGTH_SHORT).show();
        }
    }

    private void childAdd(String childName, String childFather, String childMother, String childDob, int childAge, int childGender,
                          String childPhone, boolean childBpl, double childWeight, double childHeight, String childAddress, String childGramPanchayat,
                          String childBlock, String childDistrict, String childMTC, int childTreatment, String parentSummary, String visitCount) {
        final ShowDialog showDialog = new ShowDialog(AddChildActivity.this);
        Child child = new Child();
        if (isEdit){
            showDialog.setTitle("Updating Child Details");
        }else {
            showDialog.setTitle("Adding Child Details");
            key = mRootRef.collection("Child Details").document().getId();
            mImageView.setDrawingCacheEnabled(true);
            mImageView.buildDrawingCache();
            Bitmap bitmap = mImageView.getDrawingCache();
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
            final byte[] data = byteArrayOutputStream.toByteArray();
            StorageReference mStorage = mStorageRootRef.child(key).child("IMG_" + timeStamp + ".jpg");
            String imageUrl = mStorage.getPath();
            mStorage.putBytes(data).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Toast.makeText(AddChildActivity.this, "Image Uploaded", Toast.LENGTH_SHORT).show();
                }
            });
            child.setImageUrl(imageUrl);
            child.setLatitude(latitude);
            child.setLongitude(longitude);
        }
        showDialog.setMessage("Uploading Data...");
        showDialog.show();
        child.setName(childName);
        child.setFather(childFather);
        child.setMother(childMother);
        child.setDob(childDob);
        child.setAge(childAge);
        child.setGender(childGender);
        child.setPhone(childPhone);
        child.setBpl(childBpl);
        child.setWeight(childWeight);
        child.setHeight(childHeight);
        child.setAddress(childAddress);
        child.setGramPanchayat(childGramPanchayat);
        child.setBlock(childBlock);
        child.setDistrict(childDistrict);
        child.setMtc(childMTC);
        child.setTreatment(childTreatment);
        child.setVisitCount(Integer.valueOf(visitCount));
        child.setParentSummary(parentSummary);
        if (isEdit){
            child.setParentSupport(!mParentSupport.isChecked());
        }else {
            child.setParentSupport(true);
        }

        mRootRef.collection("ChildDetails").document(key).set(child).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                FirebaseUser  user = FirebaseAuth.getInstance().getCurrentUser();
                if (isEdit){
                    Map<String, Object> childEditHashMap = new HashMap<>();
                    childEditHashMap.put("edited", FieldValue.serverTimestamp());
                    mRootRef.collection("ChildDetails").document(key).set(childEditHashMap, SetOptions.merge());
                }else {
                    Map<String, Object> childAddHashMap = new HashMap<>();
                    childAddHashMap.put("added", FieldValue.serverTimestamp());
                    Map<String, Object> pushHashMap = new HashMap<>();
                    pushHashMap.put(key, FieldValue.serverTimestamp());
                    assert user != null;
                    mRootRef.collection("WaitingList").document(user.getUid()).set(pushHashMap,SetOptions.merge());
                    mRootRef.collection("ChildDetails").document(key).set(childAddHashMap, SetOptions.merge());
                }
                showDialog.cancel();
                Toast.makeText(AddChildActivity.this, "Successfully added", Toast.LENGTH_SHORT).show();
                switchToMainActivity();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                showDialog.cancel();
                Toast.makeText(AddChildActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void switchToMainActivity() {
        finish();
    }

    private boolean isNameValid(String childName){
        return childName.length() < 3;
    }

    private boolean isAgeValid(int childAge){
        return childAge < 60;
    }

    private boolean isPhoneNumberValid(String childPhone) {
        return childPhone.length() == 10&&(childPhone.startsWith("6")||childPhone.startsWith("7")||childPhone.startsWith("8")||childPhone.startsWith("9"));
    }
    private void showGenderSpinner() {
        String[] problem;
        problem = new String[]{"Select Gender","Male","Female"};

        ArrayAdapter<String> problems = new ArrayAdapter<>(AddChildActivity.this,android.R.layout.simple_spinner_dropdown_item,problem);

        mChildGenderView.setAdapter(problems);
        problems.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    }

    protected Dialog onCreateDialog(int id) {
        if (id == 999) {
            return new DatePickerDialog(AddChildActivity.this,
                    myDateListener, year, month, day);
        }
        return null;
    }

    private DatePickerDialog.OnDateSetListener myDateListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker arg0, int year, int month, int day) {
            showDate(year, month+1, day);
        }
    };

    private void showDate(int year, int month, int day) {
        mChildDobView.setText(new StringBuilder().append(day).append("/")
                .append(month).append("/").append(year));

        Calendar startCalendar = new GregorianCalendar();
        Calendar endCalendar = new GregorianCalendar();

        Calendar c = Calendar.getInstance();

        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String formattedDate = df.format(c.getTime());
        try {
            Date today = df.parse(formattedDate);
            endCalendar.setTime(today);
            Date born = df.parse(mChildDobView.getText().toString());
            startCalendar.setTime(born);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        int diffYear = endCalendar.get(Calendar.YEAR) - startCalendar.get(Calendar.YEAR);
        int diffMonth = diffYear * 12 + endCalendar.get(Calendar.MONTH) - startCalendar.get(Calendar.MONTH);
        mChildAgeLayout.getEditText().setText(String.valueOf(diffMonth));
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
        if (isChecked){
            mParentSummaryLayout.setVisibility(View.VISIBLE);
            mParentVisitLayout.setVisibility(View.VISIBLE);
        }else {
            mParentSummaryLayout.setVisibility(View.GONE);
            mParentVisitLayout.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 7334) {
            if(resultCode == Activity.RESULT_OK){
                Toast.makeText(this, "on", Toast.LENGTH_SHORT).show();
            } if (resultCode == Activity.RESULT_CANCELED) {
                finish();
            }
        }else {
            imagePicker.handleActivityResult(resultCode, requestCode, data);
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        updateLocation();
    }

    private void updateLocation() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        LocationRequest mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(5000);
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                }
            }

            ;
        };
        LocationServices.getFusedLocationProviderClient(this).requestLocationUpdates( mLocationRequest, mLocationCallback,null);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mGoogleApiClient.isConnected() || mGoogleApiClient.isConnecting()){
            mGoogleApiClient.disconnect();
        }
        if (mFusedLocationClient != null && mLocationCallback != null) {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mGoogleApiClient.isConnected() || mGoogleApiClient.isConnecting()){
            mGoogleApiClient.disconnect();
        }
        if (mFusedLocationClient != null && mLocationCallback != null) {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        }
    }
}
