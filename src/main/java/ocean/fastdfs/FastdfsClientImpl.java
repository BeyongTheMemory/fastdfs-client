package ocean.fastdfs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import ocean.fastdfs.client.AbstractClient;
import ocean.fastdfs.client.StorageClient;
import ocean.fastdfs.client.StorageClientFactory;
import ocean.fastdfs.client.TrackerClient;
import ocean.fastdfs.client.TrackerClientFactory;
import ocean.fastdfs.data.*;
import ocean.fastdfs.exception.FastdfsIOException;

import ocean.fastdfs.image.Image;
import ocean.fastdfs.image.ImageTools;
import org.apache.commons.pool2.impl.GenericKeyedObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FastdfsClientImpl extends AbstractClient implements FastdfsClient{
	
	private static Logger logger = LoggerFactory.getLogger(FastdfsClientImpl.class);
	private GenericKeyedObjectPool<String, TrackerClient> trackerClientPool;
	private GenericKeyedObjectPool<String, StorageClient> storageClientPool;
	private List<String> trackerAddrs = new ArrayList<String>();
	private Map<String,String> storageIpMap = new ConcurrentHashMap<String, String>();
	
	public FastdfsClientImpl(List<String> trackerAddrs){
		super();
		this.trackerAddrs = trackerAddrs;
		trackerClientPool = new GenericKeyedObjectPool<String, TrackerClient>(new TrackerClientFactory());
		storageClientPool = new GenericKeyedObjectPool<String, StorageClient>(new StorageClientFactory());
		updateStorageIpMap();
	}
	
	public FastdfsClientImpl(List<String> trackerAddrs,
			GenericKeyedObjectPool<String, TrackerClient> trackerClientPool,
			GenericKeyedObjectPool<String, StorageClient> storageClientPool) {
		super();
		this.trackerAddrs = trackerAddrs;
		this.trackerClientPool = trackerClientPool;
		this.storageClientPool = storageClientPool;
	}
	
	

	@Override
	public void close() {
		this.trackerClientPool.close();
		this.storageClientPool.close();
	}

	/**
     * 针对图片进行压缩存储。 图片压缩之后，只返回原始图片的文件名。压缩文件的命名规则为：原始文件名_图片长_图片宽.文件格式。 例如： 原始图片为：685268154041344.jpg 100*100像素格式的压缩文件为：
     * 685268154041344_100_100.jpg
     * 压缩后的文件名，在请求时不返回给前端，前端可以根据上传时请求的压缩格式，拼成目标文件名；
     * @param mf 图片文件
     * @param size_x 需要压缩的尺寸；像素
	 * @param size_y 需要压缩的尺寸；像素
     * @return 原尺寸文件名
     */
	@Override
    public String uploadImgWithCompress(Attachment mf, int[] size_x, int[] size_y) {
        if (size_x.length != size_y.length) {
            //
            logger.error("图片压缩格式不匹配.");
            throw new FastdfsIOException("图片压缩格式不匹配");
        }

        String fdfsFileId = null;
        FileOutputStream os = null;
        
        try {
            File file = new File(mf.getOriginalFilename());
            os = new FileOutputStream(file);
            os.write(mf.getContent());

            // 验证文件是否为图片
            String contentType = ImageTools.getImageContentType(mf.getContentType());
            if (contentType == null) {
                logger.error("上传文件不是正确的图片格式.");
                throw new FastdfsIOException("上传文件不是正确的图片格式");
            }
            // 上传主图片
			fdfsFileId = upload(file);

            File tmpFile = null;
            FileOutputStream tmpOs = null;
            for (int i = 0; i < size_x.length; i++) {
                Image img = new Image(mf.getInputStream(), contentType);
                Image tmpImg = ImageTools.resize(img, size_x[i], size_y[i]);
                tmpFile = new File(mf.getOriginalFilename());
                tmpOs = new FileOutputStream(tmpFile);
                int b = tmpImg.getInputStream().read();
                while (b != -1) {
                    tmpOs.write(b);
                    b = tmpImg.getInputStream().read();
                }
                tmpOs.close();

                // 上传从图片
                this.uploadSlave(tmpFile, fdfsFileId, "_" + size_x[i] + "_" + size_y[i], getFileExtName(tmpFile));
            }
        } catch (Exception e) {
            throw new FastdfsIOException("上传文件异常", e);
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    throw new FastdfsIOException("上传文件异常", e);
                }
            }
        }
        return fdfsFileId;
    }

	public String uploadImg(Attachment mf) {
		String fdfsFileId = null;
		FileOutputStream os = null;
		try {
			File file = new File(mf.getOriginalFilename());
			os = new FileOutputStream(file);
			os.write(mf.getContent());

			// 验证文件是否为图片
			String contentType = ImageTools.getImageContentType(mf.getContentType());
			if (contentType == null) {
				logger.error("上传文件不是正确的图片格式.");
				throw new FastdfsIOException("上传文件不是正确的图片格式");
			}
			// 上传主图片
			fdfsFileId = upload(file);
		} catch (Exception e) {
			throw new FastdfsIOException("上传文件异常", e);
		} finally {
			if (os != null) {
				try {
					os.close();
				} catch (IOException e) {
					throw new FastdfsIOException("上传文件异常", e);
				}
			}
		}
		return fdfsFileId;
	}

	public byte[] downLoad(String fdfsFileId) {
		String trackerAddr = getTrackerAddr();
        TrackerClient trackerClient = null;
        StorageClient storageClient = null;
        String storageAddr = null;
        byte[] data = null;
        try {
            trackerClient = trackerClientPool.borrowObject(trackerAddr);
            
            if(fdfsFileId!=null){
                String[] tupple = splitFileId(fdfsFileId);
                String groupname = tupple[0];
                String filename = tupple[1];
                Result<String> result = trackerClient.getDownloadStorageAddr(groupname,filename);
                if(result.getCode()==0){
                	storageAddr = result.getData();
                	storageClient = storageClientPool.borrowObject(storageAddr);
                	
                	Result<byte[]> result2 = storageClient.donwLoad(groupname, filename);
                	data = result2.getData();
                }
            }
            

        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new FastdfsIOException("下载文件异常", e);
        } finally {
            if(storageClient!=null){
                storageClientPool.returnObject(storageAddr, storageClient);
            }
            if(trackerClient!=null){
                trackerClientPool.returnObject(trackerAddr, trackerClient);
            }
        }
        return data;
	}
	
    @Override
    public String upload(File file, String fileName, Map<String, String> meta) {
        String trackerAddr = getTrackerAddr();
        TrackerClient trackerClient = null;
        StorageClient storageClient = null;
        String storageAddr = null;
        String fdfsFileId = null;
        try {
            trackerClient = trackerClientPool.borrowObject(trackerAddr);
            Result<UploadStorage> result = trackerClient.getUploadStorage();
            if(result.getCode()==0){
                storageAddr = result.getData().getAddress();
                storageClient = storageClientPool.borrowObject(storageAddr);

                String extname =  fileName;
                if (fileName == null) {
                    extname = file.getName();
                }
                Result<String> result2 = storageClient.upload(file, extname, result.getData().getPathIndex());
                if(result2.getCode()==0){
                    fdfsFileId = result2.getData();
                }
                
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new FastdfsIOException("上传文件异常", e);
        } finally {
            if(storageClient!=null){
                storageClientPool.returnObject(storageAddr, storageClient);
            }
            if(trackerClient!=null){
                trackerClientPool.returnObject(trackerAddr, trackerClient);
            }
        }
        return fdfsFileId;
    }

    @Override
    public String uploadSlave(File file, String masterFdfsFileid, String prefix, String ext) {
        String trackerAddr = getTrackerAddr();
        TrackerClient trackerClient = null;
        StorageClient storageClient = null;
        String storageAddr = null;
        String fdfsId = null;
        try {
            trackerClient = trackerClientPool.borrowObject(trackerAddr);

            if(masterFdfsFileid!=null){
                String[] tupple = splitFileId(masterFdfsFileid);
                String groupname = tupple[0];
                String filename = tupple[1];

                Result<String> result = trackerClient.getUpdateStorageAddr(groupname,filename);
                if(result.getCode() == 0) {
                    storageAddr = result.getData();
                    storageClient = storageClientPool.borrowObject(storageAddr);
                    Result<String> result2 = storageClient.uploadSlave(file, filename, prefix, ext, null);
                    if(result2.getCode()==0){
                        fdfsId = result2.getData();
                    }
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new FastdfsIOException("上传文件异常", e);
        } finally {
            if(storageClient!=null){
                storageClientPool.returnObject(storageAddr, storageClient);
            }
            if(trackerClient!=null){
                trackerClientPool.returnObject(trackerAddr, trackerClient);
            }
        }
        return fdfsId;
    }

    private void updateStorageIpMap() {
		String trackerAddr = getTrackerAddr();
		TrackerClient trackerClient = null;
		try {
			trackerClient = trackerClientPool.borrowObject(trackerAddr);
			Result<List<GroupInfo>> result = trackerClient.getGroupInfos();
			if(result.getCode()==0){
				List<GroupInfo> groupInfos = result.getData();
				for(GroupInfo groupInfo:groupInfos){
					Result<List<StorageInfo>>  result2= trackerClient.getStorageInfos(groupInfo.getGroupName());
					if(result2.getCode()==0){
						List<StorageInfo> storageInfos = result2.getData();
						for(StorageInfo storageInfo:storageInfos){
							String hostPort = storageInfo.getDomainName();
							if(storageInfo.getStorageHttpPort()!=80){
								hostPort = hostPort + ":" + storageInfo.getStorageHttpPort();
							}
							storageIpMap.put(storageInfo.getIpAddr()+":"+storageInfo.getStoragePort(), hostPort);
						}
					}
				}
			}else{
				throw new FastdfsIOException("Get getGroupInfos Error");
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
			throw new FastdfsIOException("文件上传异常", e);
		} finally {
			if(trackerClient!=null){
				trackerClientPool.returnObject(trackerAddr, trackerClient);
			}
		}
	}
	
	private String getDownloadHostPort(String storageAddr) throws Exception{
		String downloadHostPort = storageIpMap.get(storageAddr);
		if(downloadHostPort==null){
			updateStorageIpMap();
			downloadHostPort = storageIpMap.get(storageAddr);
		}
		return downloadHostPort;
	}
	
	@Override
	public Boolean setMeta(String fdfsFileId, Map<String, String> meta) {
		String trackerAddr = getTrackerAddr();
		TrackerClient trackerClient = null;
		StorageClient storageClient = null;
		Boolean result = null;
		String storageAddr=null;
		try{
			FastDfsFile fastDfsFile = new FastDfsFile(fdfsFileId);
			trackerClient = trackerClientPool.borrowObject(trackerAddr);
			Result<String> result2 = trackerClient.getUpdateStorageAddr(fastDfsFile.group, fastDfsFile.fileName);
			if(result2.getCode()==0){
				storageAddr = result2.getData();
				storageClient = storageClientPool.borrowObject(storageAddr);
				Result<Boolean> result3 = storageClient.setMeta(fastDfsFile.group, fastDfsFile.fileName,meta);
				if(result3.getCode()==0||result3.getCode()==0){
					result = result3.getData();
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
			throw new FastdfsIOException("设置文件元数据信息异常", e);
		}finally{
			if(storageClient!=null){
				storageClientPool.returnObject(storageAddr, storageClient);
			}
			if(trackerClient!=null){
				trackerClientPool.returnObject(trackerAddr, trackerClient);
			}
		}
		return result;
	}

	@Override
	public Map<String, String> getMeta(String fdfsFileId){
		String trackerAddr = getTrackerAddr();
		TrackerClient trackerClient = null;
		StorageClient storageClient = null;
		Map<String, String> meta = null;
		String storageAddr=null;
		try{
			FastDfsFile fastDfsFile = new FastDfsFile(fdfsFileId);
			trackerClient = trackerClientPool.borrowObject(trackerAddr);
			Result<String> result2 = trackerClient.getUpdateStorageAddr(fastDfsFile.group, fastDfsFile.fileName);
			if(result2.getCode()==0){
				storageAddr = result2.getData();
				storageClient = storageClientPool.borrowObject(storageAddr);
				Result<Map<String,String>> result3 = storageClient.getMeta(fastDfsFile.group, fastDfsFile.fileName);
				if(result3.getCode()==0||result3.getCode()==0){
					meta = result3.getData();
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
			throw new FastdfsIOException("获取文件元数据信息异常", e);
		}finally{
			if(storageClient!=null){
				storageClientPool.returnObject(storageAddr, storageClient);
			}
			if(trackerClient!=null){
				trackerClientPool.returnObject(trackerAddr, trackerClient);
			}
		}
		return meta;
	}

	public String getUrl(String fdfsFileId) {
		String trackerAddr = getTrackerAddr();
		TrackerClient trackerClient = null;
		String url = null;
		try {
			FastDfsFile fastDfsFile = new FastDfsFile(fdfsFileId);
			trackerClient = trackerClientPool.borrowObject(trackerAddr);
			Result<String> result = trackerClient.getDownloadStorageAddr(fastDfsFile.group,fastDfsFile.fileName);
			if(result.getCode()==0){
				url = fastDfsFile.fileName;
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
			throw new FastdfsIOException("获取文件路径信息异常", e);
		} finally {
			if(trackerClient!=null){
				trackerClientPool.returnObject(trackerAddr, trackerClient);
			}
		}
		return url;
	}
	
	public String upload(File file) {
		return upload(file, null);
	}

	public String upload(File file,String fileName) {
		return this.upload(file,fileName,null);
	}
	
	public Boolean delete(String fdfsFileId) {
		String trackerAddr = getTrackerAddr();
		TrackerClient trackerClient = null;
		StorageClient storageClient = null;
		Boolean result=false;
		String storageAddr=null;
		try{
		    
			FastDfsFile fastDfsFile = new FastDfsFile(fdfsFileId);
			trackerClient = trackerClientPool.borrowObject(trackerAddr);
			Result<String> result2 = trackerClient.getUpdateStorageAddr(fastDfsFile.group, fastDfsFile.fileName);
			if(result2.getCode()==0){
				storageAddr = result2.getData();
				storageClient = storageClientPool.borrowObject(storageAddr);
				Result<Boolean> result3 = storageClient.delete(fastDfsFile.group, fastDfsFile.fileName);
				if(result3.getCode()==0||result3.getCode()==0){
					result = true;
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
			throw new FastdfsIOException("删除文件异常", e);
		}finally{
			if(storageClient!=null){
				storageClientPool.returnObject(storageAddr, storageClient);
			}
			if(trackerClient!=null){
				trackerClientPool.returnObject(trackerAddr, trackerClient);
			}
		}
		return result;
	}
	
	public String getTrackerAddr(){
        Random r = new Random();
        int i = r.nextInt(trackerAddrs.size());
		return trackerAddrs.get(i);
	}

    private String getFileExtName(File file) {
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
	
	private class FastDfsFile{
		private String group;
		private String fileName;
		
		public FastDfsFile(String fileId) {
			super();
			int pos = fileId.indexOf("/");
			group = fileId.substring(0, pos);
			fileName = fileId.substring(pos+1);
		}
		
	}
}
