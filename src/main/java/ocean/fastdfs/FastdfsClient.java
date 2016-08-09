package ocean.fastdfs;

import ocean.fastdfs.data.Attachment;

import java.io.File;
import java.util.Map;


public interface FastdfsClient {
	
	public String upload(File file);
	
	/**
	 * 上传文件 自定义文件名
	 * @param file 文件
	 * @param fileName 自定义文件名作为key
	 * @return
	 */
	public String upload(File file,String fileName);
	
	/**
	 * 获取fastDFS中存储的路径 如group/M00/00/00/abc.jpg
	 * @param fileId
	 */
	public String getUrl(String fileId);
	
	public Boolean setMeta(String fileId,Map<String,String> meta);
	public Map<String,String> getMeta(String fileId);
	public Boolean delete(String fileId);
	public void close();


    /**
     * 上传一个文件
     * @param file 要上传的文件
     * @param fileName 文件名
     * @param meta meta key/value的meta data，可为null
     * @return fileid 文件ID
     */
    public String upload(File file,String fileName,Map<String,String> meta);

    public  byte[] downLoad(String fileid);
    /**
     * upload slave
     * @param file
     * @param fileid 主文件ID 如 abc.jpg
     * @param prefix slave的扩展名，如200x200
     * @param ext 文件扩展名，like jpg，不带.
     * @return 上传后的fileid 如abc_200x200.jpg
     */
    public String uploadSlave(File file,String fileid, String prefix, String ext);

	/**
     * 针对图片进行压缩存储。 图片压缩之后，只返回原始图片的文件名。压缩文件的命名规则为：原始文件名_图片长_图片宽.文件格式。 例如： 原始图片为：685268154041344.jpg 100*100像素格式的压缩文件为：
     * 685268154041344_100_100.jpg
     * 压缩后的文件名，在请求时不返回给前端，前端可以根据上传时请求的压缩格式，拼成目标文件名；
     * @param mf 图片文件
     * @param size_x，size_y 需要压缩的尺寸；像素
     * @return 原尺寸文件名
     */
    public String uploadImgWithCompress(Attachment mf, int[] size_x, int[] size_y);

	public String uploadImg(Attachment mf);
}
