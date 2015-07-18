package com.akraft.muna.service;

import com.akraft.muna.R;
import com.akraft.muna.models.Profile;
import com.akraft.muna.models.wrappers.Credentials;
import com.akraft.muna.models.Mark;
import com.akraft.muna.models.MessagesPage;
import com.akraft.muna.models.wrappers.Token;
import com.akraft.muna.models.User;
import com.akraft.muna.models.wrappers.UserId;
import com.facebook.AccessToken;
import com.squareup.okhttp.Call;

import java.util.ArrayList;
import java.util.Map;

import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.Multipart;
import retrofit.http.PATCH;
import retrofit.http.POST;
import retrofit.http.Part;
import retrofit.http.Path;
import retrofit.http.Query;
import retrofit.mime.TypedFile;
import retrofit.mime.TypedInput;
import retrofit.mime.TypedString;

public interface MainService {

    @POST("/auth/login/")
    void login(@Body Credentials credentials, Callback<Token> callback);

    @POST("/users/")
    void register(@Body Credentials credentials, Callback<Object> callback);

    @GET("/auth/username-exists/")
    void usernameExists(@Query("username") String username, Callback<Boolean> callback);

    @GET("/auth/email-exists/")
    void emailExists(@Query("email") String email, Callback<Boolean> callback);

    @GET("/user/")
    void user(@Query("username") String username, @Query("email") String email, @Query("id") Long id, Callback<User> callback);

    @GET("/profile/{id}/?fields=user,id,facebook,country,avatar,level,exp,exp_curr,exp_next,marks")
    void loadProfile(@Path("id") long id, Callback<Profile> profile);

    @PATCH("/profile/{id}/?fields=user,id,facebook,country,avatar")
    void editProfile(@Path("id") long id, @Body Profile profile, Callback<Profile> callback);

    @GET("/marks/?fields=lat,lon,id,author")
    void getActiveMarksList(Callback<ArrayList<Mark>> callback);

    @GET("/marks/")
    void getActiveMarks(@Query("page") int currentPage, Callback<ArrayList<Mark>> callback);

    @GET("/marks/?id={idList}")
    void getMarks(@Path("idList") long[] idList, Callback<ArrayList<Mark>> callback);

    @GET("/marks/")
    void searchMarks(@Query("search") String constraint, Callback<ArrayList<Mark>> callback);
    
    @POST("/marks/")
    void addMark(@Body Mark mark, Callback<Mark> callback);

    @GET("/profile/{user_id}/?fields=marks")
    void getUserMarksList(@Path("user_id") long userId, Callback<Profile> callback);

    @GET("/mark/{id}/")
    void loadMark(@Path("id") long id, Callback<Mark> callback);

    @GET("/marks/{lat}/{lon}/")
    void getMarksNearby(@Path("lat") double lat, @Path("lon") double lon, @Query("radius") Float radius, Callback<ArrayList<Mark>> callback);

    @GET("/profile/{userId}/?fields=team")
    void team(@Path("userId") long userId, Callback<Profile> callback);

    @GET("/users/")
    void users(Callback<ArrayList<User>> callback);

    @GET("/users/")
    void searchUsers(@Query("search") String constraint, Callback<ArrayList<User>> callback);

    @GET("/messages/")
    void messages(@Query("user_id") long userId, @Query("page") int page, Callback<MessagesPage> callback);

    @POST("/count_mark/")
    void countMark(@Body Map<String, String> data, Callback<Profile> callback);

    @GET("/teams/define_relations/")
    void defineRelations(@Query("user_id") long userId, Callback<Integer> callback);

    @POST("/teams/send_request/")
    void sendRequest(@Body UserId userId, Callback<Object> callback);

    @POST("/teams/remove_request/")
    void removeRequest(@Body UserId userId, Callback<Object> callback);

    @POST("/teams/accept/")
    void acceptRequest(@Body UserId userId, Callback<Object> callback);

    @POST("/teams/decline/")
    void declineRequest(@Body UserId userId, Callback<Object> callback);

    @POST("/teams/remove/")
    void removeTeammate(@Body UserId userId, Callback<Object> callback);

    @GET("/profile/{userId}/?fields=incoming_requests")
    void incomingRequests(@Path("userId") long userId, Callback<Profile> callback);

    @Multipart
    @POST("/marks/upload_photo/")
    void uploadMarkPhoto(@Part("photo") TypedFile input, @Part("id") TypedString id, Callback<Object> callback);

    @Multipart
    @POST("/profile/upload_avatar/")
    void uploadAvatar(@Part("image") TypedFile input, Callback<Object> callback);

    @POST("/auth/facebook/")
    void facebookLogin(@Body Map<String, String> data, Callback<Token> callback);
}
