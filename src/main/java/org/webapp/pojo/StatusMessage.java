package org.webapp.pojo;

public record StatusMessage() {
    public static final String UNKNOWN_ERROR = "未知错误";
    public static final String SUCCESS = "操作成功完成";
    public static final String MISMATCH_USERNAME_OR_PASSWORD = "用户名或密码格式不符";
    public static final String EXIST_USERNAME = "用户名已存在";
    public static final String WRONG_USERNAME_OR_PASSWORD = "用户名或密码错误";
    public static final String NONEXISTENT_USER = "用户不存在";
    public static final String EMPTY_FILE = "文件为空";
    public static final String TOO_LARGE_FILE = "文件过大";
    public static final String WRONG_FILE_FORMAT = "文件格式错误";
    public static final String WRONG_PARAMETERS = "参数无效";
    public static final String NONEXISTENT_VIDEO = "视频不存在";
    public static final String NONEXISTENT_COMMENT = "评论不存在";
    public static final String BLOCKED_BY_OTHER = "对方屏蔽了你";
    public static final String NONEXISTENT_GROUP = "群不存在";
    public static final String IN_GROUP = "已在群里";
    public static final String NOT_IN_GROUP = "尚未入群";
    public static final String UNAUTHORIZED = "尚未登录";
    public static final String NO_PERMISSION = "没有权限";
    public static final String EXCEED_MEMBER_LIMIT = "群成员数超过上限";
}
