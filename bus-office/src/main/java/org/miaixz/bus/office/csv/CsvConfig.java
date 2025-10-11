/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.office.csv;

import java.io.Serial;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

import org.miaixz.bus.core.lang.Symbol;

/**
 * Base CSV configuration options. These options can be used for both reading and writing CSV, defining symbols such as
 * field separators and text delimiters.
 *
 * @param <T> The type of the subclass, used for returning {@code this}.
 * @author Kimi Liu
 * @since Java 17+
 */
public class CsvConfig<T extends CsvConfig<T>> implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852282577197L;

    /**
     * Field separator, default is comma ','.
     */
    protected char fieldSeparator = Symbol.C_COMMA;
    /**
     * Text delimiter, default is double quotes '"'
     */
    protected char textDelimiter = Symbol.C_DOUBLE_QUOTES;
    /**
     * Comment character, used to distinguish comment lines, default is '#'.
     */
    protected Character commentCharacter = Symbol.C_HASH;
    /**
     * Header alias map.
     */
    protected Map<String, String> headerAlias = new LinkedHashMap<>();

    /**
     * Sets the field separator. Default is comma ','.
     *
     * @param fieldSeparator The field separator character.
     * @return This configuration object, for chaining.
     */
    public T setFieldSeparator(final char fieldSeparator) {
        this.fieldSeparator = fieldSeparator;
        return (T) this;
    }

    /**
     * Sets the text delimiter. Default is double quotes '"'
     *
     * @param textDelimiter The text delimiter character.
     * @return This configuration object, for chaining.
     */
    public T setTextDelimiter(final char textDelimiter) {
        this.textDelimiter = textDelimiter;
        return (T) this;
    }

    /**
     * Disables comment handling. When writing CSV, {@link CsvWriter#writeComment(String)} will throw an exception. When
     * reading CSV, comment lines will be read as normal lines.
     *
     * @return This configuration object, for chaining.
     */
    public T disableComment() {
        return setCommentCharacter(null);
    }

    /**
     * Sets the comment character, used to distinguish comment lines. {@code null} indicates that comments should be
     * ignored.
     *
     * @param commentCharacter The comment character.
     * @return This configuration object, for chaining.
     */
    public T setCommentCharacter(final Character commentCharacter) {
        this.commentCharacter = commentCharacter;
        return (T) this;
    }

    /**
     * Sets the header alias map.
     *
     * @param headerAlias The map of header aliases.
     * @return This configuration object, for chaining.
     */
    public T setHeaderAlias(final Map<String, String> headerAlias) {
        this.headerAlias = headerAlias;
        return (T) this;
    }

    /**
     * Adds a header alias.
     *
     * @param header The original header name.
     * @param alias  The alias for the header.
     * @return This configuration object, for chaining.
     */
    public T addHeaderAlias(final String header, final String alias) {
        this.headerAlias.put(header, alias);
        return (T) this;
    }

    /**
     * Removes a header alias.
     *
     * @param header The header name whose alias is to be removed.
     * @return This configuration object, for chaining.
     */
    public T removeHeaderAlias(final String header) {
        this.headerAlias.remove(header);
        return (T) this;
    }

}
