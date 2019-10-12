/*
 * The MIT License
 *
 * Copyright (c) 2017 aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.office.excel.sax;

import org.aoju.bus.core.consts.Normal;
import org.aoju.bus.core.consts.Symbol;
import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.core.utils.IoUtils;
import org.apache.poi.hssf.eventusermodel.EventWorkbookBuilder.SheetRecordCollectingListener;
import org.apache.poi.hssf.eventusermodel.*;
import org.apache.poi.hssf.eventusermodel.dummyrecord.LastCellOfRowDummyRecord;
import org.apache.poi.hssf.eventusermodel.dummyrecord.MissingCellDummyRecord;
import org.apache.poi.hssf.model.HSSFFormulaParser;
import org.apache.poi.hssf.record.*;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Excel2003格式的事件-用户模型方式读取器，统一将此归类为Sax读取
 *
 * @author Kimi Liu
 * @version 5.0.0
 * @since JDK 1.8+
 */
public class Excel03SaxReader extends AbstractExcelSaxReader<Excel03SaxReader> implements HSSFListener {

    /**
     * 如果为公式，true表示输出公式计算后的结果值，false表示输出公式本身
     */
    private boolean isOutputFormulaValues = true;

    /**
     * 用于解析公式
     */
    private SheetRecordCollectingListener workbookBuildingListener;
    /**
     * 子工作簿，用于公式计算
     */
    private HSSFWorkbook stubWorkbook;

    /**
     * 静态字符串表
     */
    private SSTRecord sstRecord;

    private FormatTrackingHSSFListener formatListener;

    /**
     * Sheet边界记录，此Record中可以获得Sheet名
     */
    private List<BoundSheetRecord> boundSheetRecords = new ArrayList<>();

    private boolean isOutputNextStringRecord;

    // 存储行记录的容器
    private List<Object> rowCellList = new ArrayList<>();

    /**
     * 自定义需要处理的sheet编号，如果-1表示处理所有sheet
     */
    private int rid = -1;
    // 当前表索引
    private int curRid = -1;

    private RowHandler rowHandler;

    /**
     * 构造
     *
     * @param rowHandler 行处理器
     */
    public Excel03SaxReader(RowHandler rowHandler) {
        this.rowHandler = rowHandler;
    }

    @Override
    public Excel03SaxReader read(File file, int rid) throws InstrumentException {
        try {
            return read(new POIFSFileSystem(file), rid);
        } catch (IOException e) {
            throw new InstrumentException(e);
        }
    }

    @Override
    public Excel03SaxReader read(InputStream excelStream, int rid) throws InstrumentException {
        try {
            return read(new POIFSFileSystem(excelStream), rid);
        } catch (IOException e) {
            throw new InstrumentException(e);
        }
    }

    /**
     * 读取
     *
     * @param fs  {@link POIFSFileSystem}
     * @param rid sheet序号
     * @return this
     * @throws InstrumentException IO异常包装
     */
    public Excel03SaxReader read(POIFSFileSystem fs, int rid) throws InstrumentException {
        this.rid = rid;

        formatListener = new FormatTrackingHSSFListener(new MissingRecordAwareHSSFListener(this));
        final HSSFRequest request = new HSSFRequest();
        if (isOutputFormulaValues) {
            request.addListenerForAllRecords(formatListener);
        } else {
            workbookBuildingListener = new SheetRecordCollectingListener(formatListener);
            request.addListenerForAllRecords(workbookBuildingListener);
        }
        final HSSFEventFactory factory = new HSSFEventFactory();
        try {
            factory.processWorkbookEvents(request, fs);
        } catch (IOException e) {
            throw new InstrumentException(e);
        } finally {
            IoUtils.close(fs);
        }
        return this;
    }

    /**
     * 获得Sheet序号，如果处理所有sheet，获得最大的Sheet序号，从0开始
     *
     * @return sheet序号
     */
    public int getSheetIndex() {
        return this.rid;
    }

    /**
     * 获得Sheet名，如果处理所有sheet，获得后一个Sheet名，从0开始
     *
     * @return Sheet名
     */
    public String getSheetName() {
        if (this.boundSheetRecords.size() > this.rid) {
            return this.boundSheetRecords.get(this.rid > -1 ? this.rid : this.curRid).getSheetname();
        }
        return null;
    }

    /**
     * HSSFListener 监听方法，处理 Record
     *
     * @param record 记录
     */
    @Override
    public void processRecord(Record record) {
        if (this.rid > -1 && this.curRid > this.rid) {
            // 指定Sheet之后的数据不再处理
            return;
        }

        if (record instanceof BoundSheetRecord) {
            // Sheet边界记录，此Record中可以获得Sheet名
            boundSheetRecords.add((BoundSheetRecord) record);
        } else if (record instanceof SSTRecord) {
            // 静态字符串表
            sstRecord = (SSTRecord) record;
        } else if (record instanceof BOFRecord) {
            BOFRecord bofRecord = (BOFRecord) record;
            if (bofRecord.getType() == BOFRecord.TYPE_WORKSHEET) {
                // 如果有需要，则建立子工作薄
                if (workbookBuildingListener != null && stubWorkbook == null) {
                    stubWorkbook = workbookBuildingListener.getStubHSSFWorkbook();
                }
                curRid++;
            }
        } else if (isProcessCurrentSheet()) {
            if (record instanceof MissingCellDummyRecord) {
                // 空值的操作
                MissingCellDummyRecord mc = (MissingCellDummyRecord) record;
                rowCellList.add(mc.getColumn(), Normal.EMPTY);
            } else if (record instanceof LastCellOfRowDummyRecord) {
                // 行结束
                processLastCell((LastCellOfRowDummyRecord) record);
            } else {
                // 处理单元格值
                processCellValue(record);
            }
        }
    }

    /**
     * 处理单元格值
     *
     * @param record 单元格
     */
    private void processCellValue(Record record) {
        Object value = null;

        switch (record.getSid()) {
            case BlankRecord.sid:
                // 空白记录
                BlankRecord brec = (BlankRecord) record;
                rowCellList.add(brec.getColumn(), Normal.EMPTY);
                break;
            case BoolErrRecord.sid: // 布尔类型
                BoolErrRecord berec = (BoolErrRecord) record;
                rowCellList.add(berec.getColumn(), berec.getBooleanValue());
                break;
            case FormulaRecord.sid: // 公式类型
                FormulaRecord frec = (FormulaRecord) record;
                if (isOutputFormulaValues) {
                    if (Double.isNaN(frec.getValue())) {
                        // Formula result is a string
                        // This is stored in the next record
                        isOutputNextStringRecord = true;
                    } else {
                        value = formatListener.formatNumberDateCell(frec);
                    }
                } else {
                    value = '"' + HSSFFormulaParser.toFormulaString(stubWorkbook, frec.getParsedExpression()) + '"';
                }
                rowCellList.add(frec.getColumn(), value);
                break;
            case StringRecord.sid:// 单元格中公式的字符串
                if (isOutputNextStringRecord) {
                    // String for formula
                    StringRecord srec = (StringRecord) record;
                    value = srec.getString();
                    isOutputNextStringRecord = false;
                }
                break;
            case LabelRecord.sid:
                LabelRecord lrec = (LabelRecord) record;
                this.rowCellList.add(lrec.getColumn(), value);
                break;
            case LabelSSTRecord.sid: // 字符串类型
                LabelSSTRecord lsrec = (LabelSSTRecord) record;
                if (sstRecord == null) {
                    rowCellList.add(lsrec.getColumn(), Normal.EMPTY);
                } else {
                    value = sstRecord.getString(lsrec.getSSTIndex()).toString();
                    rowCellList.add(lsrec.getColumn(), value);
                }
                break;
            case NumberRecord.sid: // 数字类型
                NumberRecord numrec = (NumberRecord) record;

                final String formatString = formatListener.getFormatString(numrec);
                if (formatString.contains(Symbol.DOT)) {
                    //浮点数
                    value = numrec.getValue();
                } else if (formatString.contains(Symbol.SLASH) || formatString.contains(Symbol.COLON)) {
                    //日期
                    value = formatListener.formatNumberDateCell(numrec);
                } else {
                    double numValue = numrec.getValue();
                    final long longPart = (long) numValue;
                    // 对于无小数部分的数字类型，转为Long，否则保留原数字
                    if (longPart == numValue) {
                        value = longPart;
                    } else {
                        value = numValue;
                    }
                }

                // 向容器加入列值
                rowCellList.add(numrec.getColumn(), value);
                break;
            default:
                break;
        }
    }

    /**
     * 处理行结束后的操作，{@link LastCellOfRowDummyRecord}是行结束的标识Record
     *
     * @param lastCell 行结束的标识Record
     */
    private void processLastCell(LastCellOfRowDummyRecord lastCell) {
        // 每行结束时， 调用handle() 方法
        this.rowHandler.handle(curRid, lastCell.getRow(), this.rowCellList);
        // 清空行Cache
        this.rowCellList.clear();
    }

    /**
     * 是否处理当前sheet
     *
     * @return 是否处理当前sheet
     */
    private boolean isProcessCurrentSheet() {
        return this.rid < 0 || this.curRid == this.rid;
    }

}
