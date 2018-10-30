package com.lgf.androidaudiodev;

import android.app.Application;

import org.litepal.LitePal;

/**
 * Created by garment on 2018/10/27.
 */

public class AudioDevApplication extends Application{
    @Override
    public void onCreate() {
        super.onCreate();
        LitePal.initialize(this);
    }
}
