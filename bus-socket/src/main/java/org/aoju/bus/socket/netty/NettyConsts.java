/*
 * The MIT License
 *
 * Copyright (c) 2017 aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.socket.netty;

/**
 * @author Kimi Liu
 * @version 5.3.0
 * @since JDK 1.8+
 */
public class NettyConsts {

    public static final String EVENT = "e";
    public static final String TOPIC = "t";
    public static final String DATA = "d";

    public static final String SUBSCRIBE = "subscribe";
    public static final String MESSAGE = "message";
    public static final String CANCEL = "cancel";
    public static final String HEARTBEAT = "heartbeat";

    public static final String HEARTBEAT_TEXT = "{\"e\":\"heartbeat\",\"d\":\"ping\"}";

    public static final String TOPIC_ALL = "all";

    public static int BOSS_GROUP_THREADS = 1;
    public static int WORKER_GROUP_THREADS = 0;
    public static String END_POINT = "/ws";

}
