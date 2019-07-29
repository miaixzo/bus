package org.aoju.bus.poi;

import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.core.utils.ClassUtils;

/**
 * POI引入检查器
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class PoiChecker {

    /**
     * 没有引入POI的错误消息
     */
    public static final String NO_POI_ERROR_MSG = "You need to add dependency of 'poi-ooxml' to your project, and version >= 3.17";

    /**
     * 检查POI包的引入情况
     */
    public static void checkPoiImport() {
        try {
            Class.forName("org.apache.poi.ss.usermodel.Workbook", false, ClassUtils.getClassLoader());
        } catch (ClassNotFoundException | NoClassDefFoundError e) {
            throw new InstrumentException(NO_POI_ERROR_MSG);
        }
    }

}
