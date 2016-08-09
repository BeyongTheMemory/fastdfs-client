package ocean.fastdfs.image;

import java.io.InputStream;

public class Image {

	private InputStream inputStream;
	private String formatName;

	public Image(InputStream inputStream, String formatName) {
		this.inputStream = inputStream;
		this.formatName = formatName;
	}

	public InputStream getInputStream() {
		return inputStream;
	}

	public void setInputStream(InputStream inputStream) {
		this.inputStream = inputStream;
	}

	public String getFormatName() {
		return formatName;
	}

	public void setFormatName(String formatName) {
		this.formatName = formatName;
	}

}
