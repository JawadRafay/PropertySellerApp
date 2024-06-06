package com.hypenet.realestaterehman.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hypenet.realestaterehman.R;
import com.hypenet.realestaterehman.model.MessageModels;
import com.hypenet.realestaterehman.utils.PrefManager;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;


public class MessageItem extends RecyclerView.Adapter<MessageItem.CustomViewHolder> implements Filterable{
    public Context context;
    ArrayList<MessageModels> messageModels = new ArrayList<>();
    ArrayList<MessageModels> messageModelsFilter = new ArrayList<>();
    private OnItemClickListener listener;
    private OnLongItemClickListener longlistener;
    Integer today_day=0;
    PrefManager prefManager;
    private static final String TAG = "MessageItem";

    public interface OnItemClickListener {
        void onItemClick(MessageModels item);
    }
    public interface OnLongItemClickListener{
        void onLongItemClick(MessageModels item);
    }

    public MessageItem(Context context, ArrayList<MessageModels> user_dataList, OnItemClickListener listener, OnLongItemClickListener longlistener) {
        this.context = context;
        this.messageModels = user_dataList;
        this.messageModelsFilter = user_dataList;
        this.listener = listener;
        this.longlistener=longlistener;
        Calendar cal = Calendar.getInstance();
        today_day = cal.get(Calendar.DAY_OF_MONTH);
        prefManager = new PrefManager(context);
    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewtype) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_message,null);
        view.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));
        CustomViewHolder viewHolder = new CustomViewHolder(view);
        return viewHolder;
    }

    @Override
    public int getItemCount() {
        return messageModelsFilter.size();
    }

    class CustomViewHolder extends RecyclerView.ViewHolder {
        TextView username, lastMessage, dateCreated, counter;
        ImageView userImage;

        public CustomViewHolder(View view) {
            super(view);
            userImage = itemView.findViewById(R.id.userimages);
            username = itemView.findViewById(R.id.fullname);
            lastMessage = itemView.findViewById(R.id.message);
            dateCreated = itemView.findViewById(R.id.datetxt);
            counter = itemView.findViewById(R.id.counter);
        }

        public void bind(final MessageModels item, final OnItemClickListener listener, final  OnLongItemClickListener longItemClickListener) {

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    listener.onItemClick(item);
                }
            });


        }

    }


    @Override
    public void onBindViewHolder(final CustomViewHolder holder, final int i) {

        final MessageModels item = messageModelsFilter.get(i);
        holder.username.setText(item.getName());
        holder.lastMessage.setText(item.getMessage());
        holder.dateCreated.setText(ChangeDate(item.getTimestamp()));

        Log.d(TAG, "onBindViewHolder:message "+item.getPicture());
        if(!item.getPicture().equals("https://tagteamgx.com/public/images"))
            Picasso.with(context).
                    load(item.getPicture())
                    .resize(100,100)
                    .placeholder(R.drawable.image_placeholder_loading)
                    .error(R.drawable.profile_placeholder)
                    .into(holder.userImage);

        String status = "" + item.getStatus();

        FirebaseDatabase.getInstance().getReference("chat")
                .child(String.valueOf(prefManager.getUniqueId())+"-"+item.getId())
                .orderByChild("status")
                .equalTo("0")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Log.d(TAG, "onDataChange:count "+snapshot.getChildrenCount());
                        if (snapshot.getChildrenCount()>0){
                            holder.lastMessage.setTypeface(null, Typeface.BOLD);
                            holder.lastMessage.setTextColor(context.getResources().getColor(R.color.black));
                            holder.counter.setVisibility(View.VISIBLE);
                            holder.counter.setText(String.valueOf(snapshot.getChildrenCount()));
                        }else {
                            holder.counter.setVisibility(View.GONE);
                            holder.lastMessage.setTypeface(null, Typeface.NORMAL);
                            holder.lastMessage.setTextColor(context.getResources().getColor(R.color.gray_dark));
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.d(TAG, "onCancelled: "+error.getMessage());
                    }
                });

        if (status.equals("0")) {
            holder.lastMessage.setTypeface(null, Typeface.BOLD);
            holder.lastMessage.setTextColor(context.getResources().getColor(R.color.black));
        } else {
            holder.lastMessage.setTypeface(null, Typeface.NORMAL);
            holder.lastMessage.setTextColor(context.getResources().getColor(R.color.gray_dark));
        }

        holder.bind(item,listener,longlistener);
    }

    public String ChangeDate(String date){
        SimpleDateFormat gmtFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ssZZ");
        gmtFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        SimpleDateFormat localFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ssZZ");
        localFormat.setTimeZone(TimeZone.getDefault());

        long currenttime= Calendar.getInstance().getTime().getTime();
        long databasedate = 0;
        Date d = null;
        try {
            Date gmtDate = gmtFormat.parse(date);
            date = localFormat.format(gmtDate);
            d = localFormat.parse(date);
            databasedate = d.getTime();

        } catch (ParseException e) {
            e.printStackTrace();
        }
        long difference=currenttime-databasedate;
        Log.d(TAG, "ChangeDate:difference "+difference);
        if(difference<86400000){
            Log.d(TAG, "ChangeDate:date "+date);
            Log.d(TAG, "ChangeDate:today_day "+today_day);
            int chatday= Integer.parseInt(date.substring(0,2));
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a");
            if(today_day==chatday)
                return "Today "+sdf.format(d);
            else if((today_day-chatday)==1)
                return "Yesterday "+sdf.format(d);
        }
        else if(difference<172800000){
            int chatday= Integer.parseInt(date.substring(0,2));
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a");
            if((today_day-chatday)==1)
                return "Yesterday "+sdf.format(d);
        }

        SimpleDateFormat sdf = new SimpleDateFormat("MMM-dd-yyyy hh:mm a");

        if(d!=null)
            return sdf.format(d);
        else
            return "";
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                if (charString.isEmpty()) {
                    messageModelsFilter = messageModels;
                } else {
                    ArrayList<MessageModels> filteredList = new ArrayList<>();
                    for (MessageModels row : messageModels) {
                        if (row.getName().toLowerCase().contains(charString.toLowerCase())) {
                            filteredList.add(row);
                        }
                    }

                    messageModelsFilter = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = messageModelsFilter;
                return filterResults;

            }
            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                messageModelsFilter = (ArrayList<MessageModels>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

}