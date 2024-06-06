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
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.hypenet.realestaterehman.R;
import com.hypenet.realestaterehman.databinding.ActivityKycactivityBinding;
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

public class KYCActivity extends AppCompatActivity {

    ActivityKycactivityBinding binding;
    Uri imageUri1,imageUri2;
    private static final int PERMISSION_CODE = 1021;
    PrefManager prefManager;
    ProgressDialog progressDialog;
    private static final String TAG = "KYCActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this,R.layout.activity_kycactivity);
        init();
        setListeners();
        checkAndRequestPermissions();
    }

    public void init(){
        prefManager = new PrefManager(KYCActivity.this);
        progressDialog = new ProgressDialog(KYCActivity.this);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setMessage("Please wait...");

        binding.cnic.setText(prefManager.getCnic());
        if (!prefManager.getFront().isEmpty()){
            Glide.with(KYCActivity.this).load(prefManager.getFront()).into(binding.image1);
            Glide.with(KYCActivity.this).load(prefManager.getBack()).into(binding.image2);
        }
    }

    public void setListeners(){
        binding.image1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickImageLauncher1.launch("image/*");
            }
        });
        binding.image2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickImageLauncher2.launch("image/*");
            }
        });

        binding.upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadImage();
            }
        });

        binding.back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    public void uploadImage(){

        if (binding.cnic.getText().toString().isEmpty()) {
            Toast.makeText(this, "Enter cnic", Toast.LENGTH_SHORT).show();
            return;
        }

        if (imageUri1 == null){
            Toast.makeText(this, "Select front cnic image", Toast.LENGTH_SHORT).show();
            return;
        }

        if (imageUri2 == null){
            Toast.makeText(this, "Select back cnic image", Toast.LENGTH_SHORT).show();
            return;
        }


        RequestBody cnic_part = RequestBody.create(MultipartBody.FORM, Utils.getText(binding.cnic));


        MultipartBody.Part image1 = null;
        File file = null;
        try {
            file = getFile(KYCActivity.this,imageUri1);
            RequestBody imagePart = RequestBody.create(MediaType.parse("*/*"), file);
            image1 = MultipartBody.Part.createFormData("cnic_front",file.getName(),imagePart);
        } catch (IOException e) {
            Log.d(TAG, "update: "+e.getMessage());
        }

        MultipartBody.Part image2 = null;
        File file2 = null;
        try {
            file2 = getFile(KYCActivity.this,imageUri2);
            RequestBody imagePart = RequestBody.create(MediaType.parse("*/*"), file2);
            image2 = MultipartBody.Part.createFormData("cnic_back",file2.getName(),imagePart);
        } catch (IOException e) {
            Log.d(TAG, "update: "+e.getMessage());
        }

        progressDialog.show();
        Call<UserResponse> call = RetrofitClint.getInstance().getApi().update_kyc(String.valueOf(prefManager.getId()),cnic_part,image1,image2);
        call.enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                progressDialog.dismiss();
                Log.d(TAG, "onResponse: "+response);
                if (response.body() !=  null){
                    Log.d(TAG, "onResponse:body "+new Gson().toJson(response.body()));
                    if (response.body().getMessage().equals("success")){
                        prefManager.setVerifiedStatus(response.body().getData().getKyc_status());
                        prefManager.setCnic(response.body().getData().getCnic());
                        prefManager.setFront(response.body().getData().getCnic_front());
                        prefManager.setBack(response.body().getData().getCnic_back());
                        Toast.makeText(KYCActivity.this, "Successfully uploaded", Toast.LENGTH_SHORT).show();
                        finish();
                    }else{
                        Toast.makeText(KYCActivity.this, "Something went wrong. Try again", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(KYCActivity.this, "Server Error! please try again later", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(KYCActivity.this, "Check you internet connection and try again", Toast.LENGTH_SHORT).show();
            }
        });

    }

    public boolean checkAndRequestPermissions() {
        int write = ContextCompat.checkSelfPermission(KYCActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int read = ContextCompat.checkSelfPermission(KYCActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE);
        int read_image = ContextCompat.checkSelfPermission(KYCActivity.this, Manifest.permission.READ_MEDIA_IMAGES);
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
            ActivityCompat.requestPermissions(KYCActivity.this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), PERMISSION_CODE);
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
                            Toast.makeText(KYCActivity.this, "Permission Granted Successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            explain(getResources().getString(R.string.you_need_some_mandatory));
                        }
                    }else {
                        if (perms.get(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                                && perms.get(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                        ) {
                            Toast.makeText(KYCActivity.this, "Permission Granted Successfully", Toast.LENGTH_SHORT).show();
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
        final AlertDialog.Builder dialog = new AlertDialog.Builder(KYCActivity.this);
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


    private ActivityResultLauncher<String> pickImageLauncher1 = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            new ActivityResultCallback<Uri>() {
                @Override
                public void onActivityResult(Uri uri) {
                    if (uri != null) {
                        // Do something with the image URI
                        //binding.initialAdd.setVisibility(View.GONE);
                        imageUri1 = uri;
                        binding.image1.setImageURI(uri);
                    }
                }
            }
    );

    private ActivityResultLauncher<String> pickImageLauncher2 = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            new ActivityResultCallback<Uri>() {
                @Override
                public void onActivityResult(Uri uri) {
                    if (uri != null) {
                        // Do something with the image URI
                        //binding.initialAdd.setVisibility(View.GONE);
                        imageUri2 = uri;
                        binding.image2.setImageURI(uri);
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
}