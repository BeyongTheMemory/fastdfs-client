package ocean.fastdfs.image;


import ocean.fastdfs.exception.FastdfsIOException;

public interface ImageTransform {

	Image resize(Image image, int width, int height) throws FastdfsIOException;
}
 