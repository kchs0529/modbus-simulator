package com.drimsys.modbus.modbus;

import com.drimsys.modbus.config.ModbusProperties;
import com.drimsys.modbus.model.ModbusDataStore;
import com.fazecast.jSerialComm.SerialPort;
import com.ghgande.j2mod.modbus.ModbusException;
import com.ghgande.j2mod.modbus.procimg.*;
import com.ghgande.j2mod.modbus.slave.ModbusSlave;
import com.ghgande.j2mod.modbus.slave.ModbusSlaveFactory;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * TCP / RTU Slave 의 시작·중지·재시작을 런타임에 제어하는 매니저.
 *
 * 기존에 @ConditionalOnProperty 로 분리했던 두 Slave 서버를 이 클래스 하나로 통합한다.
 * 웹 UI에서 설정을 바꾼 뒤 restart 를 호출하면 변경된 포트/UnitId로 재기동된다.
 */
@Component
public class SlaveManager {

    private static final Logger log = LoggerFactory.getLogger(SlaveManager.class);

    private final ModbusProperties props;
    private final ModbusDataStore dataStore;

    // 현재 실행 중인 Slave 인스턴스 (null이면 중지 상태)
    private ModbusSlave tcpSlave;
    private ModbusSlave rtuSlave;

    // 상태 정보 (웹 UI에서 폴링)
    public enum SlaveStatus { STOPPED, RUNNING, ERROR }

    private final AtomicReference<SlaveStatus> tcpStatus = new AtomicReference<>(SlaveStatus.STOPPED);
    private final AtomicReference<SlaveStatus> rtuStatus = new AtomicReference<>(SlaveStatus.STOPPED);
    private String tcpError = null;
    private String rtuError = null;
    private LocalDateTime tcpStartedAt = null;
    private LocalDateTime rtuStartedAt = null;

    public SlaveManager(ModbusProperties props, ModbusDataStore dataStore) {
        this.props = props;
        this.dataStore = dataStore;
    }

    @PostConstruct
    public void init() {
        if (props.getTcp().isEnabled()) startTcp();
        if (props.getRtu().isEnabled()) startRtu();
    }

    @PreDestroy
    public void destroy() {
        stopTcp();
        stopRtu();
    }

    // ==============================
    // TCP
    // ==============================

    public synchronized void startTcp() {
        stopTcp();
        int port = props.getTcp().getPort();
        int unitId = props.getTcp().getUnitId();
        try {
            tcpSlave = ModbusSlaveFactory.createTCPSlave(port, 5);
            tcpSlave.addProcessImage(unitId, buildProcessImage());
            tcpSlave.open();
            tcpStatus.set(SlaveStatus.RUNNING);
            tcpStartedAt = LocalDateTime.now();
            tcpError = null;
            log.info("TCP Slave started - port={}, unitId={}", port, unitId);
        } catch (ModbusException e) {
            tcpStatus.set(SlaveStatus.ERROR);
            tcpError = e.getMessage();
            log.error("TCP Slave 시작 실패: {}", e.getMessage());
            stopTcp();
        }
    }

    public synchronized void stopTcp() {
        if (tcpSlave != null) {
            try {
                // ModbusSlaveFactory.close() 를 사용해 팩토리 레지스트리에서도 제거
                // slave.close() 를 직접 호출하면 UnsupportedOperationException 발생 시
                // 팩토리 내 등록이 남아 다음 createTCPSlave() 가 같은 객체를 반환하는 문제가 있음
                ModbusSlaveFactory.close(tcpSlave);
            } catch (UnsupportedOperationException e) {
                log.warn("TCP Slave 종료 중 경고 (j2mod/Java 21 호환성): {}", e.getMessage());
            } catch (Exception e) {
                log.warn("TCP Slave 종료 중 오류: {}", e.getMessage());
            } finally {
                tcpSlave = null;
            }
        }
        tcpStatus.set(SlaveStatus.STOPPED);
        tcpStartedAt = null;
        log.info("TCP Slave stopped");
    }

    public synchronized void restartTcp() {
        stopTcp();
        startTcp();
    }

    // ==============================
    // RTU
    // ==============================

    public synchronized void startRtu() {
        stopRtu();
        ModbusProperties.Rtu rtu = props.getRtu();
        try {
            com.ghgande.j2mod.modbus.util.SerialParameters sp =
                new com.ghgande.j2mod.modbus.util.SerialParameters();
            sp.setPortName(rtu.getPortName());
            sp.setBaudRate(rtu.getBaudRate());
            sp.setDatabits(rtu.getDataBits());
            sp.setStopbits(rtu.getStopBits());
            sp.setParity(rtu.getParity());

            rtuSlave = ModbusSlaveFactory.createSerialSlave(sp);
            rtuSlave.addProcessImage(rtu.getUnitId(), buildProcessImage());
            rtuSlave.open();
            rtuStatus.set(SlaveStatus.RUNNING);
            rtuStartedAt = LocalDateTime.now();
            rtuError = null;
            log.info("RTU Slave started - port={}, unitId={}", rtu.getPortName(), rtu.getUnitId());
        } catch (ModbusException e) {
            rtuStatus.set(SlaveStatus.ERROR);
            rtuError = e.getMessage();
            log.error("RTU Slave 시작 실패: {}", e.getMessage());
            stopRtu();
        }
    }

    public synchronized void stopRtu() {
        if (rtuSlave != null) {
            try {
                ModbusSlaveFactory.close(rtuSlave);
            } catch (UnsupportedOperationException e) {
                log.warn("RTU Slave 종료 중 경고 (j2mod/Java 21 호환성): {}", e.getMessage());
            } catch (Exception e) {
                log.warn("RTU Slave 종료 중 오류: {}", e.getMessage());
            } finally {
                rtuSlave = null;
            }
        }
        rtuStatus.set(SlaveStatus.STOPPED);
        rtuStartedAt = null;
        log.info("RTU Slave stopped");
    }

    public synchronized void restartRtu() {
        stopRtu();
        startRtu();
    }

    // ==============================
    // 상태 조회
    // ==============================

    public List<String> getAvailableSerialPorts() {
        return Arrays.stream(SerialPort.getCommPorts())
                .map(SerialPort::getSystemPortName)
                .collect(Collectors.toList());
    }

    public SlaveStatus getTcpStatus() { return tcpStatus.get(); }
    public SlaveStatus getRtuStatus() { return rtuStatus.get(); }
    public String getTcpError() { return tcpError; }
    public String getRtuError() { return rtuError; }
    public LocalDateTime getTcpStartedAt() { return tcpStartedAt; }
    public LocalDateTime getRtuStartedAt() { return rtuStartedAt; }

    // ==============================
    // ProcessImage 빌더 (DataStore 연동)
    // ==============================

    private SimpleProcessImage buildProcessImage() {
        SimpleProcessImage image = new SimpleProcessImage();

        int coilCount = dataStore.getAllCoils().length;
        for (int i = 0; i < coilCount; i++) {
            final int idx = i;
            image.addDigitalOut(new SimpleDigitalOut(false) {
                @Override public boolean isSet() { return dataStore.getCoil(idx); }
                @Override public void set(boolean b) { dataStore.setCoil(idx, b); }
            });
        }

        int diCount = dataStore.getAllDiscreteInputs().length;
        for (int i = 0; i < diCount; i++) {
            final int idx = i;
            image.addDigitalIn(new SimpleDigitalIn(false) {
                @Override public boolean isSet() { return dataStore.getDiscreteInput(idx); }
            });
        }

        // j2mod은 Modbus 응답 직렬화 시 getValue() 대신 toBytes()를 호출한다.
        // toBytes()도 함께 오버라이드하지 않으면 DataStore 값이 응답에 반영되지 않는다.
        int hrCount = dataStore.getAllHoldingRegisters().length;
        for (int i = 0; i < hrCount; i++) {
            final int idx = i;
            image.addRegister(new SimpleRegister(0) {
                @Override public int getValue() { return dataStore.getHoldingRegister(idx); }
                @Override public byte[] toBytes() { return toBeBytes(dataStore.getHoldingRegister(idx)); }
                @Override public void setValue(int v) { dataStore.setHoldingRegister(idx, v); }
                @Override public void setValue(short v) { dataStore.setHoldingRegister(idx, v & 0xFFFF); }
            });
        }

        int irCount = dataStore.getAllInputRegisters().length;
        for (int i = 0; i < irCount; i++) {
            final int idx = i;
            image.addInputRegister(new SimpleInputRegister(0) {
                @Override public int getValue() { return dataStore.getInputRegister(idx); }
                @Override public byte[] toBytes() { return toBeBytes(dataStore.getInputRegister(idx)); }
            });
        }

        return image;
    }

    /** 16비트 값을 Modbus Big-Endian 2바이트로 변환 */
    private static byte[] toBeBytes(int value) {
        return new byte[]{(byte) (value >> 8), (byte) (value & 0xFF)};
    }
}
