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
package org.aoju.bus.office.magic;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Kimi Liu
 * @version 5.2.9
 * @since JDK 1.8+
 */
public class Formats {

    private String name;
    private String extension;
    private String mediaType;
    private Type inputFamily;
    private Map<String, ?> loadProperties;
    private Map<Type, Map<String, ?>> storePropertiesByFamily;

    public Formats() {
    }

    public Formats(String name, String extension, String mediaType) {
        this.name = name;
        this.extension = extension;
        this.mediaType = mediaType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public String getMediaType() {
        return mediaType;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    public Type getInputFamily() {
        return inputFamily;
    }

    public void setInputFamily(Type documentFamily) {
        this.inputFamily = documentFamily;
    }

    public Map<String, ?> getLoadProperties() {
        return loadProperties;
    }

    public void setLoadProperties(Map<String, ?> loadProperties) {
        this.loadProperties = loadProperties;
    }

    public Map<Type, Map<String, ?>> getStorePropertiesByFamily() {
        return storePropertiesByFamily;
    }

    public void setStorePropertiesByFamily(Map<Type, Map<String, ?>> storePropertiesByFamily) {
        this.storePropertiesByFamily = storePropertiesByFamily;
    }

    public void setStoreProperties(Type family, Map<String, ?> storeProperties) {
        if (storePropertiesByFamily == null) {
            storePropertiesByFamily = new HashMap<Type, Map<String, ?>>();
        }
        storePropertiesByFamily.put(family, storeProperties);
    }

    public Map<String, ?> getStoreProperties(Type family) {
        if (storePropertiesByFamily == null) {
            return null;
        }
        return storePropertiesByFamily.get(family);
    }

    public enum Type {
        TEXT, SPREADSHEET, PRESENTATION, DRAWING
    }

}
