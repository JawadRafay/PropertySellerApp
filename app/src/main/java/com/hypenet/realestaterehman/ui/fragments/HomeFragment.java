package com.hypenet.realestaterehman.ui.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.hypenet.realestaterehman.R;
import com.hypenet.realestaterehman.adapters.HouseAdapter;
import com.hypenet.realestaterehman.database.RoomDatabase;
import com.hypenet.realestaterehman.databinding.FragmentHomeBinding;
import com.hypenet.realestaterehman.model.ApiResponse;
import com.hypenet.realestaterehman.model.House;
import com.hypenet.realestaterehman.ui.activities.HouseDetailsActivity;
import com.hypenet.realestaterehman.ui.activities.MainActivity;
import com.hypenet.realestaterehman.ui.activities.RegisterActivity;
import com.hypenet.realestaterehman.utils.PrefManager;
import com.hypenet.realestaterehman.utils.Utils;
import com.hypenet.realestaterehman.utils.retrofit.RetrofitClint;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    HouseAdapter adapter;
    List<House> data;
    ProgressDialog progressDialog;
    Activity activity;
    FragmentHomeBinding binding;
    RoomDatabase roomDatabase;
    PrefManager prefManager;
    private static final String TAG = "HomeFragment";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home,container,false);
        init();
        setListeners();
        progressDialog.show();
        getProperties();
        return binding.getRoot();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (Activity) context;
    }

    public void init(){
        prefManager = new PrefManager(activity);
        roomDatabase = RoomDatabase.getInstance(activity);
        progressDialog = new ProgressDialog(activity);
        progressDialog.setMessage("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);

        data = new ArrayList<>();
        adapter = new HouseAdapter(activity,data);
        adapter.setOnItemClickListener(new HouseAdapter.OnItemClickListener() {
            @Override
            public void onClick(House house) {
                startActivity(new Intent(activity, HouseDetailsActivity.class).putExtra("data",house));
            }

            @Override
            public void onDelete(House house, int position) {
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setMessage("Are you sure you want to delete this property?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        deleteProperty(position);
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.create().show();
            }
        });
        binding.recycler.setAdapter(adapter);
    }

    public void setListeners(){
        binding.swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getProperties();
            }
        });
    }

    public void getProperties(){
        Call<ApiResponse<List<House>>> call = RetrofitClint.getInstance().getApi().get_properties(prefManager.getId());
        call.enqueue(new Callback<ApiResponse<List<House>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<House>>> call, Response<ApiResponse<List<House>>> response) {
                if (progressDialog.isShowing())
                    progressDialog.dismiss();
                binding.swipeRefresh.setRefreshing(false);
                if (response.body() != null) {
                    Log.d(TAG, "onResponse:home " + new Gson().toJson(response.body()));
                    if (response.body().getMessage().equals("success")) {
                        data.clear();
                        data.addAll(response.body().getData());
                        adapter.notifyDataSetChanged();
                        if (data.size() == 0) {
                            binding.noData.setVisibility(View.VISIBLE);
                        } else {
                            binding.noData.setVisibility(View.GONE);
                        }
                    } else {
                        Toast.makeText(activity, "Something went wrong. Try again", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(activity, "Server Error! please try again later", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<House>>> call, Throwable t) {
                if (progressDialog.isShowing())
                    progressDialog.dismiss();
                binding.swipeRefresh.setRefreshing(false);
                Log.d(TAG, "onFailure: "+t.getMessage());
                Toast.makeText(activity, "Check you internet connection and try again", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void deleteProperty(int position){
        progressDialog.show();
        Call<ApiResponse<House>> call = RetrofitClint.getInstance().getApi().delete_property(data.get(position).getId());
        call.enqueue(new Callback<ApiResponse<House>>() {
            @Override
            public void onResponse(Call<ApiResponse<House>> call, Response<ApiResponse<House>> response) {
                if (progressDialog.isShowing())
                    progressDialog.dismiss();
                binding.swipeRefresh.setRefreshing(false);
                if (response.body() !=  null){
                    Log.d(TAG, "onResponse:deleteProperty "+new Gson().toJson(response.body()));
                    if (response.body().getMessage().equals("success")){
                        data.remove(position);
                        adapter.notifyItemRemoved(position);
                        if (data.size() == 0){
                            binding.noData.setVisibility(View.VISIBLE);
                        }else {
                            binding.noData.setVisibility(View.GONE);
                        }
                        Toast.makeText(activity, "Deleted Successfully", Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(activity, "Something went wrong. Try again", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(activity, "Server Error! please try again later", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<House>> call, Throwable t) {
                if (progressDialog.isShowing())
                    progressDialog.dismiss();
                binding.swipeRefresh.setRefreshing(false);
                Log.d(TAG, "onFailure: "+t.getMessage());
                Toast.makeText(activity, "Check you internet connection and try again", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
