package org.aoju.bus.core.io;

import java.io.IOException;
import java.io.OutputStream;

/**
 * 此OutputStream写出数据到<b>/dev/null</b>，既忽略所有数据<br>
 * 来自 Apache Commons io
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class NullOutputStream extends OutputStream {
 
    /**
     * 什么也不做，写出到<code>/dev/null</code>.
     *
     * @param b   写出的数据
     * @param off 开始位置
     * @param len 长度
     */
    @Override
    public void write(byte[] b, int off, int len) {
        // to /dev/null
    }

    /**
     * 什么也不做，写出到 <code>/dev/null</code>.
     *
     * @param b 写出的数据
     */
    @Override
    public void write(int b) {
        // to /dev/null
    }

    /**
     * 什么也不做，写出到 <code>/dev/null</code>.
     *
     * @param b 写出的数据
     * @throws IOException 不抛出
     */
    @Override
    public void write(byte[] b) throws IOException {
        // to /dev/null
    }

}
