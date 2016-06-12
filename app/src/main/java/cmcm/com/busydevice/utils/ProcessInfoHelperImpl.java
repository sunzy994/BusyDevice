package cmcm.com.busydevice.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Debug.MemoryInfo;


import java.io.BufferedReader;
import java.io.FileReader;
import java.lang.reflect.Method;
import java.util.ArrayList;


/**
 * 超时也在其内部控制
 * 
 * @note 不要直接调用这个函数，应该调用 ProcessInfoHelper 系列函数
 * */
public class ProcessInfoHelperImpl {
		
	public static final int INVALID_UID = -1;
	public static final String UI_PID = "ui_pid";
	
	static final long UI_PROCESS_MEMORY_QUERY_INTERVAL = 1000 * 5;
	
	static final long MAX_UI_MEMORY_HAD = 1024l * 1024l * 1024l; // 内存不会超过1G吧？？
	static final long MAX_TOTAL_MEMORY = 1024l * 1024l * 1024l * 100l; // 100G了，我x，不可能比这还高吧？？

	private Context mContext;
	
	public ProcessInfoHelperImpl(Context context) {
		this.mContext = context;
		mMemory = new PhoneMemoryInfo(getAvailableMemoryByteDirect(context), getTotalMemoryByte());
	}
	
	/**
	 * 当前内存数据是否还是缓存
	 * */
	private boolean isMemoryDataInCache() {
        boolean cache = mTimeStamp > 0
                && (System.currentTimeMillis() - mTimeStamp < mTIMEOUT);
        mMemory.mIsCache = cache;
        return cache;
	}
	
	
	private long getTotalMemoryByte() {
		if (sTotalMemByte > 1) {
			return sTotalMemByte;
		}
		
		sTotalMemByte = getTotalMemoryByteDirect();
		return sTotalMemByte;
	}

	private long getProcessMemory(Context ctx, int pid) {
		ActivityManager am = (ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE);
		
		ArrayList<Integer> pids = new ArrayList<Integer>();
		pids.add(pid);
		
		return getProcessMemory(am, pids);
	}
	
	/**
	 * ui有两种状态，在和没在。
	 * ui在启动时候就记录pid到pref，这里先查询该pid是否存在，如果不存在，则标识没ui进程。
	 * 如果存在，查值，为加速，两次查询间有一个最新时间间隔UI_PROCESS_MEMORY_QUERY_INTERVAL
	 * 
	 * */
	private boolean isNeedFlushUIProcessMemory() {
		return UI_PROCESS_MEMORY_QUERY_INTERVAL < System.currentTimeMillis() - mGetUIProcessMemoryTimeStamp; 
	}
	
	private long mGetUIProcessMemoryTimeStamp = 0;
	private long mUIProcessMemory = 0;
	
	private long mTimeStamp = 0;
	private int mTIMEOUT = IPhoneMemoryInfo.TIMEOUT_SHORT;
	private PhoneMemoryInfo mMemory = null;
	
	
	// -----------------------
	
	/**
	 * 获得可用内存大小，不再调用AMS，单位是字节
	 * 
	 * @note 不要直接调用这个函数，应该调用 ProcessInfoHelper 系列函数
	 * @return
	 */
	public static long getAvailableMemoryByteDirect(Context context) {
		MemInfoReader reader = new MemInfoReader();
		reader.readMemInfo();
		
		long freed = (reader.getFreeSize() + reader.getCachedSize());
		
		// total赋值校验
		{
			long total = reader.getTotalSize();
			
			if (0 < total && sTotalMemByte < total
					&& total < MAX_TOTAL_MEMORY) {
				sTotalMemByte = reader.getTotalSize();
			}
		}	
		
		if (freed <= 0 || sTotalMemByte <= freed) {
			//freed = (long)(totalbyte * 0.05f);
			
			long repaired = 0L;
			ActivityManager.MemoryInfo mem = getMemoryInfo(context);
			if (mem != null && 0 < mem.availMem && mem.availMem < sTotalMemByte) {
				repaired = mem.availMem;
			} else {
				repaired = (long)(sTotalMemByte * 0.15f);
			}
		
			freed = repaired;
		}
		
		return freed;
	}
	
	/**
	 * 单位是byte。</br>
	 * TotalMem可能返回0，会引起Bug，所以先修改为返回1，防止除法为0异常
	 * @note 不要直接调用这个函数，应该调用 ProcessInfoHelper 系列函数
	 * */
	
	public static long getTotalMemoryByteDirect() {
		
		String str1 = "/proc/meminfo";// 系统内存信息文件
		String str2;
		String strResult;
		long initial_memory = 0;
		try {
			FileReader localFileReader = new FileReader(str1);
			BufferedReader localBufferedReader = new BufferedReader(localFileReader, 8192);
			str2 = localBufferedReader.readLine();// 读取meminfo第一行，系统总内存大小
			strResult = str2.substring(str2.indexOf(":") + 1, str2.indexOf("kB")).trim();
			initial_memory = Integer.valueOf(strResult);
			localBufferedReader.close();
			long result = initial_memory * 1024l; // 注意单位！！！！！
			
			if (0 < result && sTotalMemByte < result && result < MAX_TOTAL_MEMORY) {
				sTotalMemByte = result;
			}
			
			return result;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if (sTotalMemByte < 0) {
			return 1;
		} else {
			return sTotalMemByte;
		}		
	}
	
	private static long sTotalMemByte = -1;
	
	
	/**
	 * 单位是byte
	 * */
	private static ActivityManager.MemoryInfo getMemoryInfo(Context context) {		
		if (context == null) {
			return null;
		}
		ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
		
		try {
			ActivityManager activityMgr = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
			activityMgr.getMemoryInfo(memoryInfo);	
		} catch (SecurityException e) {
			return memoryInfo;
		}
		
		return memoryInfo;
	} 	
	//把 所有 关联的PID 的内存值加起来
	static long getProcessMemory(ActivityManager am, ArrayList<Integer> pids) {
		
		if (pids == null || pids.size() == 0) {
			return 0;
		}
		
		int pidCount = pids.size();
		int[] ipids = new int[pidCount];
		long memory = 0;

		for (int i = 0; i < pidCount; i++) {
			ipids[i] = pids.get(i);
		}

		try {
			MemoryInfo[] memoryInfoArray = getMemoryInfosByPids(am, ipids);
			if (memoryInfoArray != null && memoryInfoArray.length > 0) {
				for ( MemoryInfo mInfo : memoryInfoArray ) {
					memory += getTotalPssMemory(mInfo);
				}	
			}			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return memory * 1024;
	}
	
	
	/**
	 * 反射获得占用内存大小
	 */
	private static int getTotalPssMemory(MemoryInfo mi) {
		try {
			if (sMethodGetTotalPss == null) {
				sMethodGetTotalPss = mi.getClass().getMethod("getTotalPss");
			}
			return (Integer) sMethodGetTotalPss.invoke(mi);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

	private static Method sMethodGetTotalPss = null;

	/**
	 * 反射获得MemoryInfo
	 */
	private static MemoryInfo[] getMemoryInfosByPids(ActivityManager am, int[] pids) {
		
		try {
			if (sMethodGetProcessMemoryInfo == null) {
				sMethodGetProcessMemoryInfo = ActivityManager.class.getMethod("getProcessMemoryInfo", int[].class);
			}
			return (MemoryInfo[]) sMethodGetProcessMemoryInfo.invoke(am, pids);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	private static Method sMethodGetProcessMemoryInfo = null;
}
