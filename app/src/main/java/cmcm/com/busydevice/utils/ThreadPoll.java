package cmcm.com.busydevice.utils;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by sunzy on 16-6-6.
 */
public class ThreadPoll {

    private ArrayList<MyThread> mThreads = new ArrayList<>();

    public void addThread(){
        MyThread thread = new MyThread();
        thread.setPriority(Thread.MAX_PRIORITY);
        thread.start();
        mThreads.add(thread);
    }

    public void endAll(){
        for (MyThread thread:mThreads){
            thread.end();
        }
    }

    public void end(){
        for (MyThread thread:mThreads){
            if(thread.running) {
                thread.end();
                break;
            };
        }
    }

    public int getThreadCount(){
        int count = 0;
        for (MyThread thread:mThreads){
            if(thread.running) {
                count ++;
            };
        }
        return count;
    }

    static class MyThread extends Thread{

        private boolean running = true;


        @Override
        public void run() {
            super.run();
            while (running){
                Log.v("sunzy", "runing...");
                int a = 0;
                a++;
            }
        }

        void end(){
            running = false;
        }

        boolean isRunning(){
            return running;
        }
    }
}

