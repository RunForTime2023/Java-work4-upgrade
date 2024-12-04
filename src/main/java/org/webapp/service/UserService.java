package org.webapp.service;

import org.webapp.pojo.UserVO;

public interface UserService {
    void saveUser(String username, String password);

    UserVO getUserById(String userId);

    UserVO getUserByUsername(String username);

    UserVO validateUser(String username, String password);

    void updatePassword(String userId, String password);

    void updateAvatarUrl(String userId, String avatarUrl);

    void updateRole(String userId, int isAdmin);

    void removeUser(String userId);
}
