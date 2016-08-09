package ocean.fastdfs.exception;

import org.springframework.core.NestedRuntimeException;

public class FastdfsIOException extends NestedRuntimeException {

	private static final long serialVersionUID = 4234899139606659965L;

    private Integer code;

    private String content;

    public FastdfsIOException(String content) {
        super(content);

        this.content = content;
    }

    public FastdfsIOException(String content, Throwable throwable) {
        super(content, throwable);

        this.content = content;
    }

    /**
     * 
     * @param code 异常编码，在i18n配置文件中配置的编码，请确保该异常编码已经定义
     * @param content 后台异常内容，这个内容主要用于输出后台日志，便于异常诊断
     */
    public FastdfsIOException(Integer code, String content) {
        super(content);
        this.code = code;
        this.content = content;
    }


    /**
     * 
     * @param code 异常编码，在i18n配置文件中配置的编码，请确保该异常编码已经定义
     * @param content 后台异常内容，这个内容主要用于输出后台日志，便于异常诊断
     * @param throwable
     */
    public FastdfsIOException(Integer code, String content, Throwable throwable) {
        super(content, throwable);
        this.code = code;
        this.content = content;
    }


    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }


}
