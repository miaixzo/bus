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
package org.miaixz.bus.validate;

import lombok.Getter;
import lombok.Setter;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.validate.magic.Material;

import java.util.ArrayList;
import java.util.List;

/**
 * 校验结果收集器
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@Setter
public class Collector {

    /**
     * 被校验对象
     */
    private Verified target;

    /**
     * 校验结果
     */
    private List<Collector> result;

    private Material material;

    private boolean pass;

    public Collector(Verified target) {
        this.target = target;
        this.result = new ArrayList<>();
    }

    public Collector(Collector collector) {
        this.target = collector.getTarget();
        this.result = new ArrayList<>();
        this.result.add(collector);
    }

    public Collector(Verified target, Material material, boolean pass) {
        this.target = target;
        this.material = material;
        this.pass = pass;
    }

    /**
     * 收集校验结果
     *
     * @param collector 校验结果
     */
    public void collect(Collector collector) {
        this.result.add(collector);
    }

    /**
     * 获取所有的基础校验结果
     *
     * @return 基础校验结果集合
     */
    public List<Collector> getResult() {
        List<Collector> list = new ArrayList<>(Normal._16);
        for (Collector collector : this.result) {
            if (collector instanceof Collector) {
                list.addAll(collector.getResult());
            } else {
                throw new IllegalArgumentException("不支持收集的校验结果对象：" + collector);
            }
        }
        return list;
    }

    public Verified getTarget() {
        return target;
    }

    public boolean isPass() {
        return this.result.stream().allMatch(Collector::isPass);
    }

}
