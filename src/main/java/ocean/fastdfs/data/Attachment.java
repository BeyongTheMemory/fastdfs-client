package ocean.fastdfs.data;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

import org.springframework.util.FileCopyUtils;

/**
 * Rest接收到的文件需要进行转换才能保证分布式环境下的执行。
 */
public class Attachment implements Serializable {

    private static final long serialVersionUID = 1L;

    private String name;

    private String originalFilename;

    private String contentType;

    private byte[] content;

    public Attachment(){}
    
    public String getName() {
        return name;
    }
    
    public String getOriginalFilename() {
        return originalFilename;
    }

    public String getContentType() {
        return contentType;
    }

    public boolean isEmpty() {
        return (this.content.length == 0);
    }

    public long getSize() {
        return this.content.length;
    }

    public byte[] getBytes() throws IOException {
        return this.content;
    }

    public InputStream getInputStream() throws IOException {
        return new ByteArrayInputStream(this.content);
    }

    public void transferTo(File dest) throws IOException, IllegalStateException {
        FileCopyUtils.copy(this.content, dest);
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setOriginalFilename(String originalFilename) {
        this.originalFilename = originalFilename;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
}
