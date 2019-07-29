package org.aoju.bus.storage.magic;

import java.io.File;
import java.util.Map;


/**
 * DOCUMENT ME!
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public interface MagicDetector {
    // get the short name of this detector

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getName();

    // get the display name of this detector

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getDisplayName();

    // get the version of this plugin

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getVersion();

    // get a list of types this detector handles

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String[] getHandledTypes();

    // get a list of file extensions this detector typically deals with

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String[] getHandledExtensions();

    // process the stream and return all matching content types

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
    public String[] process(byte[] data, int offset, int length, long bitmask, char comparator,
                            String mimeType, Map<String, String> params);

    // process the file and return all matching content types

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
    public String[] process(File file, int offset, int length, long bitmask, char comparator,
                            String mimeType, Map<String, String> params);
}
