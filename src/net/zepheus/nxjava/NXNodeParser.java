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

public class NXNodeParser {
	
	public static NXNode parse(SeekableLittleEndianAccessor slea, NXNode parent, NXFile file) {
		int stringId = slea.getInt();
		String name = file.getString(stringId);
		int childCount = slea.getUShort();
		int type = slea.getUShort(); //TODO: byte!
		//TODO: childcount should come after type, not after data!
		//We'll use a temporary setter as a hack for now
		
		NXNode ret = null;
		switch(type) {
		case 0:
			slea.skip(8);
			ret = new NXEmptyNode(name, parent, file, childCount);
			break;
			
		case 1:
			long ivalue = slea.getUInt();
			slea.skip(4);
			ret = new NXIntegerNode(name, parent, file, childCount, ivalue);
			break;
		case 2:
			double dvalue = slea.getDouble();
			ret = new NXDoubleNode(name, parent, file, childCount, dvalue);
			break;
		case 3:
			int spointer = slea.getInt();
			slea.skip(4);
			ret = new NXStringNode(name, parent, file, childCount, spointer);
			break;
		case 4:
			int xval = slea.getInt();
			int yval = slea.getInt();
			ret = new NXVectorNode(name, parent, file, childCount, xval, yval);
			break;
		case 5:
			int bitmapId = slea.getInt();
			slea.skip(4);
			ret = new NXCanvasNode(name, parent, file, childCount, bitmapId);
			break;
		
		case 6:
			int mp3Id = slea.getInt();
			slea.skip(4);
			ret = new NXMP3Node(name, parent, file, childCount, mp3Id);
			break;
		
		default:
			slea.skip(8);
			break;
		}
		
		int firstChildId = slea.getInt();
		ret.setFirstChildId(firstChildId);
		return ret;
	}
}
