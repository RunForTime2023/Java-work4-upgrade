package org.webapp.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.webapp.pojo.*;
import org.webapp.service.UserService;
import org.webapp.service.VideoService;
import org.webapp.utils.FileUtils;
import org.webapp.utils.JwtUtils;

import java.util.List;

/**
 * 视频模块
 */
@RestController
public class VideoController {
    @Autowired
    private VideoService videoService;
    @Autowired
    private UserService userService;

    /**
     * 投稿
     * 仅支持上传单个文件，文件大小不超过 10 GB
     *
     * @param file        文件
     * @param title       标题
     * @param description 简介
     * @param token       令牌
     */
    @PostMapping(value = "/video/publish", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Transactional
    public ResponseVO saveVideo(@RequestPart("data") MultipartFile file, @RequestParam("title") String title, @RequestParam("description") String description, @RequestHeader("Access-Token") String token, HttpServletResponse httpServletResponse) {
        ResponseVO response;
        if (file.getSize() == 0) {
            response = new ResponseVO(StatusCode.EMPTY_FILE, StatusMessage.EMPTY_FILE);
        } else if (file.getSize() > 10 * 1024 * 1024 * 1024L) {
            response = new ResponseVO(StatusCode.TOO_LARGE_FILE, StatusMessage.TOO_LARGE_FILE);
        } else if (title.isEmpty() || description.isEmpty()) {
            response = new ResponseVO(StatusCode.WRONG_PARAMETERS, StatusMessage.WRONG_PARAMETERS);
        } else if (!FileUtils.isVideo(file.getOriginalFilename())) {
            response = new ResponseVO(StatusCode.WRONG_FILE_FORMAT, StatusMessage.WRONG_FILE_FORMAT);
            httpServletResponse.setStatus(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
        } else {
            String userId = JwtUtils.getUserId(token);
            String suffix = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
            String videoId = videoService.saveVideo(userId, title, description, suffix);
            videoService.saveFile(file, userId, videoId, suffix);
            response = new ResponseVO(StatusCode.SUCCESS, StatusMessage.SUCCESS);
        }
        return response;
    }

    /**
     * 发布列表
     * 展示指定用户发布的视频列表
     * 搜索结果默认按上传时间降序排列
     *
     * @param userId   用户 ID
     * @param pageSize 页面尺寸
     * @param pageNum  页码
     */
    @GetMapping("/video/list")
    @Transactional(readOnly = true)
    public ResponseVO listPublishedVideo(@RequestParam("user_id") String userId, @RequestParam("page_size") int pageSize, @RequestParam("page_num") int pageNum) {
        ResponseVO response;
        if (pageSize <= 0 || pageSize > 100 || pageNum <= 0) {
            response = new ResponseVO(StatusCode.WRONG_PARAMETERS, StatusMessage.WRONG_PARAMETERS);
        } else {
            UserVO user = userService.getUserById(userId);
            if (user == null) {
                response = new ResponseVO(StatusCode.NONEXISTENT_USER, StatusMessage.NONEXISTENT_USER);
            } else {
                Page<VideoDO> videoList = videoService.listVideoByUserIdWithPaging(userId, pageSize, pageNum);
                response = new ResponseVO(StatusCode.SUCCESS, StatusMessage.SUCCESS, videoList.getRecords(), videoList.getTotal());
            }
        }
        return response;
    }

    /**
     * 观看视频
     *
     * @param videoId 视频 ID
     */
    @PostMapping(value = "/video/watch", consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @Transactional
    public ResponseVO watchVideo(@RequestParam("video_id") String videoId) {
        ResponseVO response;
        VideoDO video = videoService.getVideo(videoId);
        if (video == null) {
            response = new ResponseVO(StatusCode.NONEXISTENT_VIDEO, StatusMessage.NONEXISTENT_VIDEO);
        } else {
            videoService.updateVisitCount(videoId);
            response = new ResponseVO(StatusCode.SUCCESS, StatusMessage.SUCCESS, List.of(video));
        }
        return response;
    }


    /**
     * 热门排行榜
     * 调用Redis缓存
     *
     * @param pageSize 页面尺寸
     * @param pageNum  页码
     */
    @GetMapping("/video/popular")
    @Transactional(readOnly = true)
    public ResponseVO listPopularVideo(@RequestParam("page_size") int pageSize, @RequestParam("page_num") int pageNum) {
        ResponseVO response;
        if (pageSize <= 0 || pageSize > 100 || pageNum <= 0) {
            response = new ResponseVO(StatusCode.WRONG_PARAMETERS, StatusMessage.WRONG_PARAMETERS);
        } else {
            List<VideoDO> videoList = videoService.listVideoByVisitCountDescWithPaging(pageSize, pageNum);
            response = new ResponseVO(StatusCode.SUCCESS, StatusMessage.SUCCESS, videoList);
        }
        return response;
    }

    /**
     * 搜索视频
     * 仅搜索标题和简介，搜索记录保存到Redis
     * 搜索结果默认按观看数量降序排列
     *
     * @param keywords 关键词
     * @param pageSize 页面尺寸
     * @param pageNum  页码
     */
    @PostMapping(value = "/video/search", consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @Transactional(readOnly = true)
    public ResponseVO searchVideo(@RequestParam("keywords") String keywords, @RequestParam("page_size") int pageSize, @RequestParam("page_num") int pageNum, @RequestHeader(value = "Access-Token", required = false) String token) {
        ResponseVO response;
        if (pageSize <= 0 || pageSize > 100 || pageNum <= 0 || keywords.length() > 100) {
            response = new ResponseVO(StatusCode.WRONG_PARAMETERS, StatusMessage.WRONG_PARAMETERS);
        } else {
            Page<VideoDO> videoList = videoService.listVideoByKeywordsWithPaging(keywords, pageSize, pageNum);
            String userId = JwtUtils.getUserId(token);
            if (userId == null) {
                userId = "anonymous";
            }
            videoService.saveSearchRecord(userId, keywords);
            response = new ResponseVO(StatusCode.SUCCESS, StatusMessage.SUCCESS, videoList.getRecords(), videoList.getTotal());
        }
        return response;
    }
}
