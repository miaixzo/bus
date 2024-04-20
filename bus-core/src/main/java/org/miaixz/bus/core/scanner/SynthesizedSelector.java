/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.miaixz.bus.core.scanner;

/**
 * 注解选择器，指定两个注解，选择其中一个返回<br>
 * 该接口用于在{@link Synthetic}中用于从一批相同的注解对象中筛选最终用于合成注解对象
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@FunctionalInterface
public interface SynthesizedSelector {

    /**
     * 返回距离根对象更近的注解，当距离一样时优先返回旧注解
     */
    SynthesizedSelector NEAREST_AND_OLDEST_PRIORITY = new NearestAndOldestPrioritySelector();

    /**
     * 返回距离根对象更近的注解，当距离一样时优先返回新注解
     */
    SynthesizedSelector NEAREST_AND_NEWEST_PRIORITY = new NearestAndNewestPrioritySelector();

    /**
     * 返回距离根对象更远的注解，当距离一样时优先返回旧注解
     */
    SynthesizedSelector FARTHEST_AND_OLDEST_PRIORITY = new FarthestAndOldestPrioritySelector();

    /**
     * 返回距离根对象更远的注解，当距离一样时优先返回新注解
     */
    SynthesizedSelector FARTHEST_AND_NEWEST_PRIORITY = new FarthestAndNewestPrioritySelector();

    /**
     * 比较两个被合成的注解，选择其中的一个并返回
     *
     * @param <T>           复合注解类型
     * @param oldAnnotation 已存在的注解，该参数不允许为空
     * @param newAnnotation 新获取的注解，该参数不允许为空
     * @return 被合成的注解
     */
    <T extends Synthesized> T choose(T oldAnnotation, T newAnnotation);

    /**
     * 返回距离根对象更近的注解，当距离一样时优先返回旧注解
     */
    class NearestAndOldestPrioritySelector implements SynthesizedSelector {
        @Override
        public <T extends Synthesized> T choose(T oldAnnotation, T newAnnotation) {
            return newAnnotation.getVerticalDistance() < oldAnnotation.getVerticalDistance() ? newAnnotation : oldAnnotation;
        }
    }

    /**
     * 返回距离根对象更近的注解，当距离一样时优先返回新注解
     */
    class NearestAndNewestPrioritySelector implements SynthesizedSelector {
        @Override
        public <T extends Synthesized> T choose(T oldAnnotation, T newAnnotation) {
            return newAnnotation.getVerticalDistance() <= oldAnnotation.getVerticalDistance() ? newAnnotation : oldAnnotation;
        }
    }

    /**
     * 返回距离根对象更远的注解，当距离一样时优先返回旧注解
     */
    class FarthestAndOldestPrioritySelector implements SynthesizedSelector {
        @Override
        public <T extends Synthesized> T choose(T oldAnnotation, T newAnnotation) {
            return newAnnotation.getVerticalDistance() > oldAnnotation.getVerticalDistance() ? newAnnotation : oldAnnotation;
        }
    }

    /**
     * 返回距离根对象更远的注解，当距离一样时优先返回新注解
     */
    class FarthestAndNewestPrioritySelector implements SynthesizedSelector {
        @Override
        public <T extends Synthesized> T choose(T oldAnnotation, T newAnnotation) {
            return newAnnotation.getVerticalDistance() >= oldAnnotation.getVerticalDistance() ? newAnnotation : oldAnnotation;
        }
    }

}
