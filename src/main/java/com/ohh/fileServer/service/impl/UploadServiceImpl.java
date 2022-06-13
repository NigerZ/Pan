package com.ohh.fileServer.service.impl;

import com.ohh.fileServer.dto.FileChunkDTO;
import com.ohh.fileServer.dto.FileChunkResultDTO;
import com.ohh.fileServer.service.IUploadService;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.*;

@Service
@SuppressWarnings("all")
public class UploadServiceImpl implements IUploadService {
    //打印日志信息
    private Logger logger = LoggerFactory.getLogger(UploadServiceImpl.class);
    //获取redis连接
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    //文件上传后的存储地址
    private static String uploadFolder;

    @Override
    public FileChunkResultDTO checkChunkExist(FileChunkDTO chunkDTO) {
        //检查文件是否上传过了
        //检查是否在磁盘中已经存在了
        String fileFolderPath = getFileFolderPath(chunkDTO.getIdentifier());
        logger.info("fileFolderPath-->{}",fileFolderPath);
        String filePath = getFilePath(chunkDTO.getIdentifier(), chunkDTO.getFilename());
        File file = new File(filePath);
        boolean exists = file.exists();
        //检查是否在redis中存在
        Set<Integer> uploaded = (Set<Integer>) redisTemplate.opsForHash().get(chunkDTO.getIdentifier(), "uploaded");
        if(uploaded != null && uploaded.size() == chunkDTO.getTotalChunks() && exists){
            return new FileChunkResultDTO(true);
        }
        File fileFolder = new File(fileFolderPath);
        if(!fileFolder.exists()){
            boolean mkdirs = fileFolder.mkdirs();
            logger.info("准备工作，创建文件夹，fileFolder:{},mkdirs:{}", fileFolderPath, mkdirs);
        }
        //断点续传，返回已经上传的分片
        return new FileChunkResultDTO(false, uploaded);
    }

    /**
     * 上传文件分片
     * @param chunkDTO
     * @throws IOException
     */
    @Override
    public void uploadChunk(FileChunkDTO chunkDTO) throws IOException {
        //获取分片目录
        String chunkFileFolderPath = getFileFolderPath(chunkDTO.getIdentifier());
        //生成分片文件夹
        File chunkFileFolder = new File(chunkFileFolderPath);
        if(!chunkFileFolder.exists()){
            boolean mkdirs = chunkFileFolder.mkdirs();
            logger.info("创建分片文件夹:{}", mkdirs);
        }
        //写入分片
        try (
                InputStream inputStream = chunkDTO.getFile().getInputStream();
                FileOutputStream outputStream = new FileOutputStream(new File(chunkFileFolderPath + chunkDTO.getChunkNumber()));
                ){
            IOUtils.copy(inputStream, outputStream);
            logger.info("文件标识：{}，chunkNumber：{}",chunkDTO.getIdentifier(), chunkDTO.getChunkNumber());
            //将分片写入redis
            long size = saveToRedis(chunkDTO);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 分片写入redis
     * @param chunkDTO
     * @return
     */
    private synchronized long saveToRedis(FileChunkDTO chunkDTO) {
        //获取redis中该文件的信息
        Set<Integer> uploaded = (Set<Integer>) redisTemplate.opsForHash().get(chunkDTO.getIdentifier(), "uploaded");
        //判断redis是否存储了该分片
        if(uploaded.isEmpty()){
            //redis没有存储，就将分片信息存入redis中
            uploaded = new HashSet<>(Arrays.asList(chunkDTO.getChunkNumber()));
            HashMap<Object, Object> objectObjectHashMap = new HashMap<>();
            objectObjectHashMap.put("uploaded", uploaded);
            objectObjectHashMap.put("totalChunks", chunkDTO.getTotalChunks());
            objectObjectHashMap.put("totalSize", chunkDTO.getTotalSize());
            objectObjectHashMap.put("path", chunkDTO.getFilename());
            redisTemplate.opsForHash().put(chunkDTO.getIdentifier(), "uploaded",uploaded);
            redisTemplate.opsForHash().putAll(chunkDTO.getIdentifier(), objectObjectHashMap);
        }else {
            uploaded.add(chunkDTO.getChunkNumber());
            redisTemplate.opsForHash().put(chunkDTO.getIdentifier(), "uploaded", uploaded);
        }
        return uploaded.size();
    }

    @Override
    public boolean mergeChunk(String identifier, String fileName, Integer totalChunks) throws IOException {
        return mergeChunks(identifier, fileName, totalChunks);
    }

    /**
     * 合并分片
     * @param identifier
     * @param fileName
     * @param totalChunks
     * @return
     */
    private boolean mergeChunks(String identifier, String fileName, Integer totalChunks){
        //获取文件分片目录
        String chunkFileFolderPath = getFileFolderPath(identifier);
        //获取文件存储目录
        String filePath = getFilePath(identifier, fileName);
        //判断文件的分片文件是否上传完整
        if(checkChunks(chunkFileFolderPath, totalChunks)){
            File chunkFileFolder = new File(chunkFileFolderPath);
            File megerFile = new File(fileName);
            //获取所有分片文件
            File[] chunks = chunkFileFolder.listFiles();
            List<File> fileList = Arrays.asList(chunks);
            //将文件排序
            Collections.sort(fileList, (Comparator<File>) (o1, o2) -> {
                return Integer.parseInt(o1.getName()) - Integer.parseInt(o2.getName());
            });
            //文件的合并写入
            //RandomAccessFile既可以读取文件内容，也可以向文件输出数据。
            // 同时，RandomAccessFile支持“随机访问”的方式，程序快可以直接跳转到文件的任意地方来读写数据。
            //"r" : 以只读方式打开。调用结果对象的任何 write 方法都将导致抛出 IOException。
            //"rw": 打开以便读取和写入。
            //"rws": 打开以便读取和写入。相对于 "rw"，"rws" 还要求对“文件的内容”或“元数据”的每个更新都同步写入到基础存储设备。
            //"rwd" : 打开以便读取和写入，相对于 "rw"，"rwd" 还要求对“文件的内容”的每个更新都同步写入到基础存储设备。

            RandomAccessFile randomAccessFileWrite = null;
            RandomAccessFile randomAccessFileReader = null;
            try {
                //以读写的方式打开
                randomAccessFileWrite = new RandomAccessFile(megerFile, "rw");
                byte[] bytes = new byte[1024];
                for (File file : fileList) {
                    //每个分片的文件以读的方法打开
                    randomAccessFileReader = new RandomAccessFile(file, "r");
                    int len;
                    //文件写入操作--》每次读取分片的内容将分片内容写入文件中
                    while((len = randomAccessFileReader.read(bytes)) != -1){
                        randomAccessFileWrite.write(bytes,0,len);
                    }
                }
            } catch (FileNotFoundException e) {
                return false;
            } catch (IOException e) {
//                throw new RuntimeException(e);
                return false;
            }finally {
                //关闭流操作
                try {
                    if(randomAccessFileReader != null){
                        randomAccessFileReader.close();
                    }
                    if(randomAccessFileWrite != null){
                        randomAccessFileWrite.close();
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            return true;
        }
        return false;
    }

    /**
     * 获取文件地址
     * @param identifier
     * @param fileName
     * @return
     */
    private String getFilePath(String identifier, String fileName){
        String ext = fileName.substring(fileName.lastIndexOf("."));
        return uploadFolder + fileName;
    }

    /**
     * 判断是否所有分片已上传
     * @param checkFileFolderParh
     * @param totalChunks
     * @return
     */
    private boolean checkChunks(String checkFileFolderParh, Integer totalChunks){
        try {
            for (int i = 1; i < totalChunks + 1; i++) {
                //获取每个分片的文件
                File file = new File(checkFileFolderParh + File.separator + i);
                //判断分片文件是否存在
                if(file.exists()){
                    continue;
                }else {
                    //不存在返回false
                    return false;
                }
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * 初始化文件上传后的文件地址
     */
    private static void setUploadFolder(){
        String path = UploadServiceImpl.class.getClassLoader().getResource("").getPath();
        uploadFolder = path + "resources" + File.separator + "filePath" + File.separator;
    }

    /**
     *得到文件所属的目录
     * @param identifier 文件标识
     * @return
     */
    private String getFileFolderPath(String identifier){
        return uploadFolder + identifier.substring(0,1) + File.separator + identifier.substring(1,2) + File.separator
                + identifier + File.separator;
    }
}
