package com.ptithcm.waveapp;

import android.app.Application;

public class WaveApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ServiceLocator.init(this);
    }
}