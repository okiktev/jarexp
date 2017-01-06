package com.delfin.jarexp.utils;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

@Deprecated
public class FileContent implements Closeable {

	private interface ContentReader {
		byte[] read(byte[] data);
	}
	
	private abstract class ContentWriter extends OutputStream {
		@Override
        public void write(int b) throws IOException {
			throw new IOException("The method 'write' is not implemented in ContentWriter class");
        }
	}
	
	private abstract class DataBypasser {
		void bypass() throws IOException {
			if (data != null) {
				for (byte b : data) {
					handle(b);
				}
			} else {
				int b;
				while ((b = stream.read()) != -1) {
					handle(b);
				}
			}
		}
		protected abstract void handle(int b) throws IOException;
	}

	private static final Logger log = Logger.getLogger(FileContent.class.getCanonicalName());

	private final InputStream stream;

	private final String path;
	
	private byte[] data;

	public FileContent(File src) throws IOException {
		stream = new FileInputStream(src);
		path = src.getAbsolutePath();
	}

	public FileContent(URL url) throws IOException {
		stream = url.openStream();
		path = url.toExternalForm();
	}

	public void replace(final Map<String, String> tokens) throws IOException {
		if (tokens == null || tokens.isEmpty()) {
			return;
		}

		File tmp = File.createTempFile("tmp", "");
		tmp.deleteOnExit();
		try {
			dumpInto(tmp, new ContentReader() {
				@Override
				public byte[] read(byte[] data) {
					String line = new String(data);
					for (Entry<String, String> entry : tokens.entrySet()) {
						line = line.replace(entry.getKey(), entry.getValue());
					}
					return line.getBytes();
				}
			}, null);
		} finally {
			close();
		}

		File dst = new File(path);
		if (dst.exists()) {
			if (dst.delete()) {
				if (tmp.renameTo(dst)) {
					return;
				}
				throw new IOException("Unable rename file to " + dst);
			}
			throw new IOException("Couldn't delete file " + dst);
		}
		throw new IOException("Unexpected error. The destination file " + dst + " doesn't exist before renaming.");
	}

	public void dumpInto(File dst) throws IOException {
		dumpInto(dst, null, null);
	}
	
	public byte[] getData() throws IOException {
		if (data != null) {
			return data;
		}
		final List<Byte> dt = new LinkedList<Byte>();
		dumpInto(null, null, new ContentWriter() {
			@Override
			public void write(byte[] b, int off, int len) throws IOException {
				for (byte bt : b) {
					dt.add(bt);
				}
			}
		});
		return data = unbox(dt);
	}
	
	@Override
	public void close() {
		try {
			stream.close();
		} catch (IOException e) {
			log.log(Level.SEVERE, "Couldn't close the input stream to " + path, e);
		}
	}

	private void dumpInto(final File dst, ContentReader reader, ContentWriter writer) throws IOException {
		if (reader == null) {
			reader = new ContentReader() {
				@Override
				public byte[] read(byte[] data) {
					return data;
				}
			};
		}
		if (writer == null) {
			writer = new ContentWriter() {
				private OutputStream out = new FileOutputStream(dst);
				@Override
				public void write(byte[] b, int off, int len) throws IOException {
					out.write(b, off, len);
				}
				@Override
				public void close() throws IOException {
					out.close();
				}
			};
		}

		try {
			final List<Byte> data = new LinkedList<Byte>();
			final ContentReader r = reader;
			final ContentWriter w = writer;
			
			new DataBypasser() {
				boolean isHandle = false;

				@Override
                protected void handle(int b) throws IOException {
					data.add(Byte.valueOf((byte) b));
					boolean isEol = b == '\r' || b == '\n';
					if (isEol && isHandle) {
						handleAndWrite(data, r, w);
						isHandle = false;
					}
					isHandle = !isEol;
                }
			}.bypass();

			handleAndWrite(data, r, w);
			
			
			
//			List<Byte> data = new LinkedList<Byte>();
//			int bt;
//			boolean isHandle = false;
//			while ((bt = stream.read()) != -1) {
//				data.add(Byte.valueOf((byte) bt));
//				boolean isEol = bt == '\r' || bt == '\n';
//				if (isEol && isHandle) {
//					handleAndWrite(data, reader, writer);
//					isHandle = false;
//				}
//				isHandle = !isEol;
//			}
//			handleAndWrite(data, reader, writer);
		} catch (IOException e) {
			throw new IOException("An error while dumping stream into " + dst == null ? "memory" : ("file " + dst.getAbsolutePath()), e);
		} finally {
			try {
				writer.close();
			} catch (IOException e) {
				log.log(Level.WARNING, "An error occurred while closing dumping out stream", e);
			}
		}
	}

	private static void handleAndWrite(List<Byte> data, ContentReader reader, ContentWriter writer) throws IOException {
		if (data.isEmpty()) {
			return;
		}
		byte[] dt = unbox(data);
		dt = reader.read(dt);
		writer.write(dt, 0, dt.length);
		data.clear();
	}
	
	private static byte[] unbox(List<Byte> data) {
		byte[] dt = new byte[data.size()];
		for (int i = 0; i < data.size(); ++i) {
			dt[i] = data.get(i);
		}
		return dt;
	}
	
//	public static void main(String[] args) throws MalformedURLException, IOException {
//		
//		
//		FileContent cnt = new FileContent(new URL("http://dst.in.ua/clipboard-manager/downloads/last.html"));
//		
//		System.out.println(new String(cnt.getData()));
//		
//    }

}
