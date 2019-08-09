/*
 * The MIT License
 *
 * Copyright (c) 2017, aoju.org All rights reserved.
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
package org.aoju.bus.storage.magic;

import org.aoju.bus.core.lang.exception.CommonException;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;


/**
 * This class represents a single match test
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class MagicMatcher implements Cloneable {

    private List<MagicMatcher> subMatchers = new ArrayList<MagicMatcher>(0);
    private MagicMatch match = null;

    /**
     * constructor
     */
    public MagicMatcher() {
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public MagicMatch getMatch() {
        return this.match;
    }

    /**
     * DOCUMENT ME!
     *
     * @param match DOCUMENT ME!
     */
    public void setMatch(MagicMatch match) {
        this.match = match;
    }

    /**
     * test to see if everything is in order for this match
     *
     * @return whether or not this match has enough data to be valid
     */
    public boolean isValid() {

        if ((match == null) || (match.getTest() == null)) {
            return false;
        }

        String type = new String(match.getTest().array());
        char comparator = match.getComparator();
        String description = match.getDescription();
        String test = new String(match.getTest().array());

        if ((type != null) && !type.equals("") && (comparator != '\0') &&
                ((comparator == '=') || (comparator == '!') || (comparator == '>') ||
                        (comparator == '<')) && (description != null) && !description.equals("") &&
                (test != null) && !test.equals("")) {
            return true;
        }

        return false;
    }

    /**
     * add a submatch to this magic match
     *
     * @param m a magic match
     */
    public void addSubMatcher(MagicMatcher m) {
        subMatchers.add(m);
    }

    /**
     * get all submatches for this magic match
     *
     * @return a collection of submatches
     */
    public Collection<MagicMatcher> getSubMatchers() {
        return subMatchers;
    }

    /**
     * set all submatches
     *
     * @param a a collection of submatches
     */
    public void setSubMatchers(Collection<MagicMatcher> a) {
        subMatchers.clear();
        subMatchers.addAll(a);
    }

    /**
     * test to see if this match or any submatches match
     *
     * @param f             the file that should be used to test the match
     * @param onlyMimeMatch DOCUMENT ME!
     * @return the deepest magic match object that matched
     * @throws IOException DOCUMENT ME!
     */
    public MagicMatch test(File f, boolean onlyMimeMatch)
            throws IOException {

        int offset = match.getOffset();
        String description = match.getDescription();
        String type = match.getType();

        RandomAccessFile file = null;
        file = new RandomAccessFile(f, "r");

        try {
            int length = 0;

            if (type.equals("byte")) {
                length = 1;
            } else if (type.equals("short") || type.equals("leshort") || type.equals("beshort")) {
                length = 4;
            } else if (type.equals("long") || type.equals("lelong") || type.equals("belong")) {
                length = 8;
            } else if (type.equals("string")) {
                length = match.getTest().capacity();
            } else if (type.equals("regex")) {


                final int matchLength = match.getLength();
                length = (matchLength == 0) ? (int) file.length() - offset : matchLength;

                if (length < 0) {
                    length = 0;
                }
            } else if (type.equals("detector")) {
                length = (int) file.length() - offset;

                if (length < 0) {
                    length = 0;
                }
            }

            // we know this match won't work since there isn't enough data for the test
            if (length > (file.length() - offset)) {
                return null;
            }

            byte[] buf = new byte[length];
            file.seek(offset);

            int bytesRead = 0;
            int size = 0;
            boolean gotAllBytes = false;
            boolean done = false;

            while (!done) {
                size = file.read(buf, 0, length - bytesRead);

                if (size == -1) {
                    throw new IOException("reached end of file before all bytes were read");
                }

                bytesRead += size;

                if (bytesRead == length) {
                    gotAllBytes = true;
                    done = true;
                }
            }

            MagicMatch match = null;
            MagicMatch submatch = null;

            if (testInternal(buf)) {
                // set the top level match to this one
                try {
                    match = getMatch() != null ? (MagicMatch) getMatch()
                            .clone() : null;
                } catch (CloneNotSupportedException e) {
                    // noop
                }

                // set the data on this match
                if ((onlyMimeMatch == false) && (subMatchers != null) && (subMatchers.size() > 0)) {
                    for (int i = 0; i < subMatchers.size(); i++) {
                        MagicMatcher m = (MagicMatcher) subMatchers.get(i);
                        if ((submatch = m.test(f, false)) != null) {
                            match.addSubMatch(submatch);
                        }
                    }
                }
            }

            return match;
        } finally {
            try {
                file.close();
            } catch (Exception fce) {
            }
        }
    }

    /**
     * test to see if this match or any submatches match
     *
     * @param data          the data that should be used to test the match
     * @param onlyMimeMatch DOCUMENT ME!
     * @return the deepest magic match object that matched
     * @throws IOException DOCUMENT ME!
     */
    public MagicMatch test(byte[] data, boolean onlyMimeMatch) {
        int offset = match.getOffset();
        String description = match.getDescription();
        String type = match.getType();
        String test = new String(match.getTest().array());
        String mimeType = match.getMimeType();

        int length = 0;

        if (type.equals("byte")) {
            length = 1;
        } else if (type.equals("short") || type.equals("leshort") || type.equals("beshort")) {
            length = 4;
        } else if (type.equals("long") || type.equals("lelong") || type.equals("belong")) {
            length = 8;
        } else if (type.equals("string")) {
            length = match.getTest().capacity();
        } else if (type.equals("regex")) {
            // FIXME - something wrong here, shouldn't have to subtract 1???
            length = data.length - offset - 1;

            if (length < 0) {
                length = 0;
            }
        } else if (type.equals("detector")) {
            // FIXME - something wrong here, shouldn't have to subtract 1???
            length = data.length - offset - 1;

            if (length < 0) {
                length = 0;
            }
        } else {
            throw new CommonException("unsupported test type " + type);
        }

        byte[] buf = new byte[length];

        if ((offset + length) < data.length) {
            System.arraycopy(data, offset, buf, 0, length);

            MagicMatch match = null;
            MagicMatch submatch = null;

            if (testInternal(buf)) {
                // set the top level match to this one
                try {
                    match = getMatch() != null ? (MagicMatch) getMatch()
                            .clone() : null;
                    // set the data on this match
                    if ((onlyMimeMatch == false) && (subMatchers != null) && (subMatchers.size() > 0)) {
                        for (int i = 0; i < subMatchers.size(); i++) {
                            MagicMatcher m = (MagicMatcher) subMatchers.get(i);
                            if ((submatch = m.test(data, false)) != null) {
                                match.addSubMatch(submatch);
                            }
                        }
                    }
                } catch (CloneNotSupportedException e) {
                    // noop
                }
            }

            return match;
        } else {
            return null;
        }
    }

    /**
     * internal test switch
     *
     * @param data DOCUMENT ME!
     * @return DOCUMENT ME!
     */
    private boolean testInternal(byte[] data) {

        if (data.length == 0) {
            return false;
        }

        String type = match.getType();
        String test = new String(match.getTest().array());
        String mimeType = match.getMimeType();
        String description = match.getDescription();

        ByteBuffer buffer = ByteBuffer.allocate(data.length);

        if ((type != null) && (test != null) && (test.length() > 0)) {
            if (type.equals("string")) {
                buffer = buffer.put(data);

                return testString(buffer);
            } else if (type.equals("byte")) {
                buffer = buffer.put(data);

                return testByte(buffer);
            } else if (type.equals("short")) {
                buffer = buffer.put(data);

                return testShort(buffer);
            } else if (type.equals("leshort")) {
                buffer = buffer.put(data);
                buffer.order(ByteOrder.LITTLE_ENDIAN);

                return testShort(buffer);
            } else if (type.equals("beshort")) {
                buffer = buffer.put(data);
                buffer.order(ByteOrder.BIG_ENDIAN);

                return testShort(buffer);
            } else if (type.equals("long")) {
                buffer = buffer.put(data);

                return testLong(buffer);
            } else if (type.equals("lelong")) {
                buffer = buffer.put(data);
                buffer.order(ByteOrder.LITTLE_ENDIAN);

                return testLong(buffer);
            } else if (type.equals("belong")) {
                buffer = buffer.put(data);
                buffer.order(ByteOrder.BIG_ENDIAN);

                return testLong(buffer);
            } else if (type.equals("regex")) {
                return testRegex(new String(data));
            } else if (type.equals("detector")) {
                buffer = buffer.put(data);

                return testDetector(buffer);

                //			} else if (type.equals("date")) {
                //				return testDate(data, BIG_ENDIAN);
                //			} else if (type.equals("ledate")) {
                //				return testDate(data, LITTLE_ENDIAN);
                //			} else if (type.equals("bedate")) {
                //				return testDate(data, BIG_ENDIAN);
            }
        }

        return false;
    }

    /**
     * test the data against the test byte
     *
     * @param data the data we are testing
     * @return if we have a match
     */
    private boolean testByte(ByteBuffer data) {

        String test = new String(match.getTest().array());
        char comparator = match.getComparator();
        long bitmask = match.getBitmask();

        String s = test;
        byte b = data.get(0);
        b = (byte) (b & bitmask);

        int tst = Integer.decode(test).byteValue();
        byte t = (byte) (tst & 0xff);

        switch (comparator) {
            case '=':
                return t == b;

            case '!':
                return t != b;

            case '>':
                return t > b;

            case '<':
                return t < b;
        }

        return false;
    }

    /**
     * test the data against the byte array
     *
     * @param data the data we are testing
     * @return if we have a match
     */
    private boolean testString(ByteBuffer data) {

        ByteBuffer test = match.getTest();
        char comparator = match.getComparator();

        byte[] b = data.array();
        byte[] t = test.array();

        boolean diff = false;
        int i = 0;

        for (i = 0; i < t.length; i++) {

            if (t[i] != b[i]) {
                diff = true;

                break;
            }
        }

        switch (comparator) {
            case '=':
                return !diff;

            case '!':
                return diff;

            case '>':
                return t[i] > b[i];

            case '<':
                return t[i] < b[i];
        }

        return false;
    }

    /**
     * test the data against a short
     *
     * @param data the data we are testing
     * @return if we have a match
     */
    private boolean testShort(ByteBuffer data) {
        short val = 0;
        String test = new String(match.getTest().array());
        char comparator = match.getComparator();
        long bitmask = match.getBitmask();

        val = byteArrayToShort(data);

        // apply bitmask before the comparison
        val = (short) (val & (short) bitmask);

        short tst = 0;

        try {
            tst = Integer.decode(test).shortValue();
        } catch (NumberFormatException e) {

            return false;

            //if (test.length() == 1) {	
            //	tst = new Integer(Character.getNumericValue(test.charAt(0))).shortValue();
            //}
        }

        switch (comparator) {
            case '=':
                return val == tst;

            case '!':
                return val != tst;

            case '>':
                return val > tst;

            case '<':
                return val < tst;
        }

        return false;
    }

    /**
     * test the data against a long
     *
     * @param data the data we are testing
     * @return if we have a match
     */
    private boolean testLong(ByteBuffer data) {

        long val = 0;
        String test = new String(match.getTest().array());
        char comparator = match.getComparator();
        long bitmask = match.getBitmask();

        val = byteArrayToLong(data);

        // apply bitmask before the comparison
        val = val & bitmask;

        long tst = Long.decode(test).longValue();

        switch (comparator) {
            case '=':
                return val == tst;

            case '!':
                return val != tst;

            case '>':
                return val > tst;

            case '<':
                return val < tst;
        }

        return false;
    }

    /**
     * test the data against a regex
     *
     * @param text the data we are testing
     * @return if we have a match
     */
    private boolean testRegex(String text) {

        String test = new String(match.getTest().array());
        char comparator = match.getComparator();

        if (comparator == '=') {
            if (Pattern.matches(test, text)) {
                return true;
            } else {
                return false;
            }
        } else if (comparator == '!') {
            if (Pattern.matches(test, text)) {
                return false;
            } else {
                return true;
            }
        }

        return false;
    }

    /**
     * test the data using a detector
     *
     * @param data the data we are testing
     * @return if we have a match
     */
    private boolean testDetector(ByteBuffer data) {

        String detectorClass = new String(match.getTest().array());

        try {
            Class<?> c = Class.forName(detectorClass);
            MagicDetector detector = (MagicDetector) c.newInstance();
            String[] types = detector.process(data.array(), match.getOffset(), match.getLength(),
                    match.getBitmask(), match.getComparator(), match.getMimeType(),
                    match.getProperties());

            if ((types != null) && (types.length > 0)) {
                // the match object has no mime type set, so set from the detector class processing
                match.setMimeType(types[0]);

                return true;
            }
        } catch (ClassNotFoundException e) {
        } catch (InstantiationException e) {
        } catch (IllegalAccessException e) {
        }

        return false;
    }

    /**
     * Get the extensions for the underlying detectory
     *
     * @return DOCUMENT ME!
     */
    public String[] getDetectorExtensions() {
        String detectorClass = new String(match.getTest().array());

        try {
            Class<?> c = Class.forName(detectorClass);
            MagicDetector detector = (MagicDetector) c.newInstance();

            return detector.getHandledTypes();
        } catch (ClassNotFoundException e) {
        } catch (InstantiationException e) {
        } catch (IllegalAccessException e) {
        }

        return new String[0];
    }

    /**
     * encode a byte as an octal string
     *
     * @param b a byte of data
     * @return an octal representation of the byte data
     */
    private String byteToOctalString(byte b) {
        int n1;
        int n2;
        int n3;
        n1 = (b / 32) & 7;
        n2 = (b / 8) & 7;
        n3 = b & 7;

        return String.valueOf(n1) + String.valueOf(n2) + String.valueOf(n3);
    }

    /**
     * convert a byte array to a short
     *
     * @param data buffer of byte data
     * @return byte array converted to a short
     */
    private short byteArrayToShort(ByteBuffer data) {
        return data.getShort(0);
    }

    /**
     * convert a byte array to a long
     *
     * @param data buffer of byte data
     * @return byte arrays (high and low bytes) converted to a long value
     */
    private long byteArrayToLong(ByteBuffer data) {
        return (long) data.getInt(0);
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     * @throws CloneNotSupportedException DOCUMENT ME!
     */
    protected MagicMatcher clone()
            throws CloneNotSupportedException {
        MagicMatcher clone = new MagicMatcher();

        clone.setMatch((MagicMatch) match.clone());

        Iterator<MagicMatcher> i = subMatchers.iterator();
        List<MagicMatcher> sub = new ArrayList<MagicMatcher>();

        while (i.hasNext()) {
            MagicMatcher m = (MagicMatcher) i.next();
            sub.add(m.clone());
        }

        clone.setSubMatchers(sub);

        return clone;
    }
}
