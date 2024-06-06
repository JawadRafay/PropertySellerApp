package com.hypenet.realestaterehman.ui.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.hypenet.realestaterehman.R;
import com.hypenet.realestaterehman.adapters.ChatListAdapter;
import com.hypenet.realestaterehman.model.ChatModels;
import com.hypenet.realestaterehman.model.NotificationModel;
import com.hypenet.realestaterehman.model.UserResponse;
import com.hypenet.realestaterehman.utils.Constants;
import com.hypenet.realestaterehman.utils.PrefManager;
import com.hypenet.realestaterehman.utils.Utils;
import com.hypenet.realestaterehman.utils.retrofit.RetrofitClint;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import me.everything.android.ui.overscroll.OverScrollDecoratorHelper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatViewActivity extends AppCompatActivity {

    DatabaseReference reference;
    String senderid = "";
    static String Receiverid = "";
    String Receiver_name="";
    String Receiver_pic="null";
    public static String token="null";
    TextView typingIndicator;
    EditText message;

    private DatabaseReference adduserToInbox;
    private DatabaseReference mchatRefReteriving;
    private DatabaseReference sendTypingIndication;
    private DatabaseReference receiveTypingIndication;

    RecyclerView chatrecyclerview;
    TextView userName;
    private List<ChatModels> mChats=new ArrayList<>();
    ChatListAdapter mAdapter;
    ProgressBar progressBar;

    Query queryGetchat;
    Query myBlockStatusQuery;
    Query otherBlockStatusQuery;
    boolean isUserAlreadyBlock = false;

    ImageView profileimage;
    public static String senderidForCheckNotification = "";
    public static String uploadingImageId = "none";

    ProgressDialog loadingView;
    ImageView  sendbtn;
    ImageButton block;
    RelativeLayout chat_layout;
    public static String uploadingAudioId = "none";
    File direct;
    //value event listener
    ValueEventListener valueEventListener;
    ChildEventListener eventListener;
    ValueEventListener my_inbox_listener;
    ValueEventListener other_inbox_listener;
    String imageFilePath;
    PrefManager prefManager;
    private static final String TAG = "ChatViewActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_view);
        init();

        message.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {

            }
            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
            }
        });

        block.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(isUserAlreadyBlock)
                    unblock();
                else
                    block();
            }
        });

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            senderid = bundle.getString("Sender_Id");
            Receiverid = bundle.getString("Receiver_Id");
            Receiver_name=bundle.getString("name");
            Receiver_pic=bundle.getString("picture");
            userName.setText(Receiver_name);
            senderidForCheckNotification =Receiverid;

            if (Receiver_pic.isEmpty()) {
                profileimage.setImageDrawable(getApplicationContext().getResources().getDrawable(R.drawable.profile_placeholder));
            } else {
                Picasso.with(this).load(Receiver_pic)
                        .resize(100,100)
                        .placeholder(R.drawable.loading_placeholder)
                        .error(R.drawable.profile_placeholder)
                        .into(profileimage);
            }

            reference.child("users").child(Receiverid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Log.d(TAG, "onDataChange:token "+dataSnapshot);
                    if(dataSnapshot.exists())
                        token=dataSnapshot.child("token").getValue().toString();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });


        }

        final LinearLayoutManager layout = new LinearLayoutManager(this);
        layout.setStackFromEnd(false);
        chatrecyclerview.setLayoutManager(layout);
        chatrecyclerview.setHasFixedSize(false);
        OverScrollDecoratorHelper.setUpOverScroll(chatrecyclerview, OverScrollDecoratorHelper.ORIENTATION_VERTICAL);
        mAdapter = new ChatListAdapter(this,mChats);
        chatrecyclerview.setAdapter(mAdapter);
        chatrecyclerview.addOnScrollListener(new RecyclerView.OnScrollListener() {
            boolean userScrolled;
            int scrollOutitems;
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                    userScrolled = true;
                }
            }
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                scrollOutitems = layout.findFirstCompletelyVisibleItemPosition();

                if (userScrolled && (scrollOutitems == 0 && mChats.size()>9)) {
                    userScrolled = false;
                    loadingView.show();
                    reference.child("chat").child(senderid + "-" + Receiverid).orderByChild("chat_id")
                            .endAt(mChats.get(0).getChat_id()).limitToLast(20)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    ArrayList<ChatModels> arrayList=new ArrayList<>();
                                    for(DataSnapshot snapshot: dataSnapshot.getChildren()){
                                        ChatModels item=snapshot.getValue(ChatModels.class);
                                        arrayList.add(item);
                                    }
                                    for (int i=arrayList.size()-2; i>=0; i-- ){
                                        mChats.add(0,arrayList.get(i));
                                    }

                                    mAdapter.notifyDataSetChanged();
                                    loadingView.cancel();

                                    if(arrayList.size()>8){
                                        chatrecyclerview.scrollToPosition(arrayList.size());
                                    }

                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                }
            }
        });

        sendbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!TextUtils.isEmpty(message.getText().toString())){
                    SendMessage(message.getText().toString());
                    message.setText(null);
                }
            }
        });

        findViewById(R.id.Goback).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.hideSoftKeyboard(ChatViewActivity.this);
                onBackPressed();
            }
        });

        message.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus){
                    SendTypingIndicator(false);
                }
            }
        });


        message.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(count==0){
                    SendTypingIndicator(false);
                }
                else {
                    SendTypingIndicator(true);
                }


            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        final long[] touchtime = {System.currentTimeMillis()};
        ReceivetypeIndication();

        getChat();
    }

    private void init(){
        direct = new File(Environment.getExternalStorageDirectory() +"/BaseKencan/");

        reference = FirebaseDatabase.getInstance().getReference();
        adduserToInbox =FirebaseDatabase.getInstance().getReference();

        prefManager = new PrefManager(this);
        userName = findViewById(R.id.fullname);
        profileimage = findViewById(R.id.profileimage);
        block = findViewById(R.id.block);
        progressBar = findViewById(R.id.progress_bar);
        chatrecyclerview = findViewById(R.id.chatlist);

        sendbtn = findViewById(R.id.sendbtn);
        message =  findViewById(R.id.msgedittext);
        chat_layout = findViewById(R.id.chat_layout);

        loadingView = new ProgressDialog(this);
        loadingView.setMessage("Please wait...");
    }

    public void getChat() {
        mChats.clear();
        mchatRefReteriving = FirebaseDatabase.getInstance().getReference();
        queryGetchat = mchatRefReteriving.child("chat").child(senderid + "-" + Receiverid);

        myBlockStatusQuery = mchatRefReteriving.child("Inbox")
                .child(String.valueOf(prefManager.getUniqueId()))
                .child(Receiverid);

        otherBlockStatusQuery = mchatRefReteriving.child("Inbox")
                .child(Receiverid)
                .child(String.valueOf(prefManager.getUniqueId()));

        eventListener=new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                try {
                    ChatModels model = dataSnapshot.getValue(ChatModels.class);
                    mChats.add(model);
                    mAdapter.notifyDataSetChanged();
                    chatrecyclerview.scrollToPosition(mChats.size() - 1);
                }
                catch (Exception ex) {
                    Log.e("onChildAdded", ex.getMessage());
                }
                ChangeStatus();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {


                if (dataSnapshot != null && dataSnapshot.getValue() != null) {

                    try {
                        ChatModels model = dataSnapshot.getValue(ChatModels.class);

                        for (int i=mChats.size()-1;i>=0;i--){
                            if(mChats.get(i).getTimestamp().equals(dataSnapshot.child("timestamp").getValue())){
                                mChats.remove(i);
                                mChats.add(i,model);
                                break;
                            }
                        }
                        mAdapter.notifyDataSetChanged();
                    }
                    catch (Exception ex) {
                        Log.e("", ex.getMessage());
                    }
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("", databaseError.getMessage());
            }
        };

        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                progressBar.setVisibility(View.GONE);
                queryGetchat.removeEventListener(valueEventListener);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                progressBar.setVisibility(View.GONE);
            }
        };

        my_inbox_listener =new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && dataSnapshot.child("block").getValue()!=null){
                    String block=dataSnapshot.child("block").getValue().toString();
                    if(block.equals("1")){
                        findViewById(R.id.writechatlayout).setVisibility(View.INVISIBLE);
                    }else {
                        findViewById(R.id.writechatlayout).setVisibility(View.VISIBLE);
                    }
                }else {
                    findViewById(R.id.writechatlayout).setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        other_inbox_listener=new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && dataSnapshot.child("block").getValue()!=null){
                    String block=dataSnapshot.child("block").getValue().toString();
                    if(block.equals("1")){
                        isUserAlreadyBlock =true;
                    }else {
                        isUserAlreadyBlock =false;
                    }
                }else {
                    isUserAlreadyBlock =false;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };


        queryGetchat.limitToLast(20).addChildEventListener(eventListener);
        mchatRefReteriving.child("chat").addValueEventListener(valueEventListener);

        myBlockStatusQuery.addValueEventListener(my_inbox_listener);
        otherBlockStatusQuery.addValueEventListener(other_inbox_listener);
    }

    public void SendMessage(final String message) {
        Date c = Calendar.getInstance().getTime();
        final String formattedDate = gmtTime();

        final String current_user_ref = "chat" + "/" + senderid + "-" + Receiverid;
        final String chat_user_ref = "chat" + "/" + Receiverid + "-" + senderid;

        DatabaseReference reference = this.reference.child("chat").child(senderid + "-" + Receiverid).push();
        final String pushid = reference.getKey();
        final HashMap message_user_map = new HashMap<>();
        message_user_map.put("receiver_id", Receiverid);
        message_user_map.put("sender_id", senderid);
        message_user_map.put("chat_id",pushid);
        message_user_map.put("text", message);
        message_user_map.put("type","text");
        message_user_map.put("pic_url","");
        message_user_map.put("status", "0");
        message_user_map.put("time", "");
        message_user_map.put("sender_name", prefManager.getName());
        message_user_map.put("timestamp", formattedDate);

        final HashMap message_sender_map = new HashMap<>();
        message_sender_map.put("receiver_id", Receiverid);
        message_sender_map.put("sender_id", senderid);
        message_sender_map.put("chat_id",pushid);
        message_sender_map.put("text", message);
        message_sender_map.put("type","text");
        message_sender_map.put("pic_url","");
        message_sender_map.put("status", "1");
        message_sender_map.put("time", "");
        message_sender_map.put("sender_name", prefManager.getName());
        message_sender_map.put("timestamp", formattedDate);

        final HashMap user_map = new HashMap<>();
        user_map.put(current_user_ref + "/" + pushid, message_sender_map);
        user_map.put(chat_user_ref + "/" + pushid, message_user_map);

        this.reference.updateChildren(user_map, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                if (databaseError != null)
                    Log.d(TAG, "onComplete:databaseError "+databaseError.getMessage());


                String inbox_sender_ref = "Inbox" + "/" + senderid + "/" + Receiverid;
                String inbox_receiver_ref = "Inbox" + "/" + Receiverid + "/" + senderid;

                HashMap sendermap=new HashMap<>();
                sendermap.put("rid",senderid);
                sendermap.put("name", prefManager.getName());
                sendermap.put("pic", prefManager.getImage());
                sendermap.put("msg",message);
                sendermap.put("status","0");
                sendermap.put("timestamp", -1*gmtTimeInMili());
                sendermap.put("date",formattedDate);

                HashMap receivermap=new HashMap<>();
                receivermap.put("rid",Receiverid);
                receivermap.put("name",Receiver_name);
                receivermap.put("pic",Receiver_pic);
                receivermap.put("msg",message);
                receivermap.put("status","1");
                receivermap.put("timestamp", -1*gmtTimeInMili());
                receivermap.put("date",formattedDate);

                HashMap both_user_map = new HashMap<>();
                both_user_map.put(inbox_sender_ref , receivermap);
                both_user_map.put(inbox_receiver_ref , sendermap);

                adduserToInbox.updateChildren(both_user_map).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Log.d(TAG, "onComplete:adduserToInbox "+task.isSuccessful());
                        ChatViewActivity.SendPushNotification(prefManager.getName(),message);
                    }
                });
            }
        });
    }

    public String gmtTime(){
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ssZZ");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(Calendar.getInstance().getTime());
    }

    public long gmtTimeInMili(){
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ssZZ");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        String formatted = sdf.format(Calendar.getInstance().getTime());
        try {
            return sdf.parse(formatted).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public void UploadImage(ByteArrayOutputStream byteArrayOutputStream){
        byte[] data = byteArrayOutputStream.toByteArray();
        Date c = Calendar.getInstance().getTime();
        final String formattedDate = Constants.df.format(c);

        StorageReference reference= FirebaseStorage.getInstance().getReference();
        DatabaseReference dref= this.reference.child("chat").child(senderid+"-"+Receiverid).push();
        final String key=dref.getKey();
        uploadingImageId =key;
        final String current_user_ref = "chat" + "/" + senderid + "-" + Receiverid;
        final String chat_user_ref = "chat" + "/" + Receiverid + "-" + senderid;

        HashMap my_dummi_pic_map = new HashMap<>();
        my_dummi_pic_map.put("receiver_id", Receiverid);
        my_dummi_pic_map.put("sender_id", senderid);
        my_dummi_pic_map.put("chat_id",key);
        my_dummi_pic_map.put("text", "");
        my_dummi_pic_map.put("type","image");
        my_dummi_pic_map.put("pic_url","none");
        my_dummi_pic_map.put("status", "0");
        my_dummi_pic_map.put("time", "");
        my_dummi_pic_map.put("sender_name", prefManager.getName());
        my_dummi_pic_map.put("timestamp", formattedDate);

        HashMap dummy_push = new HashMap<>();
        dummy_push.put(current_user_ref + "/" + key, my_dummi_pic_map);
        this.reference.updateChildren(dummy_push);

        StorageReference fileReference = reference.child("images").child(key+".jpg");
        fileReference.putBytes(data).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                fileReference.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        uploadingImageId ="none";
                        HashMap message_user_map = new HashMap<>();
                        message_user_map.put("receiver_id", Receiverid);
                        message_user_map.put("sender_id", senderid);
                        message_user_map.put("chat_id",key);
                        message_user_map.put("text", "");
                        message_user_map.put("type","image");
                        message_user_map.put("pic_url",task.getResult().toString());
                        message_user_map.put("status", "0");
                        message_user_map.put("time", "");
                        message_user_map.put("sender_name", prefManager.getName());
                        message_user_map.put("timestamp", formattedDate);

                        HashMap user_map = new HashMap<>();

                        user_map.put(current_user_ref + "/" + key, message_user_map);
                        user_map.put(chat_user_ref + "/" + key, message_user_map);

                        ChatViewActivity.this.reference.updateChildren(user_map, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                String inbox_sender_ref = "Inbox" + "/" + senderid + "/" + Receiverid;
                                String inbox_receiver_ref = "Inbox" + "/" + Receiverid + "/" + senderid;

                                HashMap sendermap=new HashMap<>();
                                sendermap.put("rid",senderid);
                                sendermap.put("name", prefManager.getName());
                                sendermap.put("pic", prefManager.getImage());
                                sendermap.put("msg","Send an image...");
                                sendermap.put("status","0");
                                sendermap.put("timestamp", -1*System.currentTimeMillis());
                                sendermap.put("date",formattedDate);

                                HashMap receivermap=new HashMap<>();
                                receivermap.put("rid",Receiverid);
                                receivermap.put("name",Receiver_name);
                                receivermap.put("pic",Receiver_pic);
                                receivermap.put("msg","Send an image...");
                                receivermap.put("status","1");
                                receivermap.put("timestamp", -1*System.currentTimeMillis());
                                receivermap.put("date",formattedDate);

                                HashMap both_user_map = new HashMap<>();
                                both_user_map.put(inbox_sender_ref , receivermap);
                                both_user_map.put(inbox_receiver_ref , sendermap);

                                adduserToInbox.updateChildren(both_user_map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        ChatViewActivity.SendPushNotification(prefManager.getName(),"Sent an Image....");
                                    }
                                });
                            }
                        });
                    }
                });
            }
        });
    }

    public void ChangeStatus(){
        final Date c = Calendar.getInstance().getTime();
        final SimpleDateFormat sdf = new SimpleDateFormat("hh:mm aa");
        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        final Query query1 = reference.child("chat").child(Receiverid+"-"+senderid).orderByChild("status").equalTo("0");
        final Query query2 = reference.child("chat").child(senderid+"-"+Receiverid).orderByChild("status").equalTo("0");
        final Query query3 = reference.child("chat").child(Receiverid+"-"+senderid).orderByChild("time").equalTo("");

        final DatabaseReference inbox_change_status_1=reference.child("Inbox").child(senderid+"/"+Receiverid);
        final DatabaseReference inbox_change_status_2=reference.child("Inbox").child(Receiverid+"/"+senderid);

        query1.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot nodeDataSnapshot : dataSnapshot.getChildren()) {
                    if(!nodeDataSnapshot.child("sender_id").getValue().equals(senderid)){
                        String key = nodeDataSnapshot.getKey();
                        String path = "chat" + "/" + dataSnapshot.getKey() + "/" + key;
                        HashMap<String, Object> result = new HashMap<>();
                        result.put("status", "1");
                        result.put("time",sdf.format(c));
                        reference.child(path).updateChildren(result);
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        query2.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot nodeDataSnapshot : dataSnapshot.getChildren()) {
                    if(!nodeDataSnapshot.child("sender_id").getValue().equals(senderid)){
                        String key = nodeDataSnapshot.getKey();
                        String path = "chat" + "/" + dataSnapshot.getKey() + "/" + key;
                        HashMap<String, Object> result = new HashMap<>();
                        result.put("status", "1");
                        result.put("time",sdf.format(c));
                        reference.child(path).updateChildren(result);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        query3.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot nodeDataSnapshot : dataSnapshot.getChildren()) {
                    if(!nodeDataSnapshot.child("sender_id").getValue().equals(senderid)){
                        String key = nodeDataSnapshot.getKey();
                        String path = "chat" + "/" + dataSnapshot.getKey() + "/" + key;
                        HashMap<String, Object> result = new HashMap<>();
                        result.put("status", "1");
                        result.put("time",sdf.format(c));
                        reference.child(path).updateChildren(result);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        inbox_change_status_1.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    if(dataSnapshot.child("rid").getValue().equals(Receiverid)){
                        HashMap<String, Object> result = new HashMap<>();
                        result.put("status", "1");
                        inbox_change_status_1.updateChildren(result);

                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        inbox_change_status_2.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    if(dataSnapshot.child("rid").getValue().equals(senderid)){
                        HashMap<String, Object> result = new HashMap<>();
                        result.put("status", "1");
                        inbox_change_status_2.updateChildren(result);

                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public void update_message(ChatModels item){
        final String current_user_ref = "chat" + "/" + senderid + "-" + Receiverid;
        final String chat_user_ref = "chat" + "/" + Receiverid + "-" + senderid;


        final HashMap message_user_map = new HashMap<>();
        message_user_map.put("receiver_id", item.getReceiver_id());
        message_user_map.put("sender_id", item.getSender_id());
        message_user_map.put("chat_id",item.getChat_id());
        message_user_map.put("text", "Delete this message");
        message_user_map.put("type","delete");
        message_user_map.put("pic_url","");
        message_user_map.put("status", "0");
        message_user_map.put("time", "");
        message_user_map.put("sender_name", prefManager.getName());
        message_user_map.put("timestamp", item.getTimestamp());

        final HashMap user_map = new HashMap<>();
        user_map.put(current_user_ref + "/" + item.getChat_id(), message_user_map);
        user_map.put(chat_user_ref + "/" + item.getChat_id(), message_user_map);

        reference.updateChildren(user_map, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

            }
        });

    }

    public void Block_user(){
        reference.child("Inbox")
                .child(Receiverid)
                .child(String.valueOf(prefManager.getUniqueId())).child("block").setValue("1");
        Toast.makeText(ChatViewActivity.this, "User Blocked", Toast.LENGTH_SHORT).show();

    }

    public void UnBlock_user(){
        reference.child("Inbox")
                .child(Receiverid)
                .child(String.valueOf(prefManager.getUniqueId())).child("block").setValue("0");
        Toast.makeText(ChatViewActivity.this, "User UnBlocked", Toast.LENGTH_SHORT).show();

    }

    public boolean istodaymessage(String date) {
        Calendar cal = Calendar.getInstance();
        int today_day = cal.get(Calendar.DAY_OF_MONTH);
        long currenttime = System.currentTimeMillis();

        SimpleDateFormat f = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");
        long databasedate = 0;
        Date d = null;
        try {
            d = f.parse(date);
            databasedate = d.getTime();

        } catch (ParseException e) {
            e.printStackTrace();
        }
        long difference = currenttime - databasedate;
        if (difference < 86400000) {
            int chatday = Integer.parseInt(date.substring(0, 2));
            if (today_day == chatday)
                return true;
            else
                return false;
        }

        return false;
    }


    public void SendTypingIndicator(boolean indicate){
        if(indicate){
            final HashMap message_user_map = new HashMap<>();
            message_user_map.put("receiver_id", Receiverid);
            message_user_map.put("sender_id", senderid);

            sendTypingIndication =FirebaseDatabase.getInstance().getReference().child("typing_indicator");
            sendTypingIndication.child(senderid+"-"+Receiverid).setValue(message_user_map).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    sendTypingIndication.child(Receiverid+"-"+senderid).setValue(message_user_map).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                        }
                    });
                }
            });
        }

        else {
            sendTypingIndication =FirebaseDatabase.getInstance().getReference().child("typing_indicator");

            sendTypingIndication.child(senderid+"-"+Receiverid).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {

                    sendTypingIndication.child(Receiverid+"-"+senderid).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                        }
                    });

                }
            });

        }

    }

    public void ReceivetypeIndication(){
        typingIndicator = findViewById(R.id.typeindicator);

        receiveTypingIndication =FirebaseDatabase.getInstance().getReference().child("typing_indicator");
        receiveTypingIndication.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.child(Receiverid+"-"+senderid).exists()){
                    String receiver= String.valueOf(dataSnapshot.child(Receiverid+"-"+senderid).child("sender_id").getValue());
                    if(receiver.equals(Receiverid)){
                        typingIndicator.setVisibility(View.VISIBLE);
                    }
                }
                else {
                    typingIndicator.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        uploadingImageId ="none";
        SendTypingIndicator(false);
        queryGetchat.removeEventListener(eventListener);
        myBlockStatusQuery.removeEventListener(my_inbox_listener);
        otherBlockStatusQuery.removeEventListener(other_inbox_listener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        queryGetchat.removeEventListener(eventListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        queryGetchat.removeEventListener(eventListener);
        queryGetchat.limitToLast(20).addChildEventListener(eventListener);
    }

    public static void SendPushNotification(String title, String body){
        String url = "https://fcm.googleapis.com/fcm/send";
        String key = "key=AAAAdB4vxvo:APA91bHMOvvKLtRSF7KhRvKS68fDgEhQcSlTmWAkh1v6JHsrr18y7bxO_nktlwz4ZDZ1v2qcBfk7lgCk4y5kZxMuKZRIiNeoiuk--b1WpoEd5oLsdJ1vknvM1wZXRUDJgfAwGpfnOMTc";
        NotificationModel model = new NotificationModel("high",token,title,body);
        Log.d(TAG, "SendPushNotification:model "+new Gson().toJson(model));
        Call<UserResponse> call = RetrofitClint.getInstance().getApi().setNotification(key,url,model);
        call.enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                Log.d(TAG, "onResponse: "+response);
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                Log.d(TAG, "onFailure: "+t.getMessage());
            }
        });
    }

    public void block() {
        new AlertDialog.Builder(ChatViewActivity.this)
                .setIcon(R.mipmap.ic_launcher)
                .setTitle(getString(R.string.app_name))
                .setMessage("Are you sure you want to block this user?")
                .setPositiveButton("YES!", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        Block_user();
                    }
                })
                .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    public void unblock() {
        new AlertDialog.Builder(ChatViewActivity.this)
                .setIcon(R.mipmap.ic_launcher)
                .setTitle(getString(R.string.app_name))
                .setMessage("Are you sure you want to unblock this user?")
                .setPositiveButton("YES!", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        UnBlock_user();
                    }
                })
                .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

}