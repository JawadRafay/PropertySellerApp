package com.hypenet.realestaterehman.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.hypenet.realestaterehman.R;
import com.hypenet.realestaterehman.database.RoomDatabase;
import com.hypenet.realestaterehman.databinding.HouseItemBinding;
import com.hypenet.realestaterehman.model.House;
import com.hypenet.realestaterehman.utils.Utils;

import java.util.List;

public class HouseAdapter extends RecyclerView.Adapter<HouseAdapter.MyViewHolder> {

    OnItemClickListener onItemClickListener;
    Context context;
    List<House> data;
    RoomDatabase roomDatabase;

    public HouseAdapter(Context context, List<House> data) {
        this.context = context;
        this.data = data;
        roomDatabase = RoomDatabase.getInstance(context);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.house_item,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
         holder.bind(position);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        HouseItemBinding binding;
        public MyViewHolder(@NonNull HouseItemBinding itemView) {
            super(itemView.getRoot());
            binding = itemView;
        }

        public void bind(int position){
            House house = data.get(position);
            binding.setHouse(house);

            binding.delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClickListener.onDelete(house, position);
                }
            });

            binding.getRoot().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClickListener.onClick(house);
                }
            });
        }
    }

    public interface OnItemClickListener{
        void onClick(House house);
        void onDelete(House house, int position);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }
}
