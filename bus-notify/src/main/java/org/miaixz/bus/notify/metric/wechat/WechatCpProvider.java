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
package org.miaixz.bus.notify.metric.wechat;

import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.notify.Context;
import org.miaixz.bus.notify.metric.AbstractProvider;

/**
 * WeChat Enterprise Account/WeCom message provider.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class WechatCpProvider extends AbstractProvider<WechatNotice, Context> {

    /**
     * Constructs a {@code WechatCpProvider} with the given context.
     *
     * @param context The context containing configuration information for the provider.
     */
    public WechatCpProvider(Context context) {
        super(context);
    }

    /**
     * Sends a WeChat Enterprise Account/WeCom message. Implementations should override this method to provide specific
     * sending logic.
     *
     * @param entity The {@link WechatNotice} containing message details.
     * @return A {@link Message} indicating the result of the sending operation, or {@code null} if not implemented.
     */
    @Override
    public Message send(WechatNotice entity) {
        return null;
    }

}
