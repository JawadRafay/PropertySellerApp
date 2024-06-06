package com.hypenet.realestaterehman.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;


import com.bumptech.glide.Glide;
import com.hypenet.realestaterehman.R;
import com.hypenet.realestaterehman.databinding.ImageItemBinding;
import com.hypenet.realestaterehman.model.GalleryModel;

import java.util.List;

public class ImageDetailsAdapter extends RecyclerView.Adapter<ImageDetailsAdapter.MyViewHolder> {

    private Context context;
    private List<GalleryModel> data;

    public ImageDetailsAdapter(Context context, List<GalleryModel> data) {
        this.context = context;
        this.data = data;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ImageItemBinding binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.image_item,parent,false);
        return new MyViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        GalleryModel image = data.get(position);
        Glide.with(context).load(image.getImage()).into(holder.binding.image);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        ImageItemBinding binding;
        public MyViewHolder(@NonNull ImageItemBinding itemView) {
            super(itemView.getRoot());
            binding = itemView;
        }
    }

}
