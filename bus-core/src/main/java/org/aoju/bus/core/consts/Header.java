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
package org.aoju.bus.core.consts;

/**
 * HTTP header 对象
 *
 * @author Kimi Liu
 * @version 3.6.6
 * @since JDK 1.8+
 */
public interface Header {

    /**
     * The header Accept
     */
    String ACCEPT = "Accept";
    /**
     * The header Accept-Charset
     */
    String ACCEPT_CHARSET = "Accept-Charset";
    /**
     * The header Accept-Encoding
     */
    String ACCEPT_ENCODING = "Accept-Encoding";
    /**
     * The header Accept-Language
     */
    String ACCEPT_LANGUAGE = "Accept-Language";
    /**
     * The header Accept-Ranges
     */
    String ACCEPT_RANGES = "Accept-Ranges";
    /**
     * The header Age
     */
    String AGE = "Age";
    /**
     * The header Allow
     */
    String ALLOW = "Allow";
    /**
     * The header Cache-Control
     */
    String CACHE_CONTROL = "Cache-Control";
    /**
     * The header Connection
     */
    String CONNECTION = "Connection";
    /**
     * The header Content-Encoding
     */
    String CONTENT_ENCODING = "Content-Encoding";
    /**
     * The header Content-Language
     */
    String CONTENT_LANGUAGE = "Content-Language";
    /**
     * The header Content-Length
     */
    String CONTENT_LENGTH = "Content-Length";
    /**
     * The header Content-Location
     */
    String CONTENT_LOCATION = "Content-Location";
    /**
     * The header Content-MD5
     */
    String CONTENT_MD5 = "Content-MD5";
    /**
     * The header Content-Range
     */
    String CONTENT_RANGE = "Content-Range";
    /**
     * The header Content-Type
     */
    String CONTENT_TYPE = "Content-Type";
    /**
     * The header Content-Disposition
     */
    String CONTENT_DISPOSITION = "Content-Disposition";
    /**
     * The header User-Agent
     */
    String USER_AGENT = "User-Agent";
    /**
     * The header Transfer-Encoding
     */
    String TRANSFER_ENCODING = "Transfer-Encoding";

    /**
     * Get the name of the Header.
     *
     * @return the name of the Header,  never {@code null}
     */
    String getName();

    /**
     * Get the value of the Header.
     *
     * @return the value of the Header,  may be {@code null}
     */
    String getValue();
}
