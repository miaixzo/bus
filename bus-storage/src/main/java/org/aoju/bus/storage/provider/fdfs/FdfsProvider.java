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
package org.aoju.bus.storage.provider.fdfs;

import org.aoju.bus.storage.UploadObject;
import org.aoju.bus.storage.UploadToken;
import org.aoju.bus.storage.provider.AbstractProvider;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;

/**
 * @author Kimi Liu
 * @version 3.0.0
 * @since JDK 1.8
 */
public class FdfsProvider extends AbstractProvider {

    public static final String NAME = "fastDFS";

    private String groupName;
    // private StorageClient1 client;

    public FdfsProvider(String groupName, Properties props) {
//		this.groupName = groupName;
//		try {
//			ClientGlobal.initByProperties(props);
//			TrackerClient tracker = new TrackerClient();
//			TrackerServer trackerServer = tracker.getConnection();
//			StorageServer storageServer = tracker.getStoreStorage(trackerServer);
//			client = new StorageClient1(trackerServer, storageServer);
//		} catch (Exception e) {
//			throw new RuntimeException(e);
//		}
    }

    @Override
    public String upload(UploadObject object) {
//		NameValuePair[] metaDatas = new NameValuePair[object.getMetadata().size()];
//		int index = 0;
//		for (String key : object.getMetadata().keySet()) {
//			metaDatas[index++] = new NameValuePair(key, object.getMetadata().get(key).toString());
//		}
//		try {
//			if (object.getFile() != null) {
//				client.upload_file1(groupName, object.getFile().getAbsolutePath(), object.getMimeType(), metaDatas);
//			} else if (object.getBytes() != null) {
//
//			} else if (object.getInputStream() != null) {
//
//			}
//		} catch (Exception e) {
//			// TODO: handle exception
//		}
        return null;
    }

    @Override
    public Map<String, Object> createUploadToken(UploadToken param) {
        return null;
    }

    @Override
    public boolean delete(String fileKey) {
        return false;
    }

    @Override
    public String getUrl(String fileKey) {
        return getFullPath(fileKey);
    }

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public void close() throws IOException {

    }

}
