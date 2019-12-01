/*
 * The MIT License
 *
 * Copyright (c) 2017 aoju.org All rights reserved.
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
package org.aoju.bus.office.registry;

import org.aoju.bus.office.magic.Formats;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Kimi Liu
 * @version 5.2.8
 * @since JDK 1.8+
 */
public class DefaultFormatRegistry extends SimpleFormatRegistry {

    public DefaultFormatRegistry() {
        Formats pdf = new Formats("Portable Document Format", "pdf", "application/pdf");
        pdf.setStoreProperties(Formats.Type.TEXT, Collections.singletonMap("FilterName", "writer_pdf_Export"));
        pdf.setStoreProperties(Formats.Type.SPREADSHEET, Collections.singletonMap("FilterName", "calc_pdf_Export"));
        pdf.setStoreProperties(Formats.Type.PRESENTATION, Collections.singletonMap("FilterName", "impress_pdf_Export"));
        pdf.setStoreProperties(Formats.Type.DRAWING, Collections.singletonMap("FilterName", "draw_pdf_Export"));
        addFormat(pdf);

        Formats swf = new Formats("Macromedia Flash", "swf", "application/x-shockwave-flash");
        swf.setStoreProperties(Formats.Type.PRESENTATION, Collections.singletonMap("FilterName", "impress_flash_Export"));
        swf.setStoreProperties(Formats.Type.DRAWING, Collections.singletonMap("FilterName", "draw_flash_Export"));
        addFormat(swf);

        // disabled because it's not always available
        //DocumentFormat xhtml = new DocumentFormat("XHTML", "xhtml", "application/xhtml+xml");
        //xhtml.setStoreProperties(FormatFamily.Type.TEXT, Collections.singletonMap("FilterName", "XHTML Writer File"));
        //xhtml.setStoreProperties(FormatFamily.Type.SPREADSHEET, Collections.singletonMap("FilterName", "XHTML Calc File"));
        //xhtml.setStoreProperties(FormatFamily.Type.PRESENTATION, Collections.singletonMap("FilterName", "XHTML Impress File"));
        //addFormat(xhtml);

        Formats html = new Formats("HTML", "html", "text/html");
        // HTML is treated as Text when supplied as input, but as an output it is also
        // available for exporting Spreadsheet and Presentation formats
        html.setInputFamily(Formats.Type.TEXT);
        html.setStoreProperties(Formats.Type.TEXT, Collections.singletonMap("FilterName", "HTML (StarWriter)"));
        html.setStoreProperties(Formats.Type.SPREADSHEET, Collections.singletonMap("FilterName", "HTML (StarCalc)"));
        html.setStoreProperties(Formats.Type.PRESENTATION, Collections.singletonMap("FilterName", "impress_html_Export"));
        addFormat(html);

        Formats odt = new Formats("OpenDocument Text", "odt", "application/vnd.oasis.opendocument.text");
        odt.setInputFamily(Formats.Type.TEXT);
        odt.setStoreProperties(Formats.Type.TEXT, Collections.singletonMap("FilterName", "writer8"));
        addFormat(odt);

        Formats sxw = new Formats("OpenOffice.org 1.0 Text Document", "sxw", "application/vnd.sun.xml.writer");
        sxw.setInputFamily(Formats.Type.TEXT);
        sxw.setStoreProperties(Formats.Type.TEXT, Collections.singletonMap("FilterName", "StarOffice XML (Writer)"));
        addFormat(sxw);

        Formats doc = new Formats("Microsoft Word", "doc", "application/msword");
        doc.setInputFamily(Formats.Type.TEXT);
        doc.setStoreProperties(Formats.Type.TEXT, Collections.singletonMap("FilterName", "MS Word 97"));
        addFormat(doc);

        Formats docx = new Formats("Microsoft Word 2007 XML", "docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        docx.setInputFamily(Formats.Type.TEXT);
        addFormat(docx);

        Formats rtf = new Formats("Rich Text Format", "rtf", "text/rtf");
        rtf.setInputFamily(Formats.Type.TEXT);
        rtf.setStoreProperties(Formats.Type.TEXT, Collections.singletonMap("FilterName", "Rich Text Format"));
        addFormat(rtf);

        Formats wpd = new Formats("WordPerfect", "wpd", "application/wordperfect");
        wpd.setInputFamily(Formats.Type.TEXT);
        addFormat(wpd);

        Formats txt = new Formats("Plain Text", "txt", "text/plain");
        txt.setInputFamily(Formats.Type.TEXT);
        Map<String, Object> txtLoadAndStoreProperties = new LinkedHashMap<String, Object>();
        txtLoadAndStoreProperties.put("FilterName", "Text (encoded)");
        txtLoadAndStoreProperties.put("FilterOptions", "utf8");
        txt.setLoadProperties(txtLoadAndStoreProperties);
        txt.setStoreProperties(Formats.Type.TEXT, txtLoadAndStoreProperties);
        addFormat(txt);

        Formats wikitext = new Formats("MediaWiki wikitext", "wiki", "text/x-wiki");
        wikitext.setStoreProperties(Formats.Type.TEXT, Collections.singletonMap("FilterName", "MediaWiki"));

        Formats ods = new Formats("OpenDocument Spreadsheet", "ods", "application/vnd.oasis.opendocument.spreadsheet");
        ods.setInputFamily(Formats.Type.SPREADSHEET);
        ods.setStoreProperties(Formats.Type.SPREADSHEET, Collections.singletonMap("FilterName", "calc8"));
        addFormat(ods);

        Formats sxc = new Formats("OpenOffice.org 1.0 Spreadsheet", "sxc", "application/vnd.sun.xml.calc");
        sxc.setInputFamily(Formats.Type.SPREADSHEET);
        sxc.setStoreProperties(Formats.Type.SPREADSHEET, Collections.singletonMap("FilterName", "StarOffice XML (Calc)"));
        addFormat(sxc);

        Formats xls = new Formats("Microsoft Excel", "xls", "application/vnd.ms-excel");
        xls.setInputFamily(Formats.Type.SPREADSHEET);
        xls.setStoreProperties(Formats.Type.SPREADSHEET, Collections.singletonMap("FilterName", "MS Excel 97"));
        addFormat(xls);

        Formats xlsx = new Formats("Microsoft Excel 2007 XML", "xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        xlsx.setInputFamily(Formats.Type.SPREADSHEET);
        addFormat(xlsx);

        Formats csv = new Formats("Comma Separated Values", "csv", "text/csv");
        csv.setInputFamily(Formats.Type.SPREADSHEET);
        Map<String, Object> csvLoadAndStoreProperties = new LinkedHashMap<String, Object>();
        csvLoadAndStoreProperties.put("FilterName", "Text - txt - csv (StarCalc)");
        csvLoadAndStoreProperties.put("FilterOptions", "44,34,0");
        csv.setLoadProperties(csvLoadAndStoreProperties);
        csv.setStoreProperties(Formats.Type.SPREADSHEET, csvLoadAndStoreProperties);
        addFormat(csv);

        Formats tsv = new Formats("Tab Separated Values", "tsv", "text/tab-separated-values");
        tsv.setInputFamily(Formats.Type.SPREADSHEET);
        Map<String, Object> tsvLoadAndStoreProperties = new LinkedHashMap<String, Object>();
        tsvLoadAndStoreProperties.put("FilterName", "Text - txt - csv (StarCalc)");
        tsvLoadAndStoreProperties.put("FilterOptions", "9,34,0");
        tsv.setLoadProperties(tsvLoadAndStoreProperties);
        tsv.setStoreProperties(Formats.Type.SPREADSHEET, tsvLoadAndStoreProperties);
        addFormat(tsv);

        Formats odp = new Formats("OpenDocument Presentation", "odp", "application/vnd.oasis.opendocument.presentation");
        odp.setInputFamily(Formats.Type.PRESENTATION);
        odp.setStoreProperties(Formats.Type.PRESENTATION, Collections.singletonMap("FilterName", "impress8"));
        addFormat(odp);

        Formats sxi = new Formats("OpenOffice.org 1.0 Presentation", "sxi", "application/vnd.sun.xml.impress");
        sxi.setInputFamily(Formats.Type.PRESENTATION);
        sxi.setStoreProperties(Formats.Type.PRESENTATION, Collections.singletonMap("FilterName", "StarOffice XML (Impress)"));
        addFormat(sxi);

        Formats ppt = new Formats("Microsoft PowerPoint", "ppt", "application/vnd.ms-powerpoint");
        ppt.setInputFamily(Formats.Type.PRESENTATION);
        ppt.setStoreProperties(Formats.Type.PRESENTATION, Collections.singletonMap("FilterName", "MS PowerPoint 97"));
        addFormat(ppt);

        Formats pptx = new Formats("Microsoft PowerPoint 2007 XML", "pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation");
        pptx.setInputFamily(Formats.Type.PRESENTATION);
        addFormat(pptx);

        Formats odg = new Formats("OpenDocument Drawing", "odg", "application/vnd.oasis.opendocument.graphics");
        odg.setInputFamily(Formats.Type.DRAWING);
        odg.setStoreProperties(Formats.Type.DRAWING, Collections.singletonMap("FilterName", "draw8"));
        addFormat(odg);

        Formats svg = new Formats("Scalable Vector Graphics", "svg", "image/svg+xml");
        svg.setStoreProperties(Formats.Type.DRAWING, Collections.singletonMap("FilterName", "draw_svg_Export"));
        addFormat(svg);
    }

}
