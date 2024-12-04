package org.webapp.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("message_list")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MessageDO {
    @TableId(type = IdType.ASSIGN_UUID)
    private String messageId;
    private String fromUserId;
    private String toUserId;
    private String toGroupId;
    private String content;
    private String imageUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
    @JsonIgnore
    @TableField("is_deleted")
    private boolean deleted;

    public MessageDO(String fromUserId, String toUserId, String toGroupId, String content) {
        this.fromUserId = fromUserId;
        this.toUserId = toUserId;
        this.toGroupId = toGroupId;
        this.content = content;
        this.imageUrl = "none";
        this.createdAt = this.updatedAt = LocalDateTime.now();
        this.deletedAt = LocalDateTime.of(1970, 1, 1, 8, 0, 1);
        this.deleted = false;
    }
}