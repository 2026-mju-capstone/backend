package com.zoopick.server.repository;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@Component
@RequiredArgsConstructor
@NullMarked
public class QrCodeCacheRepository {
    private static final long QR_CODE_TTL = 3;
    private static final String QR_CODE_KEY_PREFIX = "qrCode:";

    private final RedisTemplate<String, byte[]> redisTemplate;

    private static String qrCodeKey(String key) {
        return QR_CODE_KEY_PREFIX + key;
    }

    public Optional<byte[]> get(String key) {
        return Optional.ofNullable(
                redisTemplate.opsForValue()
                        .get(qrCodeKey(key))
        );
    }

    public byte[] computeIfAbsent(String key, Function<String, byte[]> generator) {
        return get(key).orElseGet(() -> {
            byte[] bytes = generator.apply(key);
            redisTemplate.opsForValue().set(
                    qrCodeKey(key),
                    bytes,
                    QR_CODE_TTL,
                    TimeUnit.MINUTES
            );
            return bytes;
        });
    }
}
