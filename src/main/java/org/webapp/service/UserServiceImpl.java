package org.webapp.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.webapp.mapper.UserMapper;
import org.webapp.pojo.UserDO;
import org.webapp.pojo.UserVO;
import org.webapp.utils.RedisUtils;

@Slf4j
@Service
public class UserServiceImpl implements UserService, UserDetailsService {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private RedisUtils redisTools;

    @Override
    public void saveUser(String username, String password) {
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        UserDO user = new UserDO(username, bCryptPasswordEncoder.encode(password));
        userMapper.insert(user);
        log.info("The user: {} registers successfully.", username);
    }

    @Override
    public UserVO getUserById(String userId) {
        return userMapper.getUserDetailById(userId);
    }

    @Override
    public UserVO getUserByUsername(String username) {
        return userMapper.getUserByUsername(username).turnToUserVO();
    }

    @Override
    public UserVO validateUser(String username, String password) {
        UserDO user = userMapper.getUserByUsername(username);
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        if (user != null && bCryptPasswordEncoder.matches(password, user.getPassword())) {
            return user.turnToUserVO();
        } else {
            return null;
        }
    }

    @Override
    public void updatePassword(String userId, String password) {
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        userMapper.updateUser(userId, UserDO::getPassword, bCryptPasswordEncoder.encode(password));
    }

    @Override
    public void updateAvatarUrl(String userId, String avatarUrl) {
        userMapper.updateUser(userId, UserDO::getAvatarUrl, avatarUrl);
    }

    @Override
    public void updateRole(String userId, int isAdmin) {
        userMapper.updateUser(userId, UserDO::isAdmin, isAdmin);
    }

    @Override
    public void removeUser(String userId) {
        userMapper.removeUser(userId);
    }

    // 为利用索引，此处的username代表用户ID
    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        UserDO user = userMapper.selectById(userId);
        if (user == null || user.isDeleted()) {
            throw new UsernameNotFoundException("用户：" + userId + "不存在");
        }
        return user;
    }
}
