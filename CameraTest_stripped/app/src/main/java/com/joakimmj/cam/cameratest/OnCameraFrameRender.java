package com.joakimmj.cam.cameratest;

import java.util.ArrayList;
import java.util.List;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Range;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import android.content.res.Resources;

abstract class FrameRender {
    //protected TmpCameraCalibrator mCalibrator;
    protected GameEngine mapGameSpace;

    public abstract Mat render(CvCameraViewFrame inputFrame);
}

class GameEngineFrameRender extends FrameRender {
    public GameEngineFrameRender(GameEngine mapGameSpace){
        this.mapGameSpace = mapGameSpace;
    }

    @Override
    public Mat render(CvCameraViewFrame inputFrame) {
        Mat rgbaFrame = inputFrame.rgba();
        Mat grayFrame = inputFrame.gray();
        mapGameSpace.processFrame(grayFrame, rgbaFrame);

        return rgbaFrame;
    }
}
