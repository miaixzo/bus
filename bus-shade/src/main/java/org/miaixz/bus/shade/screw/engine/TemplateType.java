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
