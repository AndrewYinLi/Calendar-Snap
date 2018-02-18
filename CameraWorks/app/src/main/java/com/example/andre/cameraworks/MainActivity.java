package com.example.andre.cameraworks;

import android.icu.text.SimpleDateFormat;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.hardware.Camera;
import android.util.Log;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.cloud.vision.v1.AnnotateImageRequest;
import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.BatchAnnotateImagesResponse;
import com.google.cloud.vision.v1.EntityAnnotation;
import com.google.cloud.vision.v1.Feature;
import com.google.cloud.vision.v1.Feature.Type;
import com.google.cloud.vision.v1.Image;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.google.protobuf.ByteString;
import com.google.protobuf.Descriptors;
import com.google.protobuf.MapEntry;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;


import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;

public class MainActivity extends AppCompatActivity {

    private Camera mCamera = null;
    private CameraView mCameraView = null;
    private boolean canSnap = true;
    private byte[] imageArr;
    private TextView debugText;

    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
            if (pictureFile == null){
                return;
            }

            try {
                imageArr = data;
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();
                mCamera.stopPreview();
                //camera.startPreview();
            } catch (FileNotFoundException e) {

            } catch (IOException e) {

            }
        }
    };

    /** Create a File for saving an image or video */
    private static File getOutputMediaFile(int type){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "MyCameraApp");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_"+ timeStamp + ".jpg");
        return mediaFile;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        debugText = (TextView) findViewById(R.id.debugField);

        try{
            mCamera = Camera.open();//you can use open(int) to use different cameras
        } catch (Exception e){
            Log.d("ERROR", "Failed to get camera: " + e.getMessage());
        }

        if(mCamera != null) {
            mCameraView = new CameraView(this, mCamera);//create a SurfaceView to show camera data
            FrameLayout camera_view = (FrameLayout)findViewById(R.id.camera_view);
            camera_view.addView(mCameraView);//add the SurfaceView to the layout
        }

        //btn to close the application
        ImageButton imgClose = (ImageButton)findViewById(R.id.imgClose);
        imgClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.exit(0);
            }
        });

        // Add listeners for buttons
        final Button snapButton = (Button) findViewById(R.id.button_capture);
        final Button yesButton = (Button) findViewById(R.id.button_yes);
        final Button noButton = (Button) findViewById(R.id.button_no);
        final ProgressBar spinner = (ProgressBar)findViewById(R.id.spinner);

        snapButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // get an image from the camera
                        if(canSnap) {
                            canSnap = false;
                            mCamera.takePicture(null, null, mPicture);
                            snapButton.setVisibility(View.INVISIBLE);
                            yesButton.setVisibility(View.VISIBLE);
                            noButton.setVisibility(View.VISIBLE);
                        }
                    }
                }
        );

        noButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // get an image from the camera
                        if(!canSnap) {
                            canSnap = true;
                            snapButton.setVisibility(View.VISIBLE);
                            yesButton.setVisibility(View.INVISIBLE);
                            noButton.setVisibility(View.INVISIBLE);
                            mCamera.startPreview();
                    }
                    }
                }
        );

        yesButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // get an image from the camera
                        if(!canSnap) {
                            spinner.setVisibility(View.VISIBLE);
                            try {
                                String payload = "{\"requests\":[{\"image\":{\"source\":{\"imageUri\":\"https://marketplace.canva.com/MAB1BT5b_Fs/1/0/thumbnail_large/canva-coffee-fundraising-event-poster-MAB1BT5b_Fs.jpg\"}},\"features\":[{\"type\":\"TEXT_DETECTION\"}]}]}";
                                String output = new Vision().execute(payload).get();
                                debugText.setText(output);
                                //debugText.setText("response: " + response);
                            }
                            catch(Exception e){
                                debugText.setText(e.toString());
                            }
                            //opticalProcessing();
                            /*
                            canSnap = true;
                            snapButton.setVisibility(View.VISIBLE);
                            yesButton.setVisibility(View.INVISIBLE);
                            noButton.setVisibility(View.INVISIBLE);
                            camera.startPreview();
                            */
                        }
                    }
                }
        );
    }


    /*
    private void opticalProcessing(){
        // Instantiates a client
        try (ImageAnnotatorClient vision = ImageAnnotatorClient.create()) {
            ByteString imgBytes = ByteString.copyFrom(imageArr);
            // Builds the image annotation request
            List<AnnotateImageRequest> requests = new ArrayList<>();
            Image img = Image.newBuilder().setContent(imgBytes).build();
            Feature feat = Feature.newBuilder().setType(Type.LABEL_DETECTION).build();
            AnnotateImageRequest request = AnnotateImageRequest.newBuilder()
                    .addFeatures(feat)
                    .setImage(img)
                    .build();
            requests.add(request);

            // Performs label detection on the image file
            BatchAnnotateImagesResponse response = vision.batchAnnotateImages(requests);
            List<AnnotateImageResponse> responses = response.getResponsesList();

            for (AnnotateImageResponse res : responses) {
                if (res.hasError()) {
                    System.out.printf("Error: %s\n", res.getError().getMessage());
                    debugText.setText(res.getError().getMessage());
                    return;
                }

                String test = "";
                for (EntityAnnotation annotation : res.getLabelAnnotationsList()) {
                    Map<Descriptors.FieldDescriptor, Object> text = annotation.getAllFields();
                    for(Map.Entry<Descriptors.FieldDescriptor, Object> word : text.entrySet()){
                        test += word.getValue().toString();
                    }

                }
                debugText.setText(test);
            }
        } catch (IOException e) {
            debugText.setText(e.toString());
        } catch (Exception e) {
            debugText.setText(e.toString());
        }
    }
    */
}
