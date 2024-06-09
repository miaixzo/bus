/*
~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
~                                                                               ~
~ The MIT License (MIT)                                                         ~
~                                                                               ~
~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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
package ${entityUrl};

import lombok.Getter;
import lombok.Setter;
import entity.base.org.miaixz.bus.BaseEntity;
import javax.persistence.Table;
<#if isSwagger=="true" >
    import io.swagger.annotations.ApiModelProperty;
</#if>

/**
* ${entityComment}实体类
*
* @version: ${version}
* @author: ${author}
* @since Java 17+
*/
@Getter
@Setter
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
        <#else>
            /**
            * ${ci.comment}
            */
        </#if>
        private ${ci.javaType} ${ci.property};
    </#if>
</#list>

}
	