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
package org.miaixz.bus.extra.nlp.provider.analysis;

import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;

/**
 * Lucene-smartcn word segmentation engine implementation. This class provides a concrete implementation of
 * {@link AnalysisProvider} for the Lucene SmartChineseAnalyzer. Project homepage: <a href=
 * "https://github.com/apache/lucene-solr/tree/master/lucene/analysis/smartcn">https://github.com/apache/lucene-solr/tree/master/lucene/analysis/smartcn</a>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class SmartcnProvider extends AnalysisProvider {

    /**
     * Constructs a new {@code SmartcnProvider} instance. It initializes the provider with a new instance of
     * {@link SmartChineseAnalyzer}.
     */
    public SmartcnProvider() {
        super(new SmartChineseAnalyzer());
    }

}
