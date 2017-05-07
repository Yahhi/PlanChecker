package ru.na_uglu.planchecker;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

public class NetworkSync extends Service {

    IBinder mBinder = new LocalBinder();

    public NetworkSync() {

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        //Log.i(TAG, "bindng service...");
        return mBinder;
    }

    class LocalBinder extends Binder {
        NetworkSync getService() {
            // Return this instance of LocalService so clients can call public methods
            return NetworkSync.this;
        }
    }
}
