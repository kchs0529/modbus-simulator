package com.drimsys.modbus.controller;

import com.drimsys.modbus.service.ModbusSimulatorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Modbus 시뮬레이터 REST API.
 *
 * 레지스터 조회/변경을 통해 Modbus Master가 읽을 값을 동적으로 제어한다.
 *
 * [조회]
 *   GET /api/registers                           - 전체 덤프
 *   GET /api/registers/coils?start=0&count=10    - Coil 범위 조회
 *   GET /api/registers/discrete-inputs?start=0&count=10
 *   GET /api/registers/holding?start=0&count=10
 *   GET /api/registers/input?start=0&count=10
 *
 * [변경]
 *   PUT /api/registers/coils/{address}           - body: {"value": true}
 *   PUT /api/registers/discrete-inputs/{address} - body: {"value": false}
 *   PUT /api/registers/holding/{address}         - body: {"value": 1234}
 *   PUT /api/registers/input/{address}           - body: {"value": 5678}
 */
@RestController
@RequestMapping("/api/registers")
public class ModbusController {

    private final ModbusSimulatorService service;

    public ModbusController(ModbusSimulatorService service) {
        this.service = service;
    }

    // ---- 전체 조회 ----

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllRegisters() {
        return ResponseEntity.ok(service.getAllRegisters());
    }

    // ---- Coil (FC01) ----

    @GetMapping("/coils")
    public ResponseEntity<Map<String, Object>> getCoils(
            @RequestParam(defaultValue = "0") int start,
            @RequestParam(defaultValue = "10") int count) {
        return ResponseEntity.ok(service.getCoils(start, count));
    }

    @PutMapping("/coils/{address}")
    public ResponseEntity<String> setCoil(
            @PathVariable int address,
            @RequestBody Map<String, Object> body) {
        boolean value = Boolean.parseBoolean(body.get("value").toString());
        service.setCoil(address, value);
        return ResponseEntity.ok("Coil[" + address + "] = " + value);
    }

    // ---- Discrete Input (FC02) ----

    @GetMapping("/discrete-inputs")
    public ResponseEntity<Map<String, Object>> getDiscreteInputs(
            @RequestParam(defaultValue = "0") int start,
            @RequestParam(defaultValue = "10") int count) {
        return ResponseEntity.ok(service.getDiscreteInputs(start, count));
    }

    @PutMapping("/discrete-inputs/{address}")
    public ResponseEntity<String> setDiscreteInput(
            @PathVariable int address,
            @RequestBody Map<String, Object> body) {
        boolean value = Boolean.parseBoolean(body.get("value").toString());
        service.setDiscreteInput(address, value);
        return ResponseEntity.ok("DiscreteInput[" + address + "] = " + value);
    }

    // ---- Holding Register (FC03) ----

    @GetMapping("/holding")
    public ResponseEntity<Map<String, Object>> getHoldingRegisters(
            @RequestParam(defaultValue = "0") int start,
            @RequestParam(defaultValue = "10") int count) {
        return ResponseEntity.ok(service.getHoldingRegisters(start, count));
    }

    @PutMapping("/holding/{address}")
    public ResponseEntity<String> setHoldingRegister(
            @PathVariable int address,
            @RequestBody Map<String, Object> body) {
        int value = Integer.parseInt(body.get("value").toString());
        service.setHoldingRegister(address, value);
        return ResponseEntity.ok("HoldingRegister[" + address + "] = " + value);
    }

    // ---- Input Register (FC04) ----

    @GetMapping("/input")
    public ResponseEntity<Map<String, Object>> getInputRegisters(
            @RequestParam(defaultValue = "0") int start,
            @RequestParam(defaultValue = "10") int count) {
        return ResponseEntity.ok(service.getInputRegisters(start, count));
    }

    @PutMapping("/input/{address}")
    public ResponseEntity<String> setInputRegister(
            @PathVariable int address,
            @RequestBody Map<String, Object> body) {
        int value = Integer.parseInt(body.get("value").toString());
        service.setInputRegister(address, value);
        return ResponseEntity.ok("InputRegister[" + address + "] = " + value);
    }

    // ---- 에러 핸들링 ----

    @ExceptionHandler({IllegalArgumentException.class, ArrayIndexOutOfBoundsException.class})
    public ResponseEntity<Map<String, String>> handleBadRequest(Exception e) {
        return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }
}
