package com.drimsys.modbus.controller;

import com.drimsys.modbus.config.ModbusProperties;
import com.drimsys.modbus.modbus.SlaveManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 설정 관리 및 Slave 제어 REST API.
 *
 * GET  /api/config              - 현재 설정 + 상태 조회
 * PUT  /api/config/tcp          - TCP 설정 변경 (재시작 필요)
 * PUT  /api/config/rtu          - RTU 설정 변경 (재시작 필요)
 * POST /api/config/tcp/start    - TCP Slave 시작
 * POST /api/config/tcp/stop     - TCP Slave 중지
 * POST /api/config/tcp/restart  - TCP Slave 재시작
 * POST /api/config/rtu/start    - RTU Slave 시작
 * POST /api/config/rtu/stop     - RTU Slave 중지
 * POST /api/config/rtu/restart  - RTU Slave 재시작
 */
@RestController
@RequestMapping("/api/config")
public class ConfigController {

    private final ModbusProperties props;
    private final SlaveManager slaveManager;

    public ConfigController(ModbusProperties props, SlaveManager slaveManager) {
        this.props = props;
        this.slaveManager = slaveManager;
    }

    // ---- 전체 설정 + 상태 조회 ----

    @GetMapping
    public ResponseEntity<Map<String, Object>> getConfig() {
        return ResponseEntity.ok(buildConfigResponse());
    }

    // ---- TCP 설정 변경 ----

    @PutMapping("/tcp")
    public ResponseEntity<Map<String, Object>> updateTcpConfig(@RequestBody Map<String, Object> body) {
        ModbusProperties.Tcp tcp = props.getTcp();

        if (body.containsKey("enabled")) {
            tcp.setEnabled(Boolean.parseBoolean(body.get("enabled").toString()));
        }
        if (body.containsKey("port")) {
            int port = Integer.parseInt(body.get("port").toString());
            if (port < 1 || port > 65535) throw new IllegalArgumentException("포트 범위: 1~65535");
            tcp.setPort(port);
        }
        if (body.containsKey("unitId")) {
            int unitId = Integer.parseInt(body.get("unitId").toString());
            if (unitId < 1 || unitId > 247) throw new IllegalArgumentException("Unit ID 범위: 1~247");
            tcp.setUnitId(unitId);
        }

        return ResponseEntity.ok(buildConfigResponse());
    }

    // ---- RTU 설정 변경 ----

    @PutMapping("/rtu")
    public ResponseEntity<Map<String, Object>> updateRtuConfig(@RequestBody Map<String, Object> body) {
        ModbusProperties.Rtu rtu = props.getRtu();

        if (body.containsKey("enabled"))  rtu.setEnabled(Boolean.parseBoolean(body.get("enabled").toString()));
        if (body.containsKey("portName")) rtu.setPortName(body.get("portName").toString());
        if (body.containsKey("baudRate")) rtu.setBaudRate(Integer.parseInt(body.get("baudRate").toString()));
        if (body.containsKey("dataBits")) rtu.setDataBits(Integer.parseInt(body.get("dataBits").toString()));
        if (body.containsKey("stopBits")) rtu.setStopBits(Integer.parseInt(body.get("stopBits").toString()));
        if (body.containsKey("parity"))   rtu.setParity(Integer.parseInt(body.get("parity").toString()));
        if (body.containsKey("unitId")) {
            int unitId = Integer.parseInt(body.get("unitId").toString());
            if (unitId < 1 || unitId > 247) throw new IllegalArgumentException("Unit ID 범위: 1~247");
            rtu.setUnitId(unitId);
        }

        return ResponseEntity.ok(buildConfigResponse());
    }

    // ---- 시리얼 포트 목록 ----

    @GetMapping("/serial-ports")
    public ResponseEntity<Map<String, Object>> getSerialPorts() {
        return ResponseEntity.ok(Map.of("ports", slaveManager.getAvailableSerialPorts()));
    }

    // ---- TCP Slave 제어 ----

    @PostMapping("/tcp/start")
    public ResponseEntity<Map<String, Object>> tcpStart() {
        slaveManager.startTcp();
        return ResponseEntity.ok(buildConfigResponse());
    }

    @PostMapping("/tcp/stop")
    public ResponseEntity<Map<String, Object>> tcpStop() {
        slaveManager.stopTcp();
        return ResponseEntity.ok(buildConfigResponse());
    }

    @PostMapping("/tcp/restart")
    public ResponseEntity<Map<String, Object>> tcpRestart() {
        slaveManager.restartTcp();
        return ResponseEntity.ok(buildConfigResponse());
    }

    // ---- RTU Slave 제어 ----

    @PostMapping("/rtu/start")
    public ResponseEntity<Map<String, Object>> rtuStart() {
        slaveManager.startRtu();
        return ResponseEntity.ok(buildConfigResponse());
    }

    @PostMapping("/rtu/stop")
    public ResponseEntity<Map<String, Object>> rtuStop() {
        slaveManager.stopRtu();
        return ResponseEntity.ok(buildConfigResponse());
    }

    @PostMapping("/rtu/restart")
    public ResponseEntity<Map<String, Object>> rtuRestart() {
        slaveManager.restartRtu();
        return ResponseEntity.ok(buildConfigResponse());
    }

    // ---- 에러 핸들러 ----

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }

    // ---- 응답 포맷 ----

    private Map<String, Object> buildConfigResponse() {
        Map<String, Object> result = new LinkedHashMap<>();

        // TCP 설정
        Map<String, Object> tcp = new LinkedHashMap<>();
        tcp.put("enabled", props.getTcp().isEnabled());
        tcp.put("port", props.getTcp().getPort());
        tcp.put("unitId", props.getTcp().getUnitId());
        tcp.put("status", slaveManager.getTcpStatus().name());
        tcp.put("startedAt", slaveManager.getTcpStartedAt());
        tcp.put("error", slaveManager.getTcpError());
        result.put("tcp", tcp);

        // RTU 설정
        Map<String, Object> rtu = new LinkedHashMap<>();
        rtu.put("enabled", props.getRtu().isEnabled());
        rtu.put("portName", props.getRtu().getPortName());
        rtu.put("baudRate", props.getRtu().getBaudRate());
        rtu.put("dataBits", props.getRtu().getDataBits());
        rtu.put("stopBits", props.getRtu().getStopBits());
        rtu.put("parity", props.getRtu().getParity());
        rtu.put("unitId", props.getRtu().getUnitId());
        rtu.put("status", slaveManager.getRtuStatus().name());
        rtu.put("startedAt", slaveManager.getRtuStartedAt());
        rtu.put("error", slaveManager.getRtuError());
        result.put("rtu", rtu);

        // 레지스터 크기 정보
        Map<String, Object> reg = new LinkedHashMap<>();
        reg.put("coilCount", props.getRegister().getCoilCount());
        reg.put("discreteInputCount", props.getRegister().getDiscreteInputCount());
        reg.put("holdingRegisterCount", props.getRegister().getHoldingRegisterCount());
        reg.put("inputRegisterCount", props.getRegister().getInputRegisterCount());
        result.put("register", reg);

        result.put("serverTime", LocalDateTime.now());
        return result;
    }
}
