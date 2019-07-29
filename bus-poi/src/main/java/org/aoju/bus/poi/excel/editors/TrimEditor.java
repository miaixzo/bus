package org.aoju.bus.poi.excel.editors;

import org.aoju.bus.core.utils.StringUtils;
import org.aoju.bus.poi.excel.cell.CellEditor;
import org.apache.poi.ss.usermodel.Cell;

/**
 * 去除String类型的单元格值两边的空格
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class TrimEditor implements CellEditor {

    @Override
    public Object edit(Cell cell, Object value) {
        if (value instanceof String) {
            return StringUtils.trim((String) value);
        }
        return value;
    }

}
