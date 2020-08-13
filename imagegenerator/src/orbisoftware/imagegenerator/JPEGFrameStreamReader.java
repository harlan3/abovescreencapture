
package orbisoftware.imagegenerator;

import java.io.IOException;
import java.io.InputStream;

public class JPEGFrameStreamReader {

	private boolean playbackComplete;

	public class FramePacket {

		private InputStream iStream;
		private long frameTimeStamp;
		private byte[] newData;

		private FramePacket(InputStream iStream, int expectedSize) {
			this.iStream = iStream;
			playbackComplete = false;
		}

		public byte[] getData() {
			return newData;
		}

		public long getTimeStamp() {
			return frameTimeStamp;
		}
	}

	public FramePacket frame;

	public JPEGFrameStreamReader(InputStream iStream, int width, int height) {

		int frameSize = width * height;
		frame = new FramePacket(iStream, frameSize);
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

			frame.newData = zData;

		} catch (Exception e) {
			frame.iStream.close();
			playbackComplete = true;
			return null;
		}

		return frame;
	}
}
