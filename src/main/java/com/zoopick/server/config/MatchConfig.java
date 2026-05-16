package com.zoopick.server.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "zoopick.match")
@Getter
@Setter
public class MatchConfig {
    private float similarityThreshold; // 코사인 유사도 임계값
    private float colorBonus; // 색깔 같으면 가산점
}
