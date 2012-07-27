package jp.co.mti.sample;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class Pedometer2Activity extends Activity {

    /**
     * 歩数を表示するテキストビュー
     */
    private TextView mStepValueView;

    /**
     * 歩数
     */
    private int mStepValue;

    /**
     * 稼働中かどうか
     */
    private boolean mIsRunning;

    /**
     * 歩数を管理するサービス
     */
    private StepService mService;

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // サービスにはIBinder経由で#getService()してダイレクトにアクセス可能
            mService = ((StepService.StepBinder) service).getService();
            mService.registerCallback(mCallback);
            mService.reloadSettings();
        }
    };

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mStepValue = 0;

        setContentView(R.layout.main);

        startStepService();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onResume() {
        super.onResume();

        if (mIsRunning) {
            bindStepService();
        }

        mStepValueView = (TextView) findViewById(R.id.step_value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onPause() {
        if (mIsRunning) {
            unbindStepService();
        }
        super.onPause();
    }

    /**
     * unbindStepServiceを実行する。<br>
     */
    private void unbindStepService() {
        unbindService(mConnection);
    }

    /**
     * bindStepServiceを実行する。<br>
     */
    private void bindStepService() {
        Log.d("activity", "bind");
        bindService(new Intent(Pedometer2Activity.this, StepService.class), mConnection, Context.BIND_AUTO_CREATE);
    }

    /**
     * startStepServiceを実行する。<br>
     */
    private void startStepService() {
        Log.d("activity", "start");
        mIsRunning = true;
        startService(new Intent(Pedometer2Activity.this, StepService.class));
    }

    /**
     * stopStepServiceを実行する。<br>
     */
    private void stopStepService() {
        mIsRunning = false;
        if (mService != null) {
            stopService(new Intent(Pedometer2Activity.this, StepService.class));
        }
    }

    private void resetValues() {
        if (mService != null && mIsRunning) {
            mService.resetValues();
        } else {
            mStepValueView.setText("0");
        }
    }

    private static final int MENU_QUIT = 9;

    private static final int MENU_PAUSE = 1;

    private static final int MENU_RESUME = 2;

    private static final int MENU_RESET = 3;

    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        if (mIsRunning) {
            menu.add(0, MENU_PAUSE, 0, "Pause").setIcon(android.R.drawable.ic_media_pause).setShortcut('1', 'p');
        } else {
            menu.add(0, MENU_RESUME, 0, "Continue").setIcon(android.R.drawable.ic_media_play).setShortcut('1', 'p');
        }
        menu.add(0, MENU_RESET, 0, "Start Again").setIcon(android.R.drawable.ic_menu_close_clear_cancel).setShortcut('2', 'r');
        menu.add(0, MENU_QUIT, 0, "Exit").setIcon(android.R.drawable.ic_lock_power_off).setShortcut('9', 'q');
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case MENU_PAUSE:
            unbindStepService();
            stopStepService();
            return true;
        case MENU_RESUME:
            startStepService();
            bindStepService();
            return true;
        case MENU_RESET:
            resetValues();
            return true;
        case MENU_QUIT:
            resetValues();
            stopStepService();
            finish();
            return true;
        }
        return false;
    }

    private StepService.ICallback mCallback = new StepService.ICallback() {
        public void stepsChanged(int value) {
            Log.v("step", "step");
            mHandler.sendMessage(mHandler.obtainMessage(STEP_MSG, value, 0));
        }
    };

    private static final int STEP_MSG = 1;

    private Handler mHandler = new Handler() {

        /**
         * {@inheritDoc}
         */
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case STEP_MSG:
                mStepValue = (int) msg.arg1;
                mStepValueView.setText("" + mStepValue);
                break;
            default:
                super.handleMessage(msg);
            }

        }

    };
}