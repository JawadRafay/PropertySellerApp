package com.hypenet.realestaterehman.utils;

import android.content.Context;
import android.content.SharedPreferences;


public class PrefManager {

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private static final String PREF_NAME = "GymBuddy";
    private static final String NAME = "name";
    private static final String SURNAME = "surname";
    private static final String ADDRESS = "address";
    private static final String PHONE = "phone";
    private static final String CITY = "city";
    private static final String FAVOURITE_EXERCISE = "favourite_exercise";
    private static final String EMAIL = "email";
    private static final String ID = "id";
    private static final String UNIQUE_ID = "unique_id";
    private static final String GENDER = "gender";
    private static final String IMAGE = "image";
    private static final String REMEMBER = "remember";
    private static final String TYPE = "type";
    private static final String EXPIRES = "expires";
    private static final String VERIFIED = "verified";
    private static final String CNIC = "cnic";
    private static final String FRONT = "front";
    private static final String BACK = "back";

    private Context context;

    public PrefManager(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences(PREF_NAME,context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public void setName(String value){
        editor.putString(NAME,value);
        editor.commit();
    }

    public String getName(){
        return sharedPreferences.getString(NAME, null);
    }


    public void setEmail(String value){
        editor.putString(EMAIL,value);
        editor.commit();
    }

    public String getEmail(){
        return sharedPreferences.getString(EMAIL, null);
    }

    public void setGender(String value){
        editor.putString(GENDER,value);
        editor.commit();
    }

    public String getGender(){
        return sharedPreferences.getString(GENDER, null);
    }

    public void setId(int value){
        editor.putInt(ID,value);
        editor.commit();
    }

    public int getId(){
        return sharedPreferences.getInt(ID, 0);
    }

    public void setImage(String value){
        editor.putString(IMAGE,value);
        editor.commit();
    }

    public String getImage(){
        return sharedPreferences.getString(IMAGE, null);
    }

    public void setRemember(boolean value){
        editor.putBoolean(REMEMBER,value);
        editor.commit();
    }

    public boolean getRemember(){
        return sharedPreferences.getBoolean(REMEMBER, false);
    }

    public void setSurname(String value){
        editor.putString(SURNAME,value);
        editor.commit();
    }

    public String getSurname(){
        return sharedPreferences.getString(SURNAME, null);
    }

    public void setAddress(String value){
        editor.putString(ADDRESS,value);
        editor.commit();
    }

    public String getAddress(){
        return sharedPreferences.getString(ADDRESS, "");
    }

    public void setCity(String value){
        editor.putString(CITY,value);
        editor.commit();
    }

    public String getCity(){
        return sharedPreferences.getString(CITY, null);
    }

    public void setPhone(String value){
        editor.putString(PHONE,value);
        editor.commit();
    }

    public String getPhone(){
        return sharedPreferences.getString(PHONE, "");
    }

    public void setFavouriteExercise(String value){
        editor.putString(FAVOURITE_EXERCISE,value);
        editor.commit();
    }

    public String getFavouriteExercise(){
        return sharedPreferences.getString(FAVOURITE_EXERCISE, null);
    }

    public void setType(String value){
        editor.putString(TYPE,value);
        editor.commit();
    }

    public String getType(){
        return sharedPreferences.getString(TYPE, "Select Workout place");
    }

    public void setExpires(long value){
        editor.putLong(EXPIRES,value);
        editor.commit();
    }

    public long getExpires(){
        return sharedPreferences.getLong(EXPIRES, 0);
    }

    public void setUniqueId(int value){
        editor.putInt(UNIQUE_ID,value);
        editor.commit();
    }

    public int getUniqueId(){
        return sharedPreferences.getInt(UNIQUE_ID, 0);
    }

    public void setVerifiedStatus(String value){
        editor.putString(VERIFIED,value);
        editor.commit();
    }

    public String getVerifiedStatus(){
        return sharedPreferences.getString(VERIFIED, "Unverified");
    }

    public void setCnic(String value){
        editor.putString(CNIC,value);
        editor.commit();
    }

    public String getCnic(){
        return sharedPreferences.getString(CNIC, "");
    }

    public void setFront(String value){
        editor.putString(FRONT,value);
        editor.commit();
    }

    public String getFront(){
        return sharedPreferences.getString(FRONT, "");
    }

    public void setBack(String value){
        editor.putString(BACK,value);
        editor.commit();
    }

    public String getBack(){
        return sharedPreferences.getString(BACK, "");
    }
}


