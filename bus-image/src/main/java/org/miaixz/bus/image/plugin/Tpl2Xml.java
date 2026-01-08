/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;

/**
 * The {@code Tpl2Xml} class converts a proprietary text-based template file for private DICOM dictionaries into
 * standard XML dictionary files. Each private dictionary, identified by a private creator string in the template, is
 * converted into a separate XML file.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Tpl2Xml {

    /**
     * Constant for XML version 1.0.
     */
    private static final String XML_1_0 = "1.0";
    /**
     * Constant for XML version 1.1.
     */
    private static final String XML_1_1 = "1.1";
    /**
     * The license block to be included as a comment in the generated XML files.
     */
    private static final String licenseBlock = "/*\n"
            + " ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~\n"
            + " ~                                                                               ~\n"
            + " ~ The MIT License (MIT)                                                         ~\n"
            + " ~                                                                               ~\n"
            + " ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~\n"
            + " ~                                                                               ~\n"
            + " ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~\n"
            + " ~ of this software and associated documentation files (the \"Software\"), to deal ~\n"
            + " ~ in the Software without restriction, including without limitation the rights  ~\n"
            + " ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~\n"
            + " ~ copies of the Software, and to permit persons to whom the Software is         ~\n"
            + " ~ furnished to do so, subject to the following conditions:                      ~\n"
            + " ~                                                                               ~\n"
            + " ~ The above copyright notice and this permission notice shall be included in    ~\n"
            + " ~ all copies or substantial portions of the Software.                           ~\n"
            + " ~                                                                               ~\n"
            + " ~ THE SOFTWARE IS PROVIDED \"AS IS\", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~\n"
            + " ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~\n"
            + " ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~\n"
            + " ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~\n"
            + " ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~\n"
            + " ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~\n"
            + " ~ THE SOFTWARE.                                                                 ~\n"
            + " ~                                                                               ~\n"
            + " ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~\n" + "*/";
    /**
     * The root element name for the generated XML files.
     */
    private static final String elements = "elements";

    /**
     * Whether to format the XML output with indentation.
     */
    private boolean indent = false;
    /**
     * The XML version to be used in the output.
     */
    private String xmlVersion = XML_1_0;
    /**
     * The path to the output directory.
     */
    private String outDir;

    /**
     * Reads a template file and parses it into a map of private dictionaries.
     *
     * @param template The path to the template file.
     * @return A map where keys are private creator IDs and values are lists of dictionary elements.
     * @throws IOException if an I/O error occurs reading the file.
     */
    private static Map<String, List<DictionaryElement>> privateDictsFrom(String template) throws IOException {
        Map<String, List<DictionaryElement>> privateDictionaries = new HashMap<>();
        Files.readAllLines(Paths.get(template)).stream().filter(line -> line.length() > 0).forEach(line -> {
            String[] fields = line.split("[)\"][\\s\t\n]+");
            privateDictionaries.computeIfAbsent(fields[4].substring(7), dictionaryElement -> new ArrayList<>())
                    .add(new DictionaryElement(fields));
        });
        return privateDictionaries;
    }

    /**
     * Sets whether to indent the XML output.
     *
     * @param indent {@code true} to enable indentation.
     */
    public final void setIndent(boolean indent) {
        this.indent = indent;
    }

    /**
     * Sets the XML version for the output document.
     *
     * @param xmlVersion The XML version string (e.g., "1.0" or "1.1").
     */
    public final void setXMLVersion(String xmlVersion) {
        this.xmlVersion = xmlVersion;
    }

    /**
     * Sets the output directory for the generated XML files.
     *
     * @param outDir The path to the output directory.
     */
    public final void setOutDir(String outDir) {
        this.outDir = outDir;
    }

    /**
     * Converts a given template file into one or more XML dictionary files.
     *
     * @param template The path to the source template file.
     * @throws Exception if an error occurs during the conversion process.
     */
    private void convert(String template) throws Exception {
        Path dir = outputDirectory(template);
        for (Map.Entry<String, List<DictionaryElement>> entry : privateDictsFrom(template).entrySet()) {
            Path file = Files.createFile(dir.resolve(entry.getKey().replaceAll("[:;?\\s/]", "-") + ".xml"));
            DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();
            Document document = documentBuilder.newDocument();
            document.insertBefore(document.createComment("\n" + licenseBlock + "\n"), document.getDocumentElement());
            Element root = document.createElement(elements);
            document.appendChild(root);
            Set<String> keywords = new HashSet<>();
            Set<String> tags = new HashSet<>();
            for (DictionaryElement dictElement : entry.getValue()) {
                if (duplicateTagsOrKeywords(dictElement, keywords, tags))
                    continue;

                Element el = document.createElement("el");
                root.appendChild(el);
                el.setAttribute("tag", dictElement.getTag());
                el.setAttribute("keyword", dictElement.getKeyword());
                el.setAttribute("vr", dictElement.getVr());
                el.setAttribute("vm", dictElement.getVm());
                el.appendChild(document.createTextNode(dictElement.getValue()));
            }
            DOMSource domSource = new DOMSource(document);
            StreamResult streamResult = new StreamResult(file.toFile());
            getTransformer().transform(domSource, streamResult);
        }
    }

    /**
     * Checks for duplicate tags or keywords within a dictionary to ensure uniqueness.
     *
     * @param dictElement The dictionary element to check.
     * @param keywords    A set of keywords already encountered.
     * @param tags        A set of tags already encountered.
     * @return {@code true} if the element is a duplicate, {@code false} otherwise.
     */
    private boolean duplicateTagsOrKeywords(DictionaryElement dictElement, Set<String> keywords, Set<String> tags) {
        if (keywords.add(dictElement.getKeyword()) && tags.add(dictElement.getTag()))
            return false;

        System.out.println(
                "Ignoring duplicate tag or keyword entry: [tag=" + dictElement.getTag() + ", keyword="
                        + dictElement.getKeyword() + ", vr=" + dictElement.getVr() + ", vm=" + dictElement.getVm()
                        + ", value=" + dictElement.getValue() + "]");
        return true;
    }

    /**
     * Creates and configures a {@link Transformer} for writing the XML document to a file.
     *
     * @return A configured {@code Transformer}.
     * @throws TransformerConfigurationException if a transformer cannot be created.
     */
    private Transformer getTransformer() throws TransformerConfigurationException {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, indent ? "yes" : "no");
        if (indent)
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        transformer.setOutputProperty(OutputKeys.VERSION, xmlVersion);
        return transformer;
    }

    /**
     * Determines the output directory, creating it if it doesn't exist.
     *
     * @param template The path of the source template file.
     * @return The path to the output directory.
     * @throws IOException if an I/O error occurs.
     */
    private Path outputDirectory(String template) throws IOException {
        if (outDir == null)
            return Paths.get(template).getParent();

        return Files.createDirectories(Paths.get(outDir));
    }

    /**
     * Represents a single element from a private dictionary template.
     */
    static class DictionaryElement {

        private final String vr;
        private final String vm;
        private final String value;
        private String tag;
        private String keyword;

        /**
         * Constructs a DictionaryElement by parsing an array of fields from a template line.
         *
         * @param fields The fields parsed from a single line of the template.
         */
        DictionaryElement(String[] fields) {
            this.vr = fields[2].substring(4);
            this.vm = fields[3].substring(4);
            this.value = fields[6].endsWith("\"") ? fields[6].substring(6, fields[6].length() - 1)
                    : fields[6].substring(6);
            setTagAndKeyword(fields[0], fields[5].substring(9));
        }

        /**
         * Gets the Value Representation (VR) of the element.
         *
         * @return The VR string.
         */
        String getVr() {
            return vr;
        }

        /**
         * Gets the keyword of the element.
         *
         * @return The keyword string.
         */
        String getKeyword() {
            return keyword;
        }

        /**
         * Gets the tag of the element in "GGGGxxxx" format.
         *
         * @return The tag string.
         */
        String getTag() {
            return tag;
        }

        /**
         * Gets the Value Multiplicity (VM) of the element.
         *
         * @return The VM string.
         */
        String getVm() {
            return vm;
        }

        /**
         * Gets the descriptive value of the element.
         *
         * @return The value string.
         */
        String getValue() {
            return value;
        }

        /**
         * Sets the tag and keyword, applying cleaning logic to the keyword if necessary.
         *
         * @param tag     The raw tag string from the template.
         * @param keyword The raw keyword string from the template.
         */
        private void setTagAndKeyword(String tag, String keyword) {
            String groupTag = tag.substring(1, 5).toUpperCase();
            String elementTag = "xx" + tag.substring(8, 10).toUpperCase();
            this.keyword = keyword.equals("?") ? "_" + groupTag + "_" + elementTag + "_"
                    : !Pattern.compile("^[a-zA-Z][a-zA-Z0-9]*$").matcher(keyword).matches()
                            ? improveInvalidKeyword(keyword)
                            : keyword;
            this.tag = groupTag + elementTag;
        }

        /**
         * Cleans up an invalid keyword to conform to typical identifier standards.
         *
         * @param keyword The invalid keyword.
         * @return The cleaned keyword.
         */
        private String improveInvalidKeyword(String keyword) {
            if (Character.isDigit(keyword.charAt(0)))
                keyword = wordForFirstDigit(keyword) + keyword.substring(1);
            return keyword.replaceAll("[^A-Za-z0-9]", "");
        }

        /**
         * Converts the first digit of a string to its English word equivalent.
         *
         * @param keyword The keyword starting with a digit.
         * @return The corresponding English word for the first digit.
         */
        private String wordForFirstDigit(String keyword) {
            switch (keyword.charAt(0)) {
                case '0':
                    return "Zero";

                case '1':
                    return "One";

                case '2':
                    return "Two";

                case '3':
                    return "Three";

                case '4':
                    return "Four";

                case '5':
                    return "Five";

                case '6':
                    return "Six";

                case '7':
                    return "Seven";

                case '8':
                    return "Eight";

                case '9':
                    return "Nine";

                default:
                    return null;
            }
        }
    }

}
