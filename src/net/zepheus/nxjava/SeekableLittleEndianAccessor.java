/**
 * nxjava: a library for loading the NX file format
 * Copyright (C) 2012 Cedric Van Goethem
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.zepheus.nxjava;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

public class SeekableLittleEndianAccessor {
	private static final String EMPTY = "";
	private static final CharsetDecoder utfDecoder = Charset.forName("UTF-8").newDecoder();
	private final ByteBuffer byteBuffer;
	
	/**
	 * Creates a new seekable accessor of the little endian byte order from an array of bytes.
	 * @param bytes the array of bytes for the accessor to wrap
	 */
	public SeekableLittleEndianAccessor(byte[] bytes) {
		this.byteBuffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
	}
	
	/**
	 * Creates a new seekable accessor of the little endian byte order from a <code>ByteBuffer</code>.
	 * @param byteBuffer a byte buffer wrapping an array of bytes for the accessor
	 */
	public SeekableLittleEndianAccessor(ByteBuffer byteBuffer) {
		this.byteBuffer = byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
	}
	
	/**
	 * Gets the <code>ByteBuffer</code> wrapped by this accessor.
	 * @return the buffer wrapped by this accessor
	 */
	public ByteBuffer getBuffer() {
		return this.byteBuffer;
	}
	
	/**
	 * Gets the current position of this accessor.
	 * @return the current position of this accessor
	 */
	public int position() {
		return this.byteBuffer.position();
	}
	
	/**
	 * Gets the next byte from the buffer.
	 * @return the next <code>byte</code> in the buffer
	 */
	public byte getByte() {
		return byteBuffer.get();
	}
	
	/**
	 * Gets the next unsigned byte from the buffer as an integer.
	 * @return the next unsigned byte in the buffer
	 */
	public int getUByte() {
		return byteBuffer.get() & 0xFF;
	}
	
	/**
	 * Gets the specified <code>number</code> of bytes from the buffer as an array.
	 * @param number the amount of bytes to get
	 * @return an array of bytes containing the next <code>number</code> bytes
	 */
	public byte[] getBytes(int number) {
		byte[] ret = new byte[number];
		byteBuffer.get(ret);
		return ret;
	}
	
	/**
	 * Gets the next short integer from the buffer.
	 * @return the next <code>short</code> in the buffer
	 */
	public short getShort() {
		return byteBuffer.getShort();
	}
	
	/**
	 * Gets the next unsigned short integer from the buffer as an integer.
	 * @return the next unsigned short integer in the buffer
	 */
	public int getUShort() {
		return byteBuffer.getShort() & 0xFFFF;
	}
	
	/**
	 * Gets the next integer from the buffer.
	 * @return the next <code>int</code> in the buffer
	 */
	public int getInt() {
		return byteBuffer.getInt();
	}
	
	/**
	 * Gets the next unsigned integer from the buffer as a long integer.
	 * @return the next unsigned integer in the buffer
	 */
	public long getUInt() {
		return byteBuffer.getInt() & 0xFFFFFFFF;
	}
	
	/**
	 * Gets the next long integer from the buffer.
	 * @return the next <code>long</code> in the buffer
	 */
	public long getLong() {
		return byteBuffer.getLong();
	}
	
	/**
	 * Gets the next floating-point decimal from the buffer.
	 * @return the next <code>float</code> in the buffer
	 */
	public float getFloat() {
		return byteBuffer.getFloat();
	}
	
	/**
	 * Gets the next double-precision decimal from the buffer.
	 * @return the next <code>double</code> in the buffer
	 */
	public double getDouble() {
		return byteBuffer.getDouble();
	}
	

	public String getUTFString() {
		return SeekableLittleEndianAccessor.getUTF(this);
	}
	
	public byte[] getUTFStringB() {
		return getBytes(getUShort());
	}
	
	public String getUTFString(int length) {
		return SeekableLittleEndianAccessor.getUTF(this, length);
	}
	
	/**
	 * Skips ahead in the buffer by the specified amount.
	 * @param amount the amount to skip ahead by
	 */
	public void skip(int amount) {
		byteBuffer.position(byteBuffer.position() + amount);
	}
	
	/**
	 * Seeks to the desired offset in the buffer.
	 * @param offset the offset to seek to
	 * @throws NXException if the offset is greater than <code>Integer.MAX_VALUE</code>
	 */
	public void seek(long offset) {
		if (offset > Integer.MAX_VALUE) {
			throw new RuntimeException("Unable to seek to the specified offset due to integer limitations.");
		}
		byteBuffer.position((int) offset);
	}
	
	/**
	 * Gets the specified <code>SeekableLittleEndianAccessor</code> as a UTF-8 string.
	 * @param slea the <code>SeekableLittleEndianAccessor</code> to read as a string
	 * @return the accessor read out as a <code>String</code>
	 */
	public static String getUTF(SeekableLittleEndianAccessor slea) {
		int length = slea.getUShort();
		return getUTF(slea, length);
	}
	
	public static String getUTF(SeekableLittleEndianAccessor slea, int length) {
		byte[] data = slea.getBytes(length);
		String ret = EMPTY;
		try {
			ret = utfDecoder.decode(ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN)).toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}
}
