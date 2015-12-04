
package com.fun.mediarenderer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Map;

import org.fourthline.cling.android.AndroidUpnpService;
import org.fourthline.cling.binding.LocalServiceBinder;
import org.fourthline.cling.binding.annotations.AnnotationLocalServiceBinder;
import org.fourthline.cling.model.DefaultServiceManager;
import org.fourthline.cling.model.ServiceManager;
import org.fourthline.cling.model.ValidationException;
import org.fourthline.cling.model.meta.DeviceDetails;
import org.fourthline.cling.model.meta.DeviceIdentity;
import org.fourthline.cling.model.meta.Icon;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.meta.LocalService;
import org.fourthline.cling.model.meta.ManufacturerDetails;
import org.fourthline.cling.model.meta.ModelDetails;
import org.fourthline.cling.model.types.UDADeviceType;
import org.fourthline.cling.model.types.UDAServiceType;
import org.fourthline.cling.model.types.UDN;
import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;
import org.fourthline.cling.support.avtransport.lastchange.AVTransportLastChangeParser;
import org.fourthline.cling.support.connectionmanager.ConnectionManagerService;
import org.fourthline.cling.support.lastchange.LastChange;
import org.fourthline.cling.support.lastchange.LastChangeAwareServiceManager;
import org.fourthline.cling.support.renderingcontrol.lastchange.RenderingControlLastChangeParser;

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.util.Log;

import com.fun.mediarenderer.display.DisplayHandler;
import com.fun.mediarenderer.display.MediaPlayerHandler;
import com.fun.mediarenderer.upnp.DefAVTransportService;
import com.fun.mediarenderer.upnp.DefAudioRenderingControl;
import com.fun.mediarenderer.upnp.DefMediaPlayer;
import com.fun.mediarenderer.upnp.DefMediaPlayers;
import com.fun.mediarenderer.utils.Utils;

public class UpnpSingleton {

    private final static String TAG = "UpnpSingleton";

    private static UpnpSingleton instance;

    public String customVar;

    public final static UDN udn = UDN.uniqueSystemIdentifier("Fun MediaRenderer");
    private AndroidUpnpService upnpService;
    private LocalDevice device;
    private DisplayHandler displayHandler;

    public static final long LAST_CHANGE_FIRING_INTERVAL_MILLISECONDS = 2000;
    final protected LocalServiceBinder binder = new AnnotationLocalServiceBinder();

    // These are shared between all "logical" player instances of a single
    // service
    final protected LastChange avTransportLastChange = new LastChange(
            new AVTransportLastChangeParser());
    final protected LastChange renderingControlLastChange = new LastChange(
            new RenderingControlLastChangeParser());

    protected Map<UnsignedIntegerFourBytes, DefMediaPlayer> mediaPlayers;

    protected ServiceManager<ConnectionManagerService> connectionManager;
    protected LastChangeAwareServiceManager<DefAVTransportService> avTransport;
    protected LastChangeAwareServiceManager<DefAudioRenderingControl> renderingControl;

    protected LocalService<ConnectionManagerService> connectionManagerService;
    protected LocalService<DefAVTransportService> avTransportService;
    protected LocalService<DefAudioRenderingControl> renderingControlService;

    protected Context applicationContext;

    protected MediaPlayer mMediaPlayer; // music player

    public static void initInstance(Context context)
    {
        if (instance == null)
        {
            // Create the instance
            instance = new UpnpSingleton(context);
        }
    }

    public static UpnpSingleton getInstance()
    {
        // Return the instance
        return instance;
    }

    private UpnpSingleton(Context context)
    {
        // Constructor hidden because this is a singleton
        applicationContext = context;

        displayHandler = new MediaPlayerHandler();

        // This is the backend which manages the actual player instances
        mediaPlayers = new DefMediaPlayers(1,
                avTransportLastChange,
                renderingControlLastChange
                ) {
                    // These overrides connect the player instances to the
                    // output/display
                    @Override
                    protected void onPlayerNoMedia(DefMediaPlayer player) {
                        getDisplayHandler().onNoMedia(player);
                    }

                    @Override
                    protected void onPlayerPlay(DefMediaPlayer player) {
                        getDisplayHandler().onPlay(player);
                    }

                    @Override
                    protected void onPlayerStop(DefMediaPlayer player) {
                        getDisplayHandler().onStop(player);
                    }

                    @Override
                    protected void onPlayerPaused(DefMediaPlayer player) {
                        // getDisplayHandler().onPaused(player);
                    }

                };

        connectionManagerService =
                binder.read(ConnectionManagerService.class);

        connectionManagerService
                .setManager(
                new DefaultServiceManager<ConnectionManagerService>(connectionManagerService, null) {
                    @Override
                    protected ConnectionManagerService createServiceInstance() throws Exception {
                        return new ConnectionManagerService();
                    }
                }
                );

        // The AVTransport just passes the calls on to the backend players
        avTransportService =
                binder.read(DefAVTransportService.class);
        avTransport =
                new LastChangeAwareServiceManager<DefAVTransportService>(
                        avTransportService,
                        new AVTransportLastChangeParser()
                ) {
                    @Override
                    protected DefAVTransportService createServiceInstance() throws Exception {
                        return new DefAVTransportService(avTransportLastChange, mediaPlayers);
                    }

                    @Override
                    protected int getLockTimeoutMillis() {
                        return 2000;
                    }

                };
        avTransportService.setManager(avTransport);

        // The Rendering Control just passes the calls on to the backend players
        renderingControlService =
                binder.read(DefAudioRenderingControl.class);
        renderingControl =
                new LastChangeAwareServiceManager<DefAudioRenderingControl>(
                        renderingControlService,
                        new RenderingControlLastChangeParser()
                ) {
                    @Override
                    protected DefAudioRenderingControl createServiceInstance() throws Exception {
                        return new DefAudioRenderingControl(renderingControlLastChange,
                                mediaPlayers);
                    }
                };
        renderingControlService.setManager(renderingControl);

        try {

            device = new LocalDevice(
                    new DeviceIdentity(udn),
                    new UDADeviceType("MediaRenderer", 1),
                    new DeviceDetails(
                            "Fun MediaRenderer : " + Utils.getIPAddress(true),
                            new ManufacturerDetails("Fun", "http://www.fun.tv"),
                            new ModelDetails("Fun MediaRenderer", "MediaRenderer on Android", "1",
                                    "http://www.fun.tv")
                    ),
                    new Icon[] {
                            createDefaultDeviceIcon()
                    },
                    new LocalService[] {
                            avTransportService,
                            renderingControlService,
                            connectionManagerService
                    }
                    );

        } catch (ValidationException ex) {
            throw new RuntimeException(ex);
        }

        serviceConnection = new ServiceConnection() {

            public void onServiceConnected(ComponentName className, IBinder service) {
                upnpService = (AndroidUpnpService) service;
                LocalService<DefAVTransportService> innerAvTransportService = getAvTransportService();
                if (innerAvTransportService == null) {
                    upnpService.getRegistry().addDevice(device);
                }
            }

            public void onServiceDisconnected(ComponentName className) {
                upnpService = null;
            }
        };
    }

    public void customSingletonMethod() {
    }

    public Context getApplicationContext() {
        return this.applicationContext;
    }

    public ServiceConnection getServiceConnection() {
        return this.serviceConnection;
    }

    public LastChangeAwareServiceManager<DefAVTransportService> getAvTransport() {
        return this.avTransport;
    }

    public LastChangeAwareServiceManager<DefAudioRenderingControl> getRenderingControl() {
        return this.renderingControl;
    }

    public Map<UnsignedIntegerFourBytes, DefMediaPlayer> getMediaPlayers() {
        return mediaPlayers;
    }

    public void setMusicMediaPlayer(MediaPlayer mp) {
        this.mMediaPlayer = mp;
    }

    public MediaPlayer getMusicMediaPlayer() {
        return this.mMediaPlayer;
    }

    private ServiceConnection serviceConnection;

    protected Icon createDefaultDeviceIcon() {
        BitmapDrawable bitDw = ((BitmapDrawable) applicationContext
                .getResources()
                .getDrawable(R.drawable.mediarenderer));
        Bitmap bitmap = bitDw.getBitmap();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] imageInByte = stream.toByteArray();
        System.out.println("........length......" + imageInByte);
        ByteArrayInputStream bis = new ByteArrayInputStream(imageInByte);

        try {
            return new Icon(
                    "image/png",
                    48, 48, 8,
                    URI.create("icon.png"),
                    bis);
        } catch (IOException ex) {
            throw new RuntimeException("Could not load icon", ex);
        }
    }

    protected LocalService<DefAVTransportService> getAvTransportService() {
        if (upnpService == null)
            return null;

        LocalDevice localDevice;
        if ((localDevice = upnpService.getRegistry().getLocalDevice(udn, true)) == null)
            return null;

        return (LocalService<DefAVTransportService>) localDevice.findService(new UDAServiceType(
                "AVTransport", 1));
    }

    synchronized public DisplayHandler getDisplayHandler() {
        return displayHandler;
    }

    // The backend player instances will fill the LastChange whenever something
    // happens with
    // whatever event messages are appropriate. This loop will periodically
    // flush these changes
    // to subscribers of the LastChange state variable of each service.
    public void runLastChangePushThread() {
        // TODO: We should only run this if we actually have event subscribers
        new Thread() {
            @Override
            public void run() {
                try {
                    Log.d("runLastChangePushThread", "runLastChangePushThread is running");
                    while (true) {
                        // These operations will NOT block and wait for network
                        // responses
                        UpnpSingleton.getInstance().getAvTransport().fireLastChange();
                        UpnpSingleton.getInstance().getRenderingControl().fireLastChange();
                        Thread.sleep(LAST_CHANGE_FIRING_INTERVAL_MILLISECONDS);
                    }
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        }.start();

    }
}
