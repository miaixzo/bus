/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
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
 ********************************************************************************/
package org.aoju.bus.socket.netty;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.netty.channel.ChannelHandlerContext;
import org.aoju.bus.logger.Logger;

/**
 * @author Kimi Liu
 * @version 5.9.9
 * @since JDK 1.8+
 */
public class RequestDecoder {

    public SocketRequest decode(ChannelHandlerContext ctx, String message) {
        try {

            SocketRequest request = new SocketRequest();
            JSONObject input = JSON.parseObject(message);

            request.setContext(ctx);

            if (input.containsKey(NettyConsts.EVENT)) {
                String event = input.getString(NettyConsts.EVENT);
                request.setEvent(event);
            }

            if (input.containsKey(NettyConsts.TOPIC)) {
                String[] topic = input.getObject(NettyConsts.TOPIC, String[].class);
                request.setTopic(topic);
            }

            if (input.containsKey(NettyConsts.DATA)) {
                String data = input.getString(NettyConsts.DATA);
                request.setData(data);
            }

            return request;
        } catch (Exception e) {
            Logger.error("SocketRequest decode exception!", e);
            return null;
        }
    }

}
