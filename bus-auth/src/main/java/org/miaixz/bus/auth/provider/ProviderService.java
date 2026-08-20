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

import org.miaixz.bus.auth.Provider;
import org.miaixz.bus.auth.registry.ResourceService;

/**
 * Defines external management persistence operations for protocol-neutral {@link Provider} entities.
 * <p>
 * The service inherits the unified resource contract without adding protocol execution, implicit Registry reload, or
 * implementation details. A Provider groups Sources and never declares an authentication protocol itself. A successful
 * persistence operation becomes active only after an external loader supplies a complete snapshot that the Registry
 * validates, compiles, and commits.
 * </p>
 *
 * @author Kimi Liu
 */
public interface ProviderService extends ResourceService<Provider> {
    // The complete service surface is inherited from ResourceService.

}
