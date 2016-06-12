package cmcm.com.busydevice.utils;

import android.os.Parcel;

public class PhoneMemoryInfo implements IPhoneMemoryInfo {

	PhoneMemoryInfo() {
		
	}
	

	public PhoneMemoryInfo(long available, long total) {
		
		flush(available, total);
	}
	

	public void flush(long available, long total) {
		mAvailableMemoryByteReal = available;
		mTotalMemoryByte = total;

		mState = STATE_REAL_DATA;
		mAvailableMemoryByte = available;

		if (0 < mTotalMemoryByte && (mTotalMemoryByte > mAvailableMemoryByte)) {
			mUsedMemoryPercent = (int) (((mTotalMemoryByte - mAvailableMemoryByte) * 100f) / mTotalMemoryByte);
		} else {
			mUsedMemoryPercent = DEFAULT_USED_MEMORY_PERCENT; // default
		}
	}
	

	@Override
	public int describeContents() {
		return 0;
	}


	@Override
	public int getState() {
		return mState;
	}
	

	@Override
	public long getTotalMemoryByte() {
		return mTotalMemoryByte;
	}
	
	@Override
	public long getAvailableMemoryByte() {
		return mAvailableMemoryByte;
	}
	

	@Override
	public int getUsedMemoryPercentage() {
		return mUsedMemoryPercent;
	}

    @Override
    public boolean isInCache() {
        return mIsCache;
    }

    @Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(mTotalMemoryByte);
		dest.writeLong(mAvailableMemoryByte);
		dest.writeInt(mUsedMemoryPercent);
		dest.writeInt(mState);
		dest.writeLong(mAvailableMemoryByteReal);
        dest.writeBooleanArray(new boolean[]{mIsCache});
	}
	

		
	long mTotalMemoryByte;
	long mAvailableMemoryByte;
	int mUsedMemoryPercent;
	int mState;
	long mAvailableMemoryByteReal;
    boolean mIsCache;
	
}
