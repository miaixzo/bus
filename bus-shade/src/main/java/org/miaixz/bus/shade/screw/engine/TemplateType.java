/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.shade.screw.engine;

import java.io.Serializable;

import org.miaixz.bus.core.lang.Normal;

import lombok.Getter;
import lombok.Setter;

/**
 * Enumeration of supported template types for document generation.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public enum TemplateType implements Serializable {

    /**
     * Freemarker template engine.
     */
    FREEMARKER(Normal.META_INF + "/shade/beans/", FreemarkerEngine.class, ".ftl");

    /**
     * The directory where templates are located.
     */
    @Getter
    @Setter
    private String templateDir;
    /**
     * The implementation class for the template engine.
     */
    @Getter
    @Setter
    private Class<? extends TemplateEngine> implClass;
    /**
     * The file extension for templates of this type.
     */
    @Getter
    @Setter
    private String suffix;

    /**
     * Constructs a {@code TemplateType} enum constant.
     *
     * @param templateDir The directory where templates are located.
     * @param implClass   The implementation class for the template engine.
     * @param suffix      The file extension for templates of this type.
     */
    TemplateType(String templateDir, Class<? extends TemplateEngine> implClass, String suffix) {
        this.templateDir = templateDir;
        this.implClass = implClass;
        this.suffix = suffix;
    }

}
