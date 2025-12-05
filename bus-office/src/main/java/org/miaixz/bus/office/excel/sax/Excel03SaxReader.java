/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.office.excel.sax;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.hssf.eventusermodel.*;
import org.apache.poi.hssf.eventusermodel.EventWorkbookBuilder.SheetRecordCollectingListener;
import org.apache.poi.hssf.eventusermodel.dummyrecord.LastCellOfRowDummyRecord;
import org.apache.poi.hssf.eventusermodel.dummyrecord.MissingCellDummyRecord;
import org.apache.poi.hssf.model.HSSFFormulaParser;
import org.apache.poi.hssf.record.*;
import org.apache.poi.hssf.record.Record;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.exception.TerminateException;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.office.excel.sax.handler.RowHandler;
import org.miaixz.bus.office.excel.xyz.ExcelSaxKit;

/**
 * Excel2003 format event-user model reader, uniformly classified as SAX reader. Reference:
 * <a href="http://www.cnblogs.com/wshsdlau/p/5643862.html">http://www.cnblogs.com/wshsdlau/p/5643862.html</a>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Excel03SaxReader implements HSSFListener, ExcelSaxReader<Excel03SaxReader> {

    /**
     * If it is a formula, {@code true} means output the calculated result value, {@code false} means output the formula
     * itself.
     */
    private final boolean isOutputFormulaValues = true;
    /**
     * List of {@link BoundSheetRecord}s, which can be used to obtain sheet names.
     */
    private final List<BoundSheetRecord> boundSheetRecords = new ArrayList<>();
    /**
     * Row handler for processing each row.
     */
    private final RowHandler rowHandler;
    /**
     * Listener used to parse formulas and build the stub workbook.
     */
    private SheetRecordCollectingListener workbookBuildingListener;
    /**
     * Stub workbook, used for formula calculation.
     */
    private HSSFWorkbook stubWorkbook;
    /**
     * Static string table record.
     */
    private SSTRecord sstRecord;
    /**
     * Listener for tracking formatting.
     */
    private FormatTrackingHSSFListener formatListener;
    /**
     * Flag indicating if the next record contains the string value of a formula.
     */
    private boolean isOutputNextStringRecord;
    /**
     * Container for storing cell values of the current row.
     */
    private List<Object> rowCellList = new ArrayList<>();
    /**
     * Custom sheet ID to process. If -1, all sheets are processed.
     */
    private int sheetIndex = -1;
    /**
     * Sheet name, mainly used when reading by sheet name.
     */
    private String sheetName;
    /**
     * Current rId index (sheet index tracking).
     */
    private int curRid = -1;

    /**
     * Constructs a new {@code Excel03SaxReader}.
     *
     * @param rowHandler The row handler to process each row.
     */
    public Excel03SaxReader(final RowHandler rowHandler) {
        this.rowHandler = rowHandler;
    }

    @Override
    public Excel03SaxReader read(final File file, final String idOrRidOrSheetName) throws InternalException {
        try (final POIFSFileSystem poifsFileSystem = new POIFSFileSystem(file, true)) {
            return read(poifsFileSystem, idOrRidOrSheetName);
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    @Override
    public Excel03SaxReader read(final InputStream excelStream, final String idOrRidOrSheetName)
            throws InternalException {
        try {
            return read(new POIFSFileSystem(excelStream), idOrRidOrSheetName);
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * Reads an Excel file using SAX parsing.
     *
     * @param fs                 The {@link POIFSFileSystem} representing the Excel file.
     * @param idOrRidOrSheetName The sheet identifier in Excel, which can be a sheet ID, an rId (prefixed with "rId",
     *                           e.g., "rId0"), or a sheet name. If -1, all sheets are processed.
     * @return This reader instance, for chaining.
     * @throws InternalException If an I/O error occurs or a POI-related exception occurs.
     */
    public Excel03SaxReader read(final POIFSFileSystem fs, final String idOrRidOrSheetName) throws InternalException {
        this.initSheetIndexOrSheetName(idOrRidOrSheetName);

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
        } catch (final IOException e) {
            throw new InternalException(e);
        } catch (final TerminateException e) {
            // User throws this exception to force end reading.
        } finally {
            IoKit.closeQuietly(fs);
        }
        return this;
    }

    /**
     * Gets the sheet index to be processed. If all sheets are processed, returns the maximum sheet index. The index is
     * 0-based.
     *
     * @return The sheet index.
     */
    public int getSheetIndex() {
        return this.sheetIndex;
    }

    /**
     * Gets the sheet name. If all sheets are processed, returns the name of the current sheet being processed. The
     * index is 0-based.
     *
     * @return The sheet name.
     */
    public String getSheetName() {
        if (null != this.sheetName) {
            return this.sheetName;
        }

        if (this.boundSheetRecords.size() > this.sheetIndex) {
            return this.boundSheetRecords.get(this.sheetIndex > -1 ? this.sheetIndex : this.curRid).getSheetname();
        }

        return null;
    }

    /**
     * HSSFListener callback method, processes each {@link Record}.
     *
     * @param record The record to process.
     */
    @Override
    public void processRecord(final Record record) {
        if (this.sheetIndex > -1 && this.curRid > this.sheetIndex) {
            // Data after the specified sheet is no longer processed.
            return;
        }

        if (record instanceof BoundSheetRecord boundSheetRecord) {
            // Sheet boundary record, sheet name can be obtained from this record.
            boundSheetRecords.add(boundSheetRecord);
            final String currentSheetName = boundSheetRecord.getSheetname();
            if (null != this.sheetName && StringKit.equals(this.sheetName, currentSheetName)) {
                this.sheetIndex = this.boundSheetRecords.size() - 1;
            }
        } else if (record instanceof SSTRecord) {
            // Static string table.
            sstRecord = (SSTRecord) record;
        } else if (record instanceof BOFRecord bofRecord) {
            if (bofRecord.getType() == BOFRecord.TYPE_WORKSHEET) {
                // If needed, create a stub workbook.
                if (workbookBuildingListener != null && stubWorkbook == null) {
                    stubWorkbook = workbookBuildingListener.getStubHSSFWorkbook();
                }
                curRid++;
            }
        } else if (record instanceof EOFRecord) {
            if (this.sheetIndex < 0 && null != this.sheetName) {
                throw new InternalException("Sheet [{}] not exist!", this.sheetName);
            }
            if (this.curRid != -1 && isProcessCurrentSheet()) {
                // Only trigger the end event for the currently specified sheet, and do not process when curId=-1 to
                // avoid duplicate calls.
                processLastCellSheet();
            }
        } else if (isProcessCurrentSheet()) {
            if (record instanceof MissingCellDummyRecord) {
                // Operation for empty cells.
                final MissingCellDummyRecord mc = (MissingCellDummyRecord) record;
                addToRowCellList(mc);
            } else if (record instanceof LastCellOfRowDummyRecord) {
                // End of row.
                processLastCell((LastCellOfRowDummyRecord) record);
            } else {
                // Process cell value.
                processCellValue(record);
            }
        }
    }

    /**
     * Adds an empty cell to the row list.
     *
     * @param record The {@link MissingCellDummyRecord} representing the missing cell.
     */
    private void addToRowCellList(final MissingCellDummyRecord record) {
        addToRowCellList(record.getRow(), record.getColumn(), Normal.EMPTY);
    }

    /**
     * Adds a cell value to the row list.
     *
     * @param record The {@link CellValueRecordInterface} representing the cell.
     * @param value  The value of the cell.
     */
    private void addToRowCellList(final CellValueRecordInterface record, final Object value) {
        addToRowCellList(record.getRow(), record.getColumn(), value);
    }

    /**
     * Adds a cell value to the row list at the specified row and column.
     *
     * @param row    The row index.
     * @param column The column index.
     * @param value  The value of the cell.
     */
    private void addToRowCellList(final int row, final int column, final Object value) {
        while (column > this.rowCellList.size()) {
            // Fill in blanks for empty cells in between.
            this.rowCellList.add(Normal.EMPTY);
            this.rowHandler.handleCell(this.curRid, row, rowCellList.size() - 1, value, null);
        }

        this.rowCellList.add(column, value);
        this.rowHandler.handleCell(this.curRid, row, column, value, null);
    }

    /**
     * Processes the value of a cell record.
     *
     * @param record The {@link Record} representing the cell.
     */
    private void processCellValue(final Record record) {
        Object value = null;

        switch (record.getSid()) {
            case BlankRecord.sid:
                // Blank record.
                addToRowCellList(((BlankRecord) record), Normal.EMPTY);
                break;

            case BoolErrRecord.sid:
                // Boolean type.
                final BoolErrRecord berec = (BoolErrRecord) record;
                addToRowCellList(berec, berec.getBooleanValue());
                break;

            case FormulaRecord.sid:
                // Formula type.
                final FormulaRecord formulaRec = (FormulaRecord) record;
                if (isOutputFormulaValues) {
                    if (Double.isNaN(formulaRec.getValue())) {
                        // Formula result is a string, stored in the next record
                        isOutputNextStringRecord = true;
                    } else {
                        value = ExcelSaxKit
                                .getNumberOrDateValue(formulaRec, formulaRec.getValue(), this.formatListener);
                    }
                } else {
                    value = HSSFFormulaParser.toFormulaString(stubWorkbook, formulaRec.getParsedExpression());
                }
                addToRowCellList(formulaRec, value);
                break;

            case StringRecord.sid:
                // String for formula cell.
                if (isOutputNextStringRecord) {
                    // String for formula
                    // value = ((StringRecord) record).getString();
                    isOutputNextStringRecord = false;
                }
                break;

            case LabelRecord.sid:
                final LabelRecord lrec = (LabelRecord) record;
                value = lrec.getValue();
                addToRowCellList(lrec, value);
                break;

            case LabelSSTRecord.sid:
                // String type (Shared String Table).
                final LabelSSTRecord lsrec = (LabelSSTRecord) record;
                if (null != sstRecord) {
                    value = sstRecord.getString(lsrec.getSSTIndex()).toString();
                }
                addToRowCellList(lsrec, ObjectKit.defaultIfNull(value, Normal.EMPTY));
                break;

            case NumberRecord.sid:
                // Numeric type.
                final NumberRecord numrec = (NumberRecord) record;
                value = ExcelSaxKit.getNumberOrDateValue(numrec, numrec.getValue(), this.formatListener);
                // Add column value to container.
                addToRowCellList(numrec, value);
                break;

            default:
                break;
        }
    }

    /**
     * Processes operations after a row ends. {@link LastCellOfRowDummyRecord} is the indicator record for the end of a
     * row.
     *
     * @param lastCell The {@link LastCellOfRowDummyRecord} indicating the end of the row.
     */
    private void processLastCell(final LastCellOfRowDummyRecord lastCell) {
        // At the end of each row, call the handle() method.
        this.rowHandler.handle(curRid, lastCell.getRow(), this.rowCellList);
        // Clear row cache.
        this.rowCellList = new ArrayList<>(this.rowCellList.size());
    }

    /**
     * Processes operations after a sheet ends.
     */
    private void processLastCellSheet() {
        this.rowHandler.doAfterAllAnalysed();
    }

    /**
     * Checks if the current sheet should be processed.
     *
     * @return {@code true} if the current sheet should be processed, {@code false} otherwise.
     */
    private boolean isProcessCurrentSheet() {
        // If rid < 0 and sheet name exists, it means no sheet name matched.
        return (this.sheetIndex < 0 && null == this.sheetName) || this.sheetIndex == this.curRid;
    }

    /**
     * Initializes the sheet index or sheet name based on the input string.
     * <ul>
     * <li>If the input starts with 'rId', the 'rId' prefix is removed directly.</li>
     * <li>If the input is a pure number, it is treated as a sheet index and converted to rId.</li>
     * </ul>
     *
     * @param idOrRidOrSheetName The sheet identifier in Excel, which can be a sheet ID, an rId (prefixed with "rId",
     *                           e.g., "rId0"), or a sheet name. If -1, all sheets are processed.
     */
    private void initSheetIndexOrSheetName(final String idOrRidOrSheetName) {
        Assert.notBlank(idOrRidOrSheetName, "id or rid or sheetName must be not blank!");

        // Handle rId directly
        if (StringKit.startWithIgnoreCase(idOrRidOrSheetName, RID_PREFIX)) {
            // rId counts from 1, convert to 0-based index here
            this.sheetIndex = Integer.parseInt(StringKit.removePrefixIgnoreCase(idOrRidOrSheetName, RID_PREFIX)) - 1;
        } else if (StringKit.startWithIgnoreCase(idOrRidOrSheetName, SHEET_NAME_PREFIX)) {
            // Support arbitrary names
            this.sheetName = StringKit.removePrefixIgnoreCase(idOrRidOrSheetName, SHEET_NAME_PREFIX);
        } else {
            // Pure number input represents sheetIndex
            try {
                this.sheetIndex = Integer.parseInt(idOrRidOrSheetName);
            } catch (final NumberFormatException ignore) {
                // If non-number input, treat as sheet name
                this.sheetName = idOrRidOrSheetName;
            }
        }
    }

}
