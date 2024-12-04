package org.webapp.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.webapp.pojo.ResponseVO;
import org.webapp.pojo.StatusCode;
import org.webapp.pojo.StatusMessage;

import java.io.IOException;

@Component
public class UnauthorizedHandler implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        response.setContentType("application/json;charset=utf-8");
        ObjectMapper objectMapper = new ObjectMapper();
        ResponseVO jsonResponse = new ResponseVO(StatusCode.UNAUTHORIZED, StatusMessage.UNAUTHORIZED);
        response.getWriter().print(objectMapper.writeValueAsString(jsonResponse));
    }
}
