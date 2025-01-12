package org.webapp.mapper;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;
import org.webapp.pojo.UserDO;
import org.webapp.pojo.UserVO;

import java.time.LocalDateTime;

@Repository
public interface UserMapper extends BaseMapper<UserDO> {
    @Select("select user_id, username, password, avatar_url, is_admin, created_at, updated_at, deleted_at, is_deleted from user_list where username=#{username} and is_deleted=0 limit 1")
    UserDO getUserByUsername(String username);

    @Select("select user_id, username, avatar_url from user_list where user_id=#{user_id} and is_deleted=0")
    UserVO getUser(@Param("user_id") String userId);

    @Select("select user_id, username, avatar_url, created_at, updated_at, deleted_at from user_list where user_id=#{user_id} and is_deleted=0")
    UserVO getUserDetailById(@Param("user_id") String userId);

    default void updateUser(String userId, SFunction<UserDO, Object> column, Object value) {
        LambdaUpdateWrapper<UserDO> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        lambdaUpdateWrapper.set(column, value).set(UserDO::getUpdatedAt, LocalDateTime.now()).eq(UserDO::getUserId, userId);
        this.update(lambdaUpdateWrapper);
    }
}