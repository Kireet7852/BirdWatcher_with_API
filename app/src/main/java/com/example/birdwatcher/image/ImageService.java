package com.example.birdwatcher.image;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface ImageService {
    @Multipart
    @POST("recognize")
    Call<ResponseModel> uploadImage(@Part MultipartBody.Part image);
}

