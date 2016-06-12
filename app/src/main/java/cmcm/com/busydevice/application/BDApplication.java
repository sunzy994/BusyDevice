package cmcm.com.busydevice.application;

import android.app.Application;

/**
 * Created by sunzy on 16-6-3.
 */
public class BDApplication extends Application {

    private static Application mAppplicatoin;

    @Override
    public void onCreate() {
        super.onCreate();
        mAppplicatoin = this;
    }

    public static Application getAppplicatoinContext(){
        return mAppplicatoin;
    }
}
