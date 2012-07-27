/*
 * Copyright (c) 2012 MTI Ltd.
 */
package jp.co.mti.sample;

import java.util.ArrayList;

import android.util.Log;

/**
 * StepDisplayer.<br>
 * @author $Author$
 * @version $Revision$
 */
public class StepDisplayer implements StepListener {

    private int mCount = 0;

    public StepDisplayer() {
        notifyListener();
    }

    public void setSteps(int steps) {
        mCount = steps;
        notifyListener();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onStep() {
        Log.d("step", "step");
        mCount++;
        notifyListener();
    }

    public void reloadSettings() {
        notifyListener();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void passValue() {

    }

    public interface Listener {
        public void stepsChanged(int value);

        public void passValue();
    }

    private ArrayList<Listener> mListeners = new ArrayList<Listener>();

    public void addListener(Listener l) {
        mListeners.add(l);
    }

    public void notifyListener() {
        for (Listener listener : mListeners) {
            listener.stepsChanged((int) mCount);
        }
    }
}
