/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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

import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.setting.Format;
import org.miaixz.bus.setting.metric.ini.IniComment;
import org.miaixz.bus.setting.metric.ini.IniElement;
import org.miaixz.bus.setting.metric.ini.IniProperty;
import org.miaixz.bus.setting.metric.ini.IniSection;

import java.util.Objects;

/**
 * 默认的ini行格式器 需要三种格式器之一 {@link IniComment }, {@link IniSection }, {@link IniProperty }
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class DefaultFormatter implements Format {

    protected final ElementFormatter<IniComment> commentElementFormatter;
    protected final ElementFormatter<IniSection> sectionElementFormatter;
    protected final ElementFormatter<IniProperty> propertyElementFormatter;

    /**
     * last section
     */
    protected IniSection lastSection;

    /**
     * line number of read
     */
    private int lineNumber = 0;
    /**
     * line number of effective. empty line will not added.
     */
    private int effectiveLineNumber = 0;

    public DefaultFormatter(ElementFormatter<IniComment> commentElementFormatter,
            ElementFormatter<IniSection> sectionElementFormatter,
            ElementFormatter<IniProperty> propertyElementFormatter) {
        this.commentElementFormatter = commentElementFormatter;
        this.sectionElementFormatter = sectionElementFormatter;
        this.propertyElementFormatter = propertyElementFormatter;
    }

    /**
     * format line as element. if empty line, return null.
     *
     * @param raw line data
     * @return {@link IniElement}
     */
    @Override
    public IniElement formatLine(String raw) {
        Objects.requireNonNull(raw);
        // line number + 1
        lineNumber++;
        String line = raw.trim();
        // if empty line, return null
        if (line.length() == 0) {
            return null;
        }

        // format line.
        IniElement element;
        // pre effective line number, preEff = eff + 1
        int preEffectiveLineNumber = effectiveLineNumber + 1;

        // comment?
        if (commentElementFormatter.check(line)) {
            element = commentElementFormatter.format(line, preEffectiveLineNumber);
        } else
        // section?
        if (sectionElementFormatter.check(line)) {
            IniSection section = sectionElementFormatter.format(line, preEffectiveLineNumber);
            // save last section
            lastSection = section;
            element = section;
        } else
        // property ?
        if (propertyElementFormatter.check(line)) {
            IniProperty property = propertyElementFormatter.format(line, preEffectiveLineNumber);
            // set section if exists
            // In general it should be there, unless it's incorrectly formatted. If not, an exception is thrown.
            if (null == lastSection) {
                throw new InternalException("Cannot found section for property line " + lineNumber + " : " + line);
            }
            // set section for property
            property.setSection(lastSection);
            lastSection.add(property);
            element = property;
        } else {
            // None of them
            throw new InternalException("No matching element type found for line " + lineNumber + " : " + line);
        }

        // if no throw, update effective line number.
        effectiveLineNumber = preEffectiveLineNumber;
        return element;
    }

    /**
     * Back to the initial state
     */
    @Override
    public synchronized void init() {
        lineNumber = 0;
        effectiveLineNumber = 0;
        lastSection = null;
    }

}
