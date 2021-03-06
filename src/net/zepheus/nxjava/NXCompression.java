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
import com.github.decster.jnicompressions.Lz4Compression;

public class NXCompression {
	private static final Lz4Compression COMPRESSOR = new Lz4Compression();

	public static void decompress(ByteBuffer input, long inputOffset, long length, ByteBuffer output, int outputOffset) {
		COMPRESSOR.DecompressDirect(input, (int) inputOffset, (int) length, output, outputOffset);
	}
}
