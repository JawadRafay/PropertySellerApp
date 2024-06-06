package com.hypenet.realestaterehman.ui.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.hypenet.realestaterehman.R;
import com.hypenet.realestaterehman.adapters.ImageDetailsAdapter;
import com.hypenet.realestaterehman.adapters.SliderAdapter;
import com.hypenet.realestaterehman.database.RoomDatabase;
import com.hypenet.realestaterehman.databinding.ActivityHouseDetailsBinding;
import com.hypenet.realestaterehman.model.GalleryModel;
import com.hypenet.realestaterehman.model.House;
import com.hypenet.realestaterehman.model.SliderData;
import com.smarteist.autoimageslider.SliderView;

import java.util.ArrayList;
import java.util.List;

public class HouseDetailsActivity extends AppCompatActivity implements OnMapReadyCallback {

    ActivityHouseDetailsBinding binding;
    ImageDetailsAdapter adapter;
    List<GalleryModel> galleryModels;
    GoogleMap mGoogleMap;
    House house;
    RoomDatabase roomDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this,R.layout.activity_house_details);
        house = getIntent().getParcelableExtra("data");
        init();
        setListeners();
        setData();
    }

    public void init(){
        roomDatabase = RoomDatabase.getInstance(HouseDetailsActivity.this);
        galleryModels = new ArrayList<>();
        galleryModels.addAll(house.getProperty_images());
        adapter = new ImageDetailsAdapter(HouseDetailsActivity.this,galleryModels);
        RecyclerView recyclerView = binding.recycler;
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL,false));
        recyclerView.setAdapter(adapter);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(HouseDetailsActivity.this);
    }

    public void setListeners(){
        binding.back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        binding.overly.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openMap();
            }
        });

        binding.bookNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + house.getPhone()));
                startActivity(intent);
            }
        });
    }

    public String formatToK(int number) {
        if (number < 1000) {
            return String.valueOf(number); // No conversion needed for numbers less than 1000
        }

        int thousands = number / 1000; // Get the whole thousands
        int remainder = number % 1000; // Remainder after dividing by 1000

        if (remainder == 0) {
            return thousands + "k"; // If no remainder, return whole thousands with "k"
        } else {
            return thousands + "." + (remainder / 100) + "k"; // Include one decimal place if there's a remainder
        }
    }

    public void setData(){
        //slider
        List<SliderData> sliderDataArrayList = new ArrayList<>();
        for (int i = 0; i < house.getProperty_images().size(); i++) {
            sliderDataArrayList.add(new SliderData(house.getProperty_images().get(i).getImage()));
        }

        SliderAdapter adapter = new SliderAdapter(this, sliderDataArrayList);
        binding.slider.setAutoCycleDirection(SliderView.LAYOUT_DIRECTION_LTR);
        binding.slider.setSliderAdapter(adapter);
        binding.slider.setScrollTimeInSec(3);
        binding.slider.setAutoCycle(true);
        binding.slider.startAutoCycle();

        binding.name2.setText(house.getTitle());
        binding.description.setText(house.getDescription());
        binding.price.setText(formatToK(Integer.parseInt(house.getPrice())));

        if (roomDatabase.houseDao().checkFavourite(house.getId()) == null)
            binding.bookMark.setImageDrawable(ResourcesCompat.getDrawable(HouseDetailsActivity.this.getResources(),R.drawable.bookmark,null));
        else
            binding.bookMark.setImageDrawable(ResourcesCompat.getDrawable(HouseDetailsActivity.this.getResources(),R.drawable.bookmark_selected,null));

        binding.bookMark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (roomDatabase.houseDao().checkFavourite(house.getId()) == null){
                    roomDatabase.houseDao().insert(house);
                    binding.bookMark.setImageDrawable(ResourcesCompat.getDrawable(HouseDetailsActivity.this.getResources(),R.drawable.bookmark_selected,null));
                } else {
                    roomDatabase.houseDao().delete(house);
                    binding.bookMark.setImageDrawable(ResourcesCompat.getDrawable(HouseDetailsActivity.this.getResources(),R.drawable.bookmark,null));
                }
            }
        });
    }

    public void openMap(){
        String strUri = "http://maps.google.com/maps?q=loc:"+house.getLatitude()+","+house.getLongitude();
        Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(strUri));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
        startActivity(intent);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        moveCamera();
    }

    private void moveCamera(){
        mGoogleMap.clear();
        LatLng latLng = new LatLng(Double.parseDouble(house.getLatitude()),Double.parseDouble(house.getLongitude()));
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        mGoogleMap.addMarker(markerOptions);
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,16f));
    }
}