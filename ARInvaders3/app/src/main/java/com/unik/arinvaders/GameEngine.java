package com.unik.arinvaders;

import android.media.MediaPlayer;
import android.os.AsyncTask;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.core.Core;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.core.TermCriteria;
import org.opencv.features2d.DMatch;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.core.Mat;
import org.opencv.features2d.Features2d;
import org.opencv.features2d.KeyPoint;
import org.opencv.imgproc.Imgproc;
import org.opencv.video.Video;

import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

public class GameEngine {
    private int score = 0, level = 1;
    private FeatureDetector detector;
    private DescriptorExtractor descriptor;
    private DescriptorMatcher matcher;
    private Mat[] refImg, refDesc, desc = {new Mat(), new Mat()};
    private Mat preFrame;
    private MatOfKeyPoint[] keys = {new MatOfKeyPoint(), new MatOfKeyPoint()}, refKeys;
    private MatOfDMatch matches;
    //private MediaPlayer mpLaser;
    private boolean gameFrameCaptured = false;
    private Scalar RED = new Scalar(255,0,0);
    private Scalar GREEN = new Scalar(0,255,0);
    private int matchNumb;
    //private BackgroundCalculations calc;
    private boolean first;
    private Random randPoint;
    private boolean tmpX, tmpY;

    //LK
    private TermCriteria termcrit;
    private Size subPixWinSize, winSize;
    private MatOfByte status;
    private MatOfFloat err;
    private Point[] tmpPoint;
    private int curPoint;
    private Mat preGray;
    private MatOfPoint2f[] points;
    private Long targetTimer;
    private int targetIdx;
    private Point target;
    private int targetDelay;

    //tmp
    private float numbFeatures;
    private float numbInits;

    //TEST
    private FeatureCalculator fc;

    public GameEngine(){
        //this.mpLaser = mpLaser;
        fc = new FeatureCalculator(FeatureDetector.ORB, DescriptorExtractor.ORB, DescriptorMatcher.BRUTEFORCE_HAMMING, 100);

        detector = FeatureDetector.create(FeatureDetector.ORB);
        descriptor = DescriptorExtractor.create(DescriptorExtractor.ORB);
        matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);
        //refImg = new Mat();
        //keys = new MatOfKeyPoint();
        //refKeys = new MatOfKeyPoint();
        //desc = new Mat();
        //refDesc = new Mat();
        matches = new MatOfDMatch();
        matchNumb = 0;//0b00000001;
        first = true;
        //calc = new BackgroundCalculations();
        randPoint = new Random();
        targetTimer = System.currentTimeMillis();
        targetDelay = 10000;
        targetIdx = 0;
        tmpX = false;
        tmpY = false;
        preFrame = new Mat();

        //LK
        termcrit = new TermCriteria(TermCriteria.COUNT|TermCriteria.EPS,20,0.03);
        subPixWinSize = new Size(10,10);
        winSize = new Size(31,31);
        preGray = new Mat();
        curPoint = 1;
        points = new MatOfPoint2f[]{new MatOfPoint2f(), new MatOfPoint2f()};

        //tmp
        numbFeatures = 0;
        numbInits = 0;

        //USE
        //Imgproc.goodFeaturesToTrack();
        //Imgproc.cornerSubPix();
        //Video.calcOpticalFlowPyrLK();
    }

    public void processFrame(Mat grayFrame, Mat rgbaFrame) {

        /*
        if(!gameFrameCaptured){
            fc.getFeatures(grayFrame, desc[1], keys[1]);

        }else if (!desc[0].empty()) {*/
        fc.getMatches(grayFrame, desc[1], keys[1], desc[0], matches);

        /*
        if(!matches.empty()) {
            DMatch[] matchesArr = matches.toArray();
            int seed = (matchesArr.length >= 10) ? 10 : matchesArr.length;
            if (seed > 0) {
                targetIdx = randPoint.nextInt(seed);
                Point tmpPt = new Point(keys[1].toArray()[matchesArr[targetIdx].imgIdx].pt.x*2, keys[1].toArray()[matchesArr[targetIdx].imgIdx].pt.y*2);
                drawTarget(rgbaFrame, tmpPt);
            }
        }*/
        //}


        for(KeyPoint key : keys[1].toArray()){
            Point tmpPt = new Point(key.pt.x*2, key.pt.y*2);
            Core.circle(rgbaFrame, tmpPt, 4, RED, -1, 8, 0);
        }

        //if(gameFrameCaptured) {
        //swap(points[1], points[0]);
        MatOfKeyPoint tmpKeys = keys[0];
        keys[0] = keys[1];
        keys[1] = tmpKeys;

        Mat tmpDesc = desc[0];
        desc[0] = desc[1];
        desc[1] = tmpDesc;
        //}


        /*
        KeyPoint[] tmpKeys = keys.toArray();

        for(KeyPoint key : tmpKeys){
            key.pt.x = key.pt.x*2;
            key.pt.y = key.pt.y*2;
        }
        keys.fromArray(tmpKeys);

        Features2d.drawKeypoints(grayFrame, keys, rgbaFrame, RED, Features2d.DRAW_RICH_KEYPOINTS);
*/
        if(!keys[1].empty()){
            //numbFeatures = keys[1].toArray()[0].response;
            //numbInits = keys.toArray()[keys.toArray().length-1].response;
            numbInits = keys[1].toArray().length;

            if(!matches.empty()){
                numbFeatures = matches.toArray().length;
            }
            /*if(!keys[0].empty()) {
                //fjern etterhvert
                Mat tmp = new Mat();
                Features2d.drawMatches(grayFrame, keys[0], preFrame, keys[1], matches, tmp, GREEN, RED, new MatOfByte(), Features2d.NOT_DRAW_SINGLE_POINTS);
                Imgproc.resize(tmp, tmp, rgbaFrame.size());
                Imgproc.cvtColor(tmp, rgbaFrame, Imgproc.COLOR_RGB2RGBA, 4);
            }*/
        }

        preFrame = grayFrame.clone();

        //processLK(grayFrame, rgbaFrame);
        updateLevel();
        renderFrame(rgbaFrame);
    }

    private void drawTarget(Mat frame, Point center){
        Core.circle(frame, center, 4, RED, -1, 8, 0);
        Core.circle(frame, center, 16, RED, 4, 8, 0);
        Core.circle(frame, center, 31, RED, 4, 8, 0);
    }

    private void updateLevel(){
        if(score >= level*10){
            level++;
            targetDelay -= 500;
        }
    }

    private void computeTarget(boolean newTarget){
        if(tmpPoint != null) {
            if (newTarget && tmpPoint.length > 0) {
                int seed = (tmpPoint.length >= 10) ? 10 : tmpPoint.length;
                targetIdx = randPoint.nextInt(seed);
                target = tmpPoint[targetIdx];
                targetTimer = System.currentTimeMillis();
            } else if (tmpPoint.length <= 0) {
                target = null;
            }
        }
    }

    private void processLK(Mat grayFrame, Mat rgbaFrame){
        lkDemo(grayFrame,rgbaFrame);

        /*
        if(tmpPoint != null) {//target != null) {
            for (Point point : tmpPoint) {
                Core.circle(rgbaFrame, point, 4, RED, -1, 8, 0);
            }
        }*/
        computeTarget(targetTimer + targetDelay < System.currentTimeMillis());

        if(target != null){
            tmpX = rgbaFrame.cols() / 2 - 10 < target.x && target.x < rgbaFrame.cols() / 2 + 10;
            tmpY = rgbaFrame.rows() / 2 - 10 < target.y && target.y < rgbaFrame.rows() / 2 + 10;

            Core.circle(rgbaFrame, target, 4, RED, -1, 8, 0);
            Core.circle(rgbaFrame, target, 16, RED, 4, 8, 0);
            Core.circle(rgbaFrame, target, 31, RED, 4, 8, 0);
        }
    }

    private void lkDemo(Mat grayFrame,Mat rgbaFrame){
        if(!gameFrameCaptured || points[1].toArray().length < 20){//(tmpPoint != null && tmpPoint.length < 20)) {
            //refImg = grayFrame.clone();

            MatOfPoint tmp = new MatOfPoint(points[1].toArray());
            Imgproc.goodFeaturesToTrack(grayFrame, tmp, 500, 0.01, 10, new Mat(), 3, false, 0.4);
            points[1] = new MatOfPoint2f(tmp.toArray());
            if(!points[1].empty()) {
                Imgproc.cornerSubPix(grayFrame, points[1], subPixWinSize, new Size(-1, -1), termcrit);
            }
            numbInits++;
            //if(tmpPoint != null){
            computeTarget(true);
            //}
        }else if (!points[0].empty()) {
            MatOfByte status = new MatOfByte();
            MatOfFloat err = new MatOfFloat();

            if(preGray.empty()) {
                grayFrame.copyTo(preGray);
            }

            Video.calcOpticalFlowPyrLK(preGray, grayFrame, points[0], points[1], status, err, winSize, 3, termcrit, 0, 0.001);

            tmpPoint = points[1].toArray();
            int k = 0;
            for(int i = 0; i < tmpPoint.length; i++ ){


                if( 0 == status.toArray()[i] ) {
                    if(targetIdx >= i){
                        targetIdx--;
                    }
                    continue;
                }

                tmpPoint[k++] = tmpPoint[i];
            }

            //RESIZE tmpPoint to k
            numbFeatures = k;

            tmpPoint = Arrays.copyOf(tmpPoint, k);
            points[1] = new MatOfPoint2f(tmpPoint);

            if(targetIdx >= 0 && targetIdx < tmpPoint.length) {
                target = tmpPoint[targetIdx];
            }//else{
            //    target = null;
            //}
        }

        if(gameFrameCaptured) {
            //swap(points[1], points[0]);
            MatOfPoint2f tmp = points[0];
            points[0] = points[1];
            points[1] = tmp;

            //swap(preGray, grayFrame);
            preGray = grayFrame.clone();
        }
    }

    private void playSound(MediaPlayer mp){
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

    public void shoot(MediaPlayer mpLaser, MediaPlayer mpExplosion){
        playSound(mpLaser);

        /*
        if(tmpX && tmpY){
            playSound(mpExplosion);
            score++;
            computeTarget(true);
        }*/
    }

    /*
    private class BackgroundCalculations extends AsyncTask<Mat, Void, Void> {

        @Override
        protected Void doInBackground(Mat... mats) {
            lkDemo(mats[0], mats[1]);
            return null;
        }

        private void lkDemo(Mat grayFrame,Mat rgbaFrame){
            if(!gameFrameCaptured) {
                //refImg = grayFrame.clone();

                MatOfPoint tmp = new MatOfPoint(points[1].toArray());
                Imgproc.goodFeaturesToTrack(grayFrame, tmp, 500, 0.01, 10, new Mat(), 3, false, 0.4);
                points[1] = new MatOfPoint2f(tmp.toArray());
                if(!points[1].empty()){
                    Imgproc.cornerSubPix(grayFrame, points[1], subPixWinSize, new Size(-1, -1), termcrit);
                }
            }else if (!points[0].empty()) {
                status = new MatOfByte();
                err = new MatOfFloat();

                if(preGray.empty()) {
                    grayFrame.copyTo(preGray);
                }

                Video.calcOpticalFlowPyrLK(preGray, grayFrame, points[0], points[1], status, err, winSize, 3, termcrit, 0, 0.001);

                tmpPoint = points[0].toArray();
                int k = 0;
                for (int i = 0; i < tmpPoint.length; i++) {

                    if (status.toArray()[i] == '0') {
                        continue;
                    }

                    tmpPoint[k++] = tmpPoint[i];
                }
                tmpPoint = Arrays.copyOfRange(tmpPoint, 0, k);
                points[0] = new MatOfPoint2f(tmpPoint);
            }

            if(gameFrameCaptured) {
                //swap(points[1], points[0]);
                MatOfPoint2f tmp = points[0];
                points[0] = points[1];
                points[1] = tmp;

                //swap(preGray, grayFrame);
                preGray = grayFrame.clone();
            }
        }
    }*/

    private void renderFrame(Mat rgbaFrame) {
        Core.putText(rgbaFrame, "Points: " + score, new Point(rgbaFrame.cols() / 3 * 2, rgbaFrame.rows() * 0.1),
                Core.FONT_HERSHEY_SIMPLEX, 1.0, RED, 2);

        Core.putText(rgbaFrame, "Level: " + level, new Point(30, rgbaFrame.rows() * 0.1),
                Core.FONT_HERSHEY_SIMPLEX, 1.0, RED, 2);


        Core.putText(rgbaFrame, "Inits: " + numbInits + " Features: " + numbFeatures, new Point(30, rgbaFrame.rows() - 30),
                Core.FONT_HERSHEY_SIMPLEX, 1.0, RED, 2);

        if(!gameFrameCaptured){
            String text = "Click screen to capture game space";
            Core.putText(rgbaFrame, text, new Point(rgbaFrame.cols() / 2 - text.length()/2, rgbaFrame.rows() / 2),
                    Core.FONT_HERSHEY_SIMPLEX, 1.0, new Scalar(255, 255, 0));
        }
    }

    public void setGameFrameCaptured(boolean gameFrameCaptured) {
        this.gameFrameCaptured = gameFrameCaptured;
    }

    public boolean getGameFrameCaptured(){
        return gameFrameCaptured;
    }
}

