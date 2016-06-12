package cmcm.com.busydevice.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import cmcm.com.busydevice.BuildConfig;
import cmcm.com.busydevice.service.PollingService;
import cmcm.com.busydevice.R;
import cmcm.com.busydevice.service.Service1;
import cmcm.com.busydevice.utils.MemManager;

/**
 * 本应用的目的是通过开辟进程，提高手机占用内存，通过开辟新线程，占用cpu
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String ACTION_MEM_CHANGED = "mem_changed";
    public static final String ACTION_CPU_CHANGED = "cpu_changed";
    public static final String EXTRA_MEM_MEGA = ":mem_mega";
    public static final String EXTRA_MEM_PERCENT = ":mem_percent";
    public static final String EXTRA_THREAD_NUMBER = ":thread_number";
    public static final String EXTRA_CPU_PERCENT = ":cpu_percent";

    private TextView mMemUsed;
    private TextView mMemUsedPercent;
    private TextView mProcessNumber;
    private TextView mThreadNumber;
    private TextView mCpuUsedPercent;

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent != null){
                if(TextUtils.equals(ACTION_MEM_CHANGED, intent.getAction())){
                    updateMem(intent);
                } else if(TextUtils.equals(ACTION_CPU_CHANGED, intent.getAction())){
                    updateCpu(intent);
                }
            }
        }
    };

    private boolean mRegistered = false;

    private void updateMem(Intent intent){
        int mem = intent.getIntExtra(EXTRA_MEM_MEGA, 0);
        int memPercent = intent.getIntExtra(EXTRA_MEM_PERCENT, 0);
        //Log.v(BuildConfig.APPLICATION_ID, "memory used up to " + mem + "M, " + memPercent + "%");
        mMemUsed.setText(mem + "M");
        mMemUsedPercent.setText(memPercent + "%");
        mProcessNumber.setText("" + MemManager.getInstance().getActiveThreadNumber());
        //MemManager.getInstance().keepServiceAlive(this);
    }

    private void updateCpu(Intent intent){
        if(intent != null){
            mCpuUsedPercent.setText(intent.getIntExtra(EXTRA_CPU_PERCENT, 0) + "%");
            mThreadNumber.setText(intent.getIntExtra(EXTRA_THREAD_NUMBER, 0) + "");
        }
    }

    private void registerReciever(){
        if(!mRegistered){
            mRegistered = true;
            IntentFilter filter = new IntentFilter();
            filter.addAction(ACTION_CPU_CHANGED);
            filter.addAction(ACTION_MEM_CHANGED);
            registerReceiver(mReceiver, filter);
        }
    }

    private void unregisterReciever(){
        if(mRegistered){
            mRegistered = false;
            try {
                unregisterReceiver(mReceiver);
            } catch (Exception e){
                e.printStackTrace();
            }

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        findViewById(R.id.fab).setVisibility(View.GONE);
        initViews();
        PollingService.startDefault(this);
        registerReciever();
        //MemManager.getInstance().allocateMost();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void initViews(){
        mMemUsed = (TextView)findViewById(R.id.mem_used);
        mMemUsedPercent = (TextView)findViewById(R.id.mem_used_percent);
        mThreadNumber = (TextView)findViewById(R.id.thread_number);
        mCpuUsedPercent = (TextView)findViewById(R.id.cpu_used_percent);
        mProcessNumber = (TextView)findViewById(R.id.process_number);
        findViewById(R.id.alloc).setOnClickListener(this);
           findViewById(R.id.release).setOnClickListener(this);
//        findViewById(R.id.alloc_10).setOnClickListener(this);
//        findViewById(R.id.alloc_100).setOnClickListener(this);
//        findViewById(R.id.alloc_1000).setOnClickListener(this);
//        findViewById(R.id.release_10).setOnClickListener(this);
//        findViewById(R.id.release_100).setOnClickListener(this);
//        findViewById(R.id.release_1000).setOnClickListener(this);
        findViewById(R.id.end_thread).setOnClickListener(this);
        findViewById(R.id.start_thread).setOnClickListener(this);
        findViewById(R.id.stop_and_exit).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
//            case R.id.alloc_10:
//                toastResult(MemManager.getInstance().allocate(10));
//                break;
//            case R.id.alloc_100:
//                toastResult(MemManager.getInstance().allocate(100));
//                break;
//            case R.id.alloc_1000:
//                MemManager.getInstance().allocate(1000);
//                break;
//            case R.id.release_10:
//                MemManager.getInstance().release(10);
//                break;
//            case R.id.release_100:
//                MemManager.getInstance().release(100);
//                break;
//            case R.id.release_1000:
//                MemManager.getInstance().release(1000);
//                break;
            case R.id.alloc:
                MemManager.getInstance().allocate(this);
                break;
            case R.id.release:
                MemManager.getInstance().release(this);
                break;
            case R.id.end_thread:
                PollingService.changeThread(this, PollingService.COMMAND_END);
                break;
            case R.id.start_thread:
                PollingService.changeThread(this, PollingService.COMMAND_START);
                break;
            case R.id.stop_and_exit:
                unregisterReceiver(mReceiver);
                PollingService.changeThread(this, PollingService.COMMAND_END_ALL);
                MemManager.getInstance().releaseAll();
                finish();
                break;
        }
    }

    private void toastResult(boolean success){
        if(!success){
            Toast.makeText(this, "操作失败！", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            AboutActivity.startDefault(this);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReciever();
    }
}
