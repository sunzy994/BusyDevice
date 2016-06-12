package cmcm.com.busydevice.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;

import cmcm.com.busydevice.BuildConfig;
import cmcm.com.busydevice.utils.CPUUtils;
import cmcm.com.busydevice.utils.ProcessInfoHelperImpl;
import cmcm.com.busydevice.activity.MainActivity;
import cmcm.com.busydevice.utils.ThreadPoll;

/**
 * Created by sunzy on 16-6-3.
 */
public class PollingService extends Service {

    public static final String THREAD_COMMAND = "thread_command";
    public static final int COMMAND_START = 1;
    public static final int COMMAND_END = 2;
    public static final int COMMAND_END_ALL = 3;
    private int mThreadNumber = 0;

    private HandlerThread mMemThread;
    private HandlerThread mCpuThread;
    private Handler mMemHandler;
    private Handler mCpuHandler;
    private ThreadPoll mThreadPool;

    public static void startDefault(Context context){
        Intent intent = new Intent(context, PollingService.class);
        context.startService(intent);
    }

    public static void changeThread(Context context, int command){
        Intent intent = new Intent(context, PollingService.class);
        intent.putExtra(THREAD_COMMAND, command);
        context.startService(intent);
    }

    private void updateMem(){
        Intent intent2 = new Intent(MainActivity.ACTION_MEM_CHANGED);
        intent2.putExtra(MainActivity.EXTRA_MEM_MEGA, (int)(getMemUsed()/1024/1024));
        intent2.putExtra(MainActivity.EXTRA_MEM_PERCENT, getMemPercent());
        intent2.setPackage(getPackageName());
        sendBroadcast(intent2);
    }

    private void updateCpu(){
        Intent intent1 = new Intent(MainActivity.ACTION_CPU_CHANGED);
        intent1.putExtra(MainActivity.EXTRA_THREAD_NUMBER, mThreadNumber);
        intent1.putExtra(MainActivity.EXTRA_CPU_PERCENT, getCpuPercent());
        intent1.setPackage(getPackageName());
        sendBroadcast(intent1);
    }

    private long getMemUsed(){
        return ProcessInfoHelperImpl.getTotalMemoryByteDirect() - ProcessInfoHelperImpl.getAvailableMemoryByteDirect(this);
    }

    private int getMemPercent(){
        return (int)(100 - ProcessInfoHelperImpl.getAvailableMemoryByteDirect(this)*100/ProcessInfoHelperImpl.getTotalMemoryByteDirect());
    }

    private int getCpuPercent(){
        return CPUUtils.getCpuUsageStatistic()[0];
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //Log.v(BuildConfig.APPLICATION_ID, "cpu service on create!");
        mMemThread = new HandlerThread("mem_refresher", Thread.MAX_PRIORITY);
        mMemThread.start();
        mMemHandler = new Handler(mMemThread.getLooper()){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                updateMem();
                mMemHandler.sendEmptyMessageDelayed(9, 500L);
            }
        };
        mMemHandler.sendEmptyMessage(1);
        mCpuThread = new HandlerThread("cpu_refresher", Thread.MAX_PRIORITY);
        mCpuThread.start();
        mCpuHandler = new Handler(mCpuThread.getLooper()){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                updateCpu();
                sendEmptyMessage(9);
            }
        };
        mCpuHandler.sendEmptyMessage(9);
        mThreadPool = new ThreadPoll();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent == null){
            return START_STICKY;
        }
        switch (intent.getIntExtra(THREAD_COMMAND, 0)){
            case COMMAND_END:
                mThreadPool.end();
                mThreadNumber--;
                break;
            case COMMAND_END_ALL:
                mThreadNumber =0;
                        mThreadPool.endAll();
                break;
            case COMMAND_START:
                mThreadNumber++;
                mThreadPool.addThread();
                break;
        }
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }




}
