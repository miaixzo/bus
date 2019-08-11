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
import org.aoju.bus.core.lang.exception.InstrumentException;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.*;


/**
 * This class is the primary class for jMimeMagic
 *
 * @author Kimi Liu
 * @version 3.0.5
 * @since JDK 1.8
 */
public class Magic {

    private static boolean initialized = false;
    private static MagicParser magicParser = null;
    private static Map<String, List<MagicMatcher>> hintMap = new HashMap<String, List<MagicMatcher>>();

    /**
     * constructor
     */
    public Magic() {
    }

    /**
     * Add a hint to use the specified matcher for the given extension
     *
     * @param extension DOCUMENT ME!
     * @param matcher   DOCUMENT ME!
     */
    private static void addHint(String extension, MagicMatcher matcher) {
        if (hintMap.keySet().contains(extension)) {
            List<MagicMatcher> a = hintMap.get(extension);
            a.add(matcher);
        } else {
            List<MagicMatcher> a = new ArrayList<MagicMatcher>();
            a.add(matcher);
            hintMap.put(extension, a);
        }
    }

    /**
     * create a parser and initialize it
     *
     * @throws CommonException DOCUMENT ME!
     */
    public static synchronized void initialize()
            throws CommonException {

        if (!initialized) {
            magicParser = new MagicParser();
            magicParser.initialize();

            // build hint map
            Iterator<MagicMatcher> i = magicParser.getMatchers().iterator();

            while (i.hasNext()) {
                MagicMatcher matcher = i.next();
                String ext = matcher.getMatch().getExtension();

                if ((ext != null) && !ext.trim().equals("")) {
                    addHint(ext, matcher);
                } else if (matcher.getMatch().getType().equals("detector")) {
                    String[] exts = matcher.getDetectorExtensions();

                    for (int j = 0; j < exts.length; j++) {
                        addHint(exts[j], matcher);
                    }
                }
            }

            initialized = true;
        }
    }

    /**
     * return the parsed MagicMatch objects that were created from the magic.xml
     * definitions
     *
     * @return the parsed MagicMatch objects
     * @throws CommonException DOCUMENT ME!
     */
    public static Collection<MagicMatcher> getMatchers()
            throws CommonException {

        if (!initialized) {
            initialize();
        }

        Iterator<MagicMatcher> i = magicParser.getMatchers().iterator();
        List<MagicMatcher> m = new ArrayList<MagicMatcher>();

        while (i.hasNext()) {
            MagicMatcher matcher = (MagicMatcher) i.next();

            try {
                m.add(matcher.clone());
            } catch (CloneNotSupportedException e) {
                throw new CommonException("failed to clone matchers");
            }
        }

        return m;
    }

    /**
     * get a match from a stream of data
     *
     * @param data DOCUMENT ME!
     * @return DOCUMENT ME!
     * @throws CommonException DOCUMENT ME!
     */
    public static MagicMatch getMagicMatch(byte[] data)
            throws CommonException {
        return getMagicMatch(data, false);
    }

    /**
     * get a match from a stream of data
     *
     * @param data          DOCUMENT ME!
     * @param onlyMimeMatch DOCUMENT ME!
     * @return DOCUMENT ME!
     * @throws CommonException DOCUMENT ME!
     */
    public static MagicMatch getMagicMatch(byte[] data, boolean onlyMimeMatch)
            throws CommonException {


        if (!initialized) {
            initialize();
        }

        Collection<MagicMatcher> matchers = magicParser.getMatchers();

        MagicMatcher matcher = null;
        MagicMatch match = null;
        Iterator<MagicMatcher> i = matchers.iterator();

        while (i.hasNext()) {
            matcher = i.next();


            try {
                if ((match = matcher.test(data, onlyMimeMatch)) != null) {

                    return match;
                }
            } catch (CommonException e) {
                throw new CommonException(e);
            }
        }

        throw new CommonException();
    }

    /**
     * get a match from a file
     *
     * @param file           the file to match content in
     * @param extensionHints whether or not to use extension to optimize order of content tests
     * @return the MagicMatch object representing a match in the file
     * @throws CommonException DOCUMENT ME!
     */
    public static MagicMatch getMagicMatch(File file, boolean extensionHints)
            throws CommonException {
        return getMagicMatch(file, extensionHints, false);
    }

    /**
     * get a match from a file
     *
     * @param file           the file to match content in
     * @param extensionHints whether or not to use extension to optimize order of content tests
     * @param onlyMimeMatch  only try to get mime type, no submatches are processed when true
     * @return the MagicMatch object representing a match in the file
     * @throws CommonException DOCUMENT ME!
     */
    public static MagicMatch getMagicMatch(File file, boolean extensionHints, boolean onlyMimeMatch)
            throws CommonException {

        if (!initialized) {
            initialize();
        }

        long start = System.currentTimeMillis();

        MagicMatcher matcher = null;
        MagicMatch match = null;

        // check for extension hints
        List<MagicMatcher> checked = new ArrayList<MagicMatcher>();

        if (extensionHints) {

            String name = file.getName();
            int pos = name.lastIndexOf('.');

            if (pos > -1) {
                String ext = name.substring(pos + 1, name.length());

                if ((ext != null) && !ext.equals("")) {

                    Collection<MagicMatcher> c = hintMap.get(ext);

                    if (c != null) {
                        Iterator<MagicMatcher> i = c.iterator();

                        while (i.hasNext()) {
                            matcher = (MagicMatcher) i.next();


                            try {
                                if ((match = matcher.test(file, onlyMimeMatch)) != null) {
                                    return match;
                                }
                            } catch (CommonException e) {
                                throw new CommonException(e);
                            } catch (IOException e) {
                                throw new CommonException(e);
                            }

                            // add to the already checked list
                            checked.add(matcher);
                        }
                    }
                }
            }
        }

        Collection<MagicMatcher> matchers = magicParser.getMatchers();

        Iterator<MagicMatcher> i = matchers.iterator();

        while (i.hasNext()) {
            matcher = (MagicMatcher) i.next();

            if (!checked.contains(matcher)) {

                try {
                    if ((match = matcher.test(file, onlyMimeMatch)) != null) {
                        return match;
                    }
                } catch (CommonException e) {
                    throw new CommonException(e);
                } catch (IOException e) {
                    throw new CommonException(e);
                }
            }
        }

        throw new CommonException();
    }

    /**
     * print the contents of a magic file
     *
     * @param stream DOCUMENT ME!
     * @throws CommonException DOCUMENT ME!
     */
    public static void printMagicFile(PrintStream stream)
            throws CommonException {
        if (!initialized) {
            initialize();
        }

        Collection<MagicMatcher> matchers = Magic.getMatchers();

        MagicMatcher matcher = null;
        Iterator<MagicMatcher> i = matchers.iterator();

        while (i.hasNext()) {
            matcher = (MagicMatcher) i.next();
            printMagicMatcher(stream, matcher, "");
        }
    }

    /**
     * print a magic match
     *
     * @param stream  DOCUMENT ME!
     * @param matcher DOCUMENT ME!
     * @param spacing DOCUMENT ME!
     */
    private static void printMagicMatcher(PrintStream stream, MagicMatcher matcher, String spacing) {
        stream.println(spacing + "name: " + matcher.getMatch().getDescription());
        stream.println(spacing + "children: ");

        Collection<MagicMatcher> matchers = matcher.getSubMatchers();
        Iterator<MagicMatcher> i = matchers.iterator();

        while (i.hasNext()) {
            printMagicMatcher(stream, (MagicMatcher) i.next(), spacing + "  ");
        }
    }

    /**
     * print a magic match
     *
     * @param stream  DOCUMENT ME!
     * @param match   DOCUMENT ME!
     * @param spacing DOCUMENT ME!
     */
    public static void printMagicMatch(PrintStream stream, MagicMatch match, String spacing) {
        stream.println(spacing + "=============================");
        stream.println(spacing + "mime type: " + match.getMimeType());
        stream.println(spacing + "description: " + match.getDescription());
        stream.println(spacing + "extension: " + match.getExtension());
        stream.println(spacing + "test: " + new String(match.getTest().array()));
        stream.println(spacing + "bitmask: " + match.getBitmask());
        stream.println(spacing + "offset: " + match.getOffset());
        stream.println(spacing + "length: " + match.getLength());
        stream.println(spacing + "type: " + match.getType());
        stream.println(spacing + "comparator: " + match.getComparator());
        stream.println(spacing + "=============================");

        Collection<MagicMatch> submatches = match.getSubMatches();
        Iterator<MagicMatch> i = submatches.iterator();

        while (i.hasNext()) {
            printMagicMatch(stream, (MagicMatch) i.next(), spacing + "    ");
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param args DOCUMENT ME!
     */
    public static void main(String[] args) {
        try {
            if (args.length == 0) {
                System.err.println("usage: test <file>");
                System.exit(1);
            }
            File f = new File(args[0]);

            if (f.exists()) {
                MagicMatch match = Magic.getMagicMatch(f, true, false);
                printMagicMatch(System.out, match, "");
            }
        } catch (CommonException e) {
            throw new InstrumentException(e);
        } catch (Exception e) {
            throw new InstrumentException(e);
        }
    }
}
