package cmcm.com.busydevice.service;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.*;
import android.os.Process;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;

import cmcm.com.busydevice.R;
import cmcm.com.busydevice.activity.MainActivity;

/**
 * Created by sunzy on 16-6-3.
 */
public class Service1 extends Service {

    public static final String MEMRORY = ":mem";
    private ArrayList<MemContainer> containers = new ArrayList<>();
    private Handler mHandler;
    private HandlerThread mThread;

    @Override
    public void onCreate() {
        super.onCreate();
//        mThread = new HandlerThread("hehe");
//        mHandler = new Handler(mThread.getLooper()){
//            @Override
//            public void handleMessage(Message msg) {
//                super.handleMessage(msg);
//                sendEmptyMessageDelayed(9, 500);
//            }
//        };
//        mHandler.sendEmptyMessage(9);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int change = intent.getIntExtra(MEMRORY, 0);
//        Log.v("sunzy", getClass().getSimpleName() + " will change " + intent.getIntExtra(MEMRORY, 0));
//        bytes = new byte[bytes.length + change * 1024 * 1024];
        if(change == 0){
            stopSelf();
            android.os.Process.killProcess(Process.myPid());
            return START_NOT_STICKY;
        } else {
            containers = new ArrayList<>();
            for (int i = 0;i < 20;i++){
                containers.add(new MemContainer());
            }
            return START_STICKY;
        }

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    class MemContainer {
        //private byte[] bytes = new byte[300 * 1024 * 1024];
        private ArrayList<Bitmap> bitmaps;
        MemContainer(){
            bitmaps = new ArrayList<>();
            for (int i = 0; i < 80; i++){
                bitmaps.add(((BitmapDrawable)getResources().getDrawable(R.drawable.ddd)).getBitmap());
                bitmaps.add(((BitmapDrawable)getResources().getDrawable(R.drawable.aaaa)).getBitmap());
            }
        }
    }



}
