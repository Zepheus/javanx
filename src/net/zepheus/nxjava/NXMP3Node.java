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

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class NXMP3Node extends NXNode {

	private int mp3Id;
	
	public NXMP3Node(String name, NXNode parent, NXFile file, int childCount, int mp3Id) {
		super(name, parent, file, childCount);
		this.mp3Id = mp3Id;
	}
	
	@Override
	public Object getValue() {
		return getMP3();
	}
	
	public void play() {
		InputStream stream = new ByteArrayInputStream(getMP3());
		BinarySoundPlayer.play(stream);
	}
	
	public byte[] getMP3() {
		return file.getMP3(mp3Id);
	}
}
