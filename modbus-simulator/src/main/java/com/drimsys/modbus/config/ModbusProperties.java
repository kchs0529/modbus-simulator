package com.drimsys.modbus.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "modbus")
public class ModbusProperties {

    private Tcp tcp = new Tcp();
    private Rtu rtu = new Rtu();
    private Register register = new Register();

    // ---- TCP ----
    public static class Tcp {
        private boolean enabled = true;
        private int port = 5020;
        private int unitId = 1;

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public int getPort() { return port; }
        public void setPort(int port) { this.port = port; }
        public int getUnitId() { return unitId; }
        public void setUnitId(int unitId) { this.unitId = unitId; }
    }

    // ---- RTU ----
    public static class Rtu {
        private boolean enabled = false;
        private String portName = "/dev/ttyUSB0";
        private int baudRate = 9600;
        private int dataBits = 8;
        private int stopBits = 1;
        private int parity = 0;
        private int unitId = 1;

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public String getPortName() { return portName; }
        public void setPortName(String portName) { this.portName = portName; }
        public int getBaudRate() { return baudRate; }
        public void setBaudRate(int baudRate) { this.baudRate = baudRate; }
        public int getDataBits() { return dataBits; }
        public void setDataBits(int dataBits) { this.dataBits = dataBits; }
        public int getStopBits() { return stopBits; }
        public void setStopBits(int stopBits) { this.stopBits = stopBits; }
        public int getParity() { return parity; }
        public void setParity(int parity) { this.parity = parity; }
        public int getUnitId() { return unitId; }
        public void setUnitId(int unitId) { this.unitId = unitId; }
    }

    // ---- Register 크기 ----
    public static class Register {
        private int coilCount = 100;
        private int discreteInputCount = 100;
        private int holdingRegisterCount = 100;
        private int inputRegisterCount = 100;

        public int getCoilCount() { return coilCount; }
        public void setCoilCount(int coilCount) { this.coilCount = coilCount; }
        public int getDiscreteInputCount() { return discreteInputCount; }
        public void setDiscreteInputCount(int c) { this.discreteInputCount = c; }
        public int getHoldingRegisterCount() { return holdingRegisterCount; }
        public void setHoldingRegisterCount(int c) { this.holdingRegisterCount = c; }
        public int getInputRegisterCount() { return inputRegisterCount; }
        public void setInputRegisterCount(int c) { this.inputRegisterCount = c; }
    }

    public Tcp getTcp() { return tcp; }
    public void setTcp(Tcp tcp) { this.tcp = tcp; }
    public Rtu getRtu() { return rtu; }
    public void setRtu(Rtu rtu) { this.rtu = rtu; }
    public Register getRegister() { return register; }
    public void setRegister(Register register) { this.register = register; }
}
