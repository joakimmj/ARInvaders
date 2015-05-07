package com.joakimmj.cam.cameratest;

import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.core.Mat;
import org.opencv.core.KeyPoint;
import org.opencv.core.DMatch;

import java.util.Vector;


public class MapGameSpace {
    private FeatureDetector fDetect;
    private DescriptorExtractor fDescript;
    private DescriptorMatcher matcher;
    private Mat frame, img, detections, refImg, refDesc, desc;
    private Vector<KeyPoint> keyPoints, refKeys;
    private Vector<DMatch> matches;

    public MapGameSpace(FeatureDetector detector){
        this.fDetect = detector;
    }

    public Vector<DMatch> getMatches(){
        return matches;
    }
}
