package cmcm.com.busydevice.utils;

import java.io.FileInputStream;

public class MemInfoReader {
    byte[] mBuffer = new byte[1024];

    private long mTotalSize = 1024*1024*1024;
    private long mFreeSize = 64*1024*1024;
    private long mCachedSize = 64*1024*1024;

    private boolean matchText(byte[] buffer, int index, String text) {
        int N = text.length();
        if ((index+N) >= buffer.length) {
            return false;
        }
        for (int i=0; i<N; i++) {
            if (buffer[index+i] != text.charAt(i)) {
                return false;
            }
        }
        return true;
    }

    private long extractMemValue(byte[] buffer, int index) {
        while (index < buffer.length && buffer[index] != '\n') {
            if (buffer[index] >= '0' && buffer[index] <= '9') {
                int start = index;
                index++;
                while (index < buffer.length && buffer[index] >= '0'
                    && buffer[index] <= '9') {
                    index++;
                }
                String str = new String(buffer, 0, start, index-start);
                return ((long)Integer.parseInt(str)) * 1024;
            }
            index++;
        }
        return 0;
    }

    public void readMemInfo() {
        // Permit disk reads here, as /proc/meminfo isn't really "on
        // disk" and should be fast.  TODO: make BlockGuard ignore
        // /proc/ and /sys/ files perhaps?
        try {
            mTotalSize = 0;
            mFreeSize = 0;
            mCachedSize = 0;
            FileInputStream is = new FileInputStream("/proc/meminfo");
            int len = is.read(mBuffer);
            is.close();
            final int BUFLEN = mBuffer.length;
            int count = 0;
            for (int i=0; i<len && count < 3; i++) {
                if (matchText(mBuffer, i, "MemTotal")) {
                    i += 8;
                    mTotalSize = extractMemValue(mBuffer, i);
                    count++;
                } else if (matchText(mBuffer, i, "MemFree")) {
                    i += 7;
                    mFreeSize = extractMemValue(mBuffer, i);
                    count++;
                } else if (matchText(mBuffer, i, "Cached")) {
                    i += 6;
                    mCachedSize = extractMemValue(mBuffer, i);
                    count++;
                }
                while (i < BUFLEN && mBuffer[i] != '\n') {
                    i++;
                }
            }
        } catch (java.io.FileNotFoundException e) {
        } catch (java.io.IOException e) {
        } finally {
        }
    }

    /**
     * 返回总内存大小，单位是字节
     * */
    public long getTotalSize() {
        return mTotalSize;
    }

    /**
     * 返回空闲内存大小，单位是字节
     * */
    public long getFreeSize() {
        return mFreeSize;
    }

    /**
     * 返回缓存内存大小，单位是字节
     * */
    public long getCachedSize() {
        return mCachedSize;
    }
}
