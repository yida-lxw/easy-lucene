package com.yida.lucene.transaction;

import javax.transaction.xa.Xid;
import java.util.Objects;

/**
 * @author yida
 * @date 2024/9/2 10:23
 */
public class ElXid implements Xid {

	private final int holder;
	private byte[] bytes;

	public ElXid(int id) {
		this.holder = id;
	}

	@Override
	public int getFormatId() {
		return holder;
	}

	@Override
	public byte[] getGlobalTransactionId() {
		if (Objects.isNull(bytes)) {
			bytes = intToBytes(holder);
		}
		return bytes;
	}

	@Override
	public byte[] getBranchQualifier() {
		if (Objects.isNull(bytes)) {
			bytes = intToBytes(holder);
		}
		return bytes;
	}

	private byte[] intToBytes(int intValue) {
		// 小端
		return new byte[]{
				(byte) (intValue & 0xFF),
				(byte) ((intValue >> 8) & 0xFF),
				(byte) ((intValue >> 16) & 0xFF),
				(byte) ((intValue >> 24) & 0xFF)
		};

	}
}
