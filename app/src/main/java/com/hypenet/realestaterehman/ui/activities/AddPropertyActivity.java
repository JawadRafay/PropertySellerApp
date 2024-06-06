package com.hypenet.realestaterehman.ui.activities;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;
import android.widget.Toast;

import com.essam.simpleplacepicker.MapActivity;
import com.essam.simpleplacepicker.utils.SimplePlacePicker;
import com.google.android.gms.common.api.Status;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.gson.Gson;
import com.hypenet.realestaterehman.R;
import com.hypenet.realestaterehman.adapters.ImageDetailsAdapter;
import com.hypenet.realestaterehman.databinding.ActivityAddPropertyBinding;
import com.hypenet.realestaterehman.databinding.ActivityRegisterBinding;
import com.hypenet.realestaterehman.model.ApiResponse;
import com.hypenet.realestaterehman.model.City;
import com.hypenet.realestaterehman.model.GalleryModel;
import com.hypenet.realestaterehman.model.ImageDetect;
import com.hypenet.realestaterehman.model.TokenModel;
import com.hypenet.realestaterehman.model.UserResponse;
import com.hypenet.realestaterehman.utils.CommonFeatures;
import com.hypenet.realestaterehman.utils.NumberFormatter;
import com.hypenet.realestaterehman.utils.PrefManager;
import com.hypenet.realestaterehman.utils.Utils;
import com.hypenet.realestaterehman.utils.retrofit.RetrofitClint;
import com.hypenet.realestaterehman.utils.retrofit.RetrofitClintNyckel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddPropertyActivity extends AppCompatActivity {

    List<City> cities;
    ProgressDialog progressDialog;
    ActivityAddPropertyBinding binding;
    PrefManager prefManager;
    List<GalleryModel> galleryModels;
    ImageDetailsAdapter adapter;
    double latitude = 0.0;
    double longitude = 0.0;
    long minPrice = 50000;
    long step = 5000;
    private static final int PERMISSION_CODE = 1021;
    private static final String TAG = "AddPropertyActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this,R.layout.activity_add_property);
        init();
        setListeners();
        getCities();
    }

    public void init(){
        galleryModels = new ArrayList<>();
        cities = new ArrayList<>();
        prefManager = new PrefManager(this);
        progressDialog = new ProgressDialog(AddPropertyActivity.this);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setMessage("Please wait...");
        adapter = new ImageDetailsAdapter(this,galleryModels);
        binding.recycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL,false));
        binding.recycler.setAdapter(adapter);
    }

    public void setListeners(){
        binding.addImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkAndRequestPermissions()){
                    pickImageLauncher.launch("image/*");
                }
            }
        });

        binding.add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addProperty();
            }
        });

        binding.back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        binding.areaName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickLocation();
            }
        });

        binding.price.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                long price = minPrice + (progress * step);
                binding.priceTv.setText(NumberFormatter.formatNumber(price));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private boolean invalidateFields(){
        if (Utils.isEmpty(binding.title))
            return true;

        if (Utils.isEmpty(binding.description))
            return true;

        if (Utils.isEmpty(binding.areaName))
            return true;

        if (binding.type.getSelectedItemPosition() == 0)
            return true;

        if (Utils.isEmpty(binding.owner))
            return true;

        if (!Utils.isEmailValid(Utils.getText(binding.email)))
            return true;

        if (Utils.isEmpty(binding.phone))
            return true;

        if (Utils.isEmpty(binding.email))
            return true;

        if (binding.city.getSelectedItemPosition() == 0){
            return true;
        }

        return false;
    }

    public void addProperty(){

        if (invalidateFields()){
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }
        long price = minPrice + (binding.price.getProgress() * step);
        String city_id = cities.get(binding.city.getSelectedItemPosition()-1).getCountry_id();
        RequestBody title_part = RequestBody.create(MultipartBody.FORM, Utils.getText(binding.title));
        RequestBody description_part = RequestBody.create(MultipartBody.FORM, Utils.getText(binding.description));
        RequestBody phone_part = RequestBody.create(MultipartBody.FORM, Utils.getText(binding.phone));
        RequestBody email_part = RequestBody.create(MultipartBody.FORM, Utils.getText(binding.email));
        RequestBody latitude_part = RequestBody.create(MultipartBody.FORM, String.valueOf(latitude));
        RequestBody longitude_part = RequestBody.create(MultipartBody.FORM, String.valueOf(longitude));
        RequestBody type_part = RequestBody.create(MultipartBody.FORM, binding.type.getSelectedItem().toString());
        RequestBody owner_part = RequestBody.create(MultipartBody.FORM, Utils.getText(binding.owner));
        RequestBody city_part = RequestBody.create(MultipartBody.FORM, city_id);
        RequestBody area_name_part = RequestBody.create(MultipartBody.FORM, Utils.getText(binding.areaName));
        RequestBody price_part = RequestBody.create(MultipartBody.FORM, String.valueOf(price));
        RequestBody sell_type_part = RequestBody.create(MultipartBody.FORM, binding.sellingType.getSelectedItem().toString());
        RequestBody seller_id_part = RequestBody.create(MultipartBody.FORM, String.valueOf(prefManager.getId()));


        MultipartBody.Part[] imagesParts = new MultipartBody.Part[galleryModels.size()];

        for (int i=0;i< galleryModels.size();i++){
            try {
                File file = getFile(AddPropertyActivity.this,Uri.parse(galleryModels.get(i).getImage())); // Convert URI to file
                RequestBody imagePart = RequestBody.create(MediaType.parse("image/*"), file);
                imagesParts[i] = MultipartBody.Part.createFormData("images["+i+"]",file.getName(),imagePart);
            } catch (IOException e) {
                Log.d(TAG, "update: "+e.getMessage());
            }
        }
        progressDialog.setMessage("Please wait...");
        progressDialog.show();
        Call<UserResponse> call = RetrofitClint.getInstance().getApi().add_property(
                title_part,description_part,phone_part,email_part,owner_part,latitude_part,longitude_part,area_name_part
                ,type_part,price_part,sell_type_part,seller_id_part,city_part,imagesParts);
        call.enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                progressDialog.dismiss();
                Log.d(TAG, "onResponse: "+response);
                if (response.body() !=  null){
                    Log.d(TAG, "onResponse:body "+new Gson().toJson(response.body()));
                    if (response.body().getMessage().equals("success")){
                        Toast.makeText(AddPropertyActivity.this, "Successfully added", Toast.LENGTH_SHORT).show();
                        finish();
                    }else{
                        Toast.makeText(AddPropertyActivity.this, "Something went wrong. Try again", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(AddPropertyActivity.this, "Server Error! please try again later", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(AddPropertyActivity.this, "Check you internet connection and try again", Toast.LENGTH_SHORT).show();
            }
        });

    }

    public boolean checkAndRequestPermissions() {
        int write = ContextCompat.checkSelfPermission(AddPropertyActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int read = ContextCompat.checkSelfPermission(AddPropertyActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE);
        int read_image = ContextCompat.checkSelfPermission(AddPropertyActivity.this, Manifest.permission.READ_MEDIA_IMAGES);
        List<String> listPermissionsNeeded = new ArrayList<>();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (read_image != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(Manifest.permission.READ_MEDIA_IMAGES);
            }
        }else {
            if (write != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }

            if (read != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        }

        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(AddPropertyActivity.this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), PERMISSION_CODE);
            return false;
        }else{
            return true;
        }
    }

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {

        switch (requestCode) {
            case PERMISSION_CODE: {

                Map<String, Integer> perms = new HashMap<>();
                // Initialize the map with both permissions
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    perms.put(Manifest.permission.READ_MEDIA_IMAGES, PackageManager.PERMISSION_GRANTED);
                }else {
                    perms.put(Manifest.permission.WRITE_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
                    perms.put(Manifest.permission.READ_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
                }

                // Fill with actual results from user
                if (grantResults.length > 0) {
                    for (int i = 0; i < permissions.length; i++)
                        perms.put(permissions[i], grantResults[i]);
                    // Check for both permissions
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        if (perms.get(Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED
                        ) {
                            Toast.makeText(AddPropertyActivity.this, "Permission Granted Successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            explain(getResources().getString(R.string.you_need_some_mandatory));
                        }
                    }else {
                        if (perms.get(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                                && perms.get(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                        ) {
                            Toast.makeText(AddPropertyActivity.this, "Permission Granted Successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            explain(getResources().getString(R.string.you_need_some_mandatory));
                        }
                    }
                }
            }
            break;
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void explain(String msg) {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(AddPropertyActivity.this);
        dialog.setMessage(msg)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        checkAndRequestPermissions();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    }
                });
        dialog.show();
    }


    private ActivityResultLauncher<String> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            new ActivityResultCallback<Uri>() {
                @Override
                public void onActivityResult(Uri uri) {
                    if (uri != null) {
                        // Do something with the image URI
                        detectImage(uri);
                    }
                }
            }
    );

    public File getFile(Context context, Uri uri) throws IOException {
        File destinationFilename = new File(context.getFilesDir().getPath() + File.separatorChar + queryName(context, uri));
        try (InputStream ins = context.getContentResolver().openInputStream(uri)) {
            createFileFromStream(ins, destinationFilename);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return destinationFilename;
    }

    public void createFileFromStream(InputStream ins, File destination) {
        try (OutputStream os = new FileOutputStream(destination)) {
            byte[] buffer = new byte[4096];
            int length;
            while ((length = ins.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
            os.flush();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private String queryName(Context context, Uri uri) {
        Cursor returnCursor =
                context.getContentResolver().query(uri, null, null, null, null);
        assert returnCursor != null;
        int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        returnCursor.moveToFirst();
        String name = returnCursor.getString(nameIndex);
        returnCursor.close();
        return name;
    }

    public void getCities(){
        progressDialog.setMessage("Please wait...");
        progressDialog.show();
        Call<ApiResponse<List<City>>> call = RetrofitClint.getInstance().getApi().get_cities();
        call.enqueue(new Callback<ApiResponse<List<City>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<City>>> call, Response<ApiResponse<List<City>>> response) {
                progressDialog.dismiss();
                if (response.body() !=  null){
                    if (response.body().getMessage().equals("success")){
                        cities.addAll(response.body().getData());
                        List<String> cities_name = new ArrayList<>();
                        cities_name.add("Select city");
                        for (City city : cities) {
                            cities_name.add(city.getName());
                        }
                        ArrayAdapter ad = new ArrayAdapter(AddPropertyActivity.this, android.R.layout.simple_spinner_item, cities_name);
                        ad.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        binding.city.setAdapter(ad);
                    }else{
                        Toast.makeText(AddPropertyActivity.this, "Something went wrong. Try again", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(AddPropertyActivity.this, "Server Error! please try again later", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<City>>> call, Throwable t) {
                progressDialog.dismiss();
                Log.d(TAG, "onFailure: "+t.getMessage());
                Toast.makeText(AddPropertyActivity.this, "Check you internet connection and try again", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void detectImage(Uri uri){
        File file = null;
        try {
            file = getFile(AddPropertyActivity.this, uri);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        RequestBody imagePart = RequestBody.create(MediaType.parse("image/*"), file);
        MultipartBody.Part imagesPart = MultipartBody.Part.createFormData("image",file.getName(),imagePart);
        progressDialog.setMessage("Analyzing image...");
        progressDialog.show();
        Call<ApiResponse<ImageDetect>> call = RetrofitClint.getInstance().getApi().detect_image(imagesPart);
        call.enqueue(new Callback<ApiResponse<ImageDetect>>() {
            @Override
            public void onResponse(Call<ApiResponse<ImageDetect>> call, Response<ApiResponse<ImageDetect>> response) {
                progressDialog.dismiss();
                Log.d(TAG, "onResponse: "+response);
                if (response.body() !=  null){
                    Log.d(TAG, "onResponse:body "+new Gson().toJson(response.body()));
                    if (response.body().getMessage().equals("success")){
                        if (response.body().getData().getConfidence()>0.80){
                            if (response.body().getData().getLabelName().equalsIgnoreCase("House")){
                                binding.type.setSelection(2);
                                galleryModels.add(new GalleryModel(uri.toString()));
                                adapter.notifyItemInserted(galleryModels.size()-1);
                            }else if (response.body().getData().getLabelName().equalsIgnoreCase("Plot")){
                                binding.type.setSelection(1);
                                galleryModels.add(new GalleryModel(uri.toString()));
                                adapter.notifyItemInserted(galleryModels.size()-1);
                            }else{
                                Toast.makeText(AddPropertyActivity.this, "Image is not seems correct. Please upload house or plot image only", Toast.LENGTH_SHORT).show();
                            }
                        }else{
                            Toast.makeText(AddPropertyActivity.this, "Image is not seems correct. Please upload house or plot image only", Toast.LENGTH_SHORT).show();
                        }
                    }else{
                        Toast.makeText(AddPropertyActivity.this, "Something went wrong. Try again", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(AddPropertyActivity.this, "Server Error! please try again later", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<ImageDetect>> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(AddPropertyActivity.this, "Check you internet connection and try again", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void pickLocation(){
        Intent intent = new Intent(this, MapActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(SimplePlacePicker.API_KEY,"AIzaSyCnkwwTsjN0opFb3sjxjt8aTSzKgGDSf5U");
        intent.putExtras(bundle);
        startActivityForResult(intent, SimplePlacePicker.SELECT_LOCATION_REQUEST_CODE);
    }

    private void updateUi(Intent data){
        binding.areaName.setText(data.getStringExtra(SimplePlacePicker.SELECTED_ADDRESS));
        latitude = data.getDoubleExtra(SimplePlacePicker.LOCATION_LAT_EXTRA,-1);
        longitude = data.getDoubleExtra(SimplePlacePicker.LOCATION_LNG_EXTRA,-1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SimplePlacePicker.SELECT_LOCATION_REQUEST_CODE && resultCode == RESULT_OK){
            if (data != null)
                updateUi(data);
        }
    }
}