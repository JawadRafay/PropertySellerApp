package com.hypenet.realestaterehman.ui.activities;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

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
import android.widget.Toast;

import com.google.gson.Gson;
import com.hypenet.realestaterehman.R;
import com.hypenet.realestaterehman.databinding.ActivityRegisterBinding;
import com.hypenet.realestaterehman.model.ApiResponse;
import com.hypenet.realestaterehman.model.City;
import com.hypenet.realestaterehman.model.House;
import com.hypenet.realestaterehman.model.User;
import com.hypenet.realestaterehman.model.UserResponse;
import com.hypenet.realestaterehman.utils.PrefManager;
import com.hypenet.realestaterehman.utils.Utils;
import com.hypenet.realestaterehman.utils.retrofit.RetrofitClint;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    List<City> cities;
    ProgressDialog progressDialog;
    ActivityRegisterBinding binding;
    PrefManager prefManager;
    private Uri imageUri;
    private static final int PERMISSION_CODE = 1021;
    private static final String TAG = "RegisterActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this,R.layout.activity_register);
        init();
        setListeners();
        getCities();
    }

    public void init(){
        cities = new ArrayList<>();
        prefManager = new PrefManager(this);
        progressDialog = new ProgressDialog(RegisterActivity.this);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setMessage("Please wait...");
    }

    private boolean invalidateFields(){
        if (Utils.isEmpty(binding.name)){
            Toast.makeText(this, "Enter your name", Toast.LENGTH_SHORT).show();
            return true;
        }

        if (!Utils.isEmailValid(Utils.getText(binding.email)))
        {
            Toast.makeText(this, "Enter your email address", Toast.LENGTH_SHORT).show();
            return true;
        }

        if (Utils.isEmpty(binding.phone))
        {
            Toast.makeText(this, "Enter your phone number", Toast.LENGTH_SHORT).show();
            return true;
        }

        if (Utils.isEmpty(binding.address))
        {
            Toast.makeText(this, "Enter your address", Toast.LENGTH_SHORT).show();
            return true;
        }

        if (binding.city.getSelectedItemPosition() == 0){
            Toast.makeText(this, "Select your city ", Toast.LENGTH_SHORT).show();
            return true;
        }

        if (imageUri == null){
            Toast.makeText(this, "Select Profile Image", Toast.LENGTH_SHORT).show();
            return true;
        }

        if (Utils.getText(binding.password).length()<6)
        {
            Toast.makeText(this, "Password should be minimum 6 characters", Toast.LENGTH_SHORT).show();
            return true;
        }

        return false;
    }

    public void setListeners(){
        binding.register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register();
            }
        });

        binding.login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        binding.image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkAndRequestPermissions())
                    pickImageLauncher.launch("image/*");
            }
        });
    }
    public void register(){

        if (invalidateFields()) {
            Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        String city_id = cities.get(binding.city.getSelectedItemPosition()-1).getCountry_id();
        RequestBody name_part = RequestBody.create(MultipartBody.FORM, Utils.getText(binding.name));
        RequestBody email_part = RequestBody.create(MultipartBody.FORM, Utils.getText(binding.email));
        RequestBody phone_part = RequestBody.create(MultipartBody.FORM, Utils.getText(binding.phone));
        RequestBody password_part = RequestBody.create(MultipartBody.FORM, Utils.getText(binding.password));
        RequestBody city_part = RequestBody.create(MultipartBody.FORM, city_id);
        RequestBody address_part = RequestBody.create(MultipartBody.FORM, Utils.getText(binding.address));


        MultipartBody.Part image = null;
        if (imageUri != null){
            File file = null;
            try {
                file = getFile(RegisterActivity.this,imageUri);
                RequestBody imagePart = RequestBody.create(MediaType.parse("*/*"), file);
                image = MultipartBody.Part.createFormData("image",file.getName(),imagePart);
            } catch (IOException e) {
                Log.d(TAG, "update: "+e.getMessage());
            }
        }

        progressDialog.show();
        Call<UserResponse> call = RetrofitClint.getInstance().getApi().register(
                name_part,email_part,password_part,phone_part,city_part,address_part,image);
        call.enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                progressDialog.dismiss();
                Log.d(TAG, "onResponse: "+response);
                if (response.body() !=  null){
                    Log.d(TAG, "onResponse:body "+new Gson().toJson(response.body()));
                    if (response.body().getMessage().equals("success")){
                        prefManager.setUniqueId(response.body().getData().getUnq_id());
                        prefManager.setEmail(Utils.getText(binding.email));
                        prefManager.setName(binding.name.getText().toString().trim());
                        prefManager.setId(response.body().getData().getId());
                        prefManager.setImage(response.body().getData().getImage());
                        prefManager.setAddress(binding.address.getText().toString());
                        prefManager.setCity(binding.city.getSelectedItem().toString());
                        prefManager.setPhone(binding.phone.getText().toString().trim());
                        prefManager.setVerifiedStatus("Unverified");
                        prefManager.setCnic("");
                        prefManager.setFront("");
                        prefManager.setBack("");
                        Toast.makeText(RegisterActivity.this, "Successfully account created", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getApplicationContext(),MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK));
                        overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
                        finish();
                    }else if (response.body().getMessage().equals("already")) {
                        Toast.makeText(RegisterActivity.this, "Email address already exists", Toast.LENGTH_SHORT).show();

                    }else{
                        Toast.makeText(RegisterActivity.this, "Something went wrong. Try again", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(RegisterActivity.this, "Server Error! please try again later", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(RegisterActivity.this, "Check you internet connection and try again", Toast.LENGTH_SHORT).show();
            }
        });

    }

    public boolean checkAndRequestPermissions() {
        int write = ContextCompat.checkSelfPermission(RegisterActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int read = ContextCompat.checkSelfPermission(RegisterActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE);
        int read_image = ContextCompat.checkSelfPermission(RegisterActivity.this, Manifest.permission.READ_MEDIA_IMAGES);
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
            ActivityCompat.requestPermissions(RegisterActivity.this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), PERMISSION_CODE);
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
                            Toast.makeText(RegisterActivity.this, "Permission Granted Successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            explain(getResources().getString(R.string.you_need_some_mandatory));
                        }
                    }else {
                        if (perms.get(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                                && perms.get(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                        ) {
                            Toast.makeText(RegisterActivity.this, "Permission Granted Successfully", Toast.LENGTH_SHORT).show();
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
        final AlertDialog.Builder dialog = new AlertDialog.Builder(RegisterActivity.this);
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
                        //binding.initialAdd.setVisibility(View.GONE);
                        imageUri = uri;
                        binding.image.setImageURI(uri);
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
                        ArrayAdapter ad = new ArrayAdapter(RegisterActivity.this, android.R.layout.simple_spinner_item, cities_name);
                        ad.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        binding.city.setAdapter(ad);
                    }else{
                        Toast.makeText(RegisterActivity.this, "Something went wrong. Try again", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(RegisterActivity.this, "Server Error! please try again later", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<City>>> call, Throwable t) {
                progressDialog.dismiss();
                Log.d(TAG, "onFailure: "+t.getMessage());
                Toast.makeText(RegisterActivity.this, "Check you internet connection and try again", Toast.LENGTH_SHORT).show();
            }
        });
    }
}