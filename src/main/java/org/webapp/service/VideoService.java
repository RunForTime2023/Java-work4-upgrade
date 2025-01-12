package org.webapp.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.web.multipart.MultipartFile;
import org.webapp.pojo.VideoDO;

import java.util.List;

public interface VideoService {
    String saveVideo(String userId, String title, String description, String suffix);

    void saveSearchRecord(String userId, String keywords);

    VideoDO getVideo(String videoId);

    List<VideoDO> listVideoByVisitCountDescWithPaging(int pageSize, int pageNum);

    Page<VideoDO> listVideoByUserIdWithPaging(String userId, int pageSize, int pageNum);

    Page<VideoDO> listVideoByKeywordsWithPaging(String userId, String keywords, int pageSize, int pageNum);

    void updateVisitCount(String videoId);

    void updateLikeCount(String videoId, int plus);

    void updateCommentCount(String videoId, int plus);

    void removeVideo(String videoId);

    void saveFile(MultipartFile video, String userId, String videoId, String suffix);
}
