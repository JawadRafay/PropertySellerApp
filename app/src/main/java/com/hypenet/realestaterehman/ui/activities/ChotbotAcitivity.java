package com.hypenet.realestaterehman.ui.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.Bundle;
import android.view.View;

import com.hypenet.realestaterehman.R;
import com.hypenet.realestaterehman.adapters.ChatListAdapter;
import com.hypenet.realestaterehman.databinding.ActivityChotbotAcitivityBinding;
import com.hypenet.realestaterehman.model.Chat;

import java.util.ArrayList;

public class ChotbotAcitivity extends AppCompatActivity {

    ActivityChotbotAcitivityBinding binding;
    ChatListAdapter adapter;
    ArrayList<Chat> chats;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this,R.layout.activity_chotbot_acitivity);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        getWindow().setStatusBarColor(getResources().getColor( R.color.light_gray));
        init();
        setListeners();
        setDemo();
    }

    public void init(){
        chats = new ArrayList<>();
        binding.recycler.setLayoutManager(new LinearLayoutManager(this));
    }

    public void setListeners(){
        binding.back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    public void setDemo(){
    }
}