package com.unik.arinvaders;

import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.core.Mat;

abstract class FrameRender {
    protected GameEngine gameEngine;

    public abstract Mat render(CvCameraViewFrame inputFrame);
}

class GameEngineFrameRender extends FrameRender {
    public GameEngineFrameRender(GameEngine gameEngine){
        this.gameEngine = gameEngine;
    }

    @Override
    public Mat render(CvCameraViewFrame inputFrame) {
        Mat rgbaFrame = inputFrame.rgba();
        Mat grayFrame = inputFrame.gray();
        gameEngine.processFrame(grayFrame, rgbaFrame);

        return rgbaFrame;
    }
}

class GameFrameRender {
    private FrameRender mFrameRender;
    public GameFrameRender(FrameRender frameRender) {
        mFrameRender = frameRender;
    }
    public Mat render(CvCameraViewFrame inputFrame) {
        return mFrameRender.render(inputFrame);
    }
}