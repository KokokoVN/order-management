package com.example.authapp.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;
import java.net.URI;

@Configuration
public class DatabaseConfig {

    private static final Logger log = LoggerFactory.getLogger(DatabaseConfig.class);

    @Bean
    @Primary
    public DataSource dataSource(Environment env) {
        // Hỗ trợ tự động nhận diện DATABASE_URL từ Render.com / Railway.app / Heroku
        String dbUrl = env.getProperty("DATABASE_URL");
        if (dbUrl != null && !dbUrl.trim().isEmpty()) {
            try {
                log.info(">>> Phát hiện biến môi trường DATABASE_URL từ Cloud. Đang tự động cấu hình DataSource...");
                URI uri = new URI(dbUrl.trim());
                String userInfo = uri.getUserInfo();
                String username = "";
                String password = "";
                if (userInfo != null && userInfo.contains(":")) {
                    String[] parts = userInfo.split(":", 2);
                    username = parts[0];
                    password = parts[1];
                }

                String scheme = uri.getScheme();
                String jdbcUrl;
                String driverClassName;

                if ("postgres".equalsIgnoreCase(scheme) || "postgresql".equalsIgnoreCase(scheme)) {
                    int port = uri.getPort() != -1 ? uri.getPort() : 5432;
                    jdbcUrl = "jdbc:postgresql://" + uri.getHost() + ":" + port + uri.getPath();
                    driverClassName = "org.postgresql.Driver";
                    log.info(">>> Đã cấu hình kết nối tới PostgreSQL Cloud Database: {}:{}", uri.getHost(), port);
                } else if ("mysql".equalsIgnoreCase(scheme)) {
                    int port = uri.getPort() != -1 ? uri.getPort() : 3306;
                    jdbcUrl = "jdbc:mysql://" + uri.getHost() + ":" + port + uri.getPath() + "?useSSL=false&serverTimezone=Asia/Ho_Chi_Minh&allowPublicKeyRetrieval=true";
                    driverClassName = "com.mysql.cj.jdbc.Driver";
                    log.info(">>> Đã cấu hình kết nối tới MySQL Cloud Database: {}:{}", uri.getHost(), port);
                } else {
                    jdbcUrl = dbUrl;
                    driverClassName = null;
                }

                DataSourceBuilder<?> builder = DataSourceBuilder.create()
                        .url(jdbcUrl)
                        .username(username)
                        .password(password);
                if (driverClassName != null) {
                    builder.driverClassName(driverClassName);
                }
                return builder.build();
            } catch (Exception e) {
                log.error("Lỗi khi phân tích DATABASE_URL, chuyển sang cấu hình dự phòng: ", e);
            }
        }

        // Mặc định: Dùng cấu hình trong application.properties (H2 file cho local hoặc MySQL)
        String standardUrl = env.getProperty("spring.datasource.url", "jdbc:h2:file:./data/appdb;DB_CLOSE_DELAY=-1");
        String standardUser = env.getProperty("spring.datasource.username", "sa");
        String standardPass = env.getProperty("spring.datasource.password", "");
        String driver = env.getProperty("spring.datasource.driverClassName", "org.h2.Driver");

        log.info(">>> Khởi tạo DataSource mặc định: {}", standardUrl);
        return DataSourceBuilder.create()
                .url(standardUrl)
                .username(standardUser)
                .password(standardPass)
                .driverClassName(driver)
                .build();
    }
}
