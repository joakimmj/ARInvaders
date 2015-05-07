package com.joakimmj.unik.arinvaders;

import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.widget.ImageView;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.core.Mat;
import org.opencv.core.KeyPoint;
import org.opencv.core.DMatch;
import org.opencv.imgproc.Imgproc;
import org.opencv.features2d.Features2d;

import java.io.IOException;
import java.util.Vector;


public class GameEngine {
    private int points = 0;
    private FeatureDetector fDetect;
    private DescriptorExtractor fDescript;
    private DescriptorMatcher matcher;
    private Mat img, detections, refImg, refDesc, desc;
    private MatOfKeyPoint keyPoints, refKeys;
    private MatOfDMatch matches;
    private MediaPlayer mp;
    private boolean gameFrameCaptured = false;
    private Scalar RED = new Scalar(255,0,0);
    private Scalar GREEN = new Scalar(0,255,0);

    public GameEngine(MediaPlayer mp){
        fDetect = FeatureDetector.create(FeatureDetector.ORB);
        fDescript = DescriptorExtractor.create(DescriptorExtractor.ORB);
        matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMINGLUT);//.FLANNBASED);
        img = new Mat();
        refImg = new Mat();
        keyPoints = new MatOfKeyPoint();
        refKeys = new MatOfKeyPoint();
        refDesc = new Mat();
        desc = new Mat();
        matches = new MatOfDMatch();
        this.mp = mp;
    }

    public void processFrame(Mat grayFrame, Mat rgbaFrame) {
        if (!gameFrameCaptured) {
            calcGameSpace(grayFrame, rgbaFrame);
        }else{
            getMatches(grayFrame, rgbaFrame);
        }
        renderFrame(rgbaFrame);
    }

    public void shoot(){
        points++; //when hit

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

    public void calcGameSpace(Mat grayFrame, Mat rgbaFrame) {
        // ikke vis med imgproc og bruk grayFrame... tenker eg


        Imgproc.cvtColor(rgbaFrame, refImg, Imgproc.COLOR_RGBA2RGB);
        fDetect.detect(refImg, refKeys);
        fDescript.compute (refImg, refKeys, refDesc);
        Features2d.drawKeypoints(refImg, refKeys, refImg, RED, 0);
        Imgproc.cvtColor(refImg, rgbaFrame, Imgproc.COLOR_RGB2RGBA);

        //save game space
        //refImg = rgbaFrame;
        //refKeys = keyPoints;
        //refDesc = desc;

    }

    public void getMatches(Mat grayFrame, Mat rgbaFrame){
        Imgproc.cvtColor(rgbaFrame, img, Imgproc.COLOR_RGBA2RGB);
        fDetect.detect(img, keyPoints);
        fDescript.compute(img, keyPoints, desc);

        matcher.match(desc, refDesc, matches);

        Features2d.drawKeypoints(img, refKeys, img, RED, 0);
        Features2d.drawKeypoints(img, keyPoints, img, GREEN, 0);

        //Features2d.drawMatches(img, keyPoints, refImg, refKeys, matches, img, GREEN, RED, new MatOfByte(), Features2d.NOT_DRAW_SINGLE_POINTS);
        //Bitmap imageMatched = Bitmap.createBitmap(img.cols(), img.rows(), Bitmap.Config.RGB_565);//need to save bitmap
        //Utils.matToBitmap(img, imageMatched);

        Imgproc.cvtColor(img, rgbaFrame, Imgproc.COLOR_RGB2RGBA);
        //Highgui.imwrite();
    }

    private void renderFrame(Mat rgbaFrame) {
        Imgproc.putText(rgbaFrame, "Points: "+points, new Point(rgbaFrame.cols() / 3 * 2, rgbaFrame.rows() * 0.1),
                Core.FONT_HERSHEY_SIMPLEX, 1.0, new Scalar(255, 255, 0));
    }

    public void setGameFrameCaptured(boolean gameFrameCaptured) {
        this.gameFrameCaptured = gameFrameCaptured;
    }

    public boolean getGameFrameCaptured(){
        return gameFrameCaptured;
    }
}
