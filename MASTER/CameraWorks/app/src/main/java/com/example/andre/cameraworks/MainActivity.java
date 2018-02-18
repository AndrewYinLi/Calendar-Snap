package com.example.andre.cameraworks;

import android.hardware.Camera;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.apache.commons.codec.binary.Base64;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;


public class MainActivity extends AppCompatActivity {

    private Camera mCamera = null;
    private CameraView mCameraView = null;
    private boolean canSnap = true; // Whether you can take a picture
    private TextView debugText; // For debugging purposes only
    byte[] image;
    File picture;


    /**
     * Calls a nested function that saves picture
     * @return picture taken
     */
    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {

        /**
         * Writes picture byte array to a file.
         * @param data - byte array representation for the picture
         * @param camera - Camera object
         * @return
         */
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            File pictureFile = getOutputMediaFile();
            Log.d("pictureFile", pictureFile.toString());

            if (pictureFile == null){
                return; // If there is no file to write to, escape function.
            }
            try { // Attempt to save picture
                image = data;
                Log.d("image", image.toString());
                //debugText.setText(pictureFile.toString());

                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();
                picture = pictureFile;
                mCamera.stopPreview();
            }
            catch (FileNotFoundException e) {}
            catch (IOException e) {}
        }
    };

    /**
     * Create a File for saving an image or video
     * @return file reference to write the picture to
     */
    private static File getOutputMediaFile(){
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "MyCameraApp");

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
                                //String payload = "{\"requests\":[{\"image\":{\"source\":{\"imageUri\":\"https://marketplace.canva.com/MAB1BT5b_Fs/1/0/thumbnail_large/canva-coffee-fundraising-event-poster-MAB1BT5b_Fs.jpg\"}},\"features\":[{\"type\":\"TEXT_DETECTION\"}]}]}";
                                FileInputStream fileInputStream = new FileInputStream(picture);
                                long byteLength = picture.length();
                                byte[] fileContent = new byte[(int) byteLength];
                                fileInputStream.read(fileContent,0,(int)byteLength);

                                String encodedfile = new String(Base64.encodeBase64(fileContent), "UTF-8");
                                //debugText.setText(encodedfile);
                                String payload="{\"requests\":[{\"image\":{\"content\":\"" + encodedfile + "\"},\"features\":[{\"type\":\"TEXT_DETECTION\"}]}]}";

                                String output = new Vision().execute(payload).get();

                                //JSONObject obj = new JSONObject(output);

                                //String obj6 = obj.getJSONArray("responses").getJSONObject(0).getJSONArray("textAnnotations").getJSONObject(0).getString("description");

                                debugText.setText(output);
                                //debugText.setText("response: " + response);
                            }
                            catch(Exception e){
                                //debugText.setText(e.toString());
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
}

