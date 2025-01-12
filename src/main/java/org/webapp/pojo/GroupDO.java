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
@TableName("group_list")
public class GroupDO {
    @TableId(type = IdType.ASSIGN_ID)
    private String groupId;
    private String groupName;
    private String leaderId;
    private int memberCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
    @JsonIgnore
    @TableField("is_deleted")
    private boolean deleted;

    public GroupDO(String groupName, String leaderId, int memberCount, LocalDateTime createdAt) {
        this.groupName = groupName;
        this.leaderId = leaderId;
        this.memberCount = memberCount;
        this.createdAt = this.updatedAt = createdAt;
        this.deleted = false;
    }
}
