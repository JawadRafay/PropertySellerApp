package com.hypenet.realestaterehman.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.databinding.DataBindingUtil;

import com.bumptech.glide.Glide;
import com.hypenet.realestaterehman.R;
import com.hypenet.realestaterehman.databinding.CustomSliderItemBinding;
import com.hypenet.realestaterehman.model.SliderData;
import com.smarteist.autoimageslider.SliderViewAdapter;

import java.util.List;

public class SliderAdapter extends SliderViewAdapter<SliderAdapter.SliderAdapterViewHolder> {

    private final List<SliderData> mSliderItems;
    private Context context;

    public SliderAdapter(Context context, List<SliderData> sliderDataArrayList) {
        this.mSliderItems = sliderDataArrayList;
        this.context = context;
    }

    @Override
    public SliderAdapterViewHolder onCreateViewHolder(ViewGroup parent) {
        CustomSliderItemBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.custom_slider_item,parent,false);
        return new SliderAdapterViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(SliderAdapterViewHolder viewHolder, final int position) {

        final SliderData sliderItem = mSliderItems.get(position);
//        Glide.with(context)
//                .load(sliderItem.getImage())
//                .into(viewHolder.binding.image);
    }

    @Override
    public int getCount() {
        return mSliderItems.size();
    }

    static class SliderAdapterViewHolder extends ViewHolder {
        // Adapter class for initializing
        // the views of our slider view.
        View itemView;
        CustomSliderItemBinding binding;

        public SliderAdapterViewHolder(CustomSliderItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
