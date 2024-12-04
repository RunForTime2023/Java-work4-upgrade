package org.webapp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import org.webapp.pojo.CommentDO;

import java.util.List;

@Repository
public interface CommentMapper extends BaseMapper<CommentDO> {
    List<CommentDO> listCommentWithChild(@Param("comment_id") String commentId, @Param("offset") int offset, @Param("limit") int limit);

    int countComment(@Param("comment_id") String CommentId);

    void updateCommentCount(@Param("comment_id") String commentId);

    void removeComment(@Param("comment_id") String commentId, @Param("minus") int minus);
}