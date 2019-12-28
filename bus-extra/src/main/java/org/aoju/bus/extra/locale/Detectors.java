package org.aoju.bus.extra.locale;

/**
 * @author Kimi Liu
 * @version 5.3.9
 * @since JDK 1.8+
 */
public class Detectors extends PSM implements Locales {

    Observer mObserver = null;

    public Detectors() {
        super();
    }

    public Detectors(int langFlag) {
        super(langFlag);
    }

    public void init(Observer aObserver) {
        mObserver = aObserver;
        return;
    }

    public boolean doIt(byte[] aBuf, int aLen, boolean oDontFeedMe) {
        if (aBuf == null || oDontFeedMe)
            return false;

        this.handleData(aBuf, aLen);
        return mDone;
    }

    public void report(String charset) {
        if (mObserver != null)
            mObserver.notify(charset);
    }

    public boolean isAscii(byte[] aBuf, int aLen) {
        for (int i = 0; i < aLen; i++) {
            if ((0x0080 & aBuf[i]) != 0) {
                return false;
            }
        }
        return true;
    }

}
