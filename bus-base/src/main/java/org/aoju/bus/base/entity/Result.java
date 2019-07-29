package org.aoju.bus.base.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * <p>
 * 返回结果公用
 * </p>
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
@Data
public class Result<T> extends BaseEntity implements Serializable {

    private static final long serialVersionUID = -631369123580520198L;

    protected int total;
    protected List<T> rows;

    public Result() {
    }

    public Result(int total, List<T> rows) {
        this.total = total;
        this.rows = rows;
    }

    public Result(List<T> rows, int pageSize) {
        if (rows == null || rows.isEmpty()) {
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
