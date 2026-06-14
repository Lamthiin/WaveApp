package com.ptithcm.waveapp;

import android.app.Application;

import com.google.firebase.auth.FirebaseAuth;

public class WaveApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ServiceLocator.init(this);

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() == null) {
            firebaseAuth.signInAnonymously();
        }
    }
}