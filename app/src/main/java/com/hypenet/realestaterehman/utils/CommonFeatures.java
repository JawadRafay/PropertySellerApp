package com.hypenet.realestaterehman.utils;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;

import com.hypenet.realestaterehman.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommonFeatures {

    public static AlertDialog dialog;
    public static Location location;
    private static final String TAG = "CommonFeatures";

    public static void showLoadingDialogue(Context context){
        if (dialog != null && dialog.isShowing())
            return;
        View view = LayoutInflater.from(context).inflate(R.layout.custom_loading,null);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(view);
        builder.setCancelable(false);
        dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.show();
    }

    public static void hideDialogue(){
        if (dialog != null && dialog.isShowing())
            dialog.dismiss();
    }

    public static boolean isEmailValid(String email) {
        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    public static String getText(EditText editText){
        return editText.getText().toString().trim();
    }

    public static boolean isEmpty(EditText editText){
        return editText.getText().toString().isEmpty();
    }

    public static String getDate(){
        Date currentTime = Calendar.getInstance().getTime();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return simpleDateFormat.format(currentTime);
    }


    public static double distance(double latitude, double longitude) {
        Location shelter = new Location("Shelter");
        shelter.setLatitude(latitude);
        shelter.setLongitude(longitude);
        double distance = location.distanceTo(shelter);
        return distance * 0.000621371192f;
    }
}
