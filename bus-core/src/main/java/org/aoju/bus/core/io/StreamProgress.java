package org.aoju.bus.core.io;

/**
 * Stream进度条
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public interface StreamProgress {

    /**
     * 开始
     */
    void start();

    /**
     * 进行中
     *
     * @param progressSize 已经进行的大小
     */
    void progress(long progressSize);

    /**
     * 结束
     */
    void finish();
}
