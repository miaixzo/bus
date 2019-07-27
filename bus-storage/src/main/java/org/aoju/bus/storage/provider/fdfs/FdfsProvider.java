package org.aoju.bus.storage.provider.fdfs;

import org.aoju.bus.storage.UploadObject;
import org.aoju.bus.storage.UploadToken;
import org.aoju.bus.storage.provider.AbstractProvider;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;

/**
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class FdfsProvider extends AbstractProvider {

    public static final String NAME = "fastDFS";

    private String groupName;
    //private StorageClient1 client;

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
