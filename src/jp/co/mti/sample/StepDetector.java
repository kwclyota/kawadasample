/*
 * Copyright (c) 2012 MTI Ltd.
 */
package jp.co.mti.sample;

import java.util.ArrayList;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

/**
 * StepDetector.<br>
 * @author $Author$
 * @version $Revision$
 */
public class StepDetector implements SensorEventListener {

    private int mLimit = 30;

    private float mLastValues[] = new float[3 * 2];

    /** 尺度 */
    private float mScale[] = new float[2];

    /** 傾き */
    private float mYOffset;

    /** 方向 */
    private float mLastDirections[] = new float[3 * 2];

    /** 極度 */
    private float mLastExtremes[][] = {new float[3 * 2], new float[3 * 2] };

    /** 距離 */
    private float mLastDiff[] = new float[3 * 2];

    private int mLastMatch = -1;

    private ArrayList<StepListener> mStepListeners = new ArrayList<StepListener>();

    public StepDetector() {
        int h = 480; // TODO: remove this constant
        mYOffset = h * 0.5f;
        /** 重力 */
        mScale[0] = -(h * 0.5f * (1.0f / (SensorManager.STANDARD_GRAVITY * 2)));

        mScale[1] = -(h * 0.5f * (1.0f / (SensorManager.MAGNETIC_FIELD_EARTH_MAX)));
    }

    public void addStepListener(StepListener sl) {
        mStepListeners.add(sl);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        Log.d("step", "step");
        Sensor sensor = event.sensor;
        synchronized (this) {
            if (sensor.getType() == Sensor.TYPE_ORIENTATION) {
            } else {
                int j = (sensor.getType() == Sensor.TYPE_ACCELEROMETER) ? 1 : 0;
                if (j == 1) {
                    float vSum = 0;
                    // XYZ方向の加速度の絶対値を合計する
                    for (int i = 0; i < 3; i++) {
                        final float v = mYOffset + event.values[i] * mScale[j];
                        vSum += v;
                    }
                    int k = 0;
                    // 上の平均をとって
                    float v = vSum / 3;

                    float direction = (v > mLastValues[k] ? 1 : (v < mLastValues[k] ? -1 : 0));
                    if (direction == -mLastDirections[k]) {
                        // Direction changed
                        int extType = (direction > 0 ? 0 : 1); // minumum or maximum?
                        mLastExtremes[extType][k] = mLastValues[k];
                        float diff = Math.abs(mLastExtremes[extType][k] - mLastExtremes[1 - extType][k]);

                        if (diff > mLimit) {

                            boolean isAlmostAsLargeAsPrevious = diff > (mLastDiff[k] * 2 / 3);
                            boolean isPreviousLargeEnough = mLastDiff[k] > (diff / 3);
                            boolean isNotContra = (mLastMatch != 1 - extType);

                            if (isAlmostAsLargeAsPrevious && isPreviousLargeEnough && isNotContra) {
                                Log.i("TAG", "step");
                                for (StepListener stepListener : mStepListeners) {
                                    stepListener.onStep();
                                }
                                mLastMatch = extType;
                            } else {
                                mLastMatch = -1;
                            }
                        }
                        mLastDiff[k] = diff;
                    }
                    // 1か-1か0
                    mLastDirections[k] = direction;
                    mLastValues[k] = v;
                }
            }
        }

    }

}
