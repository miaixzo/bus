package org.aoju.bus.storage.magic;

import java.nio.ByteBuffer;
import java.util.*;


/**
 * This class represents a single match test
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class MagicMatch implements Cloneable {

    private String mimeType = null;
    private String extension = null;
    private String description = null;
    private ByteBuffer test = null;
    private int offset = 0;
    private int length = 0;

    private String type = "";
    private long bitmask = 0xFFFFFFFFL;
    private char comparator = '\0';
    private List<MagicMatch> subMatches = new ArrayList<MagicMatch>(0);
    private Map<String, String> properties;


    public MagicMatch() {
    }

    /**
     * print information about this match
     */
    public String print() {
        StringBuffer string = new StringBuffer();
        string.append("\n");
        string.append("mime type: ").append(mimeType).append("\n");
        string.append("description: ").append(description).append("\n");
        string.append("extension: ").append(extension).append("\n");
        string.append("offset: ").append(offset).append("\n");
        string.append("length: ").append(length).append("\n");
        string.append("test: ").append(new String(test.array())).append("\n");
        string.append("type: ").append(type).append("\n");
        string.append("comparator: ").append(comparator).append("\n");
        string.append("bitmask: ").append(bitmask);

        return string.toString();
    }

    /**
     * get the magic match for this magic match
     *
     * @return the mime type for this magic match
     */
    public String getMimeType() {
        return mimeType;
    }

    /**
     * set the mime type for this magic match
     *
     * @param value DOCUMENT ME!
     */
    public void setMimeType(String value) {
        mimeType = value;
    }

    /**
     * get the extension for this magic match
     *
     * @return the extension for this magic match
     */
    public String getExtension() {
        return extension;
    }

    /**
     * set the extension for this magic match
     *
     * @param value DOCUMENT ME!
     */
    public void setExtension(String value) {
        extension = value;
    }

    /**
     * get the description for this magic match
     *
     * @return the description for thie magic match
     */
    public String getDescription() {
        return description;
    }

    /**
     * set the description for this magic match
     *
     * @param value DOCUMENT ME!
     */
    public void setDescription(String value) {
        description = value;
    }

    /**
     * get the test value for this magic match
     *
     * @return DOCUMENT ME!
     */
    public ByteBuffer getTest() {
        return test;
    }

    /**
     * set the test value for thie magic match
     *
     * @param value DOCUMENT ME!
     */
    public void setTest(ByteBuffer value) {
        test = value;
    }

    /**
     * get the offset in the stream we are comparing to the test value for this magic match
     *
     * @return the offset for this magic match
     */
    public int getOffset() {
        return offset;
    }

    /**
     * set the offset in the stream we are comparing to the test value for this magic match
     *
     * @param value DOCUMENT ME!
     */
    public void setOffset(int value) {
        this.offset = value;
    }

    /**
     * get the length we are restricting the comparison to for this magic match
     *
     * @return DOCUMENT ME!
     */
    public int getLength() {
        return length;
    }

    /**
     * set the length we are restricting the comparison to for this magic match
     *
     * @param value DOCUMENT ME!
     */
    public void setLength(int value) {
        this.length = value;
    }

    /**
     * get the type of match for this magic match
     *
     * @return DOCUMENT ME!
     */
    public String getType() {
        return type;
    }

    /**
     * set the type of match to perform for this magic match
     *
     * @param value DOCUMENT ME!
     */
    public void setType(String value) {
        this.type = value;
    }

    /**
     * get the bitmask that will be applied for this magic match
     *
     * @return the bitmask for this magic match
     */
    public long getBitmask() {
        return bitmask;
    }

    /**
     * set the bitmask that will be applied for this magic match
     *
     * @param value DOCUMENT ME!
     */
    public void setBitmask(String value) {
        if (value != null) {
            this.bitmask = Long.decode(value).intValue();
        }
    }

    /**
     * get the comparator for this magic match
     *
     * @return the comparator for this magic match
     */
    public char getComparator() {
        return comparator;
    }

    /**
     * set the comparator for this magic match
     *
     * @param value DOCUMENT ME!
     */
    public void setComparator(String value) {
        this.comparator = value.charAt(0);
    }

    /**
     * get the properties for this magic match
     *
     * @return the properties for this magic match
     */
    public Map<String, String> getProperties() {
        return properties;
    }

    /**
     * set the properties for this magic match
     *
     * @param properties DOCUMENT ME!
     */
    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    /**
     * add a submatch to this magic match
     *
     * @param m a magic match
     */
    public void addSubMatch(MagicMatch m) {
        subMatches.add(m);
    }

    /**
     * get all submatches for this magic match
     *
     * @return a collection of submatches
     */
    public Collection<MagicMatch> getSubMatches() {
        return subMatches;
    }

    /**
     * set all submatches
     *
     * @param a a collection of submatches
     */
    public void setSubMatches(Collection<MagicMatch> a) {
        subMatches.clear();
        subMatches.addAll(a);
    }

    /**
     * determine if this match or any submatches has the description
     *
     * @param desc DOCUMENT ME!
     * @return whether or not the description matches
     */
    public boolean descriptionMatches(String desc) {
        if ((description != null) && description.equals(desc)) {
            return true;
        }

        Collection<MagicMatch> submatches = getSubMatches();
        Iterator<MagicMatch> i = submatches.iterator();
        MagicMatch m = null;

        while (i.hasNext()) {
            m = (MagicMatch) i.next();

            if (m.descriptionMatches(desc)) {
                return true;
            }
        }

        return false;
    }

    /**
     * determine if this match or any submatches has the description
     *
     * @param desc DOCUMENT ME!
     * @return whether or not the description matches
     */
    public boolean mimeTypeMatches(String desc) {
        if ((mimeType != null) && mimeType.equals(desc)) {
            return true;
        }

        Collection<MagicMatch> submatches = getSubMatches();
        Iterator<MagicMatch> i = submatches.iterator();
        MagicMatch m = null;

        while (i.hasNext()) {
            m = (MagicMatch) i.next();

            if (m.mimeTypeMatches(desc)) {
                return true;
            }
        }

        return false;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     * @throws CloneNotSupportedException DOCUMENT ME!
     */
    protected Object clone()
            throws CloneNotSupportedException {
        MagicMatch clone = new MagicMatch();
        clone.setBitmask(Long.toString(bitmask, 8));
        clone.setComparator("" + comparator);
        clone.setDescription(description);
        clone.setExtension(extension);
        clone.setLength(length);
        clone.setMimeType(mimeType);
        clone.setOffset(offset);

        // these properties should only be String types, so we shouldn't have to clone them
        if (properties != null) {
            Map<String, String> m = new HashMap<String, String>();
            m.putAll(properties);
            clone.setProperties(m);
        }

        Iterator<MagicMatch> i = subMatches.iterator();
        List<MagicMatch> a = new ArrayList<MagicMatch>();

        while (i.hasNext()) {
            MagicMatch mm = i.next();
            a.add(mm);
        }

        clone.setSubMatches(a);

        clone.setTest(test);
        clone.setType(type);
        return clone;
    }

}
