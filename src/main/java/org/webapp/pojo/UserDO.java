package org.webapp.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;

@Data
@AllArgsConstructor
@TableName("user_list")
public class UserDO implements UserDetails {
    @TableId(type = IdType.ASSIGN_ID)
    private String userId;
    private String username;
    private String password;
    private String avatarUrl;
    @TableField("is_admin")
    private boolean admin;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
    @TableField("is_deleted")
    private boolean deleted;

    public UserDO(String username, String password) {
        this.username = username;
        this.password = password;
        this.avatarUrl = "none";
        this.admin = this.deleted = false;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority> grantedAuthorities = new HashSet<>();
        if (admin) {
            grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        }
        grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        return grantedAuthorities;
    }

    //    @Deprecated(since = "0.6.617")
    public UserVO turnToUserVO() {
        return new UserVO(userId, username, avatarUrl, createdAt, updatedAt, deletedAt);
    }
}