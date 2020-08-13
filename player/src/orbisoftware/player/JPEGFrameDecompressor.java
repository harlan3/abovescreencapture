
package orbisoftware.player;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.IOException;
import java.io.InputStream;

import org.libjpegturbo.turbojpeg.TJ;
import org.libjpegturbo.turbojpeg.TJDecompressor;
import org.libjpegturbo.turbojpeg.TJException;

public class JPEGFrameDecompressor {

	private int imageWidth;
	private int imageHeight;
	private int flags;
	private TJDecompressor decompressor;
	private boolean playbackComplete;
	private BufferedImage fullImage;

	public class FramePacket {

		private InputStream iStream;
		private int result;
		private long frameTimeStamp;
		private int[] newData;

		private FramePacket(InputStream iStream, int expectedSize) {
			this.iStream = iStream;
			playbackComplete = false;
		}

		public int[] getData() {
			return newData;
		}

		public long getTimeStamp() {
			return frameTimeStamp;
		}
	}

	public FramePacket frame;

	public JPEGFrameDecompressor(InputStream iStream, int width, int height) {

		int frameSize = width * height;
		imageWidth = width;
		imageHeight = height;
		fullImage = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB);
		frame = new FramePacket(iStream, frameSize);
		try {
			decompressor = new TJDecompressor();
			flags = TJ.FLAG_FASTDCT;
		} catch (TJException e) {
		}
	}

	public FramePacket unpack() throws IOException {

		if (playbackComplete)
			return null;

		try {

			int i = frame.iStream.read();
			int time = i;
			time = time << 8;
			i = frame.iStream.read();
			time += i;
			time = time << 8;
			i = frame.iStream.read();
			time += i;
			time = time << 8;
			i = frame.iStream.read();
			time += i;

			frame.frameTimeStamp = (long) time;

			i = frame.iStream.read();
			int zSize = i;
			zSize = zSize << 8;
			i = frame.iStream.read();
			zSize += i;
			zSize = zSize << 8;
			i = frame.iStream.read();
			zSize += i;
			zSize = zSize << 8;
			i = frame.iStream.read();
			zSize += i;

			byte[] zData = new byte[zSize];
			frame.iStream.read(zData, 0, zSize);

			decompressor.setSourceImage(zData, zSize);
			decompressor.decompress(fullImage, flags);

			frame.newData = ((DataBufferInt) fullImage.getRaster().getDataBuffer()).getData();

		} catch (Exception e) {
			frame.iStream.close();
			playbackComplete = true;
			return null;
		}

		return frame;
	}
}
