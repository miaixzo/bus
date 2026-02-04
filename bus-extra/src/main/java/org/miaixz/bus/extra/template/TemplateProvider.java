/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.extra.template;

import org.miaixz.bus.core.lang.Wrapper;

/**
 * Interface for template engine providers. Implementations of this interface wrap specific template engines, providing
 * a unified way to initialize and retrieve templates.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface TemplateProvider extends Wrapper<Object> {

    /**
     * Initializes the template engine with the specified configuration.
     *
     * @param config The {@link TemplateConfig} containing initialization parameters for the template engine.
     * @return This {@link TemplateProvider} instance, initialized with the given configuration.
     */
    TemplateProvider init(TemplateConfig config);

    /**
     * Retrieves a {@link Template} object based on the provided resource. The interpretation of the resource string
     * depends on the specific template engine implementation; it could be the template content itself or a relative
     * path to the template file.
     *
     * @param resource The resource identifier for the template.
     * @return A {@link Template} instance ready for rendering.
     */
    Template getTemplate(String resource);

}
