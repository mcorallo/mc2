package it.mcsquared.engine.web.filter;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.servlet.ServletOutputStream;

public class GZIPOutputStream extends ServletOutputStream {
	java.util.zip.GZIPOutputStream gzipStream;
	final AtomicBoolean open = new AtomicBoolean(true);
	OutputStream output;

	public GZIPOutputStream(OutputStream output) throws IOException {
		super();
		this.output = output;
		this.gzipStream = new java.util.zip.GZIPOutputStream(output);
	}

	@Override
	public void close() throws IOException {
		if (this.open.compareAndSet(true, false)) {
			this.gzipStream.close();
		}
	}

	@Override
	public void flush() throws IOException {
		this.gzipStream.flush();
	}

	@Override
	public void write(byte b[]) throws IOException {
		write(b, 0, b.length);
	}

	@Override
	public void write(byte b[], int off, int len) throws IOException {
		if (!this.open.get())
			throw new IOException("Stream closed!");
		this.gzipStream.write(b, off, len);
	}

	@Override
	public void write(int b) throws IOException {
		if (!this.open.get())
			throw new IOException("Stream closed!");
		this.gzipStream.write(b);
	}

}
