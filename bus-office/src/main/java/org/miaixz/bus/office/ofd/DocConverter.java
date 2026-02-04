/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.office.ofd;

import java.io.IOException;
import java.nio.file.Path;

import org.miaixz.bus.core.lang.exception.InternalException;
import org.ofdrw.converter.export.*;
import org.ofdrw.converter.ofdconverter.ImageConverter;
import org.ofdrw.converter.ofdconverter.PDFConverter;
import org.ofdrw.converter.ofdconverter.TextConverter;

/**
 * Document converter based on {@code ofdrw-converter}.
 * <p>
 * Provides:
 * <ul>
 * <li>OFD and PDF mutual conversion</li>
 * <li>OFD and TEXT mutual conversion</li>
 * <li>OFD and Image mutual conversion</li>
 * </ul>
 * For more details, see:
 * <a href="https://toscode.gitee.com/ofdrw/ofdrw/blob/master/ofdrw-converter/doc/CONVERTER.md">OFDRW Converter
 * Documentation</a>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class DocConverter {

    /**
     * Converts a PDF file to an OFD file.
     *
     * @param src    The path to the source PDF file.
     * @param target The path to the target OFD file.
     * @param pages  Optional page numbers (0-indexed) to convert. If not provided, all pages are converted.
     * @throws InternalException if an I/O error occurs during conversion.
     */
    public static void pdfToOfd(final Path src, final Path target, final int... pages) {
        try (final org.ofdrw.converter.ofdconverter.DocConverter converter = new PDFConverter(target)) {
            converter.convert(src, pages);
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * Converts a plain text file to an OFD file.
     *
     * @param src      The path to the source text file.
     * @param target   The path to the target OFD file.
     * @param fontSize The font size to use for the text in the OFD document.
     * @throws InternalException if an I/O error occurs during conversion.
     */
    public static void textToOfd(final Path src, final Path target, final double fontSize) {
        try (final TextConverter converter = new TextConverter(target)) {
            converter.setFontSize(fontSize);
            converter.convert(src);
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * Converts multiple image files to a single OFD file.
     *
     * @param target The path to the target OFD file.
     * @param images An array of paths to the source image files.
     * @throws InternalException if an I/O error occurs during conversion.
     */
    public static void imgToOfd(final Path target, final Path... images) {
        try (final org.ofdrw.converter.ofdconverter.DocConverter converter = new ImageConverter(target)) {
            for (final Path image : images) {
                converter.convert(image);
            }
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * Converts an OFD file to image files.
     *
     * @param src       The path to the source OFD file.
     * @param targetDir The directory where the generated image files will be stored.
     * @param imgType   The format of the generated images, e.g., JPG, PNG, GIF, BMP, SVG.
     * @param ppm       The quality of the converted images, pixels per millimeter.
     * @throws InternalException if an I/O error occurs during conversion.
     */
    public static void odfToImage(final Path src, final Path targetDir, final String imgType, final double ppm) {
        if ("svg".equalsIgnoreCase(imgType)) {
            odfToSvg(src, targetDir, ppm);
        }
        try (final ImageExporter exporter = new ImageExporter(src, targetDir, imgType, ppm)) {
            exporter.export();
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * Converts an OFD file to an HTML file.
     *
     * @param src        The path to the source OFD file.
     * @param targetPath The path to the target HTML file.
     * @throws InternalException if an I/O error occurs during conversion.
     */
    public static void odfToHtml(final Path src, final Path targetPath) {
        try (final HTMLExporter exporter = new HTMLExporter(src, targetPath)) {
            exporter.export();
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * Converts an OFD file to a plain text file.
     *
     * @param src        The path to the source OFD file.
     * @param targetPath The path to the target text file.
     * @throws InternalException if an I/O error occurs during conversion.
     */
    public static void odfToText(final Path src, final Path targetPath) {
        try (final TextExporter exporter = new TextExporter(src, targetPath)) {
            exporter.export();
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * Converts an OFD file to a PDF file.
     *
     * @param src        The path to the source OFD file.
     * @param targetPath The path to the target PDF file.
     * @throws InternalException if an I/O error occurs during conversion, or if required PDF libraries are missing.
     */
    public static void odfToPdf(final Path src, final Path targetPath) {
        try (final OFDExporter exporter = new PDFExporterPDFBox(src, targetPath)) {
            exporter.export();
        } catch (final IOException e) {
            throw new InternalException(e);
        } catch (final Exception e) {
            // If PDF-BOX is not introduced by the user, try iText.
            try (final OFDExporter exporter = new PDFExporterIText(src, targetPath)) {
                exporter.export();
            } catch (final IOException e2) {
                throw new InternalException(e);
            }
        }
    }

    /**
     * Converts an OFD file to SVG images.
     *
     * @param src       The path to the source OFD file.
     * @param targetDir The directory where the generated SVG files will be stored.
     * @param ppm       The quality of the converted images, pixels per millimeter.
     * @throws InternalException if an I/O error occurs during conversion.
     */
    private static void odfToSvg(final Path src, final Path targetDir, final double ppm) {
        try (final SVGExporter exporter = new SVGExporter(src, targetDir, ppm)) {
            exporter.export();
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

}
