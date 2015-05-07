package com.joakimmj.cam.cameratest;

import android.app.Service;
import android.content.Context;
import android.media.MediaPlayer;

import org.opencv.core.Core;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.core.Mat;
import org.opencv.core.KeyPoint;
import org.opencv.core.DMatch;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;
import java.util.Vector;


public class GameEngine {
    private int points = 0;
    private FeatureDetector fDetect;
    private DescriptorExtractor fDescript;
    private DescriptorMatcher matcher;
    private Mat frame, img, detections, refImg, refDesc, desc;
    private Vector<KeyPoint> keyPoints, refKeys;
    private Vector<DMatch> matches;
    private MediaPlayer mp;

    public GameEngine(MediaPlayer mp){
        fDetect = FeatureDetector.create(FeatureDetector.FAST);
        this.mp = mp;
    }

    public Vector<DMatch> getMatches(){
        return matches;
    }

    public void processFrame(Mat grayFrame, Mat rgbaFrame) {
        calcGameSpace(grayFrame);
        // renderFrame(rgbaFrame);
    }

    public void shot(){
        if (mp.isPlaying()) {
            mp.stop();
            try {
                mp.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        mp.start();
    }

    private void calcGameSpace(Mat grayFrame) {

        //save game space
        GameSpaceResult.save();
    }

    private void renderFrame(Mat rgbaFrame) {
        Imgproc.putText(rgbaFrame, "Points: "+points, new Point(rgbaFrame.cols() / 3 * 2, rgbaFrame.rows() * 0.1),
                Core.FONT_HERSHEY_SIMPLEX, 1.0, new Scalar(255, 255, 0));
    }
}
