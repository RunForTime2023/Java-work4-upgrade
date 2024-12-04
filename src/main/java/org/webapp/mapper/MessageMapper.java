package org.webapp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.springframework.stereotype.Repository;
import org.webapp.pojo.MessageDO;

@Repository
public interface MessageMapper extends BaseMapper<MessageDO> {
}