package com.unik.arinvaders;

import android.media.MediaPlayer;

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
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;
import org.opencv.video.Video;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
    private Scalar BLUE = new Scalar(0,0,255);
    private Scalar BLACK = new Scalar(0,0,0);
    private Scalar WHITE = new Scalar(255,255,255);
    private int matchNumb;
    //private BackgroundCalculations calc;
    private boolean first;
    private Random randPoint;
    private boolean tmpX, tmpY;
    private double deltaDist;

    //LK
    private TermCriteria criteria;
    //private Size winSize;
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
    private double  preDist, curDist;

    //tmp
    private float numbFeatures;
    private float numbInit;
    double dd;

    //TEST
    private FeatureCalculator fc;

    public GameEngine(){
        fc = new FeatureCalculator(FeatureDetector.ORB, DescriptorExtractor.ORB, DescriptorMatcher.BRUTEFORCE_HAMMING, 100);

        detector = FeatureDetector.create(FeatureDetector.ORB);
        descriptor = DescriptorExtractor.create(DescriptorExtractor.ORB);
        matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);
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
        deltaDist = 1;
        preDist = 1;
        curDist = 1;
        dd = 1;

        //LK
        criteria = new TermCriteria(TermCriteria.COUNT|TermCriteria.EPS,20,0.03);


        preGray = new Mat();
        curPoint = 1;
        points = new MatOfPoint2f[]{new MatOfPoint2f(), new MatOfPoint2f()};

        //tmp
        numbFeatures = 0;
        numbInit = 0;
    }

    private double calcPointDinstances(Point[] kCenteredPoints, Point center) {
        double pointDistance = 0; // Opp i global

        for( Point p : kCenteredPoints ) {
            pointDistance += calcDist(center, p);
        }

        return pointDistance;

    }

    private double calcDist(Point a, Point b){
        return Math.abs(Math.sqrt(Math.pow(a.x-b.x,2) + Math.pow(a.y-b.y,2)));
    }

    private List<Point> findCenteredPoints (Point[] allPoints, double maxDist, Point center) {
        List<Point> centeredPoints = new ArrayList<>();

        for(Point p : allPoints) {
            if(calcDist(center, p) < maxDist)
                centeredPoints.add(p);
        }

        return centeredPoints;
    }

    public void processFrame(Mat grayFrame, Mat rgbaFrame) {
        //if(!gameFrameCaptured || points[1].toArray().length < 20){
        if(points[1].toArray().length < 20){
            calcFeatures(grayFrame, points[1]);
            numbInit++;
            //computeTarget(true);
        }else if (!points[0].empty()) {
            calcFlow(grayFrame, preGray, points[1], points[0]);

            //if(targetIdx >= 0 && targetIdx < tmpPoint.length) {
            //    target = tmpPoint[targetIdx];
            //}
        }

        MatOfPoint2f tmp = points[0];
        points[0] = points[1];
        points[1] = tmp;
        preGray = grayFrame.clone();

        if(!points[0].empty()) {
            Point[] tmpPoint = points[0].toArray();
            numbFeatures = tmpPoint.length;
            if (targetTimer + targetDelay < System.currentTimeMillis()) {
                computeTarget(tmpPoint);
                targetTimer = System.currentTimeMillis();
            }
            if(targetIdx < tmpPoint.length) {
                Point target = tmpPoint[targetIdx];
                Point center = new Point(rgbaFrame.cols()/2,rgbaFrame.rows()/2);
                if(tmpPoint.length > 1) curDist = calcDist(tmpPoint[0], tmpPoint[1]);
                dd = curDist/preDist;
                if(dd > 1.01){
                    deltaDist += dd;
                }else if(dd < 0.99){
                    deltaDist -= dd;
                }
                //deltaDist = dd;// + ((int) (curDist-preDist))/10;
                //preDist = curDist;
                drawTarget(rgbaFrame, target, deltaDist);
                tmpX = center.x - 10 < target.x && target.x < center.x + 10;
                tmpY = center.y - 10 < target.y && target.y < center.y + 10;
            }else{
                targetTimer-=targetDelay;
            }
        }

        /*
        if(!gameFrameCaptured){
            fc.getFeatures(grayFrame, desc[1], keys[1]);

        }else if (!desc[0].empty()) {*//*
        fc.getMatches(grayFrame, desc[1], keys[1], desc[0], matches);


        if(!matches.empty() && !keys[1].empty()) {
            DMatch[] matchesArr = matches.toArray();
            KeyPoint[] tmpKey = keys[1].toArray();
            numbInit = matchesArr.length;
            numbFeatures = tmpKey.length;

            int seed = (matchesArr.length >= 10) ? 10 : matchesArr.length;
            if (seed > 0) {
                if( targetTimer + targetDelay < System.currentTimeMillis()) {
                    targetIdx = randPoint.nextInt(seed);
                }
                Point tmpPt = new Point(tmpKey[matchesArr[targetIdx].imgIdx].pt.x*2, tmpKey[matchesArr[targetIdx].imgIdx].pt.y*2);
                drawTarget(rgbaFrame, tmpPt);
                targetTimer = System.currentTimeMillis();
            }

            for(DMatch dm : matchesArr ){
                Core.circle(rgbaFrame, new Point(tmpKey[dm.queryIdx].pt.x*2,tmpKey[dm.queryIdx].pt.y*2), 4, RED, -1, 8, 0);
            }
        }
        //}

        *//*
        for(KeyPoint key : keys[1].toArray()){
            Point tmpPt = new Point(key.pt.x*2, key.pt.y*2);
            Core.circle(rgbaFrame, tmpPt, 4, RED, -1, 8, 0);
        }*//*

        //if(gameFrameCaptured) {
        //swap(points[1], points[0]);
        MatOfKeyPoint tmpKeys = keys[0];
        keys[0] = keys[1];
        keys[1] = tmpKeys;

        Mat tmpDesc = desc[0];
        desc[0] = desc[1];
        desc[1] = tmpDesc;
        //}


        *//*
        KeyPoint[] tmpKeys = keys.toArray();

        for(KeyPoint key : tmpKeys){
            key.pt.x = key.pt.x*2;
            key.pt.y = key.pt.y*2;
        }
        keys.fromArray(tmpKeys);

        Features2d.drawKeypoints(grayFrame, keys, rgbaFrame, RED, Features2d.DRAW_RICH_KEYPOINTS);
*//*
        //if(!keys[1].empty()){
            //numbFeatures = keys[1].toArray()[0].response;
            //numbInit = keys.toArray()[keys.toArray().length-1].response;
            //numbFeatures = keys[1].toArray().length;

            //if(!matches.empty()){
            //    numbInit = matches.toArray().length;
            //}
            *//*if(!keys[0].empty()) {
                //fjern etterhvert
                Mat tmp = new Mat();
                Features2d.drawMatches(grayFrame, keys[0], preFrame, keys[1], matches, tmp, GREEN, RED, new MatOfByte(), Features2d.NOT_DRAW_SINGLE_POINTS);
                Imgproc.resize(tmp, tmp, rgbaFrame.size());
                Imgproc.cvtColor(tmp, rgbaFrame, Imgproc.COLOR_RGB2RGBA, 4);
            }*//*
        //}

        preFrame = grayFrame.clone();*/

        //processLK(grayFrame, rgbaFrame);
        updateLevel();
        renderFrame(rgbaFrame);
        preDist = curDist;
    }

    private void drawTarget(Mat frame, Point center, double deltaDist){
        if((int)(deltaDist*4) == 0) deltaDist = 0.25;
        Core.circle(frame, center, (int)(deltaDist*32), WHITE, -1, 8, 0);
        Core.circle(frame, center, (int)(deltaDist*4), RED, -1, 8, 0);
        Core.circle(frame, center, (int)(deltaDist*16), RED, 8, 8, 0);
        Core.circle(frame, center, (int)(deltaDist*32), RED, 8, 8, 0);
    }

    private void calcFeatures(Mat frame, MatOfPoint2f keys){
        MatOfPoint tmpKeys = new MatOfPoint(keys.toArray());
        Imgproc.goodFeaturesToTrack(frame, tmpKeys, 500, 0.01, 10, new Mat(), 3, false, 0.4);
        keys.fromArray(tmpKeys.toArray());

        Size winSize = new Size(10,10);
        Size zeroSone = new Size(-1, -1);

        if(!keys.empty()) {
            Imgproc.cornerSubPix(frame, keys, winSize, zeroSone, criteria);
        }
    }

    private void calcFlow(Mat frame, Mat preFrame, MatOfPoint2f keys, MatOfPoint2f preKeys){
        MatOfByte status = new MatOfByte();
        MatOfFloat err = new MatOfFloat();
        Size winSize = new Size(31,31);
        int maxLevel = 3;
        double minEigThreshold = 0.001;

        if(preGray.empty()) {
            frame.copyTo(preGray);
        }

        //Video.OPTFLOW_LK_GET_MIN_EIGENVALS eller 0? test begge
        Video.calcOpticalFlowPyrLK(preFrame, frame, preKeys, keys, status, err, winSize, maxLevel, criteria, 0, minEigThreshold);

        Point[] tmpPoint = keys.toArray();
        int k = 0;
        boolean recalcDist = false;
        for(int i = 0; i < tmpPoint.length; i++ ){

            if( 0 == status.toArray()[i] ) {
                if(targetIdx > i){
                    targetIdx--;
                }
                if(i == 0 || i == 1){
                    recalcDist = true;
                }
                continue;
            }

            tmpPoint[k++] = tmpPoint[i];
        }
        if(recalcDist) preDist = calcDist(tmpPoint[0], tmpPoint[1]);
        tmpPoint = Arrays.copyOf(tmpPoint, k);
        keys.fromArray(tmpPoint);
    }

    private void updateLevel(){
        if(score >= level*10){
            level++;
            targetDelay -= 500;
        }
    }

    private void computeTarget(Point[] keys){
        if(keys != null && keys.length > 0){
            int seed = (keys.length > 10)? 10 : keys.length;
            targetIdx = randPoint.nextInt(seed);
            target = keys[targetIdx];
            deltaDist = 1;
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


        if(tmpX && tmpY){
            playSound(mpExplosion);
            score++;
            //computeTarget(true);
        }
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
        Core.rectangle(rgbaFrame, new Point(0,0), new Point(400,50), WHITE, -1, 8, 0);
        Core.putText(rgbaFrame, "Level: " + level + " Points: " + score, new Point(30, 30),
                Core.FONT_HERSHEY_SIMPLEX, 1.0, RED, 2);

        Core.putText(rgbaFrame, " Reload: " + numbInit + " Features: " + numbFeatures + " curDist: " + curDist + " preDist: " + preDist + " deltaDist: " + dd, new Point(30, rgbaFrame.rows() - 30),
                Core.FONT_HERSHEY_SIMPLEX, 1.0, RED, 2);
    }

    public void setGameFrameCaptured(boolean gameFrameCaptured) {
        this.gameFrameCaptured = gameFrameCaptured;
    }

    public boolean getGameFrameCaptured(){
        return gameFrameCaptured;
    }
}

