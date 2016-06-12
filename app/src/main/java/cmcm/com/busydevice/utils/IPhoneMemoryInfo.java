package cmcm.com.busydevice.utils;



import android.os.Parcel;
import android.os.Parcelable;

public interface IPhoneMemoryInfo extends Parcelable {
	
	/**
	 * 当前数据是真实内存值
	 * */
	public static final int STATE_REAL_DATA = 1;
	
	/**
	 * 处于进程清理后的内存不增时间段内
	 * */
	public static final int STATE_CACHED_DATA = 2;
	
	
	public static final int TIMEOUT_SHORT = 1000 * 20;
	public static final int TIMEOUT_LONG = 1000 * 80;
	
	
	static final int DEFAULT_USED_MEMORY_PERCENT = 85;
	
	
	/**
	 * 获取当前数据状态
	 * @return IPhoneMemoryInfo.STATE_REAL_DATA</br>
	 * IPhoneMemoryInfo.STATE_CACHED_DATA
	 * */
	public int getState();
		
	/**
	 * 获取可用内存，单位byte
	 * */
	public long getAvailableMemoryByte();
	
	/**
	 * 获取总内存，单位byte
	 * */
	public long getTotalMemoryByte();
	
	
	/**
	 * 获取内存已用内存占比 
	 * */
	public int getUsedMemoryPercentage();

    /**
     * 判断当前是否处于缓存期间内
     * @return
     */
    public boolean isInCache();
	
	
	public static final Creator<IPhoneMemoryInfo> CREATOR = new Creator<IPhoneMemoryInfo>() {
		@Override
		public IPhoneMemoryInfo createFromParcel(Parcel source) {
			PhoneMemoryInfo pmi = new PhoneMemoryInfo();
			pmi.mTotalMemoryByte = source.readLong();
			pmi.mAvailableMemoryByte = source.readLong();
			pmi.mUsedMemoryPercent = source.readInt();
			pmi.mState = source.readInt();
			pmi.mAvailableMemoryByteReal = source.readLong();

            boolean[] booleanArry = source.createBooleanArray();
            if(booleanArry != null && booleanArry.length == 1){
                pmi.mIsCache = booleanArry[0];
            }

			return pmi;
		}

		@Override
		public IPhoneMemoryInfo[] newArray(int size) {
			return new PhoneMemoryInfo[size];
		}
	};
}
