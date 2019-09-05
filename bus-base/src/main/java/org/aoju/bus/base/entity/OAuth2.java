/*
 * The MIT License
 *
 * Copyright (c) 2017, aoju.org All rights reserved.
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
package org.aoju.bus.base.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Transient;

/**
 * <p>
 * 授权公用类
 * </p>
 *
 * @author Kimi Liu
 * @version 3.1.9
 * @since JDK 1.8
 */
@Data
public class OAuth2 extends Entity {

    private static final long serialVersionUID = -611369123580520190L;

    @Transient
    @ApiModelProperty("当前用户ID")
    protected String x_user_id;

    @Transient
    @ApiModelProperty("当前用户名称")
    protected String x_user_name;

    @Transient
    @ApiModelProperty("当前用户工号")
    protected String x_user_code;

    @Transient
    @ApiModelProperty("当前用户角色ID")
    private String x_role_id;

    @Transient
    @ApiModelProperty("当前用户职称ID")
    private String x_duty_id;

    @Transient
    @ApiModelProperty("当前用户组织ID")
    private String x_org_id;

    @Transient
    @ApiModelProperty("可选参数信息")
    private String x_extract;

}
