package net.zepheus.nxjava;

import javax.sound.sampled.*;
import java.io.*;

public class BinarySoundPlayer {

	private static final int BUFFER_SIZE = 4096;
	
	private static SourceDataLine line;
	private static AudioInputStream din;
	private static boolean disposed = false;

	public static void stop() throws IOException {
		if(line != null && !disposed) {
			line.drain();
			line.stop();
			line.close();
			din.close();
			disposed = true;
		}
	}

	public static void play(InputStream stream)
	{
		try {
			stop();

			AudioInputStream in= AudioSystem.getAudioInputStream(stream);
			disposed = false;
			AudioInputStream din = null;
			AudioFormat baseFormat = in.getFormat();
			AudioFormat decodedFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 
					baseFormat.getSampleRate(),
					16,
					baseFormat.getChannels(),
					baseFormat.getChannels() * 2,
					baseFormat.getSampleRate(),
					false);
			din = AudioSystem.getAudioInputStream(decodedFormat, in);

			rawplay(decodedFormat, din);
			in.close();
		} catch (Exception e)
		{
			System.out.println(e.getMessage());
		}
	} 

	private static void rawplay(AudioFormat targetFormat, AudioInputStream din) throws IOException,                                                                                                LineUnavailableException
	{
		byte[] data = new byte[BUFFER_SIZE];
		line = getLine(targetFormat); 
		if (line != null)
		{
			// Start
			line.start();
			int nBytesRead = 0;
			while (nBytesRead != -1)
			{
				nBytesRead = din.read(data, 0, data.length);
				if (nBytesRead != -1) 
					line.write(data, 0, nBytesRead);
			}
			// Stop
			line.drain();
			line.stop();
			line.close();
			din.close();
		} 
	}

	private static SourceDataLine getLine(AudioFormat audioFormat) throws LineUnavailableException
	{
		SourceDataLine res = null;
		DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
		res = (SourceDataLine) AudioSystem.getLine(info);
		res.open(audioFormat);
		return res;
	} 
}

