package ocean.fastdfs.image;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Vector;

import javax.imageio.ImageIO;

import ocean.fastdfs.exception.FastdfsIOException;
import org.apache.commons.lang.StringUtils;

public abstract class ImageTools {

	private static Collection<String> mediaTypes;

	// 水印透明度
	private static final Float ALPHA = 0.5f;

	static {
		mediaTypes = new Vector<String>();
		mediaTypes.add("image/gif");
		mediaTypes.add("image/jpeg");
		mediaTypes.add("image/png");
	}

	private static ImageTransform transform = new ImgscalrImageTransform();

	public static Image resize(Image image, int width, int height)
			throws FastdfsIOException {
		return transform.resize(image, width, height);
	}

	/**
	 * MediaType 转换成Image的contentType
	 * 
	 * @param contentType
	 *            前台传入的content-type
	 * @return 转换后的contentType;
	 */
	public static String getImageContentType(String contentType) {
		if (mediaTypes.contains(contentType)) {
			String[] strs = StringUtils.split(contentType, "\\/");
			return strs[1];
		}
		return null;
	}

	/**
	 * 添加图片水印
	 * 
	 * @param targetImg
	 *            目标图片路径，如：C://myPictrue//1.jpg
	 * @param waterImg
	 *            水印图片路径，如：C://myPictrue//logo.png
	 * @param x
	 *            水印图片距离目标图片左侧的偏移量，如果x<0, 则在正中间，原点在左上角，X 坐标轴向右方延伸
	 * @param y
	 *            水印图片距离目标图片上侧的偏移量，如果y<0, 则在正中间，原点在左上角，Y 坐标轴向下方延伸
	 * @param alpha
	 *            透明度(0.0 -- 1.0, 0.0为完全透明，1.0为完全不透明)
	 * @param degree 旋转角度
	 */
	public final static void pressImage(String targetImg, String waterImg,
			int x, int y, Float alpha,Integer degree) throws IOException {
			File targetImgFile = new File(targetImg);
			File waterImgFile = new File(waterImg);
			pressImage(targetImgFile, waterImgFile, x, y, alpha,degree);
			
	}
	
	/**
	 * @param targetImgFile 目标图片
	 * @param waterImgFile 水印图片
	 * @param x 水印图片距离目标图片左侧的偏移量，如果x<0, 则在正中间，原点在左上角，X 坐标轴向右方延伸
	 * @param y 水印图片距离目标图片上侧的偏移量，如果y<0, 则在正中间，原点在左上角，Y 坐标轴向下方延伸
	 * @param alpha  透明度(0.0 -- 1.0, 0.0为完全透明，1.0为完全不透明)
	 * @param degree 旋转角度
	 * @throws java.io.IOException
	 */
	public final static void pressImage(File targetImgFile, File waterImgFile,
			int x, int y, Float alpha,Integer degree) throws IOException {
		if(targetImgFile == null) {
			throw new IOException("要添加水印的目标图片为空");
		}
		if(waterImgFile == null) {
			throw new IOException("水印图片为空");
		}
		java.awt.Image image = ImageIO.read(targetImgFile);
		int width = image.getWidth(null);
		int height = image.getHeight(null);
		BufferedImage bufferedImage = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_RGB);
		Graphics2D g = bufferedImage.createGraphics();
		g.drawImage(image, 0, 0, width, height, null);

		java.awt.Image waterImage = ImageIO.read(waterImgFile); // 水印文件
		int width_1 = waterImage.getWidth(null);
		int height_1 = waterImage.getHeight(null);
		if (alpha == null) {
			alpha = ALPHA;
		}
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP,
				alpha));

		int widthDiff = width - width_1;
		int heightDiff = height - height_1;
		if (x < 0) {
			x = widthDiff / 2;
		} else if (x > widthDiff) {
			x = widthDiff;
		}
		if (y < 0) {
			y = heightDiff / 2;
		} else if (y > heightDiff) {
			y = heightDiff;
		}
		g.drawImage(waterImage, x, y, width_1, height_1, null); // 水印文件结束
		g.dispose();
		ImageIO.write(bufferedImage, getFileExtName(targetImgFile), targetImgFile);
	}

	/**
	 * 添加文字水印
	 * 
	 * @param targetImg
	 *            目标图片路径，如：C://myPictrue//1.jpg
	 * @param pressText
	 *            水印文字
	 * @param fontName
	 *            字体名称， 如：宋体,不填默认为宋体
	 * @param fontStyle
	 *            字体样式，如：粗体和斜体(Font.BOLD|Font.ITALIC)
	 * @param fontSize
	 *            字体大小，单位为像素
	 * @param color
	 *            字体颜色
	 * @param x
	 *            水印文字距离目标图片左侧的偏移量，如果x<0, 则在正中间，原点在左上角，X 坐标轴向右方延伸
	 * @param y
	 *            水印文字距离目标图片上侧的偏移量，如果y<0, 则在正中间，原点在左上角，Y 坐标轴向下方延伸
	 * @param alpha
	 *            透明度(0.0 -- 1.0, 0.0为完全透明，1.0为完全不透明)
	 * @param degree
	 *            旋转角度
	 */
	public static void pressText(String targetImg, String pressText,
			String fontName, int fontStyle, int fontSize, Color color, int x,
			int y, Float alpha, Integer degree) throws IOException {
			File file = new File(targetImg);
			pressText(file, pressText, fontName, fontStyle, fontSize, color, x, y, alpha, degree);
	}
	
	/**
	 * 添加文字水印
	 * @param file 目标图片
	 * @param pressText 水印文字
	 * @param fontName 字体名称， 如：宋体,不填默认为宋体
	 * @param fontStyle 字体样式，如：粗体和斜体(Font.BOLD|Font.ITALIC)
	 * @param fontSize  字体大小，单位为像素
	 * @param color 字体颜色
	 * @param x 水印文字距离目标图片左侧的偏移量，如果x<0, 则在正中间，原点在左上角，X 坐标轴向右方延伸
	 * @param y 水印文字距离目标图片上侧的偏移量，如果y<0, 则在正中间，原点在左上角，Y 坐标轴向下方延伸
	 * @param alpha  透明度(0.0 -- 1.0, 0.0为完全透明，1.0为完全不透明)
	 * @param degree 旋转角度
	 * @throws java.io.IOException
	 */
	public static void pressText(File file, String pressText,
			String fontName, int fontStyle, int fontSize, Color color, int x,
			int y, Float alpha, Integer degree) throws IOException {
		if(file == null) {
			throw new IOException("要添加水印的目标图片为空");
		}
		java.awt.Image image = ImageIO.read(file);
		int width = image.getWidth(null);
		int height = image.getHeight(null);
		BufferedImage bufferedImage = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_RGB);
		Graphics2D g = bufferedImage.createGraphics();
		g.drawImage(image, 0, 0, width, height, null);
		g.setFont(new Font(fontName, fontStyle, fontSize));
		g.setColor(color);
		if (alpha == null) {
			alpha = ALPHA;
		}
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP,
				alpha));
		if (null != degree) {
			// 设置水印旋转
			g.rotate(Math.toRadians(degree), (double) width / 2,
					(double) height / 2);
		}

		int width_1 = fontSize * getLength(pressText);
		int height_1 = fontSize;
		int widthDiff = width - width_1;
		int heightDiff = height - height_1;
		if (x < 0) {
			x = widthDiff / 2;
		} else if (x > widthDiff) {
			x = widthDiff;
		}
		if (y < 0) {
			y = heightDiff / 2;
		} else if (y > heightDiff) {
			y = heightDiff;
		}

		g.drawString(pressText, x, y + height_1);
		g.dispose();
		ImageIO.write(bufferedImage, getFileExtName(file), file);
	}

	/**
	 * 获取字符长度，一个汉字作为 1 个字符, 一个英文字母作为 0.5 个字符
	 * 
	 * @param text
	 * @return 字符长度，如：text="中国",返回 2；text="test",返回 2；text="中国ABC",返回 4.
	 */
	public static int getLength(String text) {
		int textLength = text.length();
		int length = textLength;
		for (int i = 0; i < textLength; i++) {
			if (String.valueOf(text.charAt(i)).getBytes().length > 1) {
				length++;
			}
		}
		return (length % 2 == 0) ? length / 2 : length / 2 + 1;
	}
	
    private static String getFileExtName(File file) {
        String name = file.getName();
        if (name!=null ) {
            int i = name.lastIndexOf('.');
            if (i>-1) {
                return name.substring(i+1);
            }else {
                return null;
            }
        }else {
            return null;
        }
    }
    
}
