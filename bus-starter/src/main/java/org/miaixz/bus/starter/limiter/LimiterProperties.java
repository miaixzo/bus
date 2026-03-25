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
package org.miaixz.bus.starter.limiter;

import lombok.Getter;
import lombok.Setter;
import org.miaixz.bus.limiter.Context;
import org.miaixz.bus.spring.GeniusBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for rate limiting and circuit breaking (downgrading).
 * <p>
 * This class is bound to properties under the prefix specified by {@code GeniusBuilder.LIMITER}. It extends
 * {@link Context}, allowing all properties of the core limiter context to be configured directly in the application's
 * configuration files (e.g., {@code application.yml}).
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Getter
@Setter
@ConfigurationProperties(GeniusBuilder.LIMITER)
public class LimiterProperties extends Context {

}
