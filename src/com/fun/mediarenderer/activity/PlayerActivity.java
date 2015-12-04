
package com.fun.mediarenderer.activity;

import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.fun.mediarenderer.R;
import com.fun.mediarenderer.UpnpSingleton;

public class PlayerActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_player);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(
                        R.id.media_renderer_root,
                        UpnpSingleton.getInstance().getMediaPlayers()
                                .get(new UnsignedIntegerFourBytes(0)))
                .commit();
    }

    @Override
    public void onBackPressed() {

        // clear videoView and imageView firstly
        // UpnpSingleton.getInstance().getMediaPlayers().get(new
        // UnsignedIntegerFourBytes(0)).stop(); // recycle bitmap ImageView
        // imageView = (ImageView) findViewById(R.id.myimageview);
        // BitmapDrawable bitmapDrawable = (BitmapDrawable)
        // imageView.getDrawable();
        // if (bitmapDrawable != null) {
        // Bitmap bitmap = bitmapDrawable.getBitmap();
        // if (bitmap != null &&
        // !bitmap.isRecycled())
        // bitmap.recycle();
        // }
        // moveTaskToBack(true);

        finish();
    }

}
