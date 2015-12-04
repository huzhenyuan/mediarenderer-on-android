
package com.fun.mediarenderer.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import com.fun.mediarenderer.R;
import com.fun.mediarenderer.UpnpApp;
import com.fun.mediarenderer.UpnpSingleton;

public class BackendService extends Service {

    @Override
    public void onCreate() {
        super.onCreate();

        getApplicationContext().bindService(
                new Intent(this, MediaRendererServiceImpl.class),
                UpnpSingleton.getInstance().getServiceConnection(),
                Context.BIND_AUTO_CREATE
                );

        UpnpApp application = (UpnpApp) getApplication();
        application.showNotification(getText(R.string.service_start).toString());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getApplicationContext().unbindService(UpnpSingleton.getInstance().getServiceConnection());
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
