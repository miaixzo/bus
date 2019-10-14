package org.aoju.bus.storage.provider;

import org.aoju.bus.core.utils.StreamUtils;
import org.aoju.bus.logger.Logger;
import org.aoju.bus.storage.Builder;
import org.aoju.bus.storage.Context;
import org.aoju.bus.storage.magic.Readers;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 本地文件上传
 *
 * @author Kimi Liu
 * @version 5.0.1
 * @since JDK 1.8+
 */
public class LocalFileProvider extends AbstractProvider {

    public LocalFileProvider(Context property) {
        this.property = property;
    }

    @Override
    public Readers download(String fileName) {
        return new Readers(new File(fileName));
    }

    @Override
    public Readers download(String bucketName, String fileName) {
        return null;
    }

    @Override
    public Readers download(String bucketName, String fileName, File file) {
        return null;
    }

    @Override
    public Readers download(String fileName, File file) {
        return null;
    }

    @Override
    public Readers list() {
        return null;
    }

    @Override
    public Readers rename(String oldName, String newName) {
        return null;
    }

    @Override
    public Readers rename(String bucketName, String oldName, String newName) {
        return null;
    }

    @Override
    public Readers upload(String fileName, byte[] content) {
        return null;
    }

    @Override
    public Readers upload(String bucketName, String fileName, InputStream content) {
        try {
            File dest = new File(bucketName, fileName);
            if (!new File(dest.getParent()).exists()) {
                boolean result = new File(dest.getParent()).mkdirs();
                if (!result) {
                    return new Readers(Builder.FAILURE);
                }
            }
            OutputStream out = Files.newOutputStream(dest.toPath());
            StreamUtils.copy(content, out);
            content.close();
            out.close();
            return new Readers(Builder.SUCCESS);
        } catch (IOException e) {
            Logger.error("file upload failed", e.getMessage());
        }
        return new Readers(Builder.FAILURE);
    }

    @Override
    public Readers upload(String bucketName, String fileName, byte[] content) {
        return null;
    }

    @Override
    public Readers remove(String fileName) {
        return null;
    }

    @Override
    public Readers remove(String bucketName, String fileName) {
        return null;
    }

    @Override
    public Readers remove(String bucketName, Path path) {
        return null;
    }
}
