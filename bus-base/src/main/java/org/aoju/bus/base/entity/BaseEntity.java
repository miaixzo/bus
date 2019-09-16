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

import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

/**
 * <p>
 * Entity 基本信息.
 * </p>
 *
 * @author Kimi Liu
 * @version 3.2.8
 * @since JDK 1.8
 */
@MappedSuperclass
@Data
public class BaseEntity extends Tracer {

    private static final long serialVersionUID = -601369123580520198L;

    @ApiModelProperty(value = "状态", notes = "-1删除,0无效,1正常")
    protected String status;

    @ApiModelProperty("创建人")
    protected String creator;

    @ApiModelProperty("创建时间")
    protected String created;

    @ApiModelProperty("修改人")
    protected String modifier;

    @ApiModelProperty("修改时间")
    protected String modified;

    @Transient
    @ApiModelProperty(value = "页码", notes = "默认值:1")
    protected Integer pageNo = 1;

    @Transient
    @ApiModelProperty(value = "分页大小", notes = "默认值:20")
    protected Integer pageSize = 20;

}
