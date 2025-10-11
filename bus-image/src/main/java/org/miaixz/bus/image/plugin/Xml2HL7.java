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
package org.miaixz.bus.image.plugin;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.image.metric.hl7.HL7ContentHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.OutputStreamWriter;

/**
 * The {@code Xml2HL7} class provides a utility to convert an XML representation of an HL7 message back into the
 * traditional HL7 ER7 (pipe and hat) format. It uses a SAX parser and an {@link HL7ContentHandler} to perform the
 * transformation.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Xml2HL7 {

    /**
     * Parses an XML file containing an HL7 message and prints the ER7 formatted message to standard output.
     *
     * @param fname The name of the XML file, or "-" to read from standard input.
     * @throws Exception if a parsing error occurs.
     */
    private static void parseXML(String fname) throws Exception {
        SAXParserFactory f = SAXParserFactory.newInstance();
        SAXParser p = f.newSAXParser();
        HL7ContentHandler ch = new HL7ContentHandler(new OutputStreamWriter(System.out));
        if (fname.equals(Symbol.MINUS)) {
            p.parse(System.in, ch);
        } else {
            p.parse(new File(fname), ch);
        }
    }

    /**
     * Parses command-line arguments to get the input filename and handle help/version options. This method is intended
     * for a command-line interface and contains process-exiting calls.
     *
     * @param args The command-line arguments.
     * @return The filename to process.
     */
    private static String fname(String[] args) {
        if (args.length == 0) {
            System.err.println("Usage: xml2hl7 [options] <xml-file> | -");
            System.exit(2);
        }
        if (args.length > 1) {
            System.err.println("Error: Only one file argument is accepted.");
            System.exit(2);
        }
        String arg0 = args[0];
        if (arg0.equals("-h") || arg0.equals("--help")) {
            System.out.println("Usage: xml2hl7 [options] <xml-file> | -");
            System.out.println("Converts XML presentation of HL7 message to pipe syntax.");
            System.out.println("Options:");
            System.out.println("  -h, --help     display this help and exit");
            System.out.println("  -V, --version  output version information and exit");
            System.exit(0);
        }
        if (arg0.equals("-V") || arg0.equals("--version")) {
            Package p = Xml2HL7.class.getPackage();
            String s = p.getName();
            System.out.println(s.substring(s.lastIndexOf('.') + 1) + ": " + p.getImplementationVersion());
            System.exit(0);
        }
        if (arg0.startsWith("-") && arg0.length() > 1 && !arg0.equals("-")) {
            System.err.println("Error: Unknown option " + arg0);
            System.exit(2);
        }
        return arg0;
    }

}
