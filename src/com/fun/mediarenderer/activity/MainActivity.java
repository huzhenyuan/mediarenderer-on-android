
package com.fun.mediarenderer.activity;

import com.fun.mediarenderer.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class MainActivity extends Activity {

    private static final String START_BACKEND_SERVICE = "start_backend_service";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main);

        sendBroadcast(new Intent(START_BACKEND_SERVICE));
    }

}
