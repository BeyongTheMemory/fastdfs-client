package ocean.fastdfs.client;

import java.io.IOException;
import java.util.List;

import ocean.fastdfs.data.GroupInfo;
import ocean.fastdfs.data.Result;
import ocean.fastdfs.data.StorageInfo;
import ocean.fastdfs.data.UploadStorage;

public interface TrackerClient {

	public Result<UploadStorage> getUploadStorage() throws IOException;
	public Result<String> getUpdateStorageAddr(String group,String fileName) throws IOException;
	public Result<String> getDownloadStorageAddr(String group,String fileName) throws IOException;
	public Result<List<GroupInfo>> getGroupInfos() throws IOException;
	public Result<List<StorageInfo>> getStorageInfos(String group) throws IOException;
	public void close() throws IOException;
	
}
