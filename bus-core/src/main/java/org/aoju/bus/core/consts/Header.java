/*
 * Copyright (C) 2016-2017 mzlion(mzllon@qq.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.aoju.bus.core.consts;

/**
 * HTTP header对象
 *
 * @author mzlion on 2016/12/8.
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
