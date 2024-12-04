package org.webapp.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class MessageDTO {
    private int actionType;
    private String userOrGroupId;
    private String content;
    private String image;
    private int pageSize;
    private int pageNum;

    public MessageDTO(@JsonProperty("action_type") int actionType, @JsonProperty("user_or_group_id") String userOrGroupId,
                      @JsonProperty("content") String content, @JsonProperty("image") String image,
                      @JsonProperty("page_size") int pageSize, @JsonProperty("page_num") int pageNum) {
        this.actionType = actionType;
        this.userOrGroupId = userOrGroupId;
        this.content = content;
        this.image = image;
        this.pageSize = pageSize;
        this.pageNum = pageNum;
    }
}
