package com.unik.arinvaders;

import android.media.MediaPlayer;

import org.opencv.calib3d.Calib3d;
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
    private boolean tmpX, tmpY, shot;
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
    private Mat H;
    private int closestIdx, shots;
    private ArrayList<Point> bullets;
    private MediaPlayer mpExplosion;

    //tmp
    private float numbFeatures;
    private float numbInit;
    double dd;

    //TEST
    private FeatureCalculator fc;

    public GameEngine(MediaPlayer mpExplosion){
        fc = new FeatureCalculator(FeatureDetector.ORB, DescriptorExtractor.ORB, DescriptorMatcher.BRUTEFORCE_HAMMING, 100);
        this.mpExplosion = mpExplosion;
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
        deltaDist = 36;
        preDist = 1;
        curDist = 1;
        dd = 1;
        H = new Mat();
        closestIdx = 0;
        shot = false;
        bullets = new ArrayList<>();
        shots = 0;

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
        /*
        double deltaXY = Math.abs(Math.sqrt(Math.pow(a.x-b.x, 2) + Math.pow(a.y-b.y)));
        double deltaYZ = Math.abs(Math.sqrt(Math.pow(b.x-c.x, 2) + Math.pow(b.y-c.y)));
        double deltaZX = Math.abs(Math.sqrt(Math.pow(c.x-a.x, 2) + Math.pow(c.y-a.y)));
        return (deltaXY+deltaYZ+deltaZY)/3;
         */
        return Math.abs(Math.sqrt(Math.pow(a.x-b.x,2) + Math.pow(a.y-b.y,2)));
    }

    private void calcHomography(MatOfPoint2f src, MatOfPoint2f dest) {
        //funker best paa flater....
        Point[] tmpSrc = src.toArray(), tmpDest = dest.toArray();
        if(tmpSrc.length > tmpDest.length){
            tmpSrc = Arrays.copyOf(tmpSrc, tmpDest.length);
        }else if(tmpSrc.length < tmpDest.length){
            tmpDest = Arrays.copyOf(tmpDest, tmpSrc.length);
        }
        H = Calib3d.findHomography(new MatOfPoint2f(tmpDest), new MatOfPoint2f(tmpSrc));
//        int npoints = src.toArray().length;
//        if(npoints >= 0 && dest.checkVector(2) == npoints && src.type() == dest.type()) {//dest.checkVector(2) )
//            //H = Calib3d.findHomography(source, dest, Calib3d.RANSAC, 3);
//            H = Calib3d.findHomography(src, dest);
//        }
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
        while(shots > 0) {
            bullets.add(new Point(rgbaFrame.cols() / 2, rgbaFrame.rows()));
            shots--;
        }
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
            //if(!points[1].empty()) calcHomography(points[0], points[1]);
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

                int tmpIdx = distClosest(target, tmpPoint, 20);
                closestIdx = (tmpIdx == -1)? closestIdx : tmpIdx;
                deltaDist = calcDist(target, tmpPoint[closestIdx]);
                tmpX = center.x - deltaDist < target.x && target.x < center.x + deltaDist;
                tmpY = center.y - deltaDist < target.y && target.y < center.y + deltaDist;
                drawTarget(rgbaFrame, target, tmpPoint);
                //Core.circle(rgbaFrame, target, (int)deltaDist, RED, -1, 8, 0);
//                if(tmpPoint.length > 1) curDist = calcDist(tmpPoint[0], tmpPoint[1]);
//                dd = curDist/preDist;
//                if(dd > 1.01){
//                    deltaDist += dd;
//                }else if(dd < 0.99){
//                    deltaDist -= dd;
//                }
                //deltaDist = dd;// + ((int) (curDist-preDist))/10;
                //preDist = curDist;



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
        shot = false;
    }

    private int distClosest(Point pt, Point[] points, int sub){
        int idx = 0;
        double dist = Float.MAX_VALUE;

        sub = (sub > points.length)? points.length : sub;

        for(int i = 0; i<sub; i++){
            double tmpDist = calcDist(points[i], pt);
            if(tmpDist < dist && points[i] != pt){
                dist = tmpDist;
                idx = i;
            }
        }
        return idx;
    }

    private void drawTarget(Mat frame, Point center, Point[] points){
//        if(points[closestIdx] != null && target != null) {
//            deltaDist = calcDist(target, points[closestIdx]);
////            tmpX = center.x - deltaDist/2 < target.x && target.x < center.x + deltaDist/2;
////            tmpY = center.y - deltaDist/2 < target.y && target.y < center.y + deltaDist/2;
//        }
        //if((int)(deltaDist*4) == 0) deltaDist = 0.25;
        deltaDist = (deltaDist < frame.rows()*0.03)? frame.rows()*0.03 : deltaDist;
        deltaDist = (deltaDist > frame.rows()*0.2)? frame.rows()*0.2 : deltaDist;
        Core.circle(frame, center, (int) (deltaDist), WHITE, -1, 8, 0);
        Core.circle(frame, center, (int) (deltaDist / 3), RED, -1, 8, 0);
        Core.circle(frame, center, (int) (deltaDist * 2 / 3), RED, 8, 8, 0);
        Core.circle(frame, center, (int) (deltaDist), RED, 8, 8, 0);
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
//                if(i == 0 || i == 1){
//                    recalcDist = true;
//                }
                if(closestIdx == i){
                    recalcDist = true;
                }else if(closestIdx > i){
                    closestIdx--;
                }
                continue;
            }

            tmpPoint[k++] = tmpPoint[i];
        }
        if(recalcDist && target != null) closestIdx = distClosest(target, tmpPoint, 20);//preDist = calcDist(tmpPoint[0], tmpPoint[1]);
        tmpPoint = Arrays.copyOf(tmpPoint, k);
        keys.fromArray(tmpPoint);

        //calcHomography(preKeys, keys);
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
            closestIdx = distClosest(target, keys, 20);
            //deltaDist = 1;
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
        shot = true;
        shots++;

//        if(tmpX && tmpY){
//            playSound(mpExplosion);
//            score++;
//            //computeTarget(true);
//        }
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

    private void drawBullets(ArrayList<Point> bullets, Point center, double targetSize, Mat img) {
        ArrayList<Point> rmBullets = new ArrayList<>();

        Core.putText(img, "Bullets: " + bullets.size(),
                new Point(30, img.rows() - 120), Core.FONT_HERSHEY_SIMPLEX, 1.0, RED, 2);

        for(Point p : bullets){
            p.y -= 40;

            if(p.y < center.y) {
                if(tmpX && tmpY){
                    playSound(mpExplosion);
                    score++;
                }
                rmBullets.add(p);
            }else Core.circle(img, p, 8 + (int) ((p.y - center.y) / 6), GREEN, -1, 8, 0);
        }
        bullets.removeAll(rmBullets);
    }

    private void renderFrame(Mat rgbaFrame) {
        drawBullets(bullets, new Point(rgbaFrame.cols() / 2, rgbaFrame.rows() / 2), deltaDist, rgbaFrame);//Core.line(rgbaFrame, new Point(rgbaFrame.cols()/2, rgbaFrame.rows()), new Point(rgbaFrame.cols()/2, rgbaFrame.rows()/2), RED, 8);

        Core.rectangle(rgbaFrame, new Point(0,0), new Point(400,50), WHITE, -1, 8, 0);
        Core.putText(rgbaFrame, "Level: " + level + " Points: " + score, new Point(30, 30),
                Core.FONT_HERSHEY_SIMPLEX, 1.0, RED, 2);


        double[] tmpx = null, tmpy = null;
        if(!H.empty()) {
            tmpx = H.get(0, 2);
            tmpy = H.get(1, 2);
        }
        double hx=0, hy=0;
        if(tmpx != null && tmpy != null){
            hx = tmpx[0];
            hy = tmpy[0];
        }

        Core.putText(rgbaFrame, "Reload: " + numbInit + " Features: " + numbFeatures + " curDist: "
                        + curDist + " preDist: " + preDist + " deltaDist: " + dd,
                new Point(30, rgbaFrame.rows() - 90), Core.FONT_HERSHEY_SIMPLEX, 1.0, RED, 2);
        Core.putText(rgbaFrame, "H.x " + hx + " H.y " + hy + " H: " + H.size(),
                new Point(30, rgbaFrame.rows() - 60), Core.FONT_HERSHEY_SIMPLEX, 1.0, RED, 2);
        Core.putText(rgbaFrame, "H " + H.toString(),
                new Point(30, rgbaFrame.rows() - 30), Core.FONT_HERSHEY_SIMPLEX, 1.0, RED, 2);
    }

    public void setGameFrameCaptured(boolean gameFrameCaptured) {
        this.gameFrameCaptured = gameFrameCaptured;
    }

    public boolean getGameFrameCaptured(){
        return gameFrameCaptured;
    }
}

