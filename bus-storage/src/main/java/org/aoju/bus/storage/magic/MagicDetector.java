package org.aoju.bus.storage.magic;

import java.io.File;
import java.util.Map;


/**
 * 探测器
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public interface MagicDetector {

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    String getName();

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    String getDisplayName();

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    String getVersion();

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    String[] getHandledTypes();

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    String[] getHandledExtensions();

    /**
     * DOCUMENT ME!
     *
     * @param data       DOCUMENT ME!
     * @param offset     DOCUMENT ME!
     * @param length     DOCUMENT ME!
     * @param bitmask    DOCUMENT ME!
     * @param comparator DOCUMENT ME!
     * @param mimeType   DOCUMENT ME!
     * @param params     DOCUMENT ME!
     * @return DOCUMENT ME!
     */
    String[] process(byte[] data, int offset, int length, long bitmask, char comparator,
                     String mimeType, Map<String, String> params);

    /**
     * DOCUMENT ME!
     *
     * @param file       DOCUMENT ME!
     * @param offset     DOCUMENT ME!
     * @param length     DOCUMENT ME!
     * @param bitmask    DOCUMENT ME!
     * @param comparator DOCUMENT ME!
     * @param mimeType   DOCUMENT ME!
     * @param params     DOCUMENT ME!
     * @return DOCUMENT ME!
     */
    String[] process(File file, int offset, int length, long bitmask, char comparator,
                     String mimeType, Map<String, String> params);

}
