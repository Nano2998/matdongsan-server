package com.example.matdongsanserver.domain.auth.jwt.redis;

import org.springframework.data.repository.CrudRepository;

public interface RefreshTokenRepository extends CrudRepository<RefreshToken, Long> {
    RefreshToken findByRefreshToken(String refreshToken);
}
