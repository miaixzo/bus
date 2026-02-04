/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.image.galaxy.io;

import java.util.HashMap;

import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.stream.StreamSource;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class TemplatesCache {

    private static TemplatesCache defaultCache;

    private final HashMap<String, Templates> map = new HashMap<>();

    public static synchronized TemplatesCache getDefault() {
        if (defaultCache == null) {
            defaultCache = new TemplatesCache();
        }
        return defaultCache;
    }

    public static synchronized void setDefault(TemplatesCache cache) {
        if (cache == null) {
            throw new NullPointerException();
        }
        defaultCache = cache;
    }

    public void clear() {
        map.clear();
    }

    public Templates get(String uri) throws TransformerConfigurationException {
        Templates tpl = map.get(uri);
        if (tpl == null)
            map.put(uri, tpl = SAXTransformer.newTemplates(new StreamSource(uri)));
        return tpl;
    }

}
