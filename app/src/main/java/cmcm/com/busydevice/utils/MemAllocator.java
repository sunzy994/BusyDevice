package cmcm.com.busydevice.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import cmcm.com.busydevice.application.BDApplication;
import cmcm.com.busydevice.service.Service1;

/**
 * Created by sunzy on 16-6-3.
 */
public class MemAllocator {
    private static int sMaxAllocableMem = -1;
    private int memAllocated = 0;
    private Class clazz;
    private boolean mActivated = false;

    public MemAllocator(Class clazz){
        this.clazz = clazz;
        getMaxAllocableMem();
    }

    public int getMemAllocated() {
        return memAllocated;
    }

    public Class<Service> getClazz() {
        return clazz;
    }

    /**
     * 上峰要求 开辟 mem 大小的内存，但是，具体这个进程能开辟多少
     * 是未知的，返回的是实际开辟内存大小
     */
    public int allocate(int mem){
        //本进程可以开辟的内存大小
        int available = sMaxAllocableMem - memAllocated;
        Log.v("sunzy", "allocator will allocate " + mem + ", sMaxAllocableMem = " + sMaxAllocableMem + ", memAllocated = " + memAllocated);
        //此次本进程将会开辟内存大小
        int willAllocate = Math.min(available, mem);
        if(willAllocate > 0){
            memAllocated += willAllocate;
            startService(null, willAllocate);
        }
        return willAllocate;
    }

    /**
     * 上峰要求 开辟 mem 大小的内存，但是，具体这个进程能释放多少
     * 是未知的，返回的是实际释放内存大小
     */
    public int release(int mem){
        int willRelease = Math.min(mem, memAllocated);
        if(willRelease > 0){
            memAllocated -= willRelease;
            startService(null, -willRelease);
        }
        return willRelease;
    }

    public void startService(Activity activity, int mem){
        Log.v("sunzy", " will start service " + clazz.getSimpleName() + ", mem = " + mem);
        Intent intent = new Intent(activity, clazz);
        intent.putExtra(Service1.MEMRORY, mem);
        activity.startService(intent);

//        activity.bindService(intent, new ServiceConnection() {
//            @Override
//            public void onServiceConnected(ComponentName name, IBinder service) {
//
//            }
//
//            @Override
//            public void onServiceDisconnected(ComponentName name) {
//
//            }
//        }, 0
//        );
    }

    public static int getMaxAllocableMem(){
        if(sMaxAllocableMem < 0){
            ActivityManager am = (ActivityManager) BDApplication.getAppplicatoinContext().getSystemService(Context.ACTIVITY_SERVICE);
            sMaxAllocableMem = am.getLargeMemoryClass();
        }
        return sMaxAllocableMem;
    }

    public void activate(Activity activity){
        mActivated = true;
        startService(activity, 100);
    }

    public void deactivate(Activity activity){
        mActivated = false;
        //activity.unbindService();
        startService(activity, 0);
    }

    public boolean ismActivated(){
        return mActivated;
    }

}
