package org.webapp.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.websocket.EncodeException;
import jakarta.websocket.Encoder;
import org.webapp.pojo.ResponseVO;
import org.webapp.utils.CustomizeUtils;

public class JakartaWebSocketEncoder implements Encoder.Text<ResponseVO> {
    @Override
    public String encode(ResponseVO response) throws EncodeException {
        try {
            ObjectMapper objectMapper = CustomizeUtils.customizedObjectMapper();
            return objectMapper.writeValueAsString(response);
        } catch (Exception e) {
            throw new EncodeException(response, "Fail to serialize the POJO class ResponseVO Object.", e);
        }
    }
}
