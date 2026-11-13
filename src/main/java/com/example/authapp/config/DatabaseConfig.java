package com.example.authapp.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;

@Configuration
public class DatabaseConfig {

    private static final Logger log = LoggerFactory.getLogger(DatabaseConfig.class);

    @Bean
    @Primary
    public DataSource dataSource(Environment env) {
        String dbUrl = env.getProperty("DATABASE_URL");
        if (dbUrl == null || dbUrl.trim().isEmpty()) {
            dbUrl = env.getProperty("SPRING_DATASOURCE_URL");
        }

        if (dbUrl != null && !dbUrl.trim().isEmpty()) {
            String raw = dbUrl.trim();
            log.info(">>> Phát hiện cấu hình Database đám mây: {}", maskUrl(raw));

            try {
                if (raw.startsWith("postgres://") || raw.startsWith("postgresql://")) {
                    String withoutScheme = raw.replaceFirst("^postgres(ql)?://", "");
                    int atIdx = withoutScheme.indexOf('@');
                    String userInfo = atIdx != -1 ? withoutScheme.substring(0, atIdx) : "";
                    String hostAndDb = atIdx != -1 ? withoutScheme.substring(atIdx + 1) : withoutScheme;

                    String username = "";
                    String password = "";
                    if (userInfo.contains(":")) {
                        int colonIdx = userInfo.indexOf(':');
                        username = java.net.URLDecoder.decode(userInfo.substring(0, colonIdx), StandardCharsets.UTF_8);
                        password = java.net.URLDecoder.decode(userInfo.substring(colonIdx + 1), StandardCharsets.UTF_8);
                    } else {
                        username = userInfo;
                    }

                    String hostPort = hostAndDb;
                    String dbName = "";
                    int slashIdx = hostAndDb.indexOf('/');
                    if (slashIdx != -1) {
                        hostPort = hostAndDb.substring(0, slashIdx);
                        dbName = hostAndDb.substring(slashIdx + 1);
                        if (dbName.contains("?")) {
                            dbName = dbName.substring(0, dbName.indexOf('?'));
                        }
                    }

                    // Render PostgreSQL yêu cầu kết nối có SSL (sslmode=require)
                    String jdbcUrl = "jdbc:postgresql://" + hostPort + "/" + dbName + "?sslmode=require";
                    log.info(">>> Đang kết nối tới PostgreSQL (Render): jdbc:postgresql://{}/{}", hostPort, dbName);

                    return DataSourceBuilder.create()
                            .url(jdbcUrl)
                            .username(username)
                            .password(password)
                            .driverClassName("org.postgresql.Driver")
                            .build();

                } else if (raw.startsWith("mysql://")) {
                    String withoutScheme = raw.replaceFirst("^mysql://", "");
                    int atIdx = withoutScheme.indexOf('@');
                    String userInfo = atIdx != -1 ? withoutScheme.substring(0, atIdx) : "";
                    String hostAndDb = atIdx != -1 ? withoutScheme.substring(atIdx + 1) : withoutScheme;

                    String username = "";
                    String password = "";
                    if (userInfo.contains(":")) {
                        int colonIdx = userInfo.indexOf(':');
                        username = java.net.URLDecoder.decode(userInfo.substring(0, colonIdx), StandardCharsets.UTF_8);
                        password = java.net.URLDecoder.decode(userInfo.substring(colonIdx + 1), StandardCharsets.UTF_8);
                    } else {
                        username = userInfo;
                    }

                    String sep = hostAndDb.contains("?") ? "&" : "?";
                    String jdbcUrl = "jdbc:mysql://" + hostAndDb + sep + "useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Ho_Chi_Minh&characterEncoding=UTF-8";
                    log.info(">>> Đang kết nối tới MySQL Cloud Database: {}", jdbcUrl);

                    return DataSourceBuilder.create()
                            .url(jdbcUrl)
                            .username(username)
                            .password(password)
                            .driverClassName("com.mysql.cj.jdbc.Driver")
                            .build();

                } else if (raw.startsWith("jdbc:")) {
                    String user = env.getProperty("spring.datasource.username", env.getProperty("DB_USER", ""));
                    String pass = env.getProperty("spring.datasource.password", env.getProperty("DB_PASSWORD", ""));
                    log.info(">>> Kết nối trực tiếp qua JDBC URL: {}", raw);
                    return DataSourceBuilder.create()
                            .url(raw)
                            .username(user)
                            .password(pass)
                            .build();
                }
            } catch (Exception e) {
                log.error("Lỗi khi kết nối Database Cloud theo DATABASE_URL: ", e);
                throw new IllegalStateException("Không thể kết nối đến Database đám mây: " + e.getMessage(), e);
            }
        }

        // Mặc định: Dùng H2 file database cố định tại ./data/appdb
        String standardUrl = env.getProperty("spring.datasource.url", "jdbc:h2:file:./data/appdb;DB_CLOSE_DELAY=-1");
        String standardUser = env.getProperty("spring.datasource.username", "sa");
        String standardPass = env.getProperty("spring.datasource.password", "");
        String driver = env.getProperty("spring.datasource.driverClassName", "org.h2.Driver");

        log.info(">>> Sử dụng Database mặc định: {}", standardUrl);
        return DataSourceBuilder.create()
                .url(standardUrl)
                .username(standardUser)
                .password(standardPass)
                .driverClassName(driver)
                .build();
    }

    private String maskUrl(String url) {
        if (url == null) return "";
        return url.replaceAll("://([^:]+):([^@]+)@", "://$1:****@");
    }
}
