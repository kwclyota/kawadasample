/*
 * Copyright (c) 2012 MTI Ltd.
 */
package jp.co.mti.sample;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;

/**
 * StepService.<br>
 * @author $Author$
 * @version $Revision$
 */
public class StepService extends Service {

    private SensorManager mSensorManager;

    private Sensor mSensor;

    private StepDetector mStepDetector;

    private StepDisplayer mStepDisplayer;

    private PowerManager.WakeLock wakeLock;

    private int mSteps;

    public class StepBinder extends Binder {
        StepService getService() {
            return StepService.this;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate() {
        super.onCreate();
        acquireWakeLock();
        mStepDetector = new StepDetector();
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        registerDetector();
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
        registerReceiver(mReceiver, filter);

        mStepDisplayer = new StepDisplayer();
        mStepDisplayer.setSteps(mSteps);
        mStepDisplayer.addListener(mStepListener);
        mStepDetector.addStepListener(mStepDisplayer);

        reloadSettings();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDestroy() {
        unregisterReceiver(mReceiver);
        unregisterDetector();
        wakeLock.release();
        super.onDestroy();
        mSensorManager.unregisterListener(mStepDetector);
    }

    /**
     * unregisterDetectorを実行する。<br>
     */
    private void unregisterDetector() {
        mSensorManager.unregisterListener(mStepDetector);
    }

    /**
     * reloadSettingsを実行する。<br>
     */
    public void reloadSettings() {
        if (mStepDisplayer != null)
            mStepDisplayer.reloadSettings();
    }

    /**
     * registerDetectorを実行する。<br>
     */
    private void registerDetector() {
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(mStepDetector, mSensor, SensorManager.SENSOR_DELAY_FASTEST);
    }

    /**
     * acquireWakeLockを実行する。<br>
     */
    private void acquireWakeLock() {
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        int wakeFlags = PowerManager.PARTIAL_WAKE_LOCK;
        wakeLock = pm.newWakeLock(wakeFlags, "TAG");
        wakeLock.acquire();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private final IBinder mBinder = new StepBinder();

    public interface ICallback {
        public void stepsChanged(int value);
    }

    public ICallback mCallback;

    public void registerCallback(ICallback cb) {
        mCallback = cb;
    }

    public void resetValues() {
        mStepDisplayer.setSteps(0);
    }

    private StepDisplayer.Listener mStepListener = new StepDisplayer.Listener() {
        public void stepsChanged(int value) {
            mSteps = value;
            passValue();
        }

        public void passValue() {
            if (mCallback != null) {
                mCallback.stepsChanged(mSteps);
            }
        }
    };

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                StepService.this.unregisterDetector();
                StepService.this.registerDetector();
            }
        }

    };
}
