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
import org.miaixz.bus.image.metric.net.ApplicationEntity;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class ApplicationEntityCache extends ConfigurationCache<DicomConfiguration, ApplicationEntity>
        implements IApplicationEntityCache {

    public ApplicationEntityCache(DicomConfiguration conf) {
        super(conf);
    }

    @Override
    protected ApplicationEntity find(DicomConfiguration conf, String key) throws InternalException {
        return conf.findApplicationEntity(key);
    }

    @Override
    public ApplicationEntity findApplicationEntity(String aet) throws InternalException {
        ApplicationEntity ae = get(aet);
        if (ae == null)
            throw new NotFoundException("Unknown AE: " + aet);
        if (!ae.isInstalled())
            throw new NotFoundException("AE: " + aet + " not installed");
        return ae;
    }

}
