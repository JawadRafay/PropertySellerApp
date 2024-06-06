package com.hypenet.realestaterehman.service;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.hypenet.realestaterehman.utils.PrefManager;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    PrefManager prefManager;
    private static final String TAG = "MyFirebaseMessagingServ";

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        prefManager = new PrefManager(this);
        if (prefManager.getId() != 0)
            FirebaseDatabase.getInstance().getReference().child("users").child(String.valueOf(prefManager.getUniqueId())).child("token").setValue(token);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);
        Log.d(TAG, "onMessageReceived: "+new Gson().toJson(message.getNotification()));
    }
}
