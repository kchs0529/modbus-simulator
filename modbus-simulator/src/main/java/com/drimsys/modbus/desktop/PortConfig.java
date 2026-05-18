package com.drimsys.modbus.desktop;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.server.ConfigurableWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.ServerSocket;

@Component
public class PortConfig implements WebServerFactoryCustomizer<ConfigurableWebServerFactory> {

    private static final Logger log = LoggerFactory.getLogger(PortConfig.class);

    private final Environment env;

    public PortConfig(Environment env) {
        this.env = env;
    }

    @Override
    public void customize(ConfigurableWebServerFactory factory) {
        int base = Integer.parseInt(env.getProperty("server.port", "8080"));
        int port = findAvailablePort(base);
        if (port != base) {
            log.info("포트 {} 사용 중 → {} 포트로 시작합니다", base, port);
        }
        factory.setPort(port);
    }

    private int findAvailablePort(int start) {
        for (int port = start; port < start + 10; port++) {
            try (ServerSocket ss = new ServerSocket(port)) {
                return port;
            } catch (IOException ignored) {}
        }
        return start;
    }
}