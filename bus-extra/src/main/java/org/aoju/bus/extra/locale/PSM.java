package org.aoju.bus.extra.locale;

/**
 * @author Kimi Liu
 * @version 5.3.2
 * @since JDK 1.8+
 */
public abstract class PSM {

    public static final int ALL = 0;
    public static final int JAPANESE = 1;
    public static final int CHINESE = 2;
    public static final int SIMPLIFIED_CHINESE = 3;
    public static final int TRADITIONAL_CHINESE = 4;
    public static final int KOREAN = 5;

    public static final int NO_OF_LANGUAGES = 6;
    public static final int MAX_VERIFIERS = 16;

    Verifier[] mVerifier;
    Score[] mStatisticsData;

    EUC msampler = new EUC();
    byte[] mState = new byte[MAX_VERIFIERS];
    int[] mItemIdx = new int[MAX_VERIFIERS];

    int mItems;
    int mClassItems;

    boolean mDone;
    boolean mRunsampler;
    boolean mClassRunsampler;

    public PSM() {
        initVerifiers(PSM.ALL);
        reset();
    }

    public PSM(int langFlag) {
        initVerifiers(langFlag);
        reset();
    }

    public PSM(int aItems, Verifier[] aVerifierSet,
               Score[] aStatisticsSet) {
        mClassRunsampler = (aStatisticsSet != null);
        mStatisticsData = aStatisticsSet;
        mVerifier = aVerifierSet;

        mClassItems = aItems;
        reset();
    }


    public void reset() {
        mRunsampler = mClassRunsampler;
        mDone = false;
        mItems = mClassItems;

        for (int i = 0; i < mItems; i++) {
            mState[i] = 0;
            mItemIdx[i] = i;
        }

        msampler.reset();
    }

    protected void initVerifiers(int currVerSet) {
        int idx = 0;
        int currVerifierSet;

        if (currVerSet >= 0 && currVerSet < NO_OF_LANGUAGES) {
            currVerifierSet = currVerSet;
        } else {
            currVerifierSet = PSM.ALL;
        }

        mVerifier = null;
        mStatisticsData = null;

        // 繁体中文
        if (currVerifierSet == PSM.TRADITIONAL_CHINESE) {

            mVerifier = new Verifier[]{
                    new UTF8(),
                    new BIG5(),
                    new ISO2022CN(),
                    new EUCTW(),
                    new CP1252(),
                    new UCS2BE(),
                    new UCS2LE()
            };
            mStatisticsData = new Score[]{
                    null,
                    new Big5S(),
                    null,
                    new EUCTWS(),
                    null,
                    null,
                    null
            };
        }

        // 韩国语；朝鲜语
        else if (currVerifierSet == PSM.KOREAN) {
            mVerifier = new Verifier[]{
                    new UTF8(),
                    new EUCKR(),
                    new ISO2022KR(),
                    new CP1252(),
                    new UCS2BE(),
                    new UCS2LE()
            };
        }

        // 简体中文
        else if (currVerifierSet == PSM.SIMPLIFIED_CHINESE) {
            mVerifier = new Verifier[]{
                    new UTF8(),
                    new GB2312(),
                    new GB18030(),
                    new ISO2022CN(),
                    new HZ(),
                    new CP1252(),
                    new UCS2BE(),
                    new UCS2LE()
            };
        }

        // 日语
        else if (currVerifierSet == PSM.JAPANESE) {
            mVerifier = new Verifier[]{
                    new UTF8(),
                    new SJIS(),
                    new EUCJP(),
                    new ISO2022JP(),
                    new CP1252(),
                    new UCS2BE(),
                    new UCS2LE()
            };
        }

        // 中文
        else if (currVerifierSet == PSM.CHINESE) {
            mVerifier = new Verifier[]{
                    new UTF8(),
                    new GB2312(),
                    new GB18030(),
                    new BIG5(),
                    new ISO2022CN(),
                    new HZ(),
                    new EUCTW(),
                    new CP1252(),
                    new UCS2BE(),
                    new UCS2LE()
            };
            mStatisticsData = new Score[]{
                    null,
                    new GB2312S(),
                    null,
                    new Big5S(),
                    null,
                    null,
                    new EUCTWS(),
                    null,
                    null,
                    null
            };
        }

        //所有编码
        else if (currVerifierSet == PSM.ALL) {
            mVerifier = new Verifier[]{
                    new UTF8(),
                    new SJIS(),
                    new EUCJP(),
                    new ISO2022JP(),
                    new EUCKR(),
                    new ISO2022KR(),
                    new BIG5(),
                    new EUCTW(),
                    new GB2312(),
                    new GB18030(),
                    new ISO2022CN(),
                    new HZ(),
                    new CP1252(),
                    new UCS2BE(),
                    new UCS2LE()
            };

            mStatisticsData = new Score[]{
                    null,
                    null,
                    new EUCJPS(),
                    null,
                    new EUCKRS(),
                    null,
                    new Big5S(),
                    new EUCTWS(),
                    new GB2312S(),
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            };
        }

        mClassRunsampler = (mStatisticsData != null);
        mClassItems = mVerifier.length;

    }

    public abstract void report(String charset);

    public boolean handleData(byte[] aBuf, int len) {
        int i, j;
        byte b, st;

        for (i = 0; i < len; i++) {
            b = aBuf[i];

            for (j = 0; j < mItems; ) {
                st = Verifier.getNextState(mVerifier[mItemIdx[j]],
                        b, mState[j]);
                if (st == Verifier.eItsMe) {
                    report(mVerifier[mItemIdx[j]].charset());
                    mDone = true;
                    return mDone;
                } else if (st == Verifier.eError) {
                    mItems--;
                    if (j < mItems) {
                        mItemIdx[j] = mItemIdx[mItems];
                        mState[j] = mState[mItems];
                    }
                } else {
                    mState[j++] = st;
                }
            }

            if (mItems <= 1) {
                if (1 == mItems) {
                    report(mVerifier[mItemIdx[0]].charset());
                }
                mDone = true;
                return mDone;
            } else {
                int nonUCS2Num = 0;
                int nonUCS2Idx = 0;
                for (j = 0; j < mItems; j++) {
                    if ((!(mVerifier[mItemIdx[j]].isUCS2())) &&
                            (!(mVerifier[mItemIdx[j]].isUCS2()))) {
                        nonUCS2Num++;
                        nonUCS2Idx = j;
                    }
                }
                if (1 == nonUCS2Num) {
                    report(mVerifier[mItemIdx[nonUCS2Idx]].charset());
                    mDone = true;
                    return mDone;
                }
            }
        }

        if (mRunsampler) {
            sample(aBuf, len);
        }
        return mDone;
    }


    public void DataEnd() {

        if (mDone == true) {
            return;
        }

        if (mItems == 2) {
            if ((mVerifier[mItemIdx[0]].charset()).equals("GB18030")) {
                report(mVerifier[mItemIdx[1]].charset());
                mDone = true;
            } else if ((mVerifier[mItemIdx[1]].charset()).equals("GB18030")) {
                report(mVerifier[mItemIdx[0]].charset());
                mDone = true;
            }
        }

        if (mRunsampler) {
            sample(null, 0, true);
        }
    }

    public void sample(byte[] aBuf, int aLen) {
        sample(aBuf, aLen, false);
    }

    public void sample(byte[] aBuf, int aLen, boolean aLastChance) {
        int possibleCandidateNum = 0;
        int j;
        int eucNum = 0;

        for (j = 0; j < mItems; j++) {
            if (null != mStatisticsData[mItemIdx[j]])
                eucNum++;
            if ((!mVerifier[mItemIdx[j]].isUCS2()) &&
                    (!(mVerifier[mItemIdx[j]].charset()).equals("GB18030")))
                possibleCandidateNum++;
        }

        mRunsampler = (eucNum > 1);

        if (mRunsampler) {
            mRunsampler = msampler.sample(aBuf, aLen);
            if (((aLastChance && msampler.getSomeData()) ||
                    msampler.enoughData())
                    && (eucNum == possibleCandidateNum)) {
                msampler.calFreq();

                int bestIdx = -1;
                int eucCnt = 0;
                float bestScore = 0.0f;
                for (j = 0; j < mItems; j++) {
                    if ((null != mStatisticsData[mItemIdx[j]]) &&
                            (!(mVerifier[mItemIdx[j]].charset()).equals("Big5"))) {
                        float score = msampler.getScore(
                                mStatisticsData[mItemIdx[j]].mFirstByteFreq(),
                                mStatisticsData[mItemIdx[j]].mFirstByteWeight(),
                                mStatisticsData[mItemIdx[j]].mSecondByteFreq(),
                                mStatisticsData[mItemIdx[j]].mSecondByteWeight());
                        if ((0 == eucCnt++) || (bestScore > score)) {
                            bestScore = score;
                            bestIdx = j;
                        }
                    }
                }
                if (bestIdx >= 0) {
                    report(mVerifier[mItemIdx[bestIdx]].charset());
                    mDone = true;
                }
            }
        }
    }

    public String[] getProbableCharsets() {
        if (mItems <= 0) {
            String[] nomatch = new String[1];
            nomatch[0] = "nomatch";
            return nomatch;
        }

        String ret[] = new String[mItems];
        for (int i = 0; i < mItems; i++) {
            ret[i] = mVerifier[mItemIdx[i]].charset();
        }
        return ret;
    }

}
