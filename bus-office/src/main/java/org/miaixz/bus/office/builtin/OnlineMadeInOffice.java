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
package org.miaixz.bus.office.builtin;

import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.office.Context;

/**
 * Represents the default behavior for online conversion tasks.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class OnlineMadeInOffice extends AbstractOffice {

    /**
     * Constructs a new OnlineMadeInOffice instance.
     */
    public OnlineMadeInOffice() {
        // No initialization required.
    }

    @Override
    public void execute(Context context) throws InternalException {
        Logger.debug(
                true,
                "Office",
                "Online office execution started: contextType={}",
                context == null ? null : context.getClass().getName());
        // No specific implementation for online office execution in this abstract class.
        Logger.debug(
                false,
                "Office",
                "Online office execution completed: contextType={}, result={}",
                context == null ? null : context.getClass().getName(),
                "noSpecificImplementation");
    }

}
