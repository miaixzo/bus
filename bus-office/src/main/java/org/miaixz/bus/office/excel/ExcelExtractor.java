/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.office.excel;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.extractor.XSSFExcelExtractor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * Utility wrapper for {@link org.apache.poi.ss.extractor.ExcelExtractor}.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ExcelExtractor {

    /**
     * Gets an {@link org.apache.poi.ss.extractor.ExcelExtractor} object for the given workbook.
     *
     * @param wb The {@link Workbook} to extract text from.
     * @return An {@link org.apache.poi.ss.extractor.ExcelExtractor} instance.
     */
    public static org.apache.poi.ss.extractor.ExcelExtractor getExtractor(final Workbook wb) {
        final org.apache.poi.ss.extractor.ExcelExtractor extractor;
        if (wb instanceof HSSFWorkbook) {
            extractor = new org.apache.poi.hssf.extractor.ExcelExtractor((HSSFWorkbook) wb);
        } else {
            extractor = new XSSFExcelExtractor((XSSFWorkbook) wb);
        }
        return extractor;
    }

    /**
     * Reads the content of the Excel workbook as plain text. Uses {@link org.apache.poi.ss.extractor.ExcelExtractor} to
     * extract Excel content.
     *
     * @param wb            The {@link Workbook} to read.
     * @param withSheetName {@code true} to include sheet names in the extracted text, {@code false} otherwise.
     * @return The extracted Excel content as a string.
     */
    public static String readAsText(final Workbook wb, final boolean withSheetName) {
        final org.apache.poi.ss.extractor.ExcelExtractor extractor = getExtractor(wb);
        extractor.setIncludeSheetNames(withSheetName);
        return extractor.getText();
    }

}
