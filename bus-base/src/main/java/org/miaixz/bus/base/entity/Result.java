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
package org.miaixz.bus.base.entity;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.Collections;
import java.util.List;

/**
 * 返回结果公用
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class Result<T> extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 总数据
     */
    protected int total;
    /**
     * 查询记录数
     */
    protected List<T> rows;

    public Result() {

    }

    public Result(List<T> rows) {
        this.rows = rows;
    }

    public Result(int total, List<T> rows) {
        this.total = total;
        this.rows = rows;
    }

    public Result(List<T> rows, int pageSize) {
        if (null == rows || rows.isEmpty()) {
            throw new IllegalArgumentException("data must be not empty!");
        }
        new Result<>(rows, rows.size(), pageSize);
    }

    public Result(List<T> rows, int total, int pageSize) {
        this.total = total;
        this.pageSize = pageSize;
        this.rows = rows;
    }

    public static <T> Result<T> Result(List<T> rows, int pageSize) {
        return new Result<>(rows, pageSize);
    }

    /**
     * 得到分页后的数据
     *
     * @param pageNo 页码
     * @return 分页后结果
     */
    public List<T> get(int pageNo) {
        int fromIndex = (pageNo - 1) * this.pageSize;
        if (fromIndex >= this.rows.size()) {
            return Collections.emptyList();
        }

        int toIndex = pageNo * this.pageSize;
        if (toIndex >= this.rows.size()) {
            toIndex = this.rows.size();
        }
        return this.rows.subList(fromIndex, toIndex);
    }

}
