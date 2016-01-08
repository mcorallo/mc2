package it.mcsquared.engine.web.filter;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

public class GZIPHttpServletResponseWrapper extends HttpServletResponseWrapper {
    private GZIPOutputStream gzipStream;
    private ServletOutputStream outputStream;
    private PrintWriter printWriter;

    public GZIPHttpServletResponseWrapper(HttpServletResponse response) throws IOException {
        super(response);

        response.addHeader("Content-Encoding", "gzip");
    }

    public void finish() throws IOException {
        if (this.printWriter != null) {
            this.printWriter.close();
        }
        if (this.outputStream != null) {
            this.outputStream.close();
        }
        if (this.gzipStream != null) {
            this.gzipStream.close();
        }
    }

    @Override
    public void flushBuffer() throws IOException {
        if (this.printWriter != null) {
            this.printWriter.flush();
        }
        if (this.outputStream != null) {
            this.outputStream.flush();
        }
        super.flushBuffer();
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        if (this.printWriter != null) throw new IllegalStateException("printWriter already defined");
        if (this.outputStream == null) {
            initGzip();
            this.outputStream = this.gzipStream;
        }
        return this.outputStream;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        if (this.outputStream != null) throw new IllegalStateException("printWriter already defined");
        if (this.printWriter == null) {
            initGzip();
            this.printWriter = new PrintWriter(new OutputStreamWriter(this.gzipStream, getResponse().getCharacterEncoding()));
        }
        return this.printWriter;
    }

    private void initGzip() throws IOException {
        this.gzipStream = new GZIPOutputStream(getResponse().getOutputStream());
    }

    @Override
    public void setContentLength(int len) {
        // Do nothing
    }
}
