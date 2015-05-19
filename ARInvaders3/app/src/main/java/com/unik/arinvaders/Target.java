package com.unik.arinvaders;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;

import java.util.Random;

public class Target {
    private Point center;
    private double radius, timer, delay;
    private Random randPoint;
    private int idx;

    public Target(double delay){
        this.delay = delay;

        timer = delay+System.currentTimeMillis();
        radius = 36;
        randPoint = new Random();
    }

    public void resetTimer(){
        timer -= delay;
    }

    public void create(Point[] features){
        if(features != null && features.length > 0){
            int seed = (features.length > 10)? 10 : features.length;
            idx = randPoint.nextInt(seed);
            center = features[idx];
            timer = delay+System.currentTimeMillis();
        }
    }

    public void update(){
        idx--;
    }

    public void decrTimer(int decr){
        timer-=decr;
    }

    public void draw(Mat frame, Point[] features, int sub){
        if(idx < features.length) {
            center = features[idx];
            double tmpRad = distClosest(features, sub);

            radius = (tmpRad > frame.rows()*0.02)? tmpRad : frame.rows()*0.03;
            radius = (radius < frame.rows()*0.2)? radius : frame.rows()*0.2;

            drawTarget(frame);
        }
    }

    public int getIdx(){
        return idx;
    }

    private static double calcDist(Point a, Point b){
        return Math.abs(Math.sqrt(Math.pow(a.x-b.x,2) + Math.pow(a.y-b.y,2)));
    }

    private double distClosest(Point[] points, int sub){
        double dist = Float.MAX_VALUE;

        sub = (sub > points.length)? points.length : sub;

        for(int i = 0; i<sub; i++){
            double tmpDist = calcDist(points[i], center);
            if(tmpDist < dist && points[i] != center){
                dist = tmpDist;
            }
        }
        return dist;
    }

    private void drawTarget(Mat frame){
        Scalar WHITE = new Scalar(255,255,255);
        Scalar RED = new Scalar(255,0,0);

        Core.circle(frame, center, (int) (radius), WHITE, -1, 8, 0);
        Core.circle(frame, center, (int) (radius / 3), RED, -1, 8, 0);
        Core.circle(frame, center, (int) (radius * 2 / 3), RED, 8, 8, 0);
        Core.circle(frame, center, (int) (radius), RED, 8, 8, 0);
    }

    public double getTimer(){
        return timer;
    }

    public double getRadius() { return radius; }

    public Point getPoint() { return center; }
}
