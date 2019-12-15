package org.aoju.bus.office.magic.family;

import org.aoju.bus.core.utils.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * SimpleDocumentFormatRegistry包含office支持的文档格式集合.
 *
 * @author Kimi Liu
 * @version 3.6.6
 * @since JDK 1.8+
 */
public class SimpleFormatRegistry implements FormatRegistry {

    private final Map<String, DocumentFormat> fmtsByExtension = new HashMap<>();
    private final Map<String, DocumentFormat> fmtsByMediaType = new HashMap<>();

    /**
     * 向注册表添加新格式.
     *
     * @param documentFormat 要添加的格式.
     */
    public void addFormat(final DocumentFormat documentFormat) {
        documentFormat
                .getExtensions()
                .stream()
                .map(StringUtils::lowerCase)
                .forEach(ext -> fmtsByExtension.put(ext, documentFormat));
        fmtsByMediaType.put(StringUtils.lowerCase(documentFormat.getMediaType()), documentFormat);
    }

    @Override
    public DocumentFormat getFormatByExtension(final String extension) {
        return extension == null ? null : fmtsByExtension.get(StringUtils.lowerCase(extension));
    }

    @Override
    public DocumentFormat getFormatByMediaType(final String mediaType) {
        return mediaType == null ? null : fmtsByMediaType.get(StringUtils.lowerCase(mediaType));
    }

    @Override
    public Set<DocumentFormat> getOutputFormats(final FamilyType family) {
        return Optional.ofNullable(family).map(docFam -> fmtsByMediaType
                .values()
                .stream()
                .filter(format -> format.getStoreProperties(docFam) != null)
                .collect(Collectors.toSet()))
                .orElse(new HashSet<>());
    }

}
