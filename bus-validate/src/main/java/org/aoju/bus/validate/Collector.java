package org.aoju.bus.validate;

import org.aoju.bus.validate.validators.Property;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 校验结果收集器
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
@Data
public class Collector {

    /**
     * 被校验对象
     */
    private Validated target;

    /**
     * 校验结果
     */
    private List<Collector> result;

    private Property property;

    private boolean pass;


    public Collector(Validated target) {
        this.target = target;
        this.result = new ArrayList<>();
    }

    public Collector(Collector collector) {
        this.target = collector.getTarget();
        this.result = new ArrayList<>();
        this.result.add(collector);
    }

    public Collector(Validated target, Property property, boolean pass) {
        this.target = target;
        this.property = property;
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
        List<Collector> list = new ArrayList<>(16);
        for (Collector collector : this.result) {
            if (collector instanceof Collector) {
                list.addAll(collector.getResult());
            } else {
                throw new IllegalArgumentException("不支持收集的校验结果对象：" + collector);
            }
        }
        return list;
    }

    public Validated getTarget() {
        return target;
    }

    public boolean isPass() {
        return this.result.stream().allMatch(Collector::isPass);
    }

}
