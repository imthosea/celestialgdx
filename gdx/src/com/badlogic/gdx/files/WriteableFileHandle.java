package com.badlogic.gdx.files;

import com.badlogic.gdx.utils.GdxIoException;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

public abstract class WriteableFileHandle extends FileHandle {
	protected WriteableFileHandle(String path, String name) {
		super(path, name);
	}

	public abstract WriteableFileHandle parent();
	public abstract WriteableFileHandle sibling(String name);
	public abstract WriteableFileHandle child(String name);

	public abstract OutputStream write(boolean append) throws GdxIoException;

	public OutputStream write(boolean append, int bufferSize) throws GdxIoException {
		return new BufferedOutputStream(write(append), bufferSize);
	}

	public void write(InputStream input, boolean append) throws GdxIoException {
		try(OutputStream out = write(append);
		    InputStream in = input) {
			in.transferTo(out);
		} catch(IOException e) {
			throw new GdxIoException(e);
		}
	}

	public Writer writer(boolean append) throws GdxIoException {
		return writer(append, null);
	}

	public Writer writer(boolean append, String charset) throws GdxIoException {
		if(charset == null) {
			return new OutputStreamWriter(write(append));
		} else {
			try {
				return new OutputStreamWriter(write(append), charset);
			} catch(UnsupportedEncodingException e) {
				throw new GdxIoException(e);
			}
		}
	}

	public void writeString(String string, boolean append) throws GdxIoException {
		writeString(string, append, null);
	}

	public void writeString(String string, boolean append, String charset) throws GdxIoException {
		try(Writer writer = writer(append, charset)) {
			writer.write(string);
		} catch(IOException e) {
			throw new GdxIoException(e);
		}
	}

	public void writeBytes(byte[] bytes, boolean append) throws GdxIoException {
		try(OutputStream output = write(append)) {
			output.write(bytes);
		} catch(IOException e) {
			throw new GdxIoException(e);
		}
	}

	public void writeBytes(byte[] bytes, int offset, int length, boolean append) throws GdxIoException {
		try(OutputStream output = write(append)) {
			output.write(bytes, offset, length);
		} catch(IOException e) {
			throw new GdxIoException(e);
		}
	}
}