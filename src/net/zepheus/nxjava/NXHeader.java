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

public class NXHeader {
	private String format;

	// Warning: there's no support for uint
	// There is no point implementing it as java will not be able to allocate such arrays anyway
	private int nodeCount;
	private long nodeOffset;
	
	private int stringCount;
	private long stringOffset;
	
	private int bmpCount;
	private long bmpOffset;
	
	private int mp3Count;
	private long mp3Offset;
	
	public NXHeader(SeekableLittleEndianAccessor slea){
		slea.seek(0);
		format = slea.getUTFString(4);
		if(!NXFile.PKG_FORMAT.equals(format)) {
			throw new NXException("Invalid NX file header.");
		}
		
		nodeCount = (int)slea.getUInt();
		nodeOffset=  slea.getLong();
		stringCount = (int)slea.getUInt();
		stringOffset = slea.getLong();
		bmpCount = (int)slea.getUInt();
		bmpOffset = slea.getLong();
		mp3Count = (int)slea.getUInt();
		mp3Offset = slea.getLong();
	}

	public String getFormat() {
		return format;
	}

	public int getNodeCount() {
		return nodeCount;
	}

	public long getNodeOffset() {
		return nodeOffset;
	}
	
	public int getStringCount() {
		return stringCount;
	}

	public long getStringOffset() {
		return stringOffset;
	}

	public int getBmpCount() {
		return bmpCount;
	}

	public long getBmpOffset() {
		return bmpOffset;
	}

	public int getMp3Count() {
		return mp3Count;
	}

	public long getMp3Offset() {
		return mp3Offset;
	}
}
