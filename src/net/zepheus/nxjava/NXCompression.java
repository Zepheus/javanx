package net.zepheus.nxjava;

import java.nio.ByteBuffer;
import com.github.decster.jnicompressions.Lz4Compression;

public class NXCompression {
	private static final Lz4Compression COMPRESSOR = new Lz4Compression();

	public static void decompress(ByteBuffer input, long inputOffset, long length, ByteBuffer output, int outputOffset) {
		COMPRESSOR.DecompressDirect(input, (int) inputOffset, (int) length, output, outputOffset);
	}
}
