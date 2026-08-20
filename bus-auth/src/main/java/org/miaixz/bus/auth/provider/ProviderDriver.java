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
package org.miaixz.bus.auth.provider;

import org.miaixz.bus.auth.source.SourceDriver;

/**
 * Binds one server-role protocol profile to a Source runtime factory.
 * <p>
 * The historical name describes the protocol role and does not make the persistence Provider protocol-aware. This is a
 * Source driver specialization and receives protocol configuration only from Source.
 * </p>
 *
 * @param <S> exact immutable server-role Source settings type decoded at the driver boundary
 * @author Kimi Liu
 */
public interface ProviderDriver<S extends ProviderSettings> extends SourceDriver<S> {

    /**
     * Returns the management and capability profile owned by this driver.
     *
     * @return exact Provider profile
     */
    ProviderProfile<S> profile();

}
