package org.webapp.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@TableName("comment_list")
public class CommentDO {
    @TableId(type = IdType.ASSIGN_UUID)
    private String commentId;
    private String userId;
    private String videoId;
    private String parentId;
    private int likeCount;
    private int childCount;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
    @TableField("is_deleted")
    @JsonIgnore
    private boolean deleted;

    public CommentDO(String userId, String videoId, String parentId, String content) {
        this.userId = userId;
        this.videoId = videoId;
        this.parentId = parentId;
        this.likeCount = this.childCount = 0;
        this.content = content;
        this.deleted = false;
    }
}
