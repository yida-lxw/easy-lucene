package com.yida.lucene.core;

import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.util.PrintStreamInfoStream;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/**
 * 日志级别为debug时，通过日志框架打印Lucene内部信息
 *
 * @author yida
 */
@Slf4j
public class LoggerInfoStreamAdapter extends PrintStreamInfoStream {

	public static final LoggerInfoStreamAdapter INSTANCE = new LoggerInfoStreamAdapter();

	private LoggerInfoStreamAdapter() {
		super(new PrintStream(new ByteArrayOutputStream()) {

			private int last = -1;

			private final ByteArrayOutputStream bufOut = (ByteArrayOutputStream) super.out;

			@Override
			public void write(int b) {
				if ((last == '\r') && (b == '\n')) {
					last = -1;
					return;
				} else if ((b == '\n') || (b == '\r')) {
					try {
						log.debug(bufOut.toString());
					} finally {
						bufOut.reset();
					}
				} else {
					super.write(b);
				}
				last = b;
			}

			@Override
			public void write(byte b[], int off, int len) {
				if (len < 0) {
					throw new ArrayIndexOutOfBoundsException(len);
				}
				for (int i = 0; i < len; i++) {
					write(b[off + i]);
				}
			}
		});
	}

	@Override
	public void message(String component, String message) {
		stream.println(component + " " + message);
	}
}