package com.hypenet.realestaterehman.utils.retrofit;



import com.hypenet.realestaterehman.model.ApiResponse;
import com.hypenet.realestaterehman.model.ChatBotCompletion;
import com.hypenet.realestaterehman.model.ChatBotResponse;
import com.hypenet.realestaterehman.model.City;
import com.hypenet.realestaterehman.model.House;
import com.hypenet.realestaterehman.model.ImageDetect;
import com.hypenet.realestaterehman.model.NotificationModel;
import com.hypenet.realestaterehman.model.TokenModel;
import com.hypenet.realestaterehman.model.User;
import com.hypenet.realestaterehman.model.UserResponse;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Url;

public interface Api {

    @POST("login_seller")
    Call<UserResponse> login(@Body User user);

    @Multipart
    @POST("register_seller")
    Call<UserResponse> register(
            @Part("name") RequestBody name,
            @Part("email") RequestBody email,
            @Part("password") RequestBody password,
            @Part("phone") RequestBody phone,
            @Part("city") RequestBody city,
            @Part("address") RequestBody address,
            @Part MultipartBody.Part image
    );

    @Multipart
    @POST("update_kyc/{id}")
    Call<UserResponse> update_kyc(
            @Path("id") String id,
            @Part("cnic") RequestBody cnic,
            @Part MultipartBody.Part image1,
            @Part MultipartBody.Part image2
    );

    @Multipart
    @POST("add_property")
    Call<UserResponse> add_property(
            @Part("title") RequestBody title,
            @Part("description") RequestBody description,
            @Part("phone") RequestBody phone,
            @Part("email") RequestBody email,
            @Part("owner_name") RequestBody owner_name,
            @Part("latitude") RequestBody latitude,
            @Part("longitude") RequestBody longitude,
            @Part("area_name") RequestBody area_name,
            @Part("type") RequestBody type,
            @Part("price") RequestBody price,
            @Part("sell_type") RequestBody sell_type,
            @Part("seller_id") RequestBody seller_id,
            @Part("city_id") RequestBody city_id,
            @Part MultipartBody.Part[] damage_images
    );

    @Multipart
    @POST("update_seller_profile/{id}")
    Call<UserResponse> update_profile(
            @Path("id") String id,
            @Part("name") RequestBody name,
            @Part("email") RequestBody email,
            @Part("phone") RequestBody phone,
            @Part("city") RequestBody city,
            @Part("address") RequestBody address,
            @Part MultipartBody.Part image
    );

    @GET("get_seller_properties/{id}")
    Call<ApiResponse<List<House>>> get_properties(@Path("id") int id);
    @POST("delete_property/{id}")
    Call<ApiResponse<House>> delete_property(@Path("id") int id);

    @GET("get_cities")
    Call<ApiResponse<List<City>>> get_cities();

    @POST
    Call<ChatBotResponse> chat(@Url String url, @Body ChatBotCompletion botCompletion);

    @POST
    Call<UserResponse> setNotification(@Header("Authorization") String auth, @Url String url, @Body NotificationModel model);

    @FormUrlEncoded
    @POST("connect/token")
    Call<TokenModel> accessToken(
            @Field("client_id") String client_id,
            @Field("client_secret") String client_secret,
            @Field("grant_type") String grant_type
    );

    @Multipart
    @POST("detect_image")
    Call<ApiResponse<ImageDetect>> detect_image(
            @Part MultipartBody.Part image
    );
}



