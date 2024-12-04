package org.webapp.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.websocket.DecodeException;
import jakarta.websocket.Decoder;
import lombok.extern.slf4j.Slf4j;
import org.webapp.pojo.MessageDTO;

@Slf4j
public class JakartaWebSocketDecoder implements Decoder.Text<MessageDTO> {
    @Override
    public MessageDTO decode(String s) throws DecodeException {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(s, MessageDTO.class);
        } catch (JsonProcessingException e) {
            log.warn("Detect the incorrect JSON message string: {}. Fail to decode it.", s);
            return new MessageDTO(0, "none", "none", "none", 0, 0);
        }
    }

    @Override
    public boolean willDecode(String s) {
        return true;
    }
}
