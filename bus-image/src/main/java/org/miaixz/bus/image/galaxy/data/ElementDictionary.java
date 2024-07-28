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
package org.miaixz.bus.image.galaxy.data;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class ElementDictionary {

    private static final ServiceLoader<ElementDictionary> loader = ServiceLoader.load(ElementDictionary.class);
    private static final Map<String, ElementDictionary> map = new HashMap<>();
    private final String privateCreator;
    private final Class<?> tagClass;

    protected ElementDictionary(String privateCreator, Class<?> tagClass) {
        this.privateCreator = privateCreator;
        this.tagClass = tagClass;
    }

    public static ElementDictionary getStandardElementDictionary() {
        return StandardElementDictionary.INSTANCE;
    }

    public static ElementDictionary getElementDictionary(String privateCreator) {
        if (privateCreator != null) {
            ElementDictionary dict1 = map.get(privateCreator);
            if (dict1 != null)
                return dict1;
            if (!map.containsKey(privateCreator))
                synchronized (loader) {
                    for (ElementDictionary dict : loader) {
                        map.putIfAbsent(dict.getPrivateCreator(), dict);
                        if (privateCreator.equals(dict.getPrivateCreator()))
                            return dict;
                    }
                    map.put(privateCreator, null);
                }
        }
        return getStandardElementDictionary();
    }

    public static void reload() {
        synchronized (loader) {
            loader.reload();
        }
    }

    public static VR vrOf(int tag, String privateCreator) {
        return getElementDictionary(privateCreator).vrOf(tag);
    }

    public static String keywordOf(int tag, String privateCreator) {
        return getElementDictionary(privateCreator).keywordOf(tag);
    }

    public static int tagForKeyword(String keyword, String privateCreatorID) {
        return getElementDictionary(privateCreatorID).tagForKeyword(keyword);
    }

    public final String getPrivateCreator() {
        return privateCreator;
    }

    public abstract VR vrOf(int tag);

    public abstract String keywordOf(int tag);

    public int tmTagOf(int daTag) {
        return 0;
    }

    public int daTagOf(int tmTag) {
        return 0;
    }

    public int tagForKeyword(String keyword) {
        if (tagClass != null)
            try {
                return tagClass.getField(keyword).getInt(null);
            } catch (Exception ignore) {
            }
        return -1;
    }

}
