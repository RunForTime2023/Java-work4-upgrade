package org.webapp.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.webapp.mapper.VideoMapper;
import org.webapp.pojo.VideoDO;
import org.webapp.utils.RedisUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
public class VideoServiceImpl implements VideoService {
    @Autowired
    private VideoMapper videoMapper;
    @Autowired
    private RedisUtils redisTools;
    private final String videoDirOnWin32 = "D:\\WebApp\\ServerData\\Video";
    private final String coverDirOnWin32 = "D:\\WebApp\\ServerData\\VideoCover";
    private final String videoDirOnLinux = "/usr/local/bin/WebApp/ServerData/Video";
    private final String coverDirOnLinux = "/usr/local/bin/WebApp/ServerData/VideoCover";

    @Override
    public String saveVideo(String userId, String title, String description, String suffix) {
        VideoDO video = new VideoDO(userId, title, description);
        videoMapper.insert(video);
        String videoUrl, coverUrl;
        String environment = System.getProperty("os.name").toLowerCase();
        if (environment.contains("windows")) {
            videoUrl = videoDirOnWin32 + "\\" + video.getVideoId() + suffix;
            coverUrl = coverDirOnWin32 + "\\" + video.getVideoId() + ".jpg";
        } else {
            videoUrl = videoDirOnLinux + "/" + video.getVideoId() + suffix;
            coverUrl = coverDirOnLinux + "/" + video.getVideoId() + ".jpg";
        }
        video.setVideoUrl(videoUrl);
        video.setCoverUrl(coverUrl);
        LambdaUpdateWrapper<VideoDO> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        lambdaUpdateWrapper.set(VideoDO::getVideoUrl, videoUrl).set(VideoDO::getCoverUrl, coverUrl).eq(VideoDO::getVideoId, video.getVideoId());
        videoMapper.update(lambdaUpdateWrapper);
        redisTools.saveVideo(video);
        return video.getVideoId();
    }

    @Override
    public void saveSearchRecord(String userId, String keywords) {
        redisTools.saveSearchRecord(userId, keywords);
    }

    @Override
    public VideoDO getVideo(String videoId) {
        if (redisTools.isKeyExist("video:" + videoId)) {
            return redisTools.getVideo("video:" + videoId);
        } else {
            VideoDO video = videoMapper.selectById(videoId);
            if (video != null && !video.isDeleted()) {
                redisTools.saveVideo(video);
                return video;
            }
            return null;
        }
    }

    @Override
    public List<VideoDO> listVideoByVisitCountDescWithPaging(int pageSize, int pageNum) {
        Long total = redisTools.countVideo();
        long offset = (long) pageSize * (pageNum - 1);
        List<VideoDO> videoList = new ArrayList<>();
        if (offset < total) {
            Set<String> result = redisTools.listVideoId(offset, Math.min(total, offset + pageSize) - 1);
            if (result != null) {
                for (String videoId : result) {
                    if (redisTools.isKeyExist("video:" + videoId)) {
                        videoList.add(redisTools.getVideo(videoId));
                    } else {
                        VideoDO video = videoMapper.selectById(videoId);
                        videoList.add(video);
                        redisTools.saveVideo(video);
                    }
                }
            }
        }
        return videoList;
    }

    @Override
    public Page<VideoDO> listVideoByUserIdWithPaging(String userId, int pageSize, int pageNum) {
        Page<VideoDO> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<VideoDO> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(VideoDO::getUserId, userId).eq(VideoDO::isDeleted, false).orderByDesc(VideoDO::getCreatedAt);
        return videoMapper.selectPage(page, lambdaQueryWrapper);
    }

    @Override
    public Page<VideoDO> listVideoByKeywordsWithPaging(String keywords, int pageSize, int pageNum) {
        Page<VideoDO> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<VideoDO> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        if (!keywords.isEmpty()) {
            lambdaQueryWrapper.like(VideoDO::getTitle, keywords).or().like(VideoDO::getDescription, keywords).orderByDesc(VideoDO::getVisitCount);
        }
        return videoMapper.selectPage(page, lambdaQueryWrapper);
    }

    @Override
    public void updateVisitCount(String videoId) {
        videoMapper.updateVideoCount(videoId, VideoDO::getVisitCount, 1);
        if (redisTools.isKeyExist("video:" + videoId)) {
            redisTools.updateVideo("visit_count", videoId, 1);
        } else {
            VideoDO video = videoMapper.selectById(videoId);
            if (video != null && !video.isDeleted()) {
                redisTools.saveVideo(video);
            }
        }
    }

    @Override
    public void updateLikeCount(String videoId, int plus) {
        videoMapper.updateVideoCount(videoId, VideoDO::getLikeCount, plus);
        if (redisTools.isKeyExist("video:" + videoId)) {
            redisTools.updateVideo("like_count", videoId, plus);
        } else {
            VideoDO video = videoMapper.selectById(videoId);
            if (video != null && !video.isDeleted()) {
                redisTools.saveVideo(video);
            }
        }
    }

    @Override
    public void updateCommentCount(String videoId, int plus) {
        videoMapper.updateVideoCount(videoId, VideoDO::getCommentCount, plus);
        if (redisTools.isKeyExist("video:" + videoId)) {
            redisTools.updateVideo("comment_count", videoId, plus);
        } else {
            VideoDO video = videoMapper.selectById(videoId);
            if (video != null && !video.isDeleted()) {
                redisTools.saveVideo(video);
            }
        }
    }

    @Override
    public void removeVideo(String videoId) {
        videoMapper.removeVideo(videoId);
        redisTools.removeVideo(videoId);
    }

    @Override
    @Async
    public void saveFile(MultipartFile video, String userId, String videoId, String suffix) {
        String videoName, coverName, environment = System.getProperty("os.name").toLowerCase();
        File videoFile, coverFile;
        if (environment.contains("windows")) {
            videoFile = new File(videoDirOnWin32);
            coverFile = new File(coverDirOnWin32);
            videoFile.mkdirs();
            coverFile.mkdirs();
            videoName = videoDirOnWin32 + "\\" + videoId + suffix;
            coverName = coverDirOnWin32 + "\\" + videoId + ".jpg";
        } else {
            videoFile = new File(videoDirOnLinux);
            coverFile = new File(coverDirOnLinux);
            videoFile.mkdirs();
            coverFile.mkdirs();
            videoName = videoDirOnLinux + "/" + videoId + suffix;
            coverName = coverDirOnLinux + "/" + videoId + ".jpg";
        }
        videoFile = new File(videoName);
        coverFile = new File(coverName);
        try {
            if (!videoFile.exists()) {
                videoFile.createNewFile();
            }
            if (!coverFile.exists()) {
                coverFile.createNewFile();
            }
            video.transferTo(videoFile);
            FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(videoName);
            grabber.start();
            Frame frame = grabber.grabImage();
            Java2DFrameConverter converter = new Java2DFrameConverter();
            BufferedImage image = converter.getBufferedImage(frame);
            ImageIO.write(image, "jpg", coverFile);
            converter.close();
            frame.close();
            grabber.close();
            log.info("The user: {} uploads the video: {} and its cover successfully.", userId, videoId);
        } catch (Exception e) {
            videoFile.delete();
            coverFile.delete();
            log.error("The user: {} fails to upload the video: {} and its cover.", userId, videoId);
            throw new RuntimeException(e);
        }
    }
}