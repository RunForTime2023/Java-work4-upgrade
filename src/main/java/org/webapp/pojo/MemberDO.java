package org.webapp.pojo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@TableName("member_list")
public class MemberDO {
    private String fromUserId;
    private String toGroupId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
    @TableField("is_deleted")
    private boolean deleted;

    public MemberDO(String fromUserId, String toGroupId, LocalDateTime createdAt) {
        this.fromUserId = fromUserId;
        this.toGroupId = toGroupId;
        this.createdAt = this.updatedAt = createdAt;
        this.deleted = false;
    }
}
