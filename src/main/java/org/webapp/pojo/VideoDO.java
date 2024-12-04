package org.webapp.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("video_list")
public class VideoDO {
    @TableId(type = IdType.ASSIGN_ID)
    private String videoId;
    private String userId;
    private String videoUrl;
    private String coverUrl;
    private String title;
    private String description;
    private int visitCount;
    private int likeCount;
    private int commentCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
    @TableField("is_deleted")
    @JsonIgnore
    private boolean deleted;

    public VideoDO(String userId, String title, String description) {
        this.userId = userId;
        this.videoUrl = this.coverUrl = "none";
        this.title = title;
        this.description = description;
        this.visitCount = this.likeCount = this.commentCount = 0;
        this.createdAt = this.updatedAt = LocalDateTime.now();
        this.deletedAt = LocalDateTime.of(1970, 1, 1, 8, 0, 1);
        this.deleted = false;
    }
}
