package com.example.birdwatcher.audio;

import com.example.birdwatcher.image.ResponseModel;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface AudioService {
    @Multipart
    @POST("api/predicts")
    Call<AudioResponseList> uploadAudio(@Part MultipartBody.Part audioFile);
}

