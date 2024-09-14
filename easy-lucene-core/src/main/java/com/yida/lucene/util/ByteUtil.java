package com.yida.lucene.util;

import java.nio.ByteOrder;

/**
 * 对数字和字节进行转换。<br>
 * 假设数据存储是以大端模式存储的：<br>
 * <ul>
 *     <li>byte: 字节类型 占8位二进制 00000000</li>
 *     <li>char: 字符类型 占2个字节 16位二进制 byte[0] byte[1]</li>
 *     <li>int : 整数类型 占4个字节 32位二进制 byte[0] byte[1] byte[2] byte[3]</li>
 *     <li>long: 长整数类型 占8个字节 64位二进制 byte[0] byte[1] byte[2] byte[3] byte[4] byte[5]</li>
 *     <li>long: 长整数类型 占8个字节 64位二进制 byte[0] byte[1] byte[2] byte[3] byte[4] byte[5] byte[6] byte[7]</li>
 *     <li>float: 浮点数(小数) 占4个字节 32位二进制 byte[0] byte[1] byte[2] byte[3]</li>
 *     <li>double: 双精度浮点数(小数) 占8个字节 64位二进制 byte[0] byte[1] byte[2] byte[3] byte[4]byte[5] byte[6] byte[7]</li>
 * </ul>
 * 注：注释来自Hanlp，代码提供来自pr#1492@Github
 *
 * @author looly, hanlp, FULaBUla
 * @since 5.6.3
 */
public class ByteUtil {

	public static final ByteOrder DEFAULT_ORDER = ByteOrder.LITTLE_ENDIAN;

	/**
	 * short转byte数组<br>
	 * 自定义端序
	 *
	 * @param shortValue short值
	 * @param byteOrder  端序
	 * @return byte数组
	 */
	public static byte[] shortToBytes(short shortValue, ByteOrder byteOrder) {
		byte[] b = new byte[Short.BYTES];
		if (ByteOrder.LITTLE_ENDIAN == byteOrder) {
			b[0] = (byte) (shortValue & 0xff);
			b[1] = (byte) ((shortValue >> Byte.SIZE) & 0xff);
		} else {
			b[1] = (byte) (shortValue & 0xff);
			b[0] = (byte) ((shortValue >> Byte.SIZE) & 0xff);
		}
		return b;
	}

	/**
	 * byte[]转int值<br>
	 * 自定义端序
	 *
	 * @param bytes     byte数组
	 * @param byteOrder 端序
	 * @return int值
	 * @since 5.7.21
	 */
	public static int bytesToInt(byte[] bytes, int start, ByteOrder byteOrder) {
		if (ByteOrder.LITTLE_ENDIAN == byteOrder) {
			return bytes[start] & 0xFF | //
					(bytes[1 + start] & 0xFF) << 8 | //
					(bytes[2 + start] & 0xFF) << 16 | //
					(bytes[3 + start] & 0xFF) << 24; //
		} else {
			return bytes[3 + start] & 0xFF | //
					(bytes[2 + start] & 0xFF) << 8 | //
					(bytes[1 + start] & 0xFF) << 16 | //
					(bytes[start] & 0xFF) << 24; //
		}

	}

	/**
	 * int转byte数组<br>
	 * 自定义端序
	 *
	 * @param intValue  int值
	 * @param byteOrder 端序
	 * @return byte数组
	 */
	public static byte[] intToBytes(int intValue, ByteOrder byteOrder) {

		if (ByteOrder.LITTLE_ENDIAN == byteOrder) {
			return new byte[]{ //
					(byte) (intValue & 0xFF), //
					(byte) ((intValue >> 8) & 0xFF), //
					(byte) ((intValue >> 16) & 0xFF), //
					(byte) ((intValue >> 24) & 0xFF) //
			};

		} else {
			return new byte[]{ //
					(byte) ((intValue >> 24) & 0xFF), //
					(byte) ((intValue >> 16) & 0xFF), //
					(byte) ((intValue >> 8) & 0xFF), //
					(byte) (intValue & 0xFF) //
			};
		}

	}

	/**
	 * long转byte数组<br>
	 * 自定义端序<br>
	 * from: https://stackoverflow.com/questions/4485128/how-do-i-convert-long-to-byte-and-back-in-java
	 *
	 * @param longValue long值
	 * @param byteOrder 端序
	 * @return byte数组
	 */
	public static byte[] longToBytes(long longValue, ByteOrder byteOrder) {
		byte[] result = new byte[Long.BYTES];
		if (ByteOrder.LITTLE_ENDIAN == byteOrder) {
			for (int i = 0; i < result.length; i++) {
				result[i] = (byte) (longValue & 0xFF);
				longValue >>= Byte.SIZE;
			}
		} else {
			for (int i = (result.length - 1); i >= 0; i--) {
				result[i] = (byte) (longValue & 0xFF);
				longValue >>= Byte.SIZE;
			}
		}
		return result;
	}

	/**
	 * float转byte数组，自定义端序<br>
	 *
	 * @param floatValue float值
	 * @param byteOrder  端序
	 * @return byte数组
	 * @since 5.7.18
	 */
	public static byte[] floatToBytes(float floatValue, ByteOrder byteOrder) {
		return intToBytes(Float.floatToIntBits(floatValue), byteOrder);
	}

	/**
	 * double转byte数组<br>
	 * 自定义端序<br>
	 * from: https://stackoverflow.com/questions/4485128/how-do-i-convert-long-to-byte-and-back-in-java
	 *
	 * @param doubleValue double值
	 * @param byteOrder   端序
	 * @return byte数组
	 */
	public static byte[] doubleToBytes(double doubleValue, ByteOrder byteOrder) {
		return longToBytes(Double.doubleToLongBits(doubleValue), byteOrder);
	}

	/**
	 * 将{@link Number}转换为
	 *
	 * @param number 数字
	 * @return bytes
	 */
	public static byte[] numberToBytes(Number number) {
		return numberToBytes(number, DEFAULT_ORDER);
	}

	/**
	 * 将{@link Number}转换为
	 *
	 * @param number    数字
	 * @param byteOrder 端序
	 * @return bytes
	 */
	public static byte[] numberToBytes(Number number, ByteOrder byteOrder) {
		if (number instanceof Double) {
			return doubleToBytes((Double) number, byteOrder);
		} else if (number instanceof Long) {
			return longToBytes((Long) number, byteOrder);
		} else if (number instanceof Integer) {
			return intToBytes((Integer) number, byteOrder);
		} else if (number instanceof Short) {
			return shortToBytes((Short) number, byteOrder);
		} else if (number instanceof Float) {
			return floatToBytes((Float) number, byteOrder);
		} else {
			return doubleToBytes(number.doubleValue(), byteOrder);
		}
	}

}
