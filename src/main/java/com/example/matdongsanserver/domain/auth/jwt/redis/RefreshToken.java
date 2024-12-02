package com.example.matdongsanserver.domain.auth.jwt.redis;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
@RedisHash(value = "refresh", timeToLive = 86400)
public class RefreshToken {

    private Long id;    // memberId
    private String email;   // memberEmail
    private Collection<? extends GrantedAuthority> authorities;

    @Indexed
    private String refreshToken;

    public String getAuthority() {
        return authorities.stream()
                .map(authority -> new SimpleGrantedAuthority(authority.getAuthority()))
                .toList()
                .get(0)
                .getAuthority();
    }
}
