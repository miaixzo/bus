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
package org.miaixz.bus.image.metric.api;

import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.exception.NotFoundException;
import org.miaixz.bus.image.metric.WebApplication;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class WebApplicationCache extends ConfigurationCache<DicomConfiguration, WebApplication>
        implements IWebApplicationCache {

    public WebApplicationCache(DicomConfiguration conf) {
        super(conf);
    }

    @Override
    protected WebApplication find(DicomConfiguration conf, String key) throws InternalException {
        return conf.findWebApplication(key);
    }

    @Override
    public WebApplication findWebApplication(String name) throws InternalException {
        WebApplication webApp = get(name);
        if (webApp == null)
            throw new NotFoundException("Unknown WebApplication: " + name);
        if (!webApp.isInstalled())
            throw new NotFoundException("WebApplication: " + name + " not installed");
        return webApp;
    }

}
