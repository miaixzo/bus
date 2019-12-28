package org.aoju.bus.extra.locale;

/**
 * @author Kimi Liu
 * @version 5.3.9
 * @since JDK 1.8+
 */
public class EUC {

    public int mFirstByteCnt[] = new int[94];
    public int mSecondByteCnt[] = new int[94];
    public float mFirstByteFreq[] = new float[94];
    public float mSecondByteFreq[] = new float[94];
    int mTotal = 0;
    int mThreshold = 200;
    int mState = 0;

    public EUC() {
        reset();
    }

    public void reset() {
        mTotal = 0;
        mState = 0;
        for (int i = 0; i < 94; i++)
            mFirstByteCnt[i] = mSecondByteCnt[i] = 0;
    }

    boolean enoughData() {
        return mTotal > mThreshold;
    }

    boolean getSomeData() {
        return mTotal > 1;
    }

    boolean sample(byte[] aIn, int aLen) {
        if (mState == 1)
            return false;

        int p = 0;

        int i;
        for (i = 0; (i < aLen) && (1 != mState); i++, p++) {
            switch (mState) {
                case 0:
                    if ((aIn[p] & 0x0080) != 0) {
                        if ((0xff == (0xff & aIn[p])) || (0xa1 > (0xff & aIn[p]))) {
                            mState = 1;
                        } else {
                            mTotal++;
                            mFirstByteCnt[(0xff & aIn[p]) - 0xa1]++;
                            mState = 2;
                        }
                    }
                    break;
                case 1:
                    break;
                case 2:
                    if ((aIn[p] & 0x0080) != 0) {
                        if ((0xff == (0xff & aIn[p]))
                                || (0xa1 > (0xff & aIn[p]))) {
                            mState = 1;
                        } else {
                            mTotal++;
                            mSecondByteCnt[(0xff & aIn[p]) - 0xa1]++;
                            mState = 0;
                        }
                    } else {
                        mState = 1;
                    }
                    break;
                default:
                    mState = 1;
            }
        }
        return (1 != mState);
    }

    void calFreq() {
        for (int i = 0; i < 94; i++) {
            mFirstByteFreq[i] = (float) mFirstByteCnt[i] / (float) mTotal;
            mSecondByteFreq[i] = (float) mSecondByteCnt[i] / (float) mTotal;
        }
    }

    float getScore(float[] aFirstByteFreq, float aFirstByteWeight,
                   float[] aSecondByteFreq, float aSecondByteWeight) {
        return aFirstByteWeight * getScore(aFirstByteFreq, mFirstByteFreq) +
                aSecondByteWeight * getScore(aSecondByteFreq, mSecondByteFreq);
    }

    float getScore(float[] array1, float[] array2) {
        float s;
        float sum = 0.0f;

        for (int i = 0; i < 94; i++) {
            s = array1[i] - array2[i];
            sum += s * s;
        }
        return (float) Math.sqrt(sum) / 94.0f;
    }
}


