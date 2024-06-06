package com.hypenet.realestaterehman.utils;

import com.google.android.gms.maps.model.LatLng;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class Constants {

    public static LatLng myLocation;
    public static String pref_name = "pref_name";
    public static String f_name="f_name";
    public static String l_name="l_name";
    public static String uid="uid";
    public static String u_pic="u_pic";
    public static String Lat="Lat";
    public static String Lon="Lon";
    public static String device_token="device_token";

    public static String versionname="1.0";

    public static int permission_camera_code=786;
    public static int permission_write_data=788;
    public static int permission_Read_data=789;
    public static int permission_Recording_audio=790;
    public static int Select_image_from_gallry_code=3;
    public static int successmsg;

    public static String gif_firstpart="https://media.giphy.com/media/";
    public static String gif_secondpart="/100w.gif";

    public static String gif_firstpart_chat="https://media.giphy.com/media/";
    public static String gif_secondpart_chat="/200w.gif";

    public static SimpleDateFormat df =
            new SimpleDateFormat("dd-MM-yyyy HH:mm:ssZZ", Locale.ENGLISH);

    public static final String FCM_KEY = "AIzaSyCsCK6qCcR11IEt1hDjKnHSEs80ikk9PQg";

}
