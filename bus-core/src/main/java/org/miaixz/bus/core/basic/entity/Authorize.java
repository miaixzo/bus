/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.core.basic.entity;

import java.io.Serial;

import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Represents access authorization information.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class Authorize extends Query {

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
     * The identifier for the current namespace.
     */
    @Transient
    protected String x_namespace_id;

    /**
     * The name of the current namespace.
     */
    @Transient
    protected String x_namespace_name;

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
    protected String x_raw_data;

}
