package org.webapp.pojo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@TableName("like_list")
public class LikeDO {
    private String fromUserId;
    private String toUserId;
    private String toVideoId;
    private String toCommentId;
    @TableField("is_disliked")
    private boolean disliked;
    @TableField("is_deleted")
    private boolean deleted;

    public LikeDO(String fromUserId, String toUserId, String toVideoId, String toCommentId) {
        this.fromUserId = fromUserId;
        this.toUserId = toUserId;
        this.toVideoId = toVideoId;
        this.toCommentId = toCommentId;
        this.disliked = this.deleted = false;
    }
}
