package com.drimsys.modbus;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ModbusSimulatorApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(ModbusSimulatorApplication.class);
        if (Boolean.getBoolean("modbus.desktop")) {
            app.setHeadless(false);
        }
        app.run(args);
    }
}
