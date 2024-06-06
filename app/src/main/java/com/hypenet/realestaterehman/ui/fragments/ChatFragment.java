package com.hypenet.realestaterehman.ui.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.hypenet.realestaterehman.R;
import com.hypenet.realestaterehman.adapters.ChatListAdapter;
import com.hypenet.realestaterehman.adapters.MessageItem;
import com.hypenet.realestaterehman.databinding.FragmentChatBinding;
import com.hypenet.realestaterehman.databinding.FragmentHomeBinding;
import com.hypenet.realestaterehman.model.Chat;
import com.hypenet.realestaterehman.model.ChatBotCompletion;
import com.hypenet.realestaterehman.model.ChatBotResponse;
import com.hypenet.realestaterehman.model.MessageModels;
import com.hypenet.realestaterehman.ui.activities.ChatViewActivity;
import com.hypenet.realestaterehman.utils.PrefManager;
import com.hypenet.realestaterehman.utils.Utils;
import com.hypenet.realestaterehman.utils.retrofit.RetrofitClint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatFragment extends Fragment {

    Activity activity;
    FragmentChatBinding binding;
    ArrayList<MessageModels> inboxArraylist;
    DatabaseReference rootRef;
    MessageItem inboxItem;
    ValueEventListener valueEventListener;
    Query inboxQuery;
    PrefManager prefManager;
    private static final int PERMISSION_CODE = 32141;

    private static final String TAG = "ChatFragment";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_chat,container,false);
        init();
        setListeners();
        return binding.getRoot();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (Activity) context;
    }

    public void init(){
        prefManager = new PrefManager(activity);
        rootRef = FirebaseDatabase.getInstance().getReference();
        inboxArraylist =new ArrayList<>();
        binding.recycler.setLayoutManager(new LinearLayoutManager(activity));
        inboxItem = new MessageItem(activity, inboxArraylist, new MessageItem.OnItemClickListener() {
            @Override
            public void onItemClick(MessageModels item) {
                chatFragment(String.valueOf(prefManager.getUniqueId()),item.getId(),item.getName(),item.getPicture());
            }
        }, new MessageItem.OnLongItemClickListener() {
            @Override
            public void onLongItemClick(MessageModels item) {

            }
        });

        binding.recycler.setAdapter(inboxItem);
    }

    public void setListeners(){

        binding.getRoot().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.hideSoftKeyboard(getActivity());
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: ");
        inboxQuery = rootRef.child("Inbox").child(String.valueOf(prefManager.getUniqueId())).orderByChild("timestamp");
        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange:count "+dataSnapshot.getChildrenCount());
                inboxArraylist.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    MessageModels model = new MessageModels();
                    model.setId(ds.getKey());
                    model.setName(ds.child("name").getValue().toString());
                    model.setMessage(ds.child("msg").getValue().toString());
                    model.setTimestamp(ds.child("date").getValue().toString());
                    model.setStatus(ds.child("status").getValue().toString());
                    model.setPicture(ds.child("pic").getValue().toString());
                    inboxArraylist.add(model);
                }

                inboxItem.notifyDataSetChanged();

                if (inboxArraylist.isEmpty()) {
                    binding.nomatch.setVisibility(View.VISIBLE);
                } else {
                    binding.nomatch.setVisibility(View.GONE);
                }
            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: "+databaseError.getMessage());
            }


        };
        inboxQuery.addValueEventListener(valueEventListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if(inboxQuery !=null)
            inboxQuery.removeEventListener(valueEventListener);
    }

    public void chatFragment(String senderid,String receiverid,String name,String picture){
        Intent intent = new Intent(getActivity(), ChatViewActivity.class);
        intent.putExtra("Sender_Id",senderid);
        intent.putExtra("Receiver_Id",receiverid);
        intent.putExtra("picture",picture);
        intent.putExtra("name",name);
        startActivity(intent);

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
                        } else {
                            explain(getResources().getString(R.string.you_need_some_mandatory));
                        }
                    }else {
                        if (perms.get(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                                && perms.get(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                        ) {
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

}
