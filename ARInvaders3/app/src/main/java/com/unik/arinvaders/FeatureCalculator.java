package com.unik.arinvaders;

import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.Size;
import org.opencv.features2d.DMatch;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.KeyPoint;
import org.opencv.imgproc.Imgproc;

import java.util.Arrays;
import java.util.Comparator;

public class FeatureCalculator {
    private FeatureDetector detector;
    private DescriptorExtractor descriptor;
    private DescriptorMatcher matcher;
    private int numbOfKeyPoints;

    public FeatureCalculator(int detectorType, int descriptorType, int matcherType, int numbOfKeyPoints) {
        detector = FeatureDetector.create(detectorType);
        descriptor = DescriptorExtractor.create(descriptorType);
        matcher = DescriptorMatcher.create(matcherType);
        this.numbOfKeyPoints = numbOfKeyPoints;
    }

    public void getFeatures(Mat frame, Mat desc, MatOfKeyPoint keys){
        calculateFeatures(frame, desc, keys);
    }

    public void getMatches(Mat frame, Mat desc, MatOfKeyPoint keys, Mat preDesc, MatOfDMatch matches){
        calculateFeatures(frame, desc, keys);
        if(!preDesc.empty()) {
            calculateMatches(desc, preDesc, matches);
        }
    }

    private void keyPointFilter(MatOfKeyPoint keys){
        //higher response -> richer key point
        KeyPoint[] arrKeys = keys.toArray();
        Arrays.sort(arrKeys, new Comparator<KeyPoint>(){
            @Override
            public int compare(KeyPoint keyPoint, KeyPoint keyPoint2) {
                if(keyPoint.response>keyPoint2.response)
                    return -1;
                else if(keyPoint.response<keyPoint2.response)
                    return 1;
                else
                    return 0;
            }
        });

        int tmpNumb = (arrKeys.length < numbOfKeyPoints)? arrKeys.length : numbOfKeyPoints;
        arrKeys = Arrays.copyOf(arrKeys, tmpNumb);

        keys.fromArray(arrKeys);
    }

    private void calculateFeatures(Mat frame, Mat desc, MatOfKeyPoint keys){
        //scale frame
        double scale = 0.5;
        Mat newFrame = new Mat();
        Imgproc.resize(frame, newFrame, new Size(), scale, scale, Imgproc.INTER_AREA);

        detector.detect(newFrame, keys);

        //remove weak key points
        keyPointFilter(keys);

        descriptor.compute(newFrame, keys, desc);
    }

    private void calculateMatches(Mat desc, Mat preDesc, MatOfDMatch matches){
        matcher.clear();
        matcher.match(desc, preDesc, matches);

        //remove weak matches
        double minDist=Float.MAX_VALUE;

        for(DMatch dm : matches.toArray()){
            if(dm.distance < minDist) minDist = dm.distance;
        }

        DMatch[] dm = matches.toArray();
        int k = 0;
        for(int i = 0; i < dm.length;i++) {
            if (dm[i].distance < 3 * minDist) dm[k++] = dm[i];//let live
        }
        dm = Arrays.copyOf(dm, k);
        matches.fromArray(dm);
    }
}
