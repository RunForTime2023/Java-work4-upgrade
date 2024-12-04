package org.webapp.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.springframework.stereotype.Repository;
import org.webapp.pojo.GroupDO;

import java.time.LocalDateTime;

@Repository
public interface GroupMapper extends BaseMapper<GroupDO> {
    default GroupDO getGroup(String groupId) {
        LambdaQueryWrapper<GroupDO> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(GroupDO::getGroupId, groupId).eq(GroupDO::isDeleted, false);
        return this.selectOne(lambdaQueryWrapper);
    }

    default void updateMemberCount(String groupId, int plus, LocalDateTime time) {
        LambdaUpdateWrapper<GroupDO> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        lambdaUpdateWrapper.setIncrBy(GroupDO::getMemberCount, plus).set(GroupDO::getUpdatedAt, time).eq(GroupDO::getGroupId, groupId).eq(GroupDO::isDeleted, false);
        this.update(lambdaUpdateWrapper);
    }

    default void removeGroup(String groupId) {
        LambdaUpdateWrapper<GroupDO> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        lambdaUpdateWrapper.set(GroupDO::getDeletedAt, LocalDateTime.now()).set(GroupDO::isDeleted, true).eq(GroupDO::getGroupId, groupId);
        this.update(lambdaUpdateWrapper);
    }
}
