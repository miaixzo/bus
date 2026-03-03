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
package org.miaixz.bus.setting.metric.ini;

import org.miaixz.bus.core.lang.Symbol;

/**
 * Ini file's comment.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class IniCommentService extends AbstractElement implements IniComment {

    public IniCommentService(String value, String originalValue, int lineNumber) {
        super(value, originalValue, lineNumber);
    }

    public IniCommentService(String originalValue, int lineNumber) {
        super(originalValue.substring(1), originalValue, lineNumber);
    }

    /**
     * Get instance only based on value
     *
     * @param value      value
     * @param lineNumber line number
     * @return the object
     */
    public static IniCommentService byValue(String value, int lineNumber) {
        return new IniCommentService(value, Symbol.C_HASH + value, lineNumber);
    }

    /**
     * If the value changed, change the originalValue
     *
     * @param newValue when value changes, like {@link #setValue(String)} or
     *                 {@link #setValue(java.util.function.Function)}
     * @return the object
     */
    @Override
    protected String valueChanged(String newValue) {
        return "# " + trim(newValue);
    }

}
