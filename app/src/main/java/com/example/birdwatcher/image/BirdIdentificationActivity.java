package com.example.birdwatcher.image;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.birdwatcher.LocationSet;
import com.example.birdwatcher.R;
import com.example.birdwatcher.Web_search;
import com.example.birdwatcher.helpers.MLImageHelperActivity;
import com.google.common.hash.BloomFilter;
import com.google.mlkit.common.model.LocalModel;
import com.google.mlkit.vision.common.InputImage;
//import com.google.mlkit.vision.label.ImageLabeler;
//import com.google.mlkit.vision.label.ImageLabeling;
//import com.google.mlkit.vision.label.custom.CustomImageLabelerOptions;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class BirdIdentificationActivity extends MLImageHelperActivity {

//    private ImageLabeler imageLabeler;
    private ListView outputView;
    String clickedName, clickedSciName;
    String clickedRecognitionType = "Image";
    String Base_url = "http://192.168.0.104:8080/";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        LocalModel localModel = new LocalModel.Builder().setAssetFilePath("birdy.tflite").build();
//        CustomImageLabelerOptions options = new CustomImageLabelerOptions.Builder(localModel)
//                                                .setConfidenceThreshold(0.7f)
//                                                .setMaxResultCount(5)
//                                                .build();
//        imageLabeler = ImageLabeling.getClient(options);
    }

    @Override
    protected void runDetection(Bitmap bitmap) {
        InputImage inputImage = InputImage.fromBitmap(bitmap, 0);
        outputView = findViewById(R.id.textView);
        //imageLabeler.process(inputImage).addOnSuccessListener(imageLabels -> {
//            StringBuilder sb = new StringBuilder();
//            for (ImageLabel label : imageLabels) {
//                sb.append(label.getText()).append(": ").append(label.getConfidence()).append("\n");
//            }
//            if (imageLabels.isEmpty()) {
//                getOutputTextView().setText("Could not identify!!");
//            } else {
//                getOutputTextView().setText(sb.toString());
//            }
//        }).addOnFailureListener(e -> {
//            e.printStackTrace();
//            if (imageLabels.isEmpty()) {
//                ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
//                        android.R.layout.simple_list_item_1, new String[]{"Could not identify!!"});
//                outputView.setAdapter(adapter);
//                return;
//            } else {
//                int labelSize = imageLabels.size();
//                if (labelSize > 0 && imageLabels.get(labelSize - 1).getText().equals("Could not identify!!")) {
//                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
//                            android.R.layout.simple_list_item_1, new String[]{"Could not identify!!"});
//                    outputView.setAdapter(adapter);
//                    Toast.makeText(this, "Capture Again Not able to recognize ",  Toast.LENGTH_SHORT).show();
//                    return; // Stop function if last label is "Could not identify!!"
//                }
//
//                ArrayList<String> labelList = new ArrayList<>();
//                for (ImageLabel label : imageLabels) {
//                    labelList.add(label.getText() + ": " + String.format("%.3f", label.getConfidence()*100));
//                }
//                ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
//                        android.R.layout.simple_list_item_1, labelList);
//                outputView.setAdapter(adapter);
                File imageFile = new File(getCacheDir(), "image.jpg");
                try {
                    OutputStream os = new FileOutputStream(imageFile);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
                    os.flush();
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // Create a Retrofit instance
                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl(Base_url) // Replace with your server URL
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();

                // Create the service instance
                ImageService service = retrofit.create(ImageService.class);

                // Create the request body
                RequestBody requestBody = RequestBody.create(MediaType.parse("image/jpeg"), imageFile);

                // Create the multipart request part
                MultipartBody.Part part = MultipartBody.Part.createFormData("image", imageFile.getName(), requestBody);

                // Send the request
                Call<ResponseModel> call = service.uploadImage(part);
                call.enqueue(new Callback<ResponseModel>() {
                    @Override
                    public void onResponse(Call<ResponseModel> call, retrofit2.Response<ResponseModel> response) {
                        if (response.isSuccessful()) {
                            ResponseModel responseModel = response.body();

                            String message = "Name: " + responseModel.getClassName() + ", \n" +
                                    "Scientific Name: " + responseModel.getScientificClassName() + ", \n" +
                                    "Confidence: " + String.format("%.3f", (float)responseModel.getProbability()*100);
//                            String message = String.valueOf(responseModel);
                            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                            if (response.code() != 200) {
                                ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(),
                                        android.R.layout.simple_list_item_1, new String[]{"Could not identify!!"});
                                outputView.setAdapter(adapter);
                                return;
                            }else {
                                ArrayList<String> labelList = new ArrayList<>();
//                for (ImageLabel label : imageLabels) {
                                labelList.add(message);
//                }
                                ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, labelList);
                                outputView.setAdapter(adapter);
                            }
                        } else {
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(),
                                    android.R.layout.simple_list_item_1, new String[]{"Could not identify!!"});
                            outputView.setAdapter(adapter);
                            //Toast.makeText(BirdIdentificationActivity.this, "Error: " + response.code(), Toast.LENGTH_SHORT).show();
                        }
                    }


                    @Override
                    public void onFailure(Call<ResponseModel> call, Throwable t) {
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(),
                                android.R.layout.simple_list_item_1, new String[]{"Capture again or Check Connection!!"});
                        outputView.setAdapter(adapter);
                        Toast.makeText(BirdIdentificationActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                });
//            }

            outputView.setOnItemClickListener((parent, view, position, id) -> {
                // Get the text of the clicked item
                String clickedItem = ((TextView) view).getText().toString();
                String[] lines = clickedItem.split(",");

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

                    String word = lines[0].split(": ")[1] +"-"+lines[1].split(": ")[1];


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
                    intent.putExtra("confidence_level",lines[2].split(": ")[1]);
                    startActivity(intent);
                    alertDialog.dismiss();
                });

//            });
        //}).addOnFailureListener(e -> {
//            e.printStackTrace();
        });
    }
}
