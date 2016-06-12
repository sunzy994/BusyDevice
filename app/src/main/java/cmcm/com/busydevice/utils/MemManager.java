package cmcm.com.busydevice.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.util.Log;

import java.util.HashSet;

import cmcm.com.busydevice.application.BDApplication;
import cmcm.com.busydevice.service.Service1;
import cmcm.com.busydevice.service.Service10;
import cmcm.com.busydevice.service.Service11;
import cmcm.com.busydevice.service.Service12;
import cmcm.com.busydevice.service.Service13;
import cmcm.com.busydevice.service.Service14;
import cmcm.com.busydevice.service.Service15;
import cmcm.com.busydevice.service.Service16;
import cmcm.com.busydevice.service.Service17;
import cmcm.com.busydevice.service.Service2;
import cmcm.com.busydevice.service.Service3;
import cmcm.com.busydevice.service.Service4;
import cmcm.com.busydevice.service.Service5;
import cmcm.com.busydevice.service.Service6;
import cmcm.com.busydevice.service.Service7;
import cmcm.com.busydevice.service.Service8;
import cmcm.com.busydevice.service.Service9;

/**
 * Created by sunzy on 16-6-3.
 * 管理所有的进程service，开辟和释放内存
 */
public class MemManager {

    private int MAX_ALLOCATE = -1;
    //可以开辟的进程总数
    private int TOTAL_PROCESS_COUNT = 50;
    private int mAllocated;

    private static HashSet<MemAllocator> containers =  new HashSet<>();

    private static MemManager sInstance;
    private Context mContext;

    public static MemManager getInstance(){
        if(sInstance == null){
            sInstance = new MemManager();
        }
        return sInstance;
    }

    private MemManager(){
        mContext = BDApplication.getAppplicatoinContext();
        init();
    }

    private void init(){
        ActivityManager am = (ActivityManager)mContext.getSystemService(Context.ACTIVITY_SERVICE);
        //理论上我们可以开辟内存最大值,因为application中使用了 android:largeHeap="true"，所以使用 am.getLargeMemoryClass()方法
        // am.getLargeMemoryClass()为单个进程可以使用的内存最大值，乘以本应用中声明的进程总数，得到本应用 理论上 可以开辟的最大内存
        MAX_ALLOCATE = am.getLargeMemoryClass() * TOTAL_PROCESS_COUNT;
        Log.v("sunzy", "max allocate mem = " + MAX_ALLOCATE);

        for (int i = 1;i<=50;i++){
            try{
                containers.add(new MemAllocator(Class.forName("cmcm.com.busydevice.service.Service" + i)));
            } catch (Exception e){
                Log.wtf("sunzy", e);
            }

        }
    }

    public boolean allocate(int mem){
        Log.v("sunzy", "user request to allocate " + mem + " mem");
        if(mem <= 0){
            return true;
        }
        // 该设备上总共可用内存
        int maxAvailable = (int)(ProcessInfoHelperImpl.getAvailableMemoryByteDirect(mContext)/1024/1024);
        Log.v("sunzy", "the device has " + maxAvailable + ", and we can still allocate " + (MAX_ALLOCATE - mAllocated));
        // 和我们可以开辟的最大内存取其小，有可能我们支持开辟很大的内存的，但是设备支持不了，有可能设备内存很大，但是我们只有这么多进程，占不满全部内存
        maxAvailable = Math.min(maxAvailable, MAX_ALLOCATE - mAllocated);
        if(mem > maxAvailable){
            return false;
        }
        for (MemAllocator container: containers){
            if(mem > 0){
                int allocated = container.allocate(mem);
                mem -= allocated;
                mAllocated += allocated;
            } else {
                break;
            }
        }
        // mem == 0 说明内存已经全部开辟，成功，否则失败
        return mem == 0;
    }

    public boolean release(int mem){
        Log.v("sunzy", "user request to release " + mem + " mem, we have allocated " + mAllocated);
        if(mem <= 0){
            return true;
        }
        for (MemAllocator container: containers){
            if(mem > 0){
                int released = container.release(mem);
                mem -= released;
                mAllocated -= released;
            } else {
                break;
            }
        }
        // mem == 0 说明内存已经全部释放，成功，否则失败
        return mem == 0;
    }

    public void allocate(Activity activity){
        for (MemAllocator container: containers){
            if(!container.ismActivated()){
                container.activate(activity);
                mActiveProcessNumber++;
                break;
            }
        }
    }

    public void release(Activity activity){
        for (MemAllocator container: containers){
            if(container.ismActivated()){
                container.deactivate(activity);
                mActiveProcessNumber--;
                break;
            }
        }
    }

    public void releaseAll(){
        release(mAllocated);
    }

    public void keepServiceAlive(Activity activity){
        for (MemAllocator container: containers){
            if(container.ismActivated()){
                container.activate(activity);
            }
        }
    }

    public void allocateMost(){
        for (MemAllocator container: containers){
            container.allocate(10);
        }
    }

    private int mActiveProcessNumber = 0;
    public int getActiveThreadNumber(){
        return mActiveProcessNumber;
    }
}
