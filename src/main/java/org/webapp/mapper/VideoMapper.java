package org.webapp.mapper;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import org.springframework.stereotype.Repository;
import org.webapp.pojo.VideoDO;

import java.time.LocalDateTime;

@Repository
public interface VideoMapper extends BaseMapper<VideoDO> {
    default void updateVideoCount(String videoId, SFunction<VideoDO, Object> column, int plus) {
        LambdaUpdateWrapper<VideoDO> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        lambdaUpdateWrapper.setIncrBy(column, plus).set(VideoDO::getUpdatedAt, LocalDateTime.now()).eq(VideoDO::getVideoId, videoId);
        this.update(lambdaUpdateWrapper);
    }

    default void removeVideo(String videoId) {
        LambdaUpdateWrapper<VideoDO> videoLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        videoLambdaUpdateWrapper.set(VideoDO::getDeletedAt, LocalDateTime.now()).set(VideoDO::isDeleted, true).eq(VideoDO::getVideoId, videoId);
        this.update(videoLambdaUpdateWrapper);
    }
}
