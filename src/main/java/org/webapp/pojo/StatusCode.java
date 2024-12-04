package org.webapp.pojo;

public record StatusCode() {
    public static final int UNKNOWN_ERROR = 0;
    public static final int SUCCESS = 1;
    public static final int MISMATCH_USERNAME_OR_PASSWORD = 2;
    public static final int EXIST_USERNAME = 3;
    public static final int WRONG_USERNAME_OR_PASSWORD = 4;
    public static final int NONEXISTENT_USER = 5;
    public static final int EMPTY_FILE = 6;
    public static final int TOO_LARGE_FILE = 7;
    public static final int WRONG_FILE_FORMAT = 8;
    public static final int WRONG_PARAMETERS = 9;
    public static final int NONEXISTENT_VIDEO = 10;
    public static final int NONEXISTENT_COMMENT = 11;
    public static final int BLOCKED_BY_OTHER = 12;
    public static final int NONEXISTENT_GROUP = 13;
    public static final int IN_GROUP = 14;
    public static final int NOT_IN_GROUP = 15;
    public static final int UNAUTHORIZED = 16;
    public static final int NO_PERMISSION = 17;
    public static final int EXCEED_MEMBER_LIMIT = 18;
}
