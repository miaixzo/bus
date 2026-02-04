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
package org.miaixz.bus.image.metric.hl7.api;

import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.image.metric.api.ConfigurationCache;
import org.miaixz.bus.image.metric.hl7.net.HL7Application;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class HL7ApplicationCache extends ConfigurationCache<HL7Configuration, HL7Application>
        implements IHL7ApplicationCache {

    public HL7ApplicationCache(HL7Configuration conf) {
        super(conf);
    }

    @Override
    protected HL7Application find(HL7Configuration conf, String key) throws InternalException {
        return conf.findHL7Application(key);
    }

    public HL7Application findHL7Application(String hl7AppFacility) throws InternalException {
        HL7Application hl7App = get(hl7AppFacility);
        if (hl7App == null)
            throw new InternalException("Unknown HL7 Application: " + hl7AppFacility);
        return hl7App;
    }

}
