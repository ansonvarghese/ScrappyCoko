package com.myscrap.utils;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

/**
 * Created by ms3 on 4/27/2017.
 */

public class ShakeEventManager implements SensorEventListener {

    private SensorManager sManager;
    private Sensor s;


    private static final int MOV_COUNTS = 2;
    private static final int SHAKE_WINDOW_TIME_INTERVAL = 1000; // milliseconds
    private int counter;
    private long firstMovTime;
    private ShakeListener listener;

    private boolean init = true;
    private float x1, x2, x3;
    private static final float ERROR = (float) 7.0;

    public ShakeEventManager() {
    }

    public void setListener(ShakeListener listener) {
        this.listener = listener;
    }

    public void init(Context ctx) {
        sManager = (SensorManager)  ctx.getSystemService(Context.SENSOR_SERVICE);
        if (sManager != null) {
            s = sManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            register();
        }

    }

    public void register() {
        sManager.registerListener(this, s, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onSensorChanged(SensorEvent e) {
        //Get x,y and z values
        float x,y,z;
        x = e.values[0];
        y = e.values[1];
        z = e.values[2];
        if (!init) {
            x1 = x;
            x2 = y;
            x3 = z;
            init = true;
        } else {
            float diffX = Math.abs(x1 - x);
            float diffY = Math.abs(x2 - y);
            float diffZ = Math.abs(x3 - z);
            //Handling ACCELEROMETER Noise
            if (diffX < ERROR) {
                diffX = (float) 0.0;
            }
            if (diffY < ERROR) {
                diffY = (float) 0.0;
            }
            if (diffZ < ERROR) {
                diffZ = (float) 0.0;
            }
            x1 = x;
            x2 = y;
            x3 = z;
            //Horizontal Shake Detected!
            if (diffX > diffY) {
                if (counter == 0) {
                    counter++;
                    firstMovTime = System.currentTimeMillis();
                    Log.d("SwA", "First mov..");
                } else {
                    long now = System.currentTimeMillis();
                    if ((now - firstMovTime) < SHAKE_WINDOW_TIME_INTERVAL)
                        counter++;
                    else {
                        resetAllData();
                        counter++;
                        return;
                    }
                    Log.d("SwA", "Mov counter ["+counter+"]");

                    if (counter >= MOV_COUNTS){
                        resetAllData();
                        if (listener != null)
                            listener.onShake();
                    }

                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {}

    public void deregister()  {
        sManager.unregisterListener(this);
    }

    private void resetAllData() {
        Log.d("SwA", "Reset all data");
        counter = 0;
        firstMovTime = System.currentTimeMillis();
    }


    public static interface ShakeListener {
        public void onShake();
    }

}
