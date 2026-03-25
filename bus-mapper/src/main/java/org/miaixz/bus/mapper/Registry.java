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
package org.miaixz.bus.mapper;

import java.util.List;

import org.apache.ibatis.builder.annotation.ProviderContext;
import org.apache.ibatis.mapping.MappedStatement;
import org.miaixz.bus.core.lang.loader.spi.NormalSpiLoader;
import org.miaixz.bus.core.xyz.ListKit;
import org.miaixz.bus.mapper.parsing.TableMeta;

/**
 * An interface for custom processing of {@link MappedStatement}. Implement this interface and register it via SPI to
 * customize MappedStatements.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public interface Registry {

    /**
     * The default SPI (Service Provider Interface) extension implementation. It loads and executes all registered
     * {@link Registry} implementations.
     */
    Registry SPI = new Registry() {

        private final List<Registry> registries = ListKit.of(NormalSpiLoader.loadList(Registry.class));

        @Override
        public void customize(TableMeta entity, MappedStatement ms, ProviderContext context) {
            for (Registry registry : registries) {
                registry.customize(entity, ms, context);
            }
        }
    };

    /**
     * Customizes the given {@link MappedStatement}.
     *
     * @param entity  The entity table metadata.
     * @param ms      The {@link MappedStatement} object to be customized.
     * @param context The provider context, containing information about the mapper method and interface.
     */
    void customize(TableMeta entity, MappedStatement ms, ProviderContext context);

}
