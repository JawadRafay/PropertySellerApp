package com.hypenet.realestaterehman.adapters;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;


import com.hypenet.realestaterehman.R;
import com.hypenet.realestaterehman.model.Chat;
import com.hypenet.realestaterehman.model.ChatModels;
import com.hypenet.realestaterehman.utils.PrefManager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ViewHolder> {

    private static final String TAG = "ChatListAdapter";
    private List<ChatModels> mData;
    private LayoutInflater mInflater;
    private ClipboardManager clipboard;
    private Context context;
    SimpleDateFormat simpleDateFormat;
    PrefManager prefManager;
    Integer today_day = 0;

    public ChatListAdapter(Context context, List<ChatModels> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
        clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        this.context = context;
        simpleDateFormat = new SimpleDateFormat("hh:mm a");
        prefManager = new PrefManager(context);
        Calendar cal = Calendar.getInstance();
        cal.setTimeZone(TimeZone.getDefault());
        today_day = cal.get(Calendar.DAY_OF_MONTH);
        Log.d(TAG, "ChatListAdapter:uniqueId "+prefManager.getUniqueId());
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.chat_list_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        ChatModels chat = mData.get(position);
        Log.d(TAG, "onBindViewHolder: "+chat.getSender_id());
        if(chat.getSender_id().equals(String.valueOf(prefManager.getUniqueId()))){
            holder.tvTime.setText("");
            if(!chat.getTime().isEmpty())
                holder.tvTimeMe.setText("Seen at "+chat.getTime());
            else
                holder.tvTimeMe.setText("Sent");

        }else {
            holder.tvTimeMe.setText("");
            holder.tvTime.setText(ChangeTime(chat.getTimestamp()));
        }

        if (position != 0) {
            ChatModels chat2 = mData.get(position - 1);
            if (chat2.getTimestamp().substring(0, 2).equals(chat.getTimestamp().substring(0, 2))) {
                holder.datetxt.setVisibility(View.GONE);
            } else {
                holder.datetxt.setVisibility(View.VISIBLE);
                holder.datetxt.setText(ChangeDate(chat.getTimestamp()));
            }
        }else {
            holder.datetxt.setVisibility(View.VISIBLE);
            holder.datetxt.setText(ChangeDate(chat.getTimestamp()));
        }

        holder.tvMsgMe.setText(chat.getText());
        holder.tvMsg.setText(chat.getText());

        if(!chat.getSender_id().equals(String.valueOf(prefManager.getUniqueId()))){
            holder.tvMsgMe.setVisibility(View.GONE);
            holder.tvTimeMe.setVisibility(View.GONE);
            holder.tvMsg.setVisibility(View.VISIBLE);
            holder.tvTime.setVisibility(View.VISIBLE);
        }else{
            holder.tvMsg.setVisibility(View.GONE);
            holder.tvTime.setVisibility(View.GONE);
            holder.tvMsgMe.setVisibility(View.VISIBLE);
            holder.tvTimeMe.setVisibility(View.VISIBLE);
        }

        holder.tvMsgMe.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                ClipData clip = ClipData.newPlainText("message", chat.getText());
                clipboard.setPrimaryClip(clip);
                Toast.makeText(context, "text copied to clipboard", Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        holder.tvMsg.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                ClipData clip = ClipData.newPlainText("message", chat.getText());
                clipboard.setPrimaryClip(clip);
                Toast.makeText(context, "text copied to clipboard", Toast.LENGTH_SHORT).show();
                return true;
            }
        });

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
        if(difference<86400000){
            int chatday= Integer.parseInt(date.substring(0,2));
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a");
            if(today_day == chatday)
                return "Today";
            else if((today_day-chatday)==1)
                return "Yesterday";
        }
        else if(difference<172800000){
            int chatday= Integer.parseInt(date.substring(0,2));
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a");
            if((today_day-chatday)==1)
                return "Yesterday";
        }

        SimpleDateFormat sdf = new SimpleDateFormat("MMM-dd-yyyy");

        if(d!=null)
            return sdf.format(d);
        else
            return "";
    }

    public String ChangeTime(String date){
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
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a");
        if(d!=null)
            return sdf.format(d);
        else
            return "";
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView tvMsg, tvTime,tvMsgMe, tvTimeMe, datetxt;

        ViewHolder(View itemView) {
            super(itemView);
            tvMsg = itemView.findViewById(R.id.tvMsg);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvMsgMe = itemView.findViewById(R.id.tvMsgMe);
            tvTimeMe = itemView.findViewById(R.id.tvTimeMe);
            datetxt = itemView.findViewById(R.id.datetxt);
        }
    }
}
