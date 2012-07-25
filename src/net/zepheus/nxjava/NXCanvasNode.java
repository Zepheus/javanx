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

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class NXCanvasNode extends NXNode {

	private int bitmapId;
	private BufferedImage image;
	
	public NXCanvasNode(String name, NXNode parent, NXFile file, int childCount, int bitmapId) {
		super(name, parent, file, childCount);
		this.bitmapId = bitmapId;
	}
	
	@Override
	public Object getValue() {
		return getImage();
	}
	
	public BufferedImage getImage() {
		if(image != null)
			return image;
		else {
			long offset = file.getBitmapOffset(bitmapId);
			if(offset == -1)
				throw new NXException("NX file does not this canvas.");
			
			file.lock();
			try {
				SeekableLittleEndianAccessor slea = file.getStreamAtOffset(offset);
				
				int width = slea.getUShort();
				int height = slea.getUShort();
				long length = slea.getUInt();
				
				ByteBuffer output = ByteBuffer.allocateDirect(width * height * 4);
				NXCompression.decompress(slea.getBuffer(), offset + 4, length + 4, output, 0);
				output.rewind();
				output.order(ByteOrder.LITTLE_ENDIAN);
				
				//TODO: optimize this without bitshifts.
				image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
				for (int h = 0; h < height; h++) {
					for (int w = 0; w < width; w++) {
						int b = output.get() & 0xFF;
						int g = output.get() & 0xFF;
						int r = output.get() & 0xFF;
						int a = output.get() & 0xFF;
						image.setRGB(w, h, (a << 24) | (r << 16) | (g << 8) | b);
					}
				}
			} finally { file.unlock(); }
			return image;
		}
	}
}
