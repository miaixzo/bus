package org.aoju.bus.poi.excel.cell;

/**
 * 抽象的单元格值接口，用于判断不同类型的单元格值
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public interface CellValue<T> {
    /**
     * 获取单元格值
     *
     * @return 值
     */
    T getValue();
}
