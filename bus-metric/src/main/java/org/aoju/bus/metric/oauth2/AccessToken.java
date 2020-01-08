/*
 * The MIT License
 *
 * Copyright (c) 2020 aoju.org All rights reserved.
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
package org.aoju.bus.metric.oauth2;

/**
 * @author Kimi Liu
 * @version 5.5.1
 * @since JDK 1.8++
 */
public class AccessToken {

    /**
     * 过期时间,毫秒
     */
    private long expireTimeMillis;
    private OpenUser openUser;

    /**
     * @param expireIn 有效时长，秒
     * @param openUser 用户
     */
    public AccessToken(long expireIn, OpenUser openUser) {
        super();
        if (expireIn <= 0) {
            throw new IllegalArgumentException("expireIn必须大于0");
        }
        this.expireTimeMillis = System.currentTimeMillis() + (expireIn * 1000);
        this.openUser = openUser;
    }

    public boolean isExpired() {
        return expireTimeMillis < System.currentTimeMillis();
    }

    public long getExpireTimeMillis() {
        return expireTimeMillis;
    }

    public void setExpireTimeMillis(long expireTimeMillis) {
        this.expireTimeMillis = expireTimeMillis;
    }

    public OpenUser getOpenUser() {
        return openUser;
    }

    public void setOpenUser(OpenUser openUser) {
        this.openUser = openUser;
    }

}
