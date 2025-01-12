package org.webapp.mapper;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.springframework.stereotype.Repository;
import org.webapp.pojo.MessageDO;

@Repository
public interface MessageMapper extends BaseMapper<MessageDO> {
    default void updateMessage(String messageId, String imageUrl) {
        LambdaUpdateWrapper<MessageDO> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        lambdaUpdateWrapper.set(MessageDO::getImageUrl, imageUrl).eq(MessageDO::getMessageId, messageId);
        this.update(lambdaUpdateWrapper);
    }
}