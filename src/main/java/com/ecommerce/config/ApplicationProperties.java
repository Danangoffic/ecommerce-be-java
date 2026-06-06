package com.ecommerce.config;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "app")
public class ApplicationProperties {

    private final Jwt jwt = new Jwt();
    private final Pagination pagination = new Pagination();
    private final Storage storage = new Storage();

    @Getter
    @Setter
    public static class Jwt {

        private String secret;
        private Duration accessTokenExpiration = Duration.ofHours(2);
        private Duration refreshTokenExpiration = Duration.ofDays(30);
    }

    @Getter
    @Setter
    public static class Pagination {

        @Min(1)
        private int defaultPageSize = 10;

        @Min(1)
        @Max(1000)
        private int maxPageSize = 100;
    }

    @Getter
    @Setter
    public static class Storage {

        private String productImagesDir = "uploads/products";
    }
}
