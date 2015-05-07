package com.unik.arinvaders;

import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.text.style.AlignmentSpan;

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
import org.opencv.imgproc.Imgproc;
import org.opencv.features2d.Features2d;
import org.opencv.calib3d.Calib3d;
import org.opencv.video.Video;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Vector;

public class GameEngine {
    private int score = 0;
    private FeatureDetector detector;
    private DescriptorExtractor descriptor;
    private DescriptorMatcher matcher;
    private Mat refImg, refDesc, desc;
    private MatOfKeyPoint keys, refKeys;
    private MatOfPoint2f[] points;
    private MatOfDMatch matches;
    private MediaPlayer mp;
    private boolean gameFrameCaptured = false;
    private Scalar RED = new Scalar(255,0,0);
    private Scalar GREEN = new Scalar(0,255,0);
    //private byte matchNumb;
    private int matchNumb;
    private BackgroundCalculations calc;
    private boolean first;

    private TermCriteria termcrit;
    private Size subPixWinSize, winSize;
    private MatOfByte status;
    private MatOfFloat err;
    private Point[] tmpPoint;
    private int curPoint;
    private Mat preGray;


    public GameEngine(MediaPlayer mp){
        detector = FeatureDetector.create(FeatureDetector.ORB);
        descriptor = DescriptorExtractor.create(DescriptorExtractor.ORB);
        matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMINGLUT);
        //img = new Mat();
        refImg = new Mat();
        keys = new MatOfKeyPoint();
        points = new MatOfPoint2f[]{new MatOfPoint2f(), new MatOfPoint2f()};
        refKeys = new MatOfKeyPoint();
        refDesc = new Mat();
        desc = new Mat();
        matches = new MatOfDMatch();
        this.mp = mp;
        matchNumb = 0;//0b00000001;
        first = true;

        calc = new BackgroundCalculations();
        termcrit = new TermCriteria(TermCriteria.COUNT|TermCriteria.EPS,20,0.03);
        subPixWinSize = new Size(10,10);
        winSize = new Size(31,31);
        //status = new MatOfByte();
        //err = new MatOfFloat();
        preGray = new Mat();
        //tmpPoint = new Point()[];
        curPoint = 1;

        //USE
        //Imgproc.goodFeaturesToTrack();
        //Imgproc.cornerSubPix();
        //Video.calcOpticalFlowPyrLK();
    }

    /*
    public void processGameSpace(Mat grayFrame, Mat rgbaFrame){//}, Mat rgbaFrame){
        if(calc.getStatus() == AsyncTask.Status.FINISHED) {
            calc = new BackgroundCalculations();
            calc.execute(grayFrame, null);
        }
        String text = "Click screen to capture game space";
        Core.putText(rgbaFrame, text, new Point(rgbaFrame.cols() / 2 - text.length()/2, rgbaFrame.rows() / 2),
                Core.FONT_HERSHEY_SIMPLEX, 1.0, new Scalar(255, 255, 0));
        renderFrame(rgbaFrame);
    }*/

    public void processFrame(Mat grayFrame, Mat rgbaFrame, CameraBridgeViewBase cameraView) {
        //if (!gameFrameCaptured) {
        //    calcGameSpace(grayFrame, rgbaFrame);
            //calc.execute(grayFrame, null);
        //}else if (first || calc.getStatus() == AsyncTask.Status.FINISHED){//(matchNumb >= 10){//((int) matchNumb < (int) 0b00000010){
        //if (first || calc.getStatus() == AsyncTask.Status.FINISHED){//(matchNumb >= 10){//((int) matchNumb < (int) 0b00000010){
        /*
        if (calc == null || calc.getStatus() == AsyncTask.Status.FINISHED){//(matchNumb >= 10){//((int) matchNumb < (int) 0b00000010){
            //getMatches(grayFrame, rgbaFrame);
            first = false;
            calc = new BackgroundCalculations();
            calc.execute(grayFrame);//.clone());
            score++;
            //matchNumb = 0;
        }*/


        /*
        if (first || calc.getStatus() == AsyncTask.Status.FINISHED){
            first = false;
            calc = new BackgroundCalculations();
            calc.execute(grayFrame, rgbaFrame);
            //tmpPoint = points[0].toArray();
            score++;
        }

        if(tmpPoint != null) {
            for (Point point : tmpPoint) {
                Core.circle(rgbaFrame, point, 4, RED, -1, 8, 0);
            }
        }*/

        lkDemo(grayFrame,rgbaFrame);


        //if (tmpPoint.length < 200){
            //do some shit
            // match current points with refImg/refPoints
        //}

        //for (Point point : tmpPoint){
        //    Core.circle(rgbaFrame, point, 4, RED, -1, 8, 0);
        //}

        //for (Point point : points[0].toArray()){
        //    Core.circle(rgbaFrame, point, 4, GREEN, -1, 8, 0);
       // }
        //Features2d.drawKeypoints(grayFrame, refKeys, grayFrame, RED, 0); //ref
        //Features2d.drawKeypoints(grayFrame, keys, rgbaFrame, GREEN, 0); //cur
        //matchNumb++; //(byte) (matchNumb << 1);
        //points++;// = matchNumb;
        renderFrame(rgbaFrame);
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
            MatOfByte status = new MatOfByte();
            MatOfFloat err = new MatOfFloat();

            if(preGray.empty()) {
                grayFrame.copyTo(preGray);
            }

            Video.calcOpticalFlowPyrLK(preGray, grayFrame, points[0], points[1], status, err, winSize, 3, termcrit, 0, 0.001);

            tmpPoint = points[1].toArray();
            int k = 0;
            for(int i = 0; i < tmpPoint.length; i++ ){

                if( '0' == status.toArray()[i] ) {
                    continue;
                }

                tmpPoint[k++] = tmpPoint[i];
                Core.circle(rgbaFrame, tmpPoint[i], 4, RED, -1, 8, 0);
            }
            //RESIZE tmpPoint to k
            tmpPoint = Arrays.copyOfRange(tmpPoint, 0, k);
            points[1] = new MatOfPoint2f(tmpPoint);
        }

        //if (tmpPoint.length < 200){
            //do some shit
            // match current points with refImg/refPoints
        //}

        //swap(points[1], points[0]);
        MatOfPoint2f tmp = points[0];
        points[0] = points[1];
        points[1] = tmp;

        //swap(preGray, grayFrame);
        preGray = grayFrame.clone();
    }

    public void shoot(){
        score++; //when hit

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

    /*
    public void calcGameSpace(Mat grayFrame, Mat rgbaFrame) {
        // heller be bruker ta bildet, saa finn keys etc. under loading av spill

        detector.detect(grayFrame, refKeys);
        descriptor.compute(grayFrame, refKeys, refDesc);
        //fjern etterhvert
        //Features2d.drawKeypoints(grayFrame, refKeys, refImg, RED, 0);
        //Imgproc.cvtColor(refImg, rgbaFrame, Imgproc.COLOR_RGB2RGBA, 4);
    }*/

    private class BackgroundCalculations extends AsyncTask<Mat, Void, Void> {

        @Override
        protected Void doInBackground(Mat... mats) {
            /*if (mats.length == 1){
                calcMatches(mats[0]);
            }else{
                calcGameSpace(mats[0]);
            }*/
            //if (gameFrameCaptured) {
            //    calcMatches(mats[0]);
            //}else{
            //    calcGameSpace(mats[0]);
            //}
            lkDemo(mats[0], mats[1]);
            return null;
        }

        public void calcGameSpace(Mat grayFrame) {
            //detector.detect(grayFrame, refKeys);
            //descriptor.compute(grayFrame, refKeys, refDesc);
        }

        private void calcMatches(Mat grayFrame){
            //detector.detect(grayFrame, keys);
            //descriptor.compute(grayFrame, keys, desc);
            matcher.clear();
            matcher.match(desc, refDesc, matches);
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
                    points[0] = new MatOfPoint2f(Arrays.copyOfRange(tmpPoint, 0, k));
                }
            }

            //swap(points[1], points[0]);
            MatOfPoint2f tmp = points[0];
            points[0] = points[1];
            points[1] = tmp;

            //swap(preGray, grayFrame);
            preGray = grayFrame.clone();
        }
/*
        private void lkDemo(Mat grayFrame,Mat rgbaFrame){
            if(!gameFrameCaptured) {
                //refImg = grayFrame.clone();

                MatOfPoint tmp = new MatOfPoint(points[1].toArray());
                Imgproc.goodFeaturesToTrack(grayFrame, tmp, 500, 0.01, 10, new Mat(), 3, false, 0.4);
                points[1] = new MatOfPoint2f(tmp.toArray());
                Imgproc.cornerSubPix(grayFrame, points[curPoint], subPixWinSize, new Size(-1, -1), termcrit);
            }else if (!points[0].empty()) {
                Video.calcOpticalFlowPyrLK(refImg, grayFrame, points[1], points[0], status, err, winSize, 3, termcrit, 0, 0.001);
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

        */
/*
        private void swap(Object obj1, Object obj2){
            Object tmp = obj1;
            obj1 = obj2;
            obj2 = tmp;
        }*/
    }


/*
    public void getMatches(Mat grayFrame, Mat rgbaFrame){
        detector.detect(grayFrame, keys);
        descriptor.compute(grayFrame, keys, desc);
        matcher.clear();
        matcher.match(desc, refDesc, matches);
        //List<MatOfDMatch> tmpMatches;
        //matcher.knnMatch(desc, refDesc, tmpMatches, 10);
        //matcher.radiusMatch(desc, refDesc, tmpMatches, 10);

        Mat tmp = new Mat();

        //fjern etterhvert
        //Features2d.drawKeypoints(grayFrame, refKeys, grayFrame, RED, 0);
        //Features2d.drawKeypoints(grayFrame, keys, rgbaFrame, GREEN, 0);
        //Features2d.drawMatches(grayFrame, keys, refImg, refKeys, matches, tmp, GREEN, RED, new MatOfByte(), Features2d.NOT_DRAW_SINGLE_POINTS);
        //Imgproc.resize(tmp,tmp,rgbaFrame.size());
        //Imgproc.cvtColor(tmp, rgbaFrame, Imgproc.COLOR_RGB2RGBA, 4);

        //List<DMatch> listOfMatches = matches.toList();
        //listOfMatches.size();
        //Calib3d.findHomography();
    }
    */

    private void renderFrame(Mat rgbaFrame) {
        Core.putText(rgbaFrame, "Points: " + score, new Point(rgbaFrame.cols() / 3 * 2, rgbaFrame.rows() * 0.1),
                Core.FONT_HERSHEY_SIMPLEX, 1.0, new Scalar(255, 255, 0));
        //Imgproc.putText(rgbaFrame, "Points: "+points, new Point(rgbaFrame.cols() / 3 * 2, rgbaFrame.rows() * 0.1),
        //        Core.FONT_HERSHEY_SIMPLEX, 1.0, new Scalar(255, 255, 0));

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

