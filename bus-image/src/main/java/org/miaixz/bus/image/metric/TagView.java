/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.miaixz.bus.image.metric;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.toolkit.StringKit;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class TagView {

    private final TagCamel[] tag;
    private final String format;

    public TagView(TagCamel... tag) {
        this(null, tag);
    }

    public TagView(String format, TagCamel... tag) {
        this.tag = tag;
        this.format = format;
    }

    public TagCamel[] getTag() {
        return tag;
    }

    public String getFormat() {
        return format;
    }

    public boolean containsTag(TagCamel tag) {
        for (TagCamel tagW : this.tag) {
            if (tagW.equals(tag)) {
                return true;
            }
        }
        return false;
    }

    public String getFormattedText(boolean anonymize, Readable... tagable) {
        for (TagCamel t : this.tag) {
            if (!anonymize || t.getAnonymizationType() != 1) {
                String text = t.getFormattedTagValue(TagValue.getTagValue(t, tagable), format);
                if (StringKit.hasText(text)) {
                    return text;
                }
            }
        }
        return Normal.EMPTY;
    }

}
