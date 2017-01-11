package org.ukettle.www.serialize;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

public class FileSerializer extends Serializer {

	public String getContentType() {
		return TYPE_STREAM_UTF8;
	}

	public void sendFile(File file, OutputStream os) throws IOException {
		FileInputStream is = null;
		BufferedInputStream buf = null;
		try {
			is = new FileInputStream(file);
			buf = new BufferedInputStream(is);
			int readBytes = 0;
			while ((readBytes = buf.read()) != -1) {
				os.write(readBytes);
			}
			os.flush();
		} finally {
			if (is != null) {
				is.close();
			}
			if (buf != null) {
				buf.close();
			}
		}
	}

}