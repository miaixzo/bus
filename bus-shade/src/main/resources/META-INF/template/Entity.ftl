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
package ${entityUrl};


import org.aoju.bus.base.entity.BaseEntity;
<#if isSwagger=="true" >
    import io.swagger.annotations.ApiModelProperty;
</#if>
import lombok.Data;

import javax.persistence.Table;

/**
* ${entityComment}实体类
*
* @version: ${version}
* @author: ${author}
* @since JDK 1.8+
*/
@Data
@Table(name = "${table}")
public class ${entityName} extends BaseEntity {

private static final long serialVersionUID = ${agile}L;

<#list cis as ci>
    <#if ci.property !="id"
    && ci.property !="status"
    && ci.property !="creator"
    && ci.property !="created"
    && ci.property !="modifier"
    && ci.property !="modified">
        <#if isSwagger=="true" >
            @ApiModelProperty(value = "${ci.comment}")
        </#if>
        private ${ci.javaType} ${ci.property};
    </#if>
</#list>
}
	