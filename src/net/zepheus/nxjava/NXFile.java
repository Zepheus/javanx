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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class NXFile {
	
	public static final String PKG_FORMAT = "PKG3";
	private static final boolean OPEN_BY_DEFAULT = true;
	private static final EnumSet<NXReadMode> DEFAULT_PARSE_MODE = EnumSet.noneOf(NXReadMode.class);
	
	// File access stuff
	private final RandomAccessFile file;
	private ByteBuffer byteBuffer;
	private SeekableLittleEndianAccessor slea;
	private SeekableLittleEndianAccessor node_reader;
	private boolean parsed;
	private boolean closed;
	private boolean low_memory;
	private final ReentrantLock lock = new ReentrantLock();
	
	// Format specific properties
	private EnumSet<NXReadMode> parseProperties;
	private NXHeader header;
	private String[] strings;
	private byte[][] stringsb;
	private NXNode root;
	
	public NXFile(String path) throws FileNotFoundException, IOException {
		this(new RandomAccessFile(path, "r"));
	}
	
	public NXFile(RandomAccessFile file) throws IOException {
		this(file, OPEN_BY_DEFAULT, DEFAULT_PARSE_MODE);
	}

	public NXFile(RandomAccessFile file, boolean open, EnumSet<NXReadMode> properties) throws IOException {
		this.file = file;
		this.parseProperties = properties;
		low_memory = parseProperties.contains(NXReadMode.LOW_MEMORY);
		
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
		strings = new String[header.getStringCount()];
		stringsb = new byte[header.getStringCount()][];
		slea.seek(header.getStringOffset());
		
		for(int i = 0; i < strings.length; i++) {
			//strings[i] = slea.getUTFString();
			stringsb[i] = slea.getUTFStringB();
		}
	}
	
	public String getString(int id) {
		try {
			if(strings[id] == null) {
				strings[id] = new String(stringsb[id], "UTF-8"); //TODO: benchmark if keeping them is necessary (single return mostly)
				stringsb[id] = null; //force GC
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
	
	public NXNode resolvePath(String path) {
		NXNode currentNode = getRoot();
		String[] splitted = path.split("/");
		int offset = path.startsWith("/") ? 1 : 0;
		while(offset < splitted.length) {
			currentNode = currentNode.getChild(splitted[offset++]);
			if(currentNode == null)
				throw new NXException("Invalid path");
		}
		return currentNode;
	}
	
	public SeekableLittleEndianAccessor getNodeStream(int id) {
		if(closed)
			throw new NXException("File already closed.");
		
		SeekableLittleEndianAccessor stream = low_memory ? slea : node_reader;
		stream.seek((low_memory ? header.getNodeOffset() : 0) + id * NXNode.SIZE);
		return stream;
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
			//TODO: release memory etc for node reader also
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
