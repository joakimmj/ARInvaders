package com.unik.arinvaders;

import android.media.MediaPlayer;

import org.opencv.core.Core;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfFloat;
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

public class GameEngine {
    private int score = 0, level = 1;
    private Scalar RED = new Scalar(255,0,0);
    private Scalar GREEN = new Scalar(0,255,0);
    private Scalar BLUE = new Scalar(0,128,255);
    private Scalar BLACK = new Scalar(0,0,0);
    private Scalar WHITE = new Scalar(255,255,255);
    private TermCriteria criteria;
    private Mat preGray;
    private MatOfPoint2f[] points;
    private int targetIdx;
    private int shots;
    private ArrayList<Point> bullets;
    private MediaPlayer mpExplosion;

    private Target t;

    //TEST
    private FeatureCalculator fc;

    public GameEngine(MediaPlayer mpExplosion){
        t = new Target(10000);

        fc = new FeatureCalculator(FeatureDetector.ORB, DescriptorExtractor.ORB, DescriptorMatcher.BRUTEFORCE_HAMMING, 100);
        this.mpExplosion = mpExplosion;
        //calc = new BackgroundCalculations();
        targetIdx = 0;
        bullets = new ArrayList<>();
        shots = 0;
        criteria = new TermCriteria(TermCriteria.COUNT|TermCriteria.EPS,20,0.03);
        preGray = new Mat();
        points = new MatOfPoint2f[]{new MatOfPoint2f(), new MatOfPoint2f()};
    }

    private static double calcDist(Point a, Point b){
        /*
        double deltaXY = Math.abs(Math.sqrt(Math.pow(a.x-b.x, 2) + Math.pow(a.y-b.y)));
        double deltaYZ = Math.abs(Math.sqrt(Math.pow(b.x-c.x, 2) + Math.pow(b.y-c.y)));
        double deltaZX = Math.abs(Math.sqrt(Math.pow(c.x-a.x, 2) + Math.pow(c.y-a.y)));
        return (deltaXY+deltaYZ+deltaZY)/3;
         */
        return Math.abs(Math.sqrt(Math.pow(a.x-b.x,2) + Math.pow(a.y-b.y,2)));
    }

    public void processFrame(Mat grayFrame, Mat rgbaFrame) {
        //Mat main = rgbaFrame.clone();

        //make bullets
        while(shots > 0) {
            bullets.add(new Point(rgbaFrame.cols() / 2, rgbaFrame.rows()));
            shots--;
        }

        //calculate features
        if(points[1].toArray().length < 20){
            calcFeatures(grayFrame, points[1]);
            t.resetTimer();
        }else if (!points[0].empty()) {
            calcFlow(grayFrame, preGray, points[1], points[0]);
        }

        MatOfPoint2f tmp = points[0];
        points[0] = points[1];
        points[1] = tmp;
        preGray = grayFrame.clone();

        //if features captured
        if(!points[0].empty()) {
            Point[] tmpPoint = points[0].toArray();

            //compute new target
            if (t != null && t.getTimer() < System.currentTimeMillis()) {
                t.create(tmpPoint);
            }

            //draw target
            if(t.getPoint() != null) {
                t.draw(rgbaFrame, tmpPoint, 20);
                drawBullets(bullets, t.getPoint(), t.getRadius(), rgbaFrame, new Scalar(249,50,253), mpExplosion);
            }else{
                t.resetTimer();
            }

            Core.putText(rgbaFrame, "Features: " + tmpPoint.length, new Point(90, 90),
                    Core.FONT_HERSHEY_SIMPLEX, 1.0, new Scalar(255,0,0), 2);
        }
        updateLevel();
        renderFrame(rgbaFrame, level, score);

        //Core.addWeighted(rgbaFrame, 0.8, main, 0.2, 0.0, rgbaFrame);
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
        for(int i = 0; i < tmpPoint.length; i++ ){

            if( 0 == status.toArray()[i] ) {
                if(t.getIdx() > i){
                    t.update();
                }
                continue;
            }
            tmpPoint[k++] = tmpPoint[i];
        }
        tmpPoint = Arrays.copyOf(tmpPoint, k);
        keys.fromArray(tmpPoint);
    }

    private void updateLevel(){
        if(score >= level*30){
            level++;
            t.decrTimer(500);
        }
    }

    private static void playSound(MediaPlayer mp){
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

    public void shoot(MediaPlayer mpLaser){
        playSound(mpLaser);
        shots++;
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

    private static boolean hitTarget(Point pt, Point target, double dist){
        if (target == null) return false;

        boolean x = pt.x - dist < target.x && target.x < pt.x + dist;
        boolean y = pt.y - dist < target.y && target.y < pt.y + dist;
        return x && y;
    }

    private void drawHit(Mat frame, Point pt, MediaPlayer mp, double size, Scalar color, int inc){
        playSound(mp);
        Core.circle(frame, pt, (int) size, new Scalar(255, 255, 0), -1, 8, 0);
        t.resetTimer();
        score+=inc;
    }

    private void drawBullets(ArrayList<Point> bullets, Point target, double targetRad, Mat frame, Scalar color, MediaPlayer mp) {
        ArrayList<Point> rmBullets = new ArrayList<>();
        Point center = new Point(frame.cols() / 2, frame.rows() / 2);

        for(Point p : bullets){
            p.y -= 40;

            if (p.y < center.y){
                if(hitTarget(center, target, targetRad/3)) {
                    drawHit(frame, target, mp, targetRad+10, color, 5);
                }else if(hitTarget(center, target, targetRad * 2 / 3)) {
                    drawHit(frame, target, mp, targetRad+10, color, 3);
                }else if(hitTarget(center, target, targetRad)) {
                    drawHit(frame, target, mp, targetRad+10, color, 1);
                }
                rmBullets.add(p);
            }else Core.circle(frame, p, 8 + (int) ((p.y - center.y) / 10), color, -1, 8, 0);
        }
        bullets.removeAll(rmBullets);
    }

    private static void renderFrame(Mat rgbaFrame, int level, int score) {
        Core.rectangle(rgbaFrame, new Point(0, 0), new Point(400, 50), new Scalar(255, 255, 255), -1, 8, 0);
        Core.putText(rgbaFrame, "Level: " + level + " Points: " + score, new Point(30, 30),
                Core.FONT_HERSHEY_SIMPLEX, 1.0, new Scalar(255, 0, 0), 2);
    }
}

