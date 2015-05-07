package com.unik.arinvaders;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;

import android.app.Activity;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.Toast;
import android.widget.ToggleButton;

public class GameActivity extends Activity implements CvCameraViewListener2, OnTouchListener {
    private static final String TAG = "OCVSample::Activity";

    private CameraBridgeViewBase cameraView;
    private GameEngine gameEngine;
    private GameFrameRender frameRender;

    private MediaPlayer mp;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    cameraView.enableView();
                    cameraView.setOnTouchListener(GameActivity.this);
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

        setContentView(R.layout.activity_game);

        if (!OpenCVLoader.initDebug()) {
            Toast.makeText(getApplicationContext(), "Failed to load openCV", Toast.LENGTH_SHORT).show();
        }

        //camera
        cameraView = (CameraBridgeViewBase) findViewById(R.id.game_surface_view);
        cameraView.setVisibility(SurfaceView.VISIBLE);
        cameraView.setCvCameraViewListener(this);

        //mediaplayer
        mp = MediaPlayer.create(this, R.raw.laser);

        //game engine
        gameEngine = new GameEngine(mp);
    }

    public void togglePause(View v){
        if (((ToggleButton) v).isChecked()) {
            onPause();
        }else{
            onResume();
        }
    }

    /*
    public void captureGameSpace(View v){
        gameEngine.setGameFrameCaptured(true);
        cameraView.getMatrix();
    }*/

    @Override
    public void onPause()
    {
        super.onPause();
        if (cameraView != null)
            cameraView.disableView();
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
        if (cameraView != null)
            cameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
        frameRender = new GameFrameRender(new GameEngineFrameRender(gameEngine));
        //frameRender = new GameFrameRender(new InitFrameRender(gameEngine));
    }

    public void onCameraViewStopped() {
        Toast.makeText(getApplicationContext(), "cameraViewStopped", Toast.LENGTH_SHORT).show();
    }

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        return frameRender.render(inputFrame, cameraView);
    }

    public boolean onTouch(View v, MotionEvent event) {

        if (!gameEngine.getGameFrameCaptured()){
            //frameRender = new GameFrameRender(new FeaturesFrameRender(gameEngine));
            //frameRender = new GameFrameRender(new GameEngineFrameRender(gameEngine));
            gameEngine.setGameFrameCaptured(true);
        }else{ // if (cameraView != null){
        //if (gameEngine.getGameFrameCaptured()){
            gameEngine.shoot();
        }

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