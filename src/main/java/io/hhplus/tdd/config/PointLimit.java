package io.hhplus.tdd.config;

import org.springframework.boot.context.properties.ConfigurationProperties;


@ConfigurationProperties(prefix = "point-limit")
public record PointLimit(Long max, Long min) {
}
