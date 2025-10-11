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
package org.miaixz.bus.core.basic.entity;

import java.io.Serial;

import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Represents access authorization information.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Authorize extends Entity {

    @Serial
    private static final long serialVersionUID = 2852290950589L;

    /**
     * The identifier for the current tenant.
     */
    @Transient
    protected String x_tenant_id;

    /**
     * The name of the current tenant.
     */
    @Transient
    protected String x_tenant_name;

    /**
     * The identifier for the current user.
     */
    @Transient
    protected String x_user_id;

    /**
     * The email address of the current user.
     */
    @Transient
    protected String x_user_email;

    /**
     * The code or username of the current user.
     */
    @Transient
    protected String x_user_code;

    /**
     * The nickname of the current user.
     */
    @Transient
    protected String x_user_nick;

    /**
     * The full name of the current user.
     */
    @Transient
    protected String x_user_name;

    /**
     * The avatar URL of the current user.
     */
    @Transient
    protected String x_user_avatar;

    /**
     * The role identifier of the current user.
     */
    @Transient
    protected String x_role_id;

    /**
     * The duty or position identifier of the current user.
     */
    @Transient
    protected String x_duty_id;

    /**
     * The device identifier of the current user.
     */
    @Transient
    protected String x_device_id;

    /**
     * The identifier of the current application.
     */
    @Transient
    protected String x_app_id;

    /**
     * The identifier for the current API call.
     */
    @Transient
    protected String x_api_id;

    /**
     * Extended parameter information.
     */
    @Transient
    protected String x_extension;

}
