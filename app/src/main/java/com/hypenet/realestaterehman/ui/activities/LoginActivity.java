package com.hypenet.realestaterehman.ui.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;
import com.hypenet.realestaterehman.R;
import com.hypenet.realestaterehman.databinding.ActivityLoginBinding;
import com.hypenet.realestaterehman.model.User;
import com.hypenet.realestaterehman.model.UserResponse;
import com.hypenet.realestaterehman.utils.PrefManager;
import com.hypenet.realestaterehman.utils.Utils;
import com.hypenet.realestaterehman.utils.retrofit.RetrofitClint;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    ActivityLoginBinding binding;
    ProgressDialog progressDialog;
    PrefManager prefManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this,R.layout.activity_login);
        init();
        setListener();
    }

    private void init(){
        prefManager = new PrefManager(this);
        progressDialog = new ProgressDialog(LoginActivity.this);
        progressDialog.setMessage("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);
    }

    public void setListener(){
        binding.login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                login();
            }
        });

        binding.register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this,RegisterActivity.class));
            }
        });
    }

    private boolean invalidateFields(){
        if (!Utils.isEmailValid(Utils.getText(binding.email)))
        {
            Toast.makeText(this, "Enter your email", Toast.LENGTH_SHORT).show();
            return true;
        }

        if (Utils.getText(binding.password).length()<6)
        {
            Toast.makeText(this, "Enter your phone", Toast.LENGTH_SHORT).show();
            return true;
        }

        return false;
    }

    private void login(){
        if (invalidateFields())
            return;

        User user = new User(Utils.getText(binding.email),Utils.getText(binding.password));
        progressDialog.show();

        Call<UserResponse> call = RetrofitClint.getInstance().getApi().login(user);
        call.enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                progressDialog.dismiss();
                if (response.body() != null){
                    if (response.body().getMessage().equals("success")){
                        prefManager.setEmail(Utils.getText(binding.email));
                        prefManager.setName(response.body().getData().getName());
                        prefManager.setId(response.body().getData().getId());
                        prefManager.setImage(response.body().getData().getImage());
                        prefManager.setAddress(response.body().getData().getAddress());
                        prefManager.setCity(response.body().getData().getCity());
                        prefManager.setPhone(response.body().getData().getPhone());
                        prefManager.setUniqueId(response.body().getData().getUnq_id());
                        prefManager.setVerifiedStatus(response.body().getData().getKyc_status());
                        prefManager.setCnic(response.body().getData().getCnic());
                        prefManager.setFront(response.body().getData().getCnic_front());
                        prefManager.setBack(response.body().getData().getCnic_back());
                        getToken();
                    }else{
                        Toast.makeText(LoginActivity.this, "Email/Password are incorrect", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(LoginActivity.this, "Server Error! please try again", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                progressDialog.dismiss();
                Log.d("TAG", "onFailure: "+t.getMessage());
                Toast.makeText(LoginActivity.this, "Connection Error! please try again", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void getToken(){
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        progressDialog.dismiss();
                        if (task.isSuccessful()) {
                            String token = task.getResult();
                            FirebaseDatabase.getInstance().getReference().child("users").child(String.valueOf(prefManager.getUniqueId())).child("token").setValue(token);
                        }
                        Toast.makeText(LoginActivity.this, "Successfully Logged In", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getApplicationContext(),MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK));
                        overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
                        finish();
                    }
                });
    }

}