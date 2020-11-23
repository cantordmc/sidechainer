import java.io.*;

public class Main
{
	public static void main(String[] args)
	{
		final int BUFSIZ = 300;
		try
		{
			WavFile triggerWav = WavFile.openWavFile(new File("sources/"+args[0]+".wav"));
			WavFile automatedWav = WavFile.openWavFile(new File("sources/"+args[1]+".wav"));
			int sampleRate = (int)triggerWav.getSampleRate();		// Samples per second
			if (automatedWav.getSampleRate() != sampleRate) {
				System.out.println("ERROR: Sample rates do not match");
				return;
			}

			// Calculate the number of frames required for specified duration
			long numFramesTr = triggerWav.getNumFrames();
			long numFramesAu = automatedWav.getNumFrames();

			long numFrames = (numFramesTr > numFramesAu) ? numFramesTr : numFramesAu;

			WavFile out = WavFile.newWavFile(new File("output/"+args[2]+".wav"), 2, numFrames, 16, sampleRate);
			// Create a buffer of 100 frames
			double[][] triggerBuffer = new double[2][BUFSIZ];
			double[][] automatedBuffer = new double[2][BUFSIZ];
			double[][] outBuffer = new double[2][BUFSIZ];

			// Initialise a local frame counter
			long frameCounter = 0;

			// Loop until all frames written
			while (frameCounter < numFrames)
			{
				// Determine how many frames to write, up to a maximum of the buffer size
				long remaining = out.getFramesRemaining();
				int toWrite = (remaining > BUFSIZ) ? BUFSIZ : (int) remaining;

				triggerWav.readFrames(triggerBuffer, BUFSIZ);
				automatedWav.readFrames(automatedBuffer, BUFSIZ);

				double max = 0;
				for (int s=0; s<BUFSIZ; s++) {
					if (Math.abs(triggerBuffer[0][s]) > max) {
						max = Math.abs(triggerBuffer[0][s]);
					}
					if (Math.abs(triggerBuffer[1][s]) > max) {
						max = Math.abs(triggerBuffer[1][s]);
					}
				}

				// Fill the buffer, one tone per channel
				for (int s=0 ; s<toWrite ; s++, frameCounter++)
				{
					outBuffer[0][s] = triggerBuffer[0][s]+(1-max)*automatedBuffer[0][s];
					outBuffer[1][s] = triggerBuffer[1][s]+(1-max)*automatedBuffer[1][s];
				}

				// Write the buffer
				out.writeFrames(outBuffer, toWrite);
			}

			// Close the wavFile
			triggerWav.close();
			automatedWav.close();
			out.close();
		}
		catch (Exception e)
		{
			System.err.println(e);
		}
	}
}
