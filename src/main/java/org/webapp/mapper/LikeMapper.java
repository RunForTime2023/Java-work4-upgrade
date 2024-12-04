package org.webapp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import org.webapp.pojo.LikeDO;
import org.webapp.pojo.UserVO;
import org.webapp.pojo.VideoDO;

import java.util.List;

@Repository
public interface LikeMapper extends BaseMapper<LikeDO> {
    List<VideoDO> listLikeToVideo(@Param("user_id") String userId, @Param("offset") int offset, @Param("limit") int limit);

    List<UserVO> listFollow(@Param("user_id") String userId, @Param("offset") int offset, @Param("limit") int limit);

    List<UserVO> listFans(@Param("user_id") String userId, @Param("offset") int offset, @Param("limit") int limit);

    List<UserVO> listFriend(@Param("user_id") String userId, @Param("offset") int offset, @Param("limit") int limit);

    Long countFriend(@Param("user_id") String userId);

    List<UserVO> listBlock(@Param("user_id") String userId, @Param("offset") int offset, @Param("limit") int limit);
}
