package org.webapp.pojo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

import java.util.List;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseVO {
    private int code;
    private String message;
    private List<?> data;
    private Long total;
    private String token;

    public ResponseVO(int code, String message) {
        this.code = code;
        this.message = message;
        this.data = null;
    }

    public ResponseVO(int code, String message, List<?> data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public ResponseVO(int code, String message, List<?> data, String token) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.token = token;
    }

    public ResponseVO(int code, String message, List<?> data, Long total) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.total = total;
    }
}