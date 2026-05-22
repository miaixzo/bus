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
package org.miaixz.bus.starter.mapper;

import org.springframework.boot.context.properties.ConfigurationProperties;

import org.miaixz.bus.mapper.runtime.MapperOptions;
import org.miaixz.bus.spring.GeniusBuilder;

/**
 * Spring Boot configuration properties for MyBatis Mapper.
 * <p>
 * The pure option model is inherited from {@link MapperOptions}; this starter type only keeps the Spring Boot binding
 * entry point. Resource resolution, bean registration, and schema scanning are intentionally handled by
 * {@link MapperConfiguration} so this class remains a thin configuration holder.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@ConfigurationProperties(prefix = GeniusBuilder.MAPPER)
public class MapperProperties extends MapperOptions {

    /**
     * Constructs a new MapperProperties instance for Spring Boot property binding.
     */
    public MapperProperties() {
        // No initialization required.
    }

}
