
package orbisoftware.recorder;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import org.libjpegturbo.turbojpeg.TJ;
import org.libjpegturbo.turbojpeg.TJCompressor;
import org.libjpegturbo.turbojpeg.TJException;

import java.io.IOException;
import java.io.OutputStream;

public class JPEGFrameCompressor {

	private FramePacket frame;
	private TJCompressor tjc;

	private int imageWidth;
	private int imageHeight;

	private ByteBuffer byteBuffer;
	private IntBuffer intBuffer;
	
	private String myOS = System.getProperty("os.name").toLowerCase();

	public class FramePacket {

		private OutputStream oStream;
		private long frameTime;

		private FramePacket(OutputStream oStream) {
			this.oStream = oStream;
		}
	}

	public JPEGFrameCompressor(OutputStream oStream, int width, int height) {

		imageWidth = width;
		imageHeight = height;
		frame = new FramePacket(oStream);
		byteBuffer = ByteBuffer.allocate(width * height * 4);
		intBuffer = byteBuffer.asIntBuffer();

		try {
			tjc = new TJCompressor();
			tjc.setSubsamp(TJ.SAMP_420);
			tjc.setJPEGQuality(75);
		} catch (TJException e) {
			e.printStackTrace();
		}
	}
	
	public boolean isWindows() {

		return (myOS.indexOf("win") >= 0);
	}

	public void pack(int[] newData, long frameTimeStamp, boolean reset) throws IOException {

		frame.frameTime = frameTimeStamp;

		frame.oStream.write(((int) frame.frameTime & 0xFF000000) >>> 24);
		frame.oStream.write(((int) frame.frameTime & 0x00FF0000) >>> 16);
		frame.oStream.write(((int) frame.frameTime & 0x0000FF00) >>> 8);
		frame.oStream.write(((int) frame.frameTime & 0x000000FF));
		frame.oStream.flush();

		byteBuffer.rewind();
		intBuffer.rewind();
		intBuffer.put(newData);

		if (isWindows())
			tjc.setSourceImage(byteBuffer.array(), 0, 0, imageWidth, 0, imageHeight, TJ.PF_XBGR);
		else
			tjc.setSourceImage(byteBuffer.array(), 0, 0, imageWidth, 0, imageHeight, TJ.PF_XRGB);
		
		byte[] jpegBuf = tjc.compress(TJ.FLAG_FASTDCT);
		int jpegSize = tjc.getCompressedSize();
		tjc.close();

		frame.oStream.write((jpegSize & 0xFF000000) >>> 24);
		frame.oStream.write((jpegSize & 0x00FF0000) >>> 16);
		frame.oStream.write((jpegSize & 0x0000FF00) >>> 8);
		frame.oStream.write((jpegSize & 0x000000FF));
		frame.oStream.write(jpegBuf, 0, jpegSize);
		frame.oStream.flush();
	}
}
