package com.ohh.fileServer.controller;

import com.ohh.fileServer.dto.FileChunkDTO;
import com.ohh.fileServer.dto.FileChunkResultDTO;
import com.ohh.fileServer.dto.RestApiResponse;
import com.ohh.fileServer.service.IUploadService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/upload")
@Api("文件上传接口")
public class UploaderController {

    @Autowired
    private IUploadService uploadService;
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 上传文件分片
     * @param chunkDTO
     * @return
     */
    @PostMapping("uploadChunk")
    @ApiOperation("上传文件分片")
    public RestApiResponse<Object> uploadChunk(FileChunkDTO chunkDTO){
        try {
            uploadService.uploadChunk(chunkDTO);
            return RestApiResponse.success(chunkDTO.getIdentifier());
        } catch (IOException e) {
            return RestApiResponse.error(e.getMessage());
        }
    }

    /**
     * 检查分片是否存在
     *
     * @return
     */
    @GetMapping("chunk")
    @ApiOperation("检查分片是否存在")
    public RestApiResponse<Object> checkChunkExist(FileChunkDTO chunkDTO) {
        FileChunkResultDTO fileChunkCheckDTO;
        try {
            fileChunkCheckDTO = uploadService.checkChunkExist(chunkDTO);
            return RestApiResponse.success(fileChunkCheckDTO);
        } catch (Exception e) {
            return RestApiResponse.error(e.getMessage());
        }
    }


    /**
     * 请求合并文件分片
     *
     * @param chunkDTO
     * @return
     */
    @PostMapping("merge")
    @ApiOperation("合并文件分片")
    public RestApiResponse<Object> mergeChunks(@RequestBody FileChunkDTO chunkDTO) {
        try {
            boolean success = uploadService.mergeChunk(chunkDTO.getIdentifier(), chunkDTO.getFilename(), chunkDTO.getTotalChunks());
            return RestApiResponse.flag(success);
        } catch (Exception e) {
            return RestApiResponse.error(e.getMessage());
        }
    }

/*    @GetMapping("setRedis")
    @ApiOperation("测试redis")
    public void testRedis(String key, String value){
        redisTemplate.opsForValue().set(key,value);
        Object key1 = redisTemplate.opsForValue().get("key1");
        System.out.println(key1);
        System.out.println(key + ":" + value);
    }*/
}
