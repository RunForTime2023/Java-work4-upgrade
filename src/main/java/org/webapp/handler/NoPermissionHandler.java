package org.webapp.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import org.webapp.pojo.ResponseVO;
import org.webapp.pojo.StatusCode;
import org.webapp.pojo.StatusMessage;

import java.io.IOException;

@Component
public class NoPermissionHandler implements AccessDeniedHandler {
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        response.setContentType("application/json;charset=utf-8");
        ObjectMapper objectMapper = new ObjectMapper();
        ResponseVO jsonResponse = new ResponseVO(StatusCode.NO_PERMISSION, StatusMessage.NO_PERMISSION);
        response.getWriter().print(objectMapper.writeValueAsString(jsonResponse));
    }
}
