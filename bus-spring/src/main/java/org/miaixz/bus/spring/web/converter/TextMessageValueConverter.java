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
package org.miaixz.bus.spring.web.converter;

import org.springframework.http.converter.StringHttpMessageConverter;

import org.miaixz.bus.core.lang.Charset;

/**
 * Reads and writes strings with UTF-8 as the default charset.
 *
 * @author Kimi Liu
 */
public class TextMessageValueConverter extends StringHttpMessageConverter implements MessageValueConverter<String> {

    /**
     * Creates a UTF-8 string converter.
     */
    public TextMessageValueConverter() {
        super(Charset.UTF_8);
    }

    /**
     * Returns the diagnostic name of the Bus text converter.
     *
     * @return converter name
     */
    @Override
    public String name() {
        return "BusText";
    }

    /**
     * Orders the string converter after the Bus JSON converter.
     *
     * @return converter order
     */
    @Override
    public int getOrder() {
        return 100;
    }

}
