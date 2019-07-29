package org.aoju.bus.cache.entity;

/**
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public interface Expire {

    int NO = -1;

    int FOREVER = 0;

    int ONE_SEC = 1000;

    int FIVE_SEC = 4 * ONE_SEC;

    int TEN_SEC = 2 * FIVE_SEC;

    int ONE_MIN = 6 * TEN_SEC;

    int FIVE_MIN = 5 * ONE_MIN;

    int TEN_MIN = 2 * FIVE_MIN;

    int HALF_HOUR = 30 * TEN_MIN;

    int ONE_HOUR = 2 * HALF_HOUR;

    int TWO_HOUR = 2 * ONE_HOUR;

    int SIX_HOUR = 3 * TWO_HOUR;

    int TWELVE_HOUR = 2 * SIX_HOUR;

    int ONE_DAY = 2 * TWELVE_HOUR;

    int TWO_DAY = 2 * ONE_DAY;

    int ONE_WEEK = 7 * ONE_DAY;
}