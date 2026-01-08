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
package org.miaixz.bus.setting.format;

import java.util.Objects;

import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.setting.Format;
import org.miaixz.bus.setting.metric.ini.IniComment;
import org.miaixz.bus.setting.metric.ini.IniElement;
import org.miaixz.bus.setting.metric.ini.IniProperty;
import org.miaixz.bus.setting.metric.ini.IniSection;

/**
 * The default line formatter for INI files. It uses a chain of responsibility pattern, attempting to parse a line as a
 * comment, a section, or a property, in that order.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class DefaultFormatter implements Format {

    /**
     * Formatter for comment lines.
     */
    protected final ElementFormatter<IniComment> commentElementFormatter;
    /**
     * Formatter for section headers.
     */
    protected final ElementFormatter<IniSection> sectionElementFormatter;
    /**
     * Formatter for property lines (key-value pairs).
     */
    protected final ElementFormatter<IniProperty> propertyElementFormatter;
    /**
     * The most recently parsed section, used to associate properties with it.
     */
    protected IniSection lastSection;
    /**
     * The current physical line number being read.
     */
    private int lineNumber = 0;
    /**
     * The current effective line number, excluding empty lines.
     */
    private int effectiveLineNumber = 0;

    /**
     * Constructs a new DefaultFormatter with the specified element formatters.
     *
     * @param commentElementFormatter  The formatter for comments.
     * @param sectionElementFormatter  The formatter for sections.
     * @param propertyElementFormatter The formatter for properties.
     */
    public DefaultFormatter(ElementFormatter<IniComment> commentElementFormatter,
            ElementFormatter<IniSection> sectionElementFormatter,
            ElementFormatter<IniProperty> propertyElementFormatter) {
        this.commentElementFormatter = commentElementFormatter;
        this.sectionElementFormatter = sectionElementFormatter;
        this.propertyElementFormatter = propertyElementFormatter;
    }

    /**
     * Formats a raw line from an INI file into an {@link IniElement}. Empty lines are ignored and return null.
     *
     * @param raw The raw string data for the line.
     * @return The parsed {@link IniElement}, or null for an empty line.
     * @throws InternalException if the line cannot be parsed as a comment, section, or property.
     */
    @Override
    public IniElement formatLine(String raw) {
        Objects.requireNonNull(raw);
        lineNumber++;
        String line = raw.trim();
        if (line.isEmpty()) {
            return null;
        }

        IniElement element;
        int preEffectiveLineNumber = effectiveLineNumber + 1;

        if (commentElementFormatter.check(line)) {
            element = commentElementFormatter.format(line, preEffectiveLineNumber);
        } else if (sectionElementFormatter.check(line)) {
            IniSection section = sectionElementFormatter.format(line, preEffectiveLineNumber);
            lastSection = section;
            element = section;
        } else if (propertyElementFormatter.check(line)) {
            IniProperty property = propertyElementFormatter.format(line, preEffectiveLineNumber);
            if (null == lastSection) {
                throw new InternalException("Cannot find section for property on line " + lineNumber + ": " + line);
            }
            property.setSection(lastSection);
            lastSection.add(property);
            element = property;
        } else {
            throw new InternalException("No matching element type found for line " + lineNumber + ": " + line);
        }

        effectiveLineNumber = preEffectiveLineNumber;
        return element;
    }

    /**
     * Resets the formatter to its initial state, clearing the line counters and the last seen section. This should be
     * called before parsing a new file.
     */
    @Override
    public synchronized void init() {
        lineNumber = 0;
        effectiveLineNumber = 0;
        lastSection = null;
    }

}
