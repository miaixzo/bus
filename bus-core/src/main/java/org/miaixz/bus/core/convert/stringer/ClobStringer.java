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
package org.miaixz.bus.core.convert.stringer;

import java.io.Reader;
import java.sql.Clob;
import java.sql.SQLException;
import java.util.function.Function;

import org.miaixz.bus.core.lang.exception.ConvertException;
import org.miaixz.bus.core.xyz.IoKit;

/**
 * Converts a {@link Clob} to a String.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class ClobStringer implements Function<Object, String> {

    /**
     * Constructs a new ClobStringer. Utility class constructor for static access.
     */
    private ClobStringer() {
    }

    /**
     * Singleton instance.
     */
    public static final ClobStringer INSTANCE = new ClobStringer();

    /**
     * Converts a {@link Clob} object to a String.
     *
     * @param clob The {@link Clob} to be converted.
     * @return The String representation of the Clob.
     * @throws ConvertException if a {@link SQLException} occurs.
     */
    private static String toString(final Clob clob) {
        Reader reader = null;
        try {
            reader = clob.getCharacterStream();
            return IoKit.read(reader);
        } catch (final SQLException e) {
            throw new ConvertException(e);
        } finally {
            IoKit.closeQuietly(reader);
        }
    }

    /**
     * Apply method.
     *
     * @return the String value
     */
    @Override
    public String apply(final Object o) {
        return toString((Clob) o);
    }

}
