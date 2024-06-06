package com.hypenet.realestaterehman.ui.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;

import com.google.firebase.FirebaseApp;
import com.hypenet.realestaterehman.R;
import com.hypenet.realestaterehman.utils.PrefManager;

public class SplashActivity extends AppCompatActivity {

    PrefManager prefManager;
    private Handler handler = new Handler();
    private Runnable runnable;
    private boolean isTrigger = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
       super.onCreate(savedInstanceState);
       setContentView(R.layout.activity_splash);

       Window window = getWindow();
       window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
       window.setStatusBarColor(getResources().getColor(R.color.light_gray));
       prefManager = new PrefManager(SplashActivity.this);
       runnable = new Runnable() {
             @Override
             public void run() {
                 if (prefManager.getId() != 0)
                    startActivity(new Intent(SplashActivity.this,MainActivity.class));
                 else {
                     startActivity(new Intent(SplashActivity.this,LoginActivity.class));
                 }
                 overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
                 finish();
             }
        };
    }

    @Override
    protected void onPause() {
       super.onPause();
       handler.removeCallbacks(runnable);
       handler.removeCallbacksAndMessages(null);
       isTrigger = false;
    }

    @Override
    protected void onResume() {
       super.onResume();
       if (!isTrigger){
           isTrigger = true;
           handler.postDelayed(runnable,1500);
       }
    }
}