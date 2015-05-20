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

    private MediaPlayer mpLaser, mpExplosion;

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

        //media player
        mpLaser = MediaPlayer.create(this, R.raw.laser);
        mpExplosion = MediaPlayer.create(this, R.raw.explosion);

        //game engine
        gameEngine = new GameEngine(mpExplosion);
    }

    public void togglePause(View v){
        if (((ToggleButton) v).isChecked()) {
            onPause();
        }else{
            onResume();
        }
    }

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
    }

    public void onCameraViewStopped() {
        Toast.makeText(getApplicationContext(), "cameraViewStopped", Toast.LENGTH_SHORT).show();
    }

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        return frameRender.render(inputFrame);
    }

    public boolean onTouch(View v, MotionEvent event) {
        gameEngine.shoot(mpLaser);

        return false;
    }
}
