/*
 * The MIT License
 *
 * Copyright (c) 2017 aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.office.registry;

import org.aoju.bus.office.magic.Formats;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Kimi Liu
 * @version 5.3.1
 * @since JDK 1.8+
 */
public class SimpleFormatRegistry implements FormatRegistry {

    private List<Formats> documents = new ArrayList<Formats>();

    public void addFormat(Formats document) {
        documents.add(document);
    }

    public Formats getFormatByExtension(String extension) {
        if (extension == null) {
            return null;
        }
        String lowerExtension = extension.toLowerCase();
        for (Formats format : documents) {
            if (format.getExtension().equals(lowerExtension)) {
                return format;
            }
        }
        return null;
    }

    public Formats getFormatByMediaType(String mediaType) {
        if (mediaType == null) {
            return null;
        }
        for (Formats format : documents) {
            if (format.getMediaType().equals(mediaType)) {
                return format;
            }
        }
        return null;
    }

    public Set<Formats> getOutputFormats(Formats.Type family) {
        Set<Formats> formats = new HashSet<Formats>();
        for (Formats format : documents) {
            if (format.getStoreProperties(family) != null) {
                formats.add(format);
            }
        }
        return formats;
    }

}
