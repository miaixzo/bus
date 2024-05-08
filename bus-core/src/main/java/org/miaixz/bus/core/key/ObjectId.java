/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.miaixz.bus.core.key;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;

import java.io.Serializable;
import java.lang.management.ManagementFactory;
import java.net.NetworkInterface;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.Enumeration;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 对象的全局唯一标识符
 * 由12个字节组成,分割如下:
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ObjectId implements Comparable<ObjectId>, Serializable {

    private static final long serialVersionUID = 1L;
    private static final int _genmachine;
    private static final AtomicInteger _nextInc = new AtomicInteger(
            (new Random()).nextInt());

    static {
        try {
            // build a 2-byte machine piece based on NICs info
            int machinePiece;
            {
                try {
                    StringBuilder sb = new StringBuilder();
                    Enumeration<NetworkInterface> e = NetworkInterface
                            .getNetworkInterfaces();
                    while (e.hasMoreElements()) {
                        NetworkInterface ni = e.nextElement();
                        sb.append(ni.toString());
                    }
                    machinePiece = sb.toString().hashCode() << Normal._16;
                } catch (Exception e) {
                    // exception sometimes happens with IBM JVM, use random
                    machinePiece = (new Random().nextInt()) << Normal._16;
                }
            }
            // add a 2 byte process piece. It must represent not only the JVM
            // but the class loader.
            // Since static var belong to class loader there could be collisions
            // otherwise
            final int processPiece;
            {
                int processId = new Random().nextInt();
                try {
                    processId = ManagementFactory.getRuntimeMXBean().getName()
                            .hashCode();
                } catch (Throwable t) {
                }
                ClassLoader loader = ObjectId.class.getClassLoader();
                int loaderId = null != loader ? System.identityHashCode(loader)
                        : 0;

                StringBuilder sb = new StringBuilder();
                sb.append(Integer.toHexString(processId));
                sb.append(Integer.toHexString(loaderId));
                processPiece = sb.toString().hashCode() & 0xFFFF;
            }
            _genmachine = machinePiece | processPiece;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private final int _time;
    private final int _machine;
    private final int _inc;
    private boolean _new;

    public ObjectId(Date time) {
        this(time, _genmachine, _nextInc.getAndIncrement());
    }

    public ObjectId(Date time, int inc) {
        this(time, _genmachine, inc);
    }

    public ObjectId(Date time, int machine, int inc) {
        _time = (int) (time.getTime() / 1000);
        _machine = machine;
        _inc = inc;
        _new = false;
    }

    /**
     * Creates a new instance from a string.
     *
     * @param s the string to convert
     * @throws IllegalArgumentException if the string is not a valid id
     */
    public ObjectId(String s) {
        this(s, false);
    }

    public ObjectId(String s, boolean babble) {

        if (!isValid(s))
            throw new IllegalArgumentException("invalid ObjectId [" + s + "]");

        if (babble)
            s = babbleToMongod(s);

        byte[] b = new byte[12];
        for (int i = 0; i < b.length; i++) {
            b[i] = (byte) Integer.parseInt(s.substring(i * 2, i * 2 + 2), Normal._16);
        }
        ByteBuffer bb = ByteBuffer.wrap(b);
        _time = bb.getInt();
        _machine = bb.getInt();
        _inc = bb.getInt();
        _new = false;
    }

    public ObjectId(byte[] b) {
        if (b.length != 12)
            throw new IllegalArgumentException("need 12 bytes");
        ByteBuffer bb = ByteBuffer.wrap(b);
        _time = bb.getInt();
        _machine = bb.getInt();
        _inc = bb.getInt();
        _new = false;
    }

    /**
     * Creates an ObjectId
     *
     * @param time    time in seconds
     * @param machine machine ID
     * @param inc     incremental value
     */
    public ObjectId(int time, int machine, int inc) {
        _time = time;
        _machine = machine;
        _inc = inc;
        _new = false;
    }

    /**
     * Create a new object id.
     */
    public ObjectId() {
        _time = (int) (System.currentTimeMillis() / 1000);
        _machine = _genmachine;
        _inc = _nextInc.getAndIncrement();
        _new = true;
    }

    /**
     * 获取ObjectId.
     *
     * @return the new id
     */
    public static String id() {
        return new ObjectId().toString();
    }

    /**
     * 获取ObjectId
     *
     * @return the new id
     */
    public static ObjectId get() {
        return new ObjectId();
    }

    /**
     * 检查字符串是否可以是ObjectId
     *
     * @param text 字符串
     * @return true/false
     */
    public static boolean isValid(String text) {
        if (null == text)
            return false;

        final int len = text.length();
        if (len != 24)
            return false;

        for (int i = 0; i < len; i++) {
            char c = text.charAt(i);
            if (c >= Symbol.C_ZERO && c <= Symbol.C_NINE)
                continue;
            if (c >= 'a' && c <= 'f')
                continue;
            if (c >= 'A' && c <= 'F')
                continue;

            return false;
        }
        return true;
    }

    /**
     * 将对象转换为ObjectId
     *
     * @param o 要转换的对象
     * @return objectid/null
     */
    public static ObjectId massageToObjectId(Object o) {
        if (null == o)
            return null;

        if (o instanceof ObjectId)
            return (ObjectId) o;

        if (o instanceof String) {
            String s = o.toString();
            if (isValid(s))
                return new ObjectId(s);
        }

        return null;
    }

    static String _pos(String s, int p) {
        return s.substring(p * 2, (p * 2) + 2);
    }

    public static String babbleToMongod(String b) {
        if (!isValid(b))
            throw new IllegalArgumentException("invalid object id: " + b);

        StringBuilder buf = new StringBuilder(24);
        for (int i = 7; i >= 0; i--)
            buf.append(_pos(b, i));
        for (int i = 11; i >= 8; i--)
            buf.append(_pos(b, i));

        return buf.toString();
    }

    /**
     * 获取生成的机器ID,标识机器/进程/类 加载程序
     *
     * @return the int
     */
    public static int getGenMachineId() {
        return _genmachine;
    }

    /**
     * 获取自动增量的当前值
     *
     * @return the int
     */
    public static int getCurrentInc() {
        return _nextInc.get();
    }

    public static int _flip(int x) {
        int z = 0;
        z |= ((x << 24) & 0xFF000000);
        z |= ((x << 8) & 0x00FF0000);
        z |= ((x >> 8) & 0x0000FF00);
        z |= ((x >> 24) & 0x000000FF);
        return z;
    }

    public int hashCode() {
        int x = _time;
        x += (_machine * 111);
        x += (_inc * 17);
        return x;
    }

    public boolean equals(Object o) {

        if (this == o)
            return true;

        ObjectId other = massageToObjectId(o);
        if (null == other)
            return false;

        return _time == other._time && _machine == other._machine
                && _inc == other._inc;
    }

    public String toStringBabble() {
        return babbleToMongod(toStringMongod());
    }

    public String toStringMongod() {
        byte[] b = toByteArray();

        StringBuilder buf = new StringBuilder(24);

        for (int i = 0; i < b.length; i++) {
            int x = b[i] & 0xFF;
            String s = Integer.toHexString(x);
            if (s.length() == 1)
                buf.append(Symbol.ZERO);
            buf.append(s);
        }

        return buf.toString();
    }

    public byte[] toByteArray() {
        byte[] b = new byte[12];
        ByteBuffer bb = ByteBuffer.wrap(b);
        // by default BB is big endian like we need
        bb.putInt(_time);
        bb.putInt(_machine);
        bb.putInt(_inc);
        return b;
    }

    public String toString() {
        return toStringMongod();
    }

    int _compareUnsigned(int i, int j) {
        long li = 0xFFFFFFFFL;
        li = i & li;
        long lj = 0xFFFFFFFFL;
        lj = j & lj;
        long diff = li - lj;
        if (diff < Integer.MIN_VALUE)
            return Integer.MIN_VALUE;
        if (diff > Integer.MAX_VALUE)
            return Integer.MAX_VALUE;
        return (int) diff;
    }

    public int compareTo(ObjectId id) {
        if (null == id)
            return -1;

        int x = _compareUnsigned(_time, id._time);
        if (x != 0)
            return x;

        x = _compareUnsigned(_machine, id._machine);
        if (x != 0)
            return x;

        return _compareUnsigned(_inc, id._inc);
    }

    public int getMachine() {
        return _machine;
    }

    /**
     * 获取此ID的时间(以毫秒为单位)
     *
     * @return the long
     */
    public long getTime() {
        return _time * 1000L;
    }

    /**
     * 获取此ID的时间(以秒为单位)
     *
     * @return the int
     */
    public int getTimeSecond() {
        return _time;
    }

    public int getInc() {
        return _inc;
    }

    public int _time() {
        return _time;
    }

    public int _machine() {
        return _machine;
    }

    public int _inc() {
        return _inc;
    }

    public boolean isNew() {
        return _new;
    }

    public void notNew() {
        _new = false;
    }

}
