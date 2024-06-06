package com.hypenet.realestaterehman.ui.fragments;

import android.Manifest;
import android.app.Activity;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.hypenet.realestaterehman.R;
import com.hypenet.realestaterehman.databinding.FragmentHomeBinding;
import com.hypenet.realestaterehman.databinding.FragmentProfileBinding;
import com.hypenet.realestaterehman.model.ApiResponse;
import com.hypenet.realestaterehman.model.City;
import com.hypenet.realestaterehman.model.UserResponse;
import com.hypenet.realestaterehman.ui.activities.MainActivity;
import com.hypenet.realestaterehman.ui.activities.RegisterActivity;
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

public class ProfileFragment extends Fragment {

    List<City> cities;
    ProgressDialog progressDialog;
    PrefManager prefManager;
    private Uri imageUri;
    private static final int PERMISSION_CODE = 1021;
    Activity activity;
    FragmentProfileBinding binding;
    private static final String TAG = "HomeFragment";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_profile,container,false);
        init();
        setListener();
        return binding.getRoot();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (Activity) context;
    }

    public void init(){
        cities = new ArrayList<>();
        prefManager = new PrefManager(activity);
        progressDialog = new ProgressDialog(activity);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setMessage("Please wait...");

        binding.name.setText(prefManager.getName());
        binding.phone.setText(prefManager.getPhone());
        binding.address.setText(prefManager.getAddress());
        binding.email.setText(prefManager.getEmail());
        Log.d(TAG, "init:image "+prefManager.getImage());
        Glide.with(activity).load(prefManager.getImage()).into(binding.profile);
        getCities();
    }
    public void setListener(){
        binding.update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateProfile();
            }
        });

        binding.change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickImageLauncher.launch("image/*");
            }
        });

        binding.camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickImageLauncher.launch("image/*");
            }
        });
    }

    private boolean invalidateFields(){
        if (Utils.isEmpty(binding.name))
            return true;

        if (Utils.isEmpty(binding.phone))
            return true;

        if (Utils.isEmpty(binding.address))
            return true;

        if (binding.city.getSelectedItemPosition() == 0){
            return true;
        }

        return false;
    }

    public void updateProfile(){

        if (invalidateFields())
            return;

        String city_id = cities.get(binding.city.getSelectedItemPosition()-1).getCountry_id();
        RequestBody name_part = RequestBody.create(MultipartBody.FORM, Utils.getText(binding.name));
        RequestBody phone_part = RequestBody.create(MultipartBody.FORM, Utils.getText(binding.phone));
        RequestBody city_part = RequestBody.create(MultipartBody.FORM, city_id);
        RequestBody address_part = RequestBody.create(MultipartBody.FORM, Utils.getText(binding.address));
        RequestBody email_part = RequestBody.create(MultipartBody.FORM, Utils.getText(binding.email));

        MultipartBody.Part image = null;
        if (imageUri != null){
            File file = null;
            try {
                file = getFile(activity,imageUri);
                RequestBody imagePart = RequestBody.create(MediaType.parse("*/*"), file);
                image = MultipartBody.Part.createFormData("image",file.getName(),imagePart);
            } catch (IOException e) {
                Log.d(TAG, "update: "+e.getMessage());
            }
        }

        progressDialog.show();
        Call<UserResponse> call = RetrofitClint.getInstance().getApi().update_profile(
                String.valueOf(prefManager.getId()),name_part,email_part,phone_part,city_part,address_part,image);
        call.enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                progressDialog.dismiss();
                if (response.body() !=  null){
                    if (response.body().getMessage().equals("success")){
                        prefManager.setName(binding.name.getText().toString().trim());
                        if (imageUri != null)
                            prefManager.setImage(response.body().getData().getImage());
                        prefManager.setAddress(binding.address.getText().toString());
                        prefManager.setCity(binding.city.getSelectedItem().toString());
                        prefManager.setPhone(binding.phone.getText().toString().trim());
                        Toast.makeText(activity, "Successfully profile update", Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(activity, "Something went wrong. Try again", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(activity, "Server Error! please try again later", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(activity, "Check you internet connection and try again", Toast.LENGTH_SHORT).show();
            }
        });

    }

    public boolean checkAndRequestPermissions() {
        int write = ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int read = ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE);
        int read_image = ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_MEDIA_IMAGES);
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
            ActivityCompat.requestPermissions(activity, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), PERMISSION_CODE);
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
                            Toast.makeText(activity, "Permission Granted Successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            explain(getResources().getString(R.string.you_need_some_mandatory));
                        }
                    }else {
                        if (perms.get(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                                && perms.get(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                        ) {
                            Toast.makeText(activity, "Permission Granted Successfully", Toast.LENGTH_SHORT).show();
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
        final AlertDialog.Builder dialog = new AlertDialog.Builder(activity);
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
                        binding.profile.setImageURI(uri);
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
                        int selected = 0;
                        for (int i = 0; i < cities.size(); i++) {
                            cities_name.add(cities.get(i).getName());
                            if (cities.get(i).getName().equals(prefManager.getCity())){
                                selected = i;
                            }
                        }
                        ArrayAdapter ad = new ArrayAdapter(activity, android.R.layout.simple_spinner_item, cities_name);
                        ad.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        binding.city.setAdapter(ad);
                        binding.city.setSelection(selected+1);
                    }else{
                        Toast.makeText(activity, "Something went wrong. Try again", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(activity, "Server Error! please try again later", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<City>>> call, Throwable t) {
                progressDialog.dismiss();
                Log.d(TAG, "onFailure: "+t.getMessage());
                Toast.makeText(activity, "Check you internet connection and try again", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
