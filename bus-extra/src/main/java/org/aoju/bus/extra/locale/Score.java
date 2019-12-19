package org.aoju.bus.extra.locale;

/**
 * @author Kimi Liu
 * @version 5.3.3
 * @since JDK 1.8+
 */
public abstract class Score {

    public Score() {
    }

    public abstract float[] mFirstByteFreq();

    public abstract float mFirstByteStdDev();

    public abstract float mFirstByteMean();

    public abstract float mFirstByteWeight();

    public abstract float[] mSecondByteFreq();

    public abstract float mSecondByteStdDev();

    public abstract float mSecondByteMean();

    public abstract float mSecondByteWeight();

}
