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
package org.miaixz.bus.limiter.support.rate.redis;

import org.miaixz.bus.core.lang.Normal;
import org.redisson.RedissonObject;
import org.redisson.api.RFuture;
import org.redisson.client.codec.Codec;
import org.redisson.client.codec.LongCodec;
import org.redisson.client.protocol.RedisCommand;
import org.redisson.client.protocol.RedisCommands;
import org.redisson.command.CommandAsyncExecutor;

import java.util.Arrays;
import java.util.Objects;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class RedisRatelimiterObject extends RedissonObject {


    public RedisRatelimiterObject(Codec codec, CommandAsyncExecutor commandExecutor, String name) {
        super(codec, commandExecutor, name);
    }

    public RedisRatelimiterObject(CommandAsyncExecutor commandExecutor, String name) {
        super(commandExecutor, name);
    }

    public boolean tryAcquire(long permits, double rate, long capacity) {
        if (permits > capacity) return false;
        return get(tryAcquireAsync(RedisCommands.EVAL_LONG, permits, rate, capacity)) != -1;
    }

    private <T> RFuture<T> tryAcquireAsync(RedisCommand<T> command, long permits, double rate, long capacity) {
        String hash = Objects.hash(rate, capacity) + Normal.EMPTY;
        return commandExecutor.evalWriteAsync(getName(), LongCodec.INSTANCE, command,
                "local capacity = tonumber(ARGV[1])\n" +
                        "local rate = tonumber(ARGV[2])\n" +
                        "local acq = tonumber(ARGV[3])\n" +
                        "if (redis.call('exists',KEYS[1]) == 0 or redis.call('hget',KEYS[1],'hash') ~= ARGV[4]) then\n" +
                        "    -- create\n" +
                        "    redis.call('hset', KEYS[1], 'capacity ', capacity)\n" +
                        "    redis.call('hset', KEYS[1], 'rate', rate)\n" +
                        "    redis.call('hset', KEYS[1], 'permit', capacity-acq)\n" +
                        "    redis.call('hset', KEYS[1], 'hash', ARGV[4])\n" +
                        "    redis.call('expire', KEYS[1],3153600000000)\n" +
                        "    redis.call('hset', KEYS[1], 'pttl',3153600000000000)\n" +
                        "    return capacity-acq\n" +
                        "end\n" +
                        "local rate = tonumber(redis.call('hget', KEYS[1],'rate'))\n" +
                        "local now = redis.call('pttl', KEYS[1])\n" +
                        "local pttl = tonumber(redis.call('hget', KEYS[1], 'pttl'))\n" +
                        "local permit = tonumber(redis.call('hget', KEYS[1], 'permit'))\n" +
                        "local newPermit = permit + ((pttl-now)/1000*rate)\n" +
                        "if(newPermit >= capacity) then\n" +
                        "     redis.call('hset', KEYS[1], 'permit', capacity-acq)\n" +
                        "     redis.call('hset', KEYS[1], 'pttl', now)\n" +
                        "     return capacity-acq\n" +
                        "end\n" +
                        "if(newPermit >= acq) then\n" +
                        "    redis.call('hset', KEYS[1], 'permit', newPermit-acq)\n" +
                        "    redis.call('hset', KEYS[1], 'pttl', now)\n" +
                        "    return newPermit-acq\n" +
                        "end\n" +
                        "return -1 ",
                Arrays.asList(getName()),
                permits, rate, capacity, hash);
    }


}
