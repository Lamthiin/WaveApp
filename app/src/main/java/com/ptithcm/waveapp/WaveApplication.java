package com.ptithcm.waveapp;

import android.app.Application;
import com.ptithcm.waveapp.config.ServiceLocator;

public class WaveApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Khởi tạo ServiceLocator với Context của ứng dụng
        ServiceLocator.init(this);
    }
}
