package org.aoju.bus.poi.excel.cell;

/**
 * 公式类型的值
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class FormulaCellValue implements CellValue<String> {

    /**
     * 公式
     */
    String formula;

    public FormulaCellValue(String formula) {
        this.formula = formula;
    }

    @Override
    public String getValue() {
        return this.formula;
    }

}
