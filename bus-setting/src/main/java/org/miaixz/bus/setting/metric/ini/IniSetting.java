/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.setting.metric.ini;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.StringJoiner;

import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Keys;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.setting.metric.props.Props;

/**
 * Represents the in-memory structure of an INI file, extending {@code ArrayList<IniElement>}. An empty line in the INI
 * file is represented by a {@code null} element in this list. To create an instance of this class programmatically, use
 * the {@link INI} builder.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class IniSetting extends ArrayList<IniElement> {

    /**
     * Constructs an empty list with an initial capacity of ten.
     */
    public IniSetting() {
    }

    /**
     * Constructs an empty list with the specified initial capacity.
     *
     * @param initialCapacity the initial capacity of the list.
     */
    public IniSetting(int initialCapacity) {
        super(initialCapacity);
    }

    /**
     * Constructs a list containing the elements of the specified collection.
     *
     * @param c the collection whose elements are to be placed into this list.
     */
    public IniSetting(Collection<? extends IniElement> c) {
        super(c);
    }

    /**
     * Returns a string representation of the INI data, formatted as a valid INI file.
     *
     * @return The INI content as a string.
     */
    @Override
    public String toString() {
        if (this.isEmpty()) {
            return Normal.EMPTY;
        } else {
            String newLineSplit = getNewLineSplit();
            StringJoiner joiner = new StringJoiner(newLineSplit);
            for (IniElement iniElement : this) {
                // A null element represents an empty line.
                joiner.add(null == iniElement ? Normal.EMPTY : iniElement.toString());
            }
            return joiner.toString();
        }
    }

    /**
     * Gets the system-dependent newline character sequence.
     *
     * @return The newline string.
     */
    private String getNewLineSplit() {
        return System.getProperty(Keys.LINE_SEPARATOR, Symbol.LF);
    }

    /**
     * Converts the INI data into a {@link Props} object. The keys in the resulting properties will be a combination of
     * the section name and the property key, joined by the specified delimiter.
     * <p>
     * For example, with a delimiter of '.':
     * 
     * <pre>
     * [se1]
     * key1=value1
     * </pre>
     * 
     * becomes a property with the key {@code "se1.key1"}.
     *
     * @param delimiter The string to use for joining the section name and property key. If null, only the property key
     *                  is used.
     * @return A {@link Props} object representing the INI data.
     */
    public Props toProperties(String delimiter) {
        final Props prop = new Props();
        for (IniElement next : this) {
            if (next != null && next.isProperty()) {
                String pk;
                IniProperty inip = (IniProperty) next;
                if (null != delimiter) {
                    pk = inip.getSection().value() + delimiter + inip.key();
                } else {
                    pk = inip.key();
                }
                prop.setProperty(pk, next.value());
            }
        }
        return prop;
    }

    /**
     * Converts the INI data into a {@link Props} object using '.' as the delimiter between section names and keys.
     *
     * @return A {@link Props} object.
     * @see #toProperties(String)
     */
    public Props toProperties() {
        return toProperties(Symbol.DOT);
    }

    /**
     * Writes the INI content to an {@link OutputStream}.
     *
     * @param out         The output stream to write to.
     * @param charset     The character set to use for encoding.
     * @param withComment If {@code true}, comments will be included in the output.
     * @throws IOException if an I/O error occurs.
     */
    public void write(OutputStream out, java.nio.charset.Charset charset, boolean withComment) throws IOException {
        String text;
        for (IniElement element : this) {
            if (!withComment && element != null && element.isComment()) {
                continue;
            }
            text = null == element ? getNewLineSplit()
                    : withComment ? element + getNewLineSplit() : element.toNoCommentString() + getNewLineSplit();
            out.write(text.getBytes(charset));
        }
        out.flush();
    }

    /**
     * Writes the INI content to an {@link OutputStream} using UTF-8 encoding.
     *
     * @param out         The output stream to write to.
     * @param withComment If {@code true}, comments will be included in the output.
     * @throws IOException if an I/O error occurs.
     * @see #write(OutputStream, java.nio.charset.Charset, boolean)
     */
    public void write(OutputStream out, boolean withComment) throws IOException {
        write(out, Charset.UTF_8, withComment);
    }

    /**
     * Writes the INI content to a {@link Writer}.
     *
     * @param writer      The writer to use.
     * @param withComment If {@code true}, comments will be included in the output.
     * @throws IOException if an I/O error occurs.
     */
    public void write(Writer writer, boolean withComment) throws IOException {
        String text;
        for (IniElement element : this) {
            if (!withComment && element != null && element.isComment()) {
                continue;
            }
            text = null == element ? getNewLineSplit()
                    : withComment ? element + getNewLineSplit() : element.toNoCommentString() + getNewLineSplit();
            writer.write(text);
        }
        writer.flush();
    }

    /**
     * Writes the INI content to a {@link PrintStream}.
     *
     * @param print       The print stream to write to.
     * @param withComment If {@code true}, comments will be included in the output.
     */
    public void write(PrintStream print, boolean withComment) {
        String text;
        for (IniElement element : this) {
            if (!withComment && element != null && element.isComment()) {
                continue;
            }
            text = null == element ? Normal.EMPTY : withComment ? element.toString() : element.toNoCommentString();
            print.println(text);
        }
        print.flush();
    }

    /**
     * Writes the INI content to a {@link File}.
     *
     * @param file        The destination file.
     * @param charset     The character set to use.
     * @param withComment If {@code true}, comments will be included in the output.
     * @throws IOException if an I/O error occurs.
     */
    public void write(File file, java.nio.charset.Charset charset, boolean withComment) throws IOException {
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            file.createNewFile();
        }
        try (BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file))) {
            write(out, charset, withComment);
        }
    }

    /**
     * Writes the INI content to a {@link File} using UTF-8 encoding.
     *
     * @param file        The destination file.
     * @param withComment If {@code true}, comments will be included in the output.
     * @throws IOException if an I/O error occurs.
     */
    public void write(File file, boolean withComment) throws IOException {
        write(file, org.miaixz.bus.core.lang.Charset.UTF_8, withComment);
    }

    /**
     * Writes the INI content to a {@link Path}.
     *
     * @param path        The destination path.
     * @param charset     The character set to use.
     * @param withComment If {@code true}, comments will be included in the output.
     * @throws IOException if an I/O error occurs.
     */
    public void write(Path path, java.nio.charset.Charset charset, boolean withComment) throws IOException {
        if (!Files.exists(path)) {
            Files.createDirectories(path.getParent());
            Files.createFile(path);
        }
        try (BufferedOutputStream out = new BufferedOutputStream(Files.newOutputStream(path))) {
            write(out, charset, withComment);
        }
    }

    /**
     * Writes the INI content to a {@link Path} using UTF-8 encoding.
     *
     * @param path        The destination path.
     * @param withComment If {@code true}, comments will be included in the output.
     * @throws IOException if an I/O error occurs.
     */
    public void write(Path path, boolean withComment) throws IOException {
        write(path, Charset.UTF_8, withComment);
    }

}
