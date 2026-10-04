package com.box.controller;

import com.box.common.Result;
import com.box.entity.User;
import com.box.exception.BusinessException;
import com.box.mapper.UserMapper;
import com.box.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;

import java.util.Date;

@RestController
@RequestMapping("/api/image")
@RequiredArgsConstructor
public class ImageController {

    @Value("${app.upload.dir}")
    private String uploadDir;

    private final UserMapper userMapper;

    @PostMapping("/avatar")
    public Result<String> avatar(@RequestHeader("Authorization") String authHeader,
                                 @RequestParam("file") MultipartFile file){
        Integer uid = JwtUtils.getUid(authHeader);
        User user = userMapper.selectById(uid);
        checkFile(file, user);

        String fileName = file.getOriginalFilename();
        String suffix = ".jpg";
        if (fileName != null && fileName.contains(".")) {
            suffix = fileName.substring(fileName.lastIndexOf("."));
        }

        String filePath = uploadDir + "avatar/" + uid + suffix;

        File dest = new File(filePath);
        dest.getParentFile().mkdirs();
        try {
            file.transferTo(dest);
        } catch (IOException e) {
            throw new BusinessException("上传失败");
        }

        user.setAvatar("/avatar/" + uid + suffix);
        userMapper.updateById(user);
        return Result.success(filePath);
    }


    @PostMapping("/cache")
    public Result<String> cache(@RequestHeader("Authorization") String authHeader,
                                @RequestParam("file") MultipartFile file){
        //获得uid
        Integer uid = JwtUtils.getUid(authHeader);
        User user = userMapper.selectById(uid);
        //查看user和file是否为null
        checkFile(file, user);
        //生成日期
        String date = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        //??
        String diskPath = uploadDir + "cache/" + date;
        File dest = new File(diskPath);
        dest.getParentFile().mkdirs();
        try {
            file.transferTo(dest);
        } catch (IOException e) {
            throw new BusinessException("上传失败");
        }
        return Result.success("/cache/" + date);
    }









    private static void checkFile(MultipartFile file, User user) {
        if (user == null){
            throw new BusinessException("用户不存在");
        }
        if (file.isEmpty()){
            throw new BusinessException("文件为空");
        }

        String contentType = file.getContentType();
        if (!"image/jpeg".equals(contentType) && !"image/png".equals(contentType)) {
            throw new BusinessException("只能上传 JPG 或 PNG 图片");
        }
    }

}
