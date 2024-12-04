package org.webapp.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.springframework.stereotype.Repository;
import org.webapp.pojo.MemberDO;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MemberMapper extends BaseMapper<MemberDO> {
    default MemberDO getMember(String userId, String groupId) {
        LambdaQueryWrapper<MemberDO> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(MemberDO::getFromUserId, userId).eq(MemberDO::getToGroupId, groupId);
        return this.selectOne(lambdaQueryWrapper, false);
    }

    default List<MemberDO> getMembersInGroup(String userId, String groupId) {
        LambdaQueryWrapper<MemberDO> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(MemberDO::getToGroupId, groupId).eq(MemberDO::isDeleted, false).ne(MemberDO::getFromUserId, userId);
        return this.selectList(lambdaQueryWrapper);
    }

    default void updateMember(String userId, String groupId, LocalDateTime time) {
        LambdaUpdateWrapper<MemberDO> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        lambdaUpdateWrapper.set(MemberDO::getUpdatedAt, time).set(MemberDO::isDeleted, false).eq(MemberDO::getFromUserId, userId).eq(MemberDO::getToGroupId, groupId);
        this.update(lambdaUpdateWrapper);
    }

    default void removeMember(String userId, String groupId, LocalDateTime time) {
        LambdaUpdateWrapper<MemberDO> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        lambdaUpdateWrapper.set(MemberDO::getDeletedAt, time).set(MemberDO::isDeleted, true).eq(MemberDO::getFromUserId, userId).eq(MemberDO::getToGroupId, groupId);
        this.update(lambdaUpdateWrapper);
    }
}
