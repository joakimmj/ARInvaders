
package com.joakimmj.cam.cameratest;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;

import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.Features2d;
import org.opencv.core.Core;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.res.Resources;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.IOException;

public class CameraActivity extends Activity implements CvCameraViewListener2, OnTouchListener {
    private static final String TAG = "OCVSample::Activity";

    private CameraBridgeViewBase mOpenCvCameraView;
    private CameraCalibrator mCalibrator;
    private OnCameraFrameRender mOnCameraFrameRender;
    private int mWidth;
    private int mHeight;

    private FeatureDetector detector;
    private MediaPlayer mp;

    //static{ System.loadLibrary("opencv_java"); }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                    mOpenCvCameraView.setOnTouchListener(CameraActivity.this);
                    detector = FeatureDetector.create(FeatureDetector.FAST);
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.camera_calibration_surface_view);

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.camera_calibration_java_surface_view);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);

        //mOpenCvCameraView.enableView();
        //mOpenCvCameraView.setOnTouchListener(CameraActivity.this);
        mp = MediaPlayer.create(this, R.raw.laser);

        //Toast.makeText(getApplicationContext(), "FAST: "+FeatureDetector.FAST, Toast.LENGTH_SHORT).show();
        //detector = FeatureDetector.create(FeatureDetector.FAST);
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Toast.makeText(getApplicationContext(), "Failed to load openCV", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), "onResume: else", Toast.LENGTH_SHORT).show();
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
        if (mWidth != width || mHeight != height) {
            mWidth = width;
            mHeight = height;
            mCalibrator = new CameraCalibrator(mWidth, mHeight);
            if (CalibrationResult.tryLoad(this, mCalibrator.getCameraMatrix(), mCalibrator.getDistortionCoefficients())) {
                mCalibrator.setCalibrated();
            }

            mOnCameraFrameRender = new OnCameraFrameRender(new CalibrationFrameRender(mCalibrator));
        }

    }

    public void onCameraViewStopped() {
    }

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        return mOnCameraFrameRender.render(inputFrame);
    }

    public boolean onTouch(View v, MotionEvent event) {
        //detector.detect(mOpenCvCameraView);
        //Toast.makeText(getApplicationContext(), "touched", Toast.LENGTH_SHORT).show();

        if (mp.isPlaying()) {
            mp.stop();
            try {
                mp.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        mp.start();

        return false;
    }
}


/*
package com.joakimmj.cam.cameratest;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
//import android.graphics.Outline;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.IOException;

//import com.joakimmj.cam.cameratest.R;

public class CameraActivity extends Activity { //ActionBarActivity {

    private Camera cameraObject;
    private ShowCamera showCamera;
    private ImageView pic;
    private MediaPlayer mp;
    private int points = 0;

    static{ System.loadLibrary("opencv_java"); }

    public static Camera isCameraAvailiable(){
        Camera object = null;
        try {
            object = Camera.open();
        }
        catch (Exception e){
        }
        return object;
    }

    private PictureCallback capturedIt = new PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            Bitmap bitmap = BitmapFactory.decodeByteArray(data , 0, data .length);
            if(bitmap==null){
                Toast.makeText(getApplicationContext(), "not taken", Toast.LENGTH_SHORT).show();
            }
            else
            {
                Toast.makeText(getApplicationContext(), "taken", Toast.LENGTH_SHORT).show();
            }
            cameraObject.release();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        mp = MediaPlayer.create(this, R.raw.laser);
        //pic = (ImageView)findViewById(R.id.imageView1);
        cameraObject = isCameraAvailiable();
        showCamera = new ShowCamera(this, cameraObject);
    }

    /*public void snapIt(View v) throws IOException {
        cameraObject.takePicture(null, null, capturedIt);
    }*/
/*
    public void initGame(View v) {//throws InterruptedException {
        setContentView(R.layout.activity_init);
        Toast.makeText(getApplicationContext(), "inits game..", Toast.LENGTH_SHORT).show();
        //FrameLayout preview = (FrameLayout) findViewById(R.id.init_back);
        //preview.addView(showCamera);

        //wait(1000);
        points = 0;
        playGame();
    }
*/    /*
    private void openCamera(int id){
        cameraObject = isCameraAvailiable();
        showCamera = new ShowCamera(this, cameraObject);
        FrameLayout preview = (FrameLayout) findViewById(id);
        preview.removeAllViews();
        preview.addView(showCamera);
    }*/
/*
    public void playGame(){
        setContentView(R.layout.activity_game);
        ((TextView) findViewById(R.id.game_points)).setText(points+"");
        //openCamera(R.id.camera_preview);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        //preview.removeAllViews();
        preview.addView(showCamera);
    }

    public void incPoints(){
        points++;
        ((TextView) findViewById(R.id.game_points)).setText(points+"");
    }

    public void gameShoot(View v) throws IOException {
        if (((ToggleButton) findViewById(R.id.toggleBtnMenu)).isChecked()) return;

        //temp
        incPoints();

        if (mp.isPlaying()) {
            mp.stop();
            mp.prepare();
        }
        mp.start();
    }

    public void gamePause(View v){

        if (((ToggleButton) v).isChecked()) {
            //cameraObject.takePicture(null, null, capturedIt);
            cameraObject.stopPreview();
        }else{
            cameraObject.startPreview();
            //openCamera(R.id.camera_preview);
        }
        //View menu = findViewById(R.id.menuView);
*/        //Toast.makeText(getApplicationContext(), "menu: "+menu, Toast.LENGTH_SHORT).show();
        /*
        if (((ToggleButton) v).isChecked()) {
            setContentView(R.layout.menu);
            //menu.setVisibility(View.VISIBLE);
        }else{
            setContentView(R.layout.activity_game);
            //menu.setVisibility(View.GONE);
        }*/
/*

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_camera, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case R.id.about:
                Toast.makeText(getApplicationContext(), "About menu item pressed", Toast.LENGTH_SHORT).show();
                break;
            case R.id.help:
                Toast.makeText(getApplicationContext(), "Help menu item pressed", Toast.LENGTH_SHORT).show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
*/