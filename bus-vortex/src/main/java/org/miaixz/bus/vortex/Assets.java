/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.vortex;

import java.util.Objects;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Represents an API definition or asset within the Vortex module. This class holds various properties defining an API
 * endpoint, including its identification, network details, request characteristics, and security settings.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@RequiredArgsConstructor
public class Assets {

    /**
     * The unique identifier for this asset.
     */
    private String id;
    /**
     * The name of the asset.
     */
    private String name;
    /**
     * The icon of the asset.
     */
    private String icon;
    /**
     * The host address of the server.
     */
    private String host;
    /**
     * The context path of the API.
     */
    private String path;
    /**
     * The port number of the server.
     */
    private Integer port;
    /**
     * The full URL of the method.
     */
    private String url;
    /**
     * The method Key.
     */
    private String method;
    /**
     * The routing mode: 1.HTTP, 2.MQ, 3.SSE, 4.STDIO ,5.OPENAPI, 6.STREAMABLE-HTTP.
     */
    private Integer mode;
    /**
     * The request type: 1.GET, 2.POST, 3.HEAD, 4.PUT, 5.PATCH, 6.DELETE, 7.OPTIONS, 8.TRACE.
     */
    private Integer type;
    /**
     * Authorization setting: 0. No validation, 1. required.
     */
    private Integer token;
    /**
     * Signature setting.
     */
    private Integer sign;
    /**
     * The scope of applicability.
     */
    private Integer scope;
    /**
     * The scope of applicability.
     */
    private Integer retries;
    /**
     * The scope of applicability.
     */
    private Integer balance;
    /**
     * The weight of applicability.
     */
    private Integer weight;
    /**
     * The scope of applicability.
     */
    private String args;
    /**
     * The scope of applicability.
     */
    private String command;
    /**
     * The metadata of applicability.
     */
    private String metadata;
    /**
     * Exception rules for firewall.
     */
    private Integer firewall;
    /**
     * The version of the asset.
     */
    private String version;
    /**
     * A description of the asset.
     */
    private String description;
    /**
     * The timeout duration in milliseconds for requests. Default is 10000 milliseconds.
     */
    private long timeout = 10000;

    /**
     * Compares this Assets object with the specified object for equality. The comparison is based on the {@code id}
     * field.
     *
     * @param o The object to compare with.
     * @return {@code true} if the objects are equal (have the same id), {@code false} otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (null == o || getClass() != o.getClass())
            return false;
        Assets assets = (Assets) o;
        return id.equals(assets.id);
    }

    /**
     * Returns a hash code value for the object. This hash code is based on the {@code id} field.
     *
     * @return A hash code value for this object.
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}
