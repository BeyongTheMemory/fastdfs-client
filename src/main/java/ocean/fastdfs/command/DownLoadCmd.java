package ocean.fastdfs.command;

import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;

import ocean.fastdfs.data.Result;

public class DownLoadCmd extends AbstractCmd<byte[]>{

	public DownLoadCmd(String group,String fileName) {
		super();
		this.requestCmd = STORAGE_PROTO_CMD_DOWNLOAD_FILE;
		this.responseCmd = STORAGE_PROTO_CMD_RESP;
		
		byte[] bsOffset;
		byte[] bsDownBytes;
		byte[] groupBytes;
		byte[] filenameBytes;
		byte[] bs;
		int groupLen;
		final long file_offset = 0;
		final long download_bytes = 0;
		bsOffset = long2buff(file_offset);
		bsDownBytes = long2buff(download_bytes);
		groupBytes = new byte[FDFS_GROUP_NAME_MAX_LEN];
		bs = group.getBytes(charset);
		filenameBytes = fileName.getBytes(charset);

		Arrays.fill(groupBytes, (byte) 0);
		if (bs.length <= groupBytes.length) {
			groupLen = bs.length;
		} else {
			groupLen = groupBytes.length;
		}
		System.arraycopy(bs, 0, groupBytes, 0, groupLen);
		
		body1 = new byte[bsOffset.length
							+ bsDownBytes.length + groupBytes.length
							+ filenameBytes.length];
		Arrays.fill(body1, (byte) 0);
		System.arraycopy(bsOffset, 0, body1, 0, bsOffset.length);
		System.arraycopy(bsDownBytes, 0, body1, bsOffset.length, bsDownBytes.length);
		System.arraycopy(groupBytes, 0, body1, bsOffset.length + bsDownBytes.length, groupBytes.length);
		System.arraycopy(filenameBytes, 0, body1, bsOffset.length + bsDownBytes.length + groupBytes.length,
				filenameBytes.length);
		
		this.responseSize = -1;

	}
	
	@Override
	public Result<byte[]> exec(Socket socket) throws IOException {
		request(socket.getOutputStream());
		Response response = response(socket.getInputStream());
		if(response.isSuccess()){
			byte[] data = response.getData();
			return new Result<byte[]>(response.getCode(),data);
		}else{
			return new Result<byte[]>(response.getCode(),"Delete Error");
		}
	}

}
