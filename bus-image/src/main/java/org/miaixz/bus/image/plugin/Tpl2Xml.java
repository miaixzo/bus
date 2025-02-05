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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class Tpl2Xml {

    private static final String XML_1_0 = "1.0";
    private static final String XML_1_1 = "1.1";
    private static final String licenseBlock = "/*\n"
            + " ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~\n"
            + " ~                                                                               ~\n"
            + " ~ The MIT License (MIT)                                                         ~\n"
            + " ~                                                                               ~\n"
            + " ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~\n"
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
            + " ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~\n" + " */" + "  ~";
    private static final String elements = "elements";

    private boolean indent = false;
    private String xmlVersion = XML_1_0;
    private String outDir;

    private static Map<String, List<DictionaryElement>> privateDictsFrom(String template) throws IOException {
        Map<String, List<DictionaryElement>> privateDictionaries = new HashMap<>();
        Files.readAllLines(Paths.get(template)).stream().filter(line -> line.length() > 0).forEach(line -> {
            String[] fields = line.split("[)\"][\\s\t\n]+");
            privateDictionaries.computeIfAbsent(fields[4].substring(7), dictionaryElement -> new ArrayList<>())
                    .add(new DictionaryElement(fields));
        });
        return privateDictionaries;
    }

    public final void setIndent(boolean indent) {
        this.indent = indent;
    }

    public final void setXMLVersion(String xmlVersion) {
        this.xmlVersion = xmlVersion;
    }

    public final void setOutDir(String outDir) {
        this.outDir = outDir;
    }

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

    private boolean duplicateTagsOrKeywords(DictionaryElement dictElement, Set<String> keywords, Set<String> tags) {
        if (keywords.add(dictElement.getKeyword()) && tags.add(dictElement.getTag()))
            return false;

        System.out.println("Ignoring duplicate tag or keyword entry: [tag=" + dictElement.getTag() + ", keyword="
                + dictElement.getKeyword() + ", vr=" + dictElement.getVr() + ", vm=" + dictElement.getVm() + ", value="
                + dictElement.getValue() + "]");
        return true;
    }

    private Transformer getTransformer() throws TransformerConfigurationException {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, indent ? "yes" : "no");
        if (indent)
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        transformer.setOutputProperty(OutputKeys.VERSION, xmlVersion);
        return transformer;
    }

    private Path outputDirectory(String template) throws IOException {
        if (outDir == null)
            return Paths.get(template).getParent();

        return Files.createDirectories(Paths.get(outDir));
    }

    static class DictionaryElement {
        private final String vr;
        private final String vm;
        private final String value;
        private String tag;
        private String keyword;

        DictionaryElement(String[] fields) {
            this.vr = fields[2].substring(4);
            this.vm = fields[3].substring(4);
            this.value = fields[6].endsWith("\"") ? fields[6].substring(6, fields[6].length() - 1)
                    : fields[6].substring(6);
            setTagAndKeyword(fields[0], fields[5].substring(9));
        }

        String getVr() {
            return vr;
        }

        String getKeyword() {
            return keyword;
        }

        String getTag() {
            return tag;
        }

        String getVm() {
            return vm;
        }

        String getValue() {
            return value;
        }

        private void setTagAndKeyword(String tag, String keyword) {
            String groupTag = tag.substring(1, 5).toUpperCase();
            String elementTag = "xx" + tag.substring(8, 10).toUpperCase();
            this.keyword = keyword.equals("?") ? "_" + groupTag + "_" + elementTag + "_"
                    : !Pattern.compile("^[a-zA-Z][a-zA-Z0-9]*$").matcher(keyword).matches()
                            ? improveInvalidKeyword(keyword)
                            : keyword;
            this.tag = groupTag + elementTag;
        }

        private String improveInvalidKeyword(String keyword) {
            if (Character.isDigit(keyword.charAt(0)))
                keyword = wordForFirstDigit(keyword) + keyword.substring(1);
            return keyword.replaceAll("[^A-Za-z0-9]", "");
        }

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
            }
            return null;
        }
    }

}
