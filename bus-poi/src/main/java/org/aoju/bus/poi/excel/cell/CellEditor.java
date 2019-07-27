package org.aoju.bus.poi.excel.cell;

import org.apache.poi.ss.usermodel.Cell;

/**
 * 单元格编辑器接口
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public interface CellEditor {
    /**
     * 编辑
     *
     * @param cell  单元格对象，可以获取单元格行、列样式等信息
     * @param value 单元格值
     * @return 编辑后的对象
     */
    public Object edit(Cell cell, Object value);
}
