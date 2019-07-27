package org.aoju.bus.storage.magic;

import java.util.HashMap;
import java.util.Map;

public class MimeTypeFile {

    private static Map<String, String> maps = new HashMap<>();

    static {
        maps.put("image/jpeg", ".jpg");
        maps.put("image/gif", ".gif");
        maps.put("image/png", ".png");
        maps.put("image/bmp", ".bmp");
        maps.put("text/plain", ".txt");
        maps.put("application/zip", ".zip");
        maps.put("application/x-zip-compressed", ".zip");
        maps.put("multipart/x-zip", ".zip");
        maps.put("application/x-compressed", ".zip");
        maps.put("audio/mpeg3", ".mp3");
        maps.put("video/avi", ".avi");
        maps.put("audio/wav", ".wav");
        maps.put("application/x-gzip", ".gzip");
        maps.put("application/x-gzip", ".gz");
        maps.put("text/html", ".html");
        maps.put("application/x-shockwave-flash", ".svg");
        maps.put("application/pdf", ".pdf");
        maps.put("application/msword", ".doc");
        maps.put("application/vnd.openxmlformats-officedocument.wordprocessingml.document", ".docx");
        maps.put("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", ".xlsx");
        maps.put("application/vnd.ms-excel", ".xls");
        maps.put("application/vnd.ms-powerpoint", ".ppt");
        maps.put("application/vnd.openxmlformats-officedocument.presentationml.presentation", ".pptx");
    }

    public static String getFileExtension(String mimeType) {
        return maps.get(mimeType);
    }
}
