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
package org.miaixz.bus.image.metric;

import java.io.IOException;

import org.miaixz.bus.image.galaxy.data.Attributes;

/**
 * @author Kimi Liu
 * @since Java 21+
 */
public interface DimseRSP {

    /**
     * 发送下一个响应，完成后返回false
     *
     * @return 如果有更多要发送的响应，则为True
     * @throws IOException          网络交互中是否有问题
     * @throws InterruptedException 如果线程被中断
     */
    boolean next() throws IOException, InterruptedException;

    /**
     * 获取响应命令对象
     *
     * @return 属性命令对象
     */
    Attributes getCommand();

    /**
     * 获取此响应中包含的数据集，如果没有数据集，则为null
     *
     * @return 属性此响应中包含的数据集(如果有)
     */
    Attributes getDataset();

    /**
     * 如果这是可以取消*的DIMSE操作(例如C-FIND)，请取消操作
     *
     * @param association 关联活动的关联对象
     * @throws IOException 网络交互中是否有问题。
     */
    void cancel(Association association) throws IOException;

}
