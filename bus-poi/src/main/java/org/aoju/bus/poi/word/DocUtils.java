package org.aoju.bus.poi.word;

import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.core.utils.FileUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

import java.io.File;
import java.io.IOException;

/**
 * Word Document工具
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class DocUtils {

    /**
     * 创建{@link XWPFDocument}，如果文件已存在则读取之，否则创建新的
     *
     * @param file docx文件
     * @return {@link XWPFDocument}
     */
    public static XWPFDocument create(File file) {
        try {
            return FileUtils.exist(file) ? new XWPFDocument(OPCPackage.open(file)) : new XWPFDocument();
        } catch (InvalidFormatException | IOException e) {
            throw new InstrumentException(e);
        }
    }

}
