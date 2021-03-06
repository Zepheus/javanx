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

import java.util.EnumSet;
import java.util.concurrent.locks.ReentrantLock;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;

public class NXFile {
	
	public static final String PKG_FORMAT = "PKG3";
	private static final String ENCODING = "UTF-8";
	private static final boolean OPEN_BY_DEFAULT = true;
	private static final EnumSet<NXReadMode> DEFAULT_PARSE_MODE = EnumSet.of(NXReadMode.EAGER_PARSE_STRINGS);
	
	// Read properties
	private boolean low_memory;
	private boolean lazy_strings;
	
	// File access stuff
	private final RandomAccessFile file;
	private ByteBuffer byteBuffer;
	private SeekableLittleEndianAccessor slea;
	private SeekableLittleEndianAccessor node_reader;
	private boolean parsed;
	private boolean closed;
	private final ReentrantLock lock = new ReentrantLock();
	
	// Format specific properties
	private EnumSet<NXReadMode> parseProperties;
	private NXHeader header;
	private String[] strings;
	private byte[][] stringsb;
	
	private long[] string_offsets;
	private long[] bmp_offsets;
	private long[] mp3_offsets;
	
	// Data containers
	private BufferedImage[] bmp_loaded;
	private byte[][] mp3_loaded;
	
	private NXNode root;
	
	public NXFile(String path) throws FileNotFoundException, IOException {
		this(new RandomAccessFile(path, "r"));
	}
	
	public NXFile(String path, EnumSet<NXReadMode> properties) throws FileNotFoundException, IOException {
		this(new RandomAccessFile(path, "r"), OPEN_BY_DEFAULT, properties);
	}
	
	public NXFile(RandomAccessFile file) throws IOException {
		this(file, OPEN_BY_DEFAULT, DEFAULT_PARSE_MODE);
	}

	public NXFile(RandomAccessFile file, boolean open, EnumSet<NXReadMode> properties) throws IOException {
		this.file = file;
		this.parseProperties = properties;
		low_memory = parseProperties.contains(NXReadMode.LOW_MEMORY);
		lazy_strings = parseProperties.contains(NXReadMode.EAGER_PARSE_STRINGS) || low_memory;
		
		if (open) {
			this.open();
			this.parse();
		}
	}

	public void open() throws IOException {
		FileChannel fileChannel = file.getChannel();
		byteBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, fileChannel.size());
		slea = new SeekableLittleEndianAccessor(byteBuffer);
	}
	
	public void parse() {
		if(parsed)
			return;
		
		lock();
		try {
			header = new NXHeader(slea);
			parseStrings();
			
			parsed = true;
		} finally { unlock(); }
	}
	
	private void parseStrings()
	{
		long offset = header.getStringOffset();
		int stringCount = header.getStringCount();
		strings = new String[stringCount];
		
		slea.seek(offset);
		if(!lazy_strings) {
			stringsb = new byte[stringCount][];
			
			for(int i = 0; i < strings.length; i++) {
				//strings[i] = slea.getUTFString();
				stringsb[i] = slea.getUTFStringB();
			}
		} else {
			string_offsets = new long[stringCount];
			for(int i = 0; i < stringCount; i++) {
				int size = slea.getUShort();
				string_offsets[i] = offset;
				slea.skip(size);
				offset += (size + 2);
			}
		}
	}
	
	public String getString(int id) {
		try {
			if(strings[id] == null) {
				if(lazy_strings) {
					lock();
					try { //TODO: check all deadlocks that could happen during getstring
						int cpos = slea.position();
						slea.seek(string_offsets[id]);
						strings[id] = slea.getUTFString();
						slea.seek(cpos);
					} finally { unlock(); }
				} else {
					strings[id] = new String(stringsb[id], ENCODING);
					stringsb[id] = null; //force GC
				}
			}
			return strings[id];
		} catch (UnsupportedEncodingException e) {
			return null;
		}
	}
	
	public NXNode getRoot() {
		if(root == null) {	
			lock();
			try {
				if(!low_memory) {
					slea.seek(header.getNodeOffset());
					ByteBuffer node_buff = ByteBuffer.wrap(slea.getBytes(header.getNodeCount() * NXNode.SIZE));
					node_reader = new SeekableLittleEndianAccessor(node_buff);
				}
				root = NXNodeParser.parse(getNodeStream(0), null, this);
			} finally { unlock(); }
		}
		return root;
	}
	
	public NXNode resolvePath(String... path) {
		NXNode currentNode = getRoot();
		
		int offset = 0;
		while(offset < path.length) {
			currentNode = currentNode.getChild(path[offset++]);
			if(currentNode == null)
				throw new NXException("Invalid path");
		}
		return currentNode;
	}
	
	public byte[] getMP3(int id) {
		byte[] value;
		if(!low_memory && mp3_loaded != null && (value = mp3_loaded[id]) != null) {
			return value;
		} else {
			long offset = getMP3Offset(id);
			if(offset < 0)
				throw new NXException("The NX file does not include this MP3.");
			
			lock();
			try {
				SeekableLittleEndianAccessor slea = getStreamAtOffset(offset);
				int size = (int)slea.getUInt(); //Warning: this could go out of bounds (but unlikely)
				value = slea.getBytes(size);
			} finally { unlock(); }
			
			if(!low_memory) {
				mp3_loaded[id] = value;
			}
			return value;
		}
	}
	
	public BufferedImage getBitmap(int id) {
		BufferedImage value;
		if(!low_memory && bmp_loaded != null && (value = bmp_loaded[id]) != null) {
			return value;
		} else {
			long offset = getBitmapOffset(id);
			if(offset == -1)
				throw new NXException("NX file does not this canvas.");
			
			lock();
			try {
				SeekableLittleEndianAccessor slea = getStreamAtOffset(offset);
				
				int width = slea.getUShort();
				int height = slea.getUShort();
				long length = slea.getUInt();
				
				ByteBuffer output = ByteBuffer.allocateDirect(width * height * 4);
				NXCompression.decompress(slea.getBuffer(), offset + 4, length + 4, output, 0);
				output.rewind();
				output.order(ByteOrder.LITTLE_ENDIAN);
				
				//TODO: optimize this without bitshifts.
				value = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
				for (int h = 0; h < height; h++) {
					for (int w = 0; w < width; w++) {
						int b = output.get() & 0xFF;
						int g = output.get() & 0xFF;
						int r = output.get() & 0xFF;
						int a = output.get() & 0xFF;
						value.setRGB(w, h, (a << 24) | (r << 16) | (g << 8) | b);
					}
				}
			} finally { unlock(); }
			
			if(!low_memory) bmp_loaded[id] = value;
			return value;
		}
	}
	
	public long getBitmapOffset(int id) {
		int count = header.getBmpCount();
		if(count == 0 || id > count)
			return -1;
		
		if(bmp_offsets == null) {
			bmp_offsets = new long[count];
			lock();
			try {
				populateOffsetTable(bmp_offsets, header.getBmpOffset());
			} finally { unlock(); }
			
			if(!low_memory) {
				bmp_loaded = new BufferedImage[count];
			}
		}
		return bmp_offsets[id];
	}
	
	public long getMP3Offset(int id) {
		int count = header.getMp3Count();
		if(count == 0 || id > count)
			return -1;
		
		if(mp3_offsets == null) {
			mp3_offsets = new long[count];
			lock();
			try {
				populateOffsetTable(mp3_offsets, header.getMp3Offset());
			} finally { unlock(); }
			
			if(!low_memory) {
				mp3_loaded = new byte[count][];
			}
		}
		return mp3_offsets[id];
	}
	
	private void populateOffsetTable(long[] to, long start) {
		slea.seek(start);
		for(int i = 0; i < to.length; i++) {
			to[i] = slea.getLong();
		}
	}
	
	public SeekableLittleEndianAccessor getNodeStream(int id) {
		if(closed)
			throw new NXException("File already closed.");
		
		SeekableLittleEndianAccessor stream = low_memory ? slea : node_reader;
		stream.seek((low_memory ? header.getNodeOffset() : 0) + id * NXNode.SIZE);
		return stream;
	}
	
	public SeekableLittleEndianAccessor getStreamAtOffset(long offset) {
		if(closed)
			throw new NXException("File already closed.");
		slea.seek(offset);
		return slea;
	}
	
	public void lock()
	{
		lock.lock();
	}
	
	public void unlock()
	{
		lock.unlock();
	}
	
	public void close()
	{
		if(closed)
			return;
		
		lock();
		try {
			file.close();
			slea = null;
			node_reader = null;
			byteBuffer = null;
			closed = true;
		} catch (IOException e) {}
		finally { unlock(); }
	}
	
	
	public NXHeader getHeader() {
		return header;
	}
}
