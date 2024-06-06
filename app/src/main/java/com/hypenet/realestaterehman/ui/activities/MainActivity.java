package com.hypenet.realestaterehman.ui.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.database.FirebaseDatabase;
import com.hypenet.realestaterehman.R;
import com.hypenet.realestaterehman.databinding.ActivityMainBinding;
import com.hypenet.realestaterehman.ui.fragments.ChatFragment;
import com.hypenet.realestaterehman.ui.fragments.HomeFragment;
import com.hypenet.realestaterehman.ui.fragments.ProfileFragment;
import com.hypenet.realestaterehman.utils.CommonFeatures;
import com.hypenet.realestaterehman.utils.PrefManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    boolean gps_enabled = false;
    boolean isPermissionRequested = false;
    private boolean isDialogueShowing = false;
    private static final int PERMISSION_CODE_LOCATION = 1022;
    PrefManager prefManager;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationManager locationManager;
    FragmentManager fragmentManager;
    FragmentTransaction transaction;
    String[] fragmentTags = {"homeFragment", "chatFragment", "profileFragment"};
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this,R.layout.activity_main);
        init();
        setListeners();
        checkAndRequestLocationPermissions();
        loadFragments();
    }

    private void init(){
        locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        prefManager = new PrefManager(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        binding.verified.setText(prefManager.getVerifiedStatus());
        if (prefManager.getVerifiedStatus().equalsIgnoreCase("Unverified")){
            binding.verified.setBackground(ResourcesCompat.getDrawable(getResources(),R.drawable.circle_red,null));
        }else if (prefManager.getVerifiedStatus().equalsIgnoreCase("Verified")){
            binding.verified.setBackground(ResourcesCompat.getDrawable(getResources(),R.drawable.circle_green,null));
        }else{
            binding.verified.setBackground(ResourcesCompat.getDrawable(getResources(),R.drawable.circle_orange,null));
        }
    }

    public void setListeners(){

        binding.bottomNavigation.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                icon(item.getItemId());
                if (item.getItemId()==R.id.homeFragment){
                    changeFragment(fragmentTags[0]);
                    binding.addProperty.setVisibility(View.VISIBLE);
                    return true;
                }else if (item.getItemId()==R.id.chatFragment){
                    changeFragment(fragmentTags[1]);
                    binding.addProperty.setVisibility(View.GONE);
                    return true;
                }else if (item.getItemId()==R.id.profileFragment){
                    changeFragment(fragmentTags[2]);
                    binding.addProperty.setVisibility(View.GONE);
                    return true;
                }else {
                    return false;
                }
            }
        });

        binding.verified.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),KYCActivity.class));
                overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
            }
        });

        binding.addProperty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,AddPropertyActivity.class));
            }
        });

        binding.more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popup = new PopupMenu(MainActivity.this, view);
                popup.getMenuInflater().inflate(R.menu.popup_menu, popup.getMenu());
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        switch (menuItem.getItemId()){
                            case R.id.logout:
                                FirebaseDatabase.getInstance().getReference().child("users").child(String.valueOf(prefManager.getId())).child("token").setValue("");
                                prefManager.setId(0);
                                startActivity(new Intent(getApplicationContext(),LoginActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK));
                                overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
                                finish();
                                break;
                            case R.id.term:
                                openLink("https://www.google.com/");
                                break;
                            case R.id.privacy:
                                openLink("https://www.google.com/");
                                break;
                        }
                        return true;
                    }
                });

                popup.setOnDismissListener(new PopupMenu.OnDismissListener() {
                    @Override
                    public void onDismiss(PopupMenu menu) {
                        // Respond to popup being dismissed.
                    }
                });

                // Show the popup menu.
                popup.show();
            }
        });

    }

    public void icon(int id){
        Menu menu = binding.bottomNavigation.getMenu();
        menu.findItem(R.id.homeFragment).setIcon(id==R.id.homeFragment ? R.drawable.home_active : R.drawable.home);
        menu.findItem(R.id.chatFragment).setIcon(id==R.id.chatFragment ? R.drawable.chat_active : R.drawable.chat);
        menu.findItem(R.id.profileFragment).setIcon(id==R.id.profileFragment ? R.drawable.user_active : R.drawable.user);
    }

    public void loadFragments(){
        HomeFragment homeFragment = new HomeFragment();
        ChatFragment chatFragment = new ChatFragment();
        ProfileFragment profileFragment = new ProfileFragment();
        fragmentManager = getSupportFragmentManager();
        FragmentManager fragmentManager = getSupportFragmentManager();
        transaction = fragmentManager.beginTransaction();
        transaction.add(R.id.fragment, homeFragment, "homeFragment");
        transaction.add(R.id.fragment, chatFragment, "chatFragment");
        transaction.add(R.id.fragment, profileFragment, "profileFragment");
        transaction.hide(chatFragment);
        transaction.hide(profileFragment);
        transaction.commit();
    }

    public void changeFragment(String id){
        if (id.equals(fragmentTags[0]))
            binding.title.setText("Home");
        else if (id.equals(fragmentTags[1]))
            binding.title.setText("Chat");
        else
            binding.title.setText("Profile");

        transaction = getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out);
        for (String name : fragmentTags){
            if (name.equals(id))
                transaction.show(fragmentManager.findFragmentByTag(name));
            else
                transaction.hide(fragmentManager.findFragmentByTag(name));
        }
        transaction.commit();
    }


    private void checkAndRequestLocationPermissions() {
        int fine = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        int coarse = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
        List<String> listPermissionsNeeded = new ArrayList<>();

        if (fine != PackageManager.PERMISSION_GRANTED && coarse != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION);
            listPermissionsNeeded.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }

        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), PERMISSION_CODE_LOCATION);
        } else {
            checkGPS();
        }
    }

    private void checkGPS() {
        try {
            gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ignored) {
        }

        Log.d(TAG, "checkGPS: " + gps_enabled);
        if (!gps_enabled) {
            if (!isDialogueShowing) {
                isDialogueShowing = true;
                new AlertDialog.Builder(MainActivity.this)
                        .setMessage("Please turn on your location")
                        .setCancelable(false)
                        .setPositiveButton("Goto Settings", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                                paramDialogInterface.cancel();
                                isDialogueShowing = false;
                                isPermissionRequested = false;
                                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                            }
                        })
                        .show();
            }
        } else {
            currentLocation();
        }
    }

    private void currentLocation() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        mFusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                new CancellationTokenSource().getToken()
        ).addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                Location location = task.getResult();
                if (location != null) {
                    CommonFeatures.location = location;
                }else{
                    getLastLocation();
                }
            }
        });
    }

    public void getLastLocation(){

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        mFusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                Location location = task.getResult();
                if (location != null) {
                    CommonFeatures.location = location;
                }
            }
        });
    }

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {

        switch (requestCode) {
            case PERMISSION_CODE_LOCATION: {

                Map<String, Integer> perms = new HashMap<>();
                perms.put(Manifest.permission.ACCESS_FINE_LOCATION, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.ACCESS_COARSE_LOCATION, PackageManager.PERMISSION_GRANTED);
                if (grantResults.length > 0) {
                    for (int i = 0; i < permissions.length; i++)
                        perms.put(permissions[i], grantResults[i]);

                    if (perms.get(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                            || perms.get(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    ) {
                        checkGPS();
                    } else {
                        explain("You need to give Location permission to get your location");
                    }
                }
            }
            break;
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void explain(String msg) {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
        dialog.setMessage(msg)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        checkAndRequestLocationPermissions();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    }
                });
        dialog.show();
    }

    public void openLink(String url){
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }

}