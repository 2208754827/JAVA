package com.sky.controller.admin;

import com.sky.constant.MessageConstant;
import com.sky.result.Result;
import com.sky.utils.AliOssUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

/**
 * 通用接口
 */
@RestController
@RequestMapping("/admin/common")
@Api(tags = "通用接口")
@Slf4j
public class CommonController {

    @Autowired
    private AliOssUtil aliOssUtil;

    /**
     * 文件上传
     *
     * @param file 前端上传的文件
     * @return 文件访问 URL
     */
    @PostMapping("/upload")
    @ApiOperation("文件上传")
    public Result<String> upload(MultipartFile file) {
        log.info("文件上传：{}", file.getOriginalFilename());

        try {
            // 1、获取原始文件名
            String originalFilename = file.getOriginalFilename();

            // 2、截取文件扩展名（比如 .jpg、.png）
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));

            // 3、用 UUID 生成新文件名，避免重名覆盖（比如 a1b2c3d4.jpg）
            String objectName = UUID.randomUUID().toString() + extension;

            // 4、调用阿里云 OSS 工具类上传文件，返回文件访问 URL
            String url = aliOssUtil.upload(file.getBytes(), objectName);

            // 5、返回 URL 给前端
            return Result.success(url);

        } catch (IOException e) {
            log.error("文件上传失败：{}", e.getMessage());

        }
        return Result.error(MessageConstant.UPLOAD_FAILED);
    }

}
