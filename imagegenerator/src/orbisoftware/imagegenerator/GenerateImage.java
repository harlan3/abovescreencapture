
package orbisoftware.imagegenerator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class GenerateImage implements Runnable {

	private JPEGFrameStreamReader decompressor;

	private FileInputStream iStream;
	private String videoFile;
	private String destDir;
	private int width;
	private int height;
	private boolean finished;
	private long nextFrame;

	public GenerateImage(String videoFile, String destDir) {

		this.videoFile = videoFile;
		this.destDir = destDir;

		initialize();
	}

	private void initialize() {

		finished = false;
		nextFrame = 0;

		try {

			iStream = new FileInputStream(videoFile);

			width = iStream.read();
			width = width << 8;
			width += iStream.read();

			height = iStream.read();
			height = height << 8;
			height += iStream.read();

			decompressor = new JPEGFrameStreamReader(iStream, width, height);
		} catch (Exception e) {
			e.printStackTrace();
		}

		File dir = new File(destDir);

		if (!dir.mkdir()) {
			System.out.println("Could not make dest dir: " + destDir);
			System.exit(0);
		}

		new Thread(this, "Generate Image").start();
	}

	public synchronized void run() {

		while (!finished) {
			try {
				generateFrame();
			} catch (IOException e) {
				e.printStackTrace();
			}

			if (finished)
				System.out.println("Images generated in: " + destDir);
		}
	}

	private void generateFrame() throws IOException {

		JPEGFrameStreamReader.FramePacket frame = decompressor.unpack();

		if (frame == null) {
			finished = true;
			return;
		}

		nextFrame++;

		File file = new File(destDir + File.separator + "IMG_" + nextFrame + ".jpg");
		FileOutputStream fos = null;

		try {
			fos = new FileOutputStream(file);
			System.out.println("Generating image: " + "IMG_" + nextFrame + ".jpg");
			fos.write(frame.getData());
		} catch (Exception e) {
			System.out.println("Error writing: " + "IMG_" + nextFrame + ".jpg");
		} finally {
			if (fos != null)
				fos.close();
		}
	}
}
