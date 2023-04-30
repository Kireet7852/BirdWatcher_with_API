package com.example.birdwatcher.audio;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.birdwatcher.LocationSet;
import com.example.birdwatcher.R;
import com.example.birdwatcher.Web_search;
import com.example.birdwatcher.helpers.ApiAudioHelperActivity;
import com.example.birdwatcher.image.ResponseModel;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class BirdNetAudioClassificationActivity extends ApiAudioHelperActivity {

    private ListView outputView;
    String clickedName;
    String clickedRecognitionType = "Audio";
    String Base_url = "http://192.168.0.104:8080/";

    @Override
    protected void onCreate(Bundle savedInstance){
        super.onCreate(savedInstance);

        outputView = findViewById(R.id.audioListView);
    }

    //@Override
//    protected void audioDetection(Uri selectedAudioUri){
//
//        // Convert the selected audio file to a RequestBody object
//        String dir_Name = selectedAudioUri.getPath();
//        String tempfile = dir_Name.substring(0, dir_Name.indexOf(":"));
//        File audioFile = new File(tempfile);
//        Log.d("FilePath",tempfile);
//        RequestBody requestBody = RequestBody.create(MediaType.parse("audio/*"), audioFile);
//
//        // Create the multipart request part
//        MultipartBody.Part audioPart = MultipartBody.Part.createFormData("image", audioFile.getName(), requestBody);
//
//
//        // Create a Retrofit instance
//        Retrofit retrofit = new Retrofit.Builder()
//                .baseUrl(Base_url) // Replace with your server URL
//                .addConverterFactory(GsonConverterFactory.create())
//                .build();
//
//        // Create the Retrofit instance and call the API
//        AudioService api = retrofit.create(AudioService.class);
//        Call<ResponseModel> call = api.uploadAudio(audioPart);
//
//        call.enqueue(new Callback<ResponseModel>() {
//            @Override
//            public void onResponse(Call<ResponseModel> call, Response<ResponseModel> response) {
//                if(response.isSuccessful()){
//                    ResponseModel responseModel = response.body();
//
//                    String message = String.valueOf(responseModel);
//                    Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
//                }
//            }
//
//            @Override
//            public void onFailure(Call<ResponseModel> call, Throwable t) {
//                Log.d("Error",t.getMessage());
//                Toast.makeText(BirdNetAudioClassificationActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
//            }
//        });
//    }


    @Override
    protected void audioDetection(Uri selectedAudioUri){
        // Get an input stream for the selected audio file
        InputStream inputStream = null;
        try {
            ContentResolver resolver = getContentResolver();
            ParcelFileDescriptor pfd = resolver.openFileDescriptor(selectedAudioUri, "r");
            FileDescriptor fd = pfd.getFileDescriptor();
            inputStream = new FileInputStream(fd);

            // Read the contents of the input stream into a byte array
            byte[] audioData = new byte[inputStream.available()];
            inputStream.read(audioData);

            // Create a RequestBody from the byte array
            RequestBody requestBody = RequestBody.create(MediaType.parse("audio/*"), audioData);

            // Create the multipart request part
            MultipartBody.Part audioPart = MultipartBody.Part.createFormData("audio", "audio_file", requestBody);

            // Create a Retrofit instance and call the API
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(Base_url) // Replace with your server URL
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            AudioService api = retrofit.create(AudioService.class);
            Call<AudioResponseList> call = api.uploadAudio(audioPart);

            call.enqueue(new Callback<AudioResponseList>() {
                @Override
                public void onResponse(Call<AudioResponseList> call, Response<AudioResponseList> response) {
                    if(response.isSuccessful()) {
                        AudioResponseList responseModel = response.body();

//                        String message = String.valueOf(responseModel);
                        List<AudioResponseModel> audioList = responseModel.getAudio();
                        if (audioList != null && audioList.size() > 0) {
                            List<String> resultList = new ArrayList<>();
                            for (AudioResponseModel audio : audioList) {
                                String result = "Common name: " + audio.getCommonName()
                                        + ", \nScientific name: " + audio.getScientificName()
                                        + ", \nConfidence: " + audio.getConfidence()
                                        + ", \nStart: " + audio.getStart()
                                        + ", End: " + audio.getEnd();
                                resultList.add(result);
                            }
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(BirdNetAudioClassificationActivity.this,
                                    android.R.layout.simple_list_item_1, resultList);
                            outputView.setAdapter(adapter);
                        }
                    }
                    else {
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(),
                                android.R.layout.simple_list_item_1, new String[]{"Could not identify!!"});
                        outputView.setAdapter(adapter);
                        Toast.makeText(getApplicationContext(), "No audio classification results found!", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<AudioResponseList> call, Throwable t) {
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(),
                            android.R.layout.simple_list_item_1, new String[]{"Upload Again or recheck network connection!!"});
                    outputView.setAdapter(adapter);
                    Toast.makeText(BirdNetAudioClassificationActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

            outputView.setOnItemClickListener((parent, view, position, id) -> {
                // Get the text of the clicked item
                String clickedItem = ((TextView) view).getText().toString();
                String[] lines = clickedItem.split(",");

                //get the start and end time from clickedItem
                String start = lines[3].split(": ")[1];
                String end = lines[4].split(": ")[1];

                // Display a toast message with the clicked item text
                Toast.makeText(this, "Clicked: " + clickedItem, Toast.LENGTH_SHORT).show();

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                //builder.setTitle("Choose an option");
                View dialogView = getLayoutInflater().inflate(R.layout.activity_custom_dialog_layout, null);
                builder.setView(dialogView);
                AlertDialog alertDialog = builder.create();
                alertDialog.show();

                Button cancelButton = dialogView.findViewById(R.id.cancel_button);

                cancelButton.setOnClickListener((v) -> {

                    String word = lines[0].split(": ")[1] + "-" + lines[1].split(": ")[1];


                    Intent intent = new Intent(this, Web_search.class);
                    intent.putExtra("querys", word);
                    startActivity(intent);
                    alertDialog.dismiss();
                });


                Button okButton = dialogView.findViewById(R.id.ok_button);

                okButton.setOnClickListener((v) -> {
                    // Add your own logic here for what to do when the "Ok" button is clicked
                    String word = lines[0].split(": ")[1];
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault());
                    Date date = new Date();

                    String formattedDateTime = dateFormat.format(date);


                    Intent intent = new Intent(this, LocationSet.class);
                    intent.putExtra("clickedName", lines[0].split(": ")[1]);
                    intent.putExtra("clickedSciName", lines[1].split(": ")[1]);
                    intent.putExtra("clickedRecognitionType", clickedRecognitionType);
                    intent.putExtra("formattedDataTime", formattedDateTime);
                    intent.putExtra("confidence_level", lines[2].split(": ")[1]);
                    startActivity(intent);
                    alertDialog.dismiss();
                });
            });


        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }





//    public String getRealPathFromURI(Uri contentUri) {
//        String[] projection = { MediaStore.Audio.Media.DATA };
//        String filePath = null;
//        try (Cursor cursor = getContentResolver().query(contentUri, projection, null, null, null)) {
//            if (cursor != null && cursor.moveToFirst()) {
//                int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
//                filePath = cursor.getString(columnIndex);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return filePath;
//    }
//
//    public String getPath(Uri uri) {
//        String[] projection = { MediaStore.Images.Media.DATA };
//        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
//        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
//        cursor.moveToFirst();
//        String path = cursor.getString(column_index);
//        cursor.close();
//        return path;
//    }
//    public String getNameFromUri(Uri uri){
//        String fileName = "";
//        Cursor cursor = null;
//        cursor = getContentResolver().query(uri, new String[]{
//                MediaStore.Images.ImageColumns.DISPLAY_NAME
//        }, null, null, null);
//        if (cursor != null && cursor.moveToFirst()) {
//            fileName = cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DISPLAY_NAME));
//        }
//        if (cursor != null) {
//            cursor.close();
//        }
//        return fileName;
//    }


}
