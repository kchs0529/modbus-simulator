package com.drimsys.modbus.service;

import com.drimsys.modbus.model.ModbusDataStore;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * REST API Controller와 DataStore 사이의 서비스 레이어.
 * 유효성 검사, 응답 포맷 구성 등을 담당한다.
 */
@Service
public class ModbusSimulatorService {

    private final ModbusDataStore dataStore;

    public ModbusSimulatorService(ModbusDataStore dataStore) {
        this.dataStore = dataStore;
    }

    // ==============================
    // 전체 조회
    // ==============================

    public Map<String, Object> getAllRegisters() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("coils", toIntArray(dataStore.getAllCoils()));
        result.put("discreteInputs", toIntArray(dataStore.getAllDiscreteInputs()));
        result.put("holdingRegisters", dataStore.getAllHoldingRegisters());
        result.put("inputRegisters", dataStore.getAllInputRegisters());
        return result;
    }

    // ==============================
    // Coil
    // ==============================

    public Map<String, Object> getCoils(int start, int count) {
        boolean[] values = dataStore.getCoils(start, count);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("startAddress", start);
        result.put("count", count);
        result.put("values", toIntArray(values));
        return result;
    }

    public void setCoil(int address, boolean value) {
        dataStore.setCoil(address, value);
    }

    // ==============================
    // Discrete Input
    // ==============================

    public Map<String, Object> getDiscreteInputs(int start, int count) {
        boolean[] values = dataStore.getDiscreteInputs(start, count);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("startAddress", start);
        result.put("count", count);
        result.put("values", toIntArray(values));
        return result;
    }

    public void setDiscreteInput(int address, boolean value) {
        dataStore.setDiscreteInput(address, value);
    }

    // ==============================
    // Holding Register
    // ==============================

    public Map<String, Object> getHoldingRegisters(int start, int count) {
        int[] values = dataStore.getHoldingRegisters(start, count);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("startAddress", start);
        result.put("count", count);
        result.put("values", values);
        return result;
    }

    public void setHoldingRegister(int address, int value) {
        if (value < 0 || value > 65535) {
            throw new IllegalArgumentException("Holding Register 값은 0~65535 범위여야 합니다. 입력값: " + value);
        }
        dataStore.setHoldingRegister(address, value);
    }

    // ==============================
    // Input Register
    // ==============================

    public Map<String, Object> getInputRegisters(int start, int count) {
        int[] values = dataStore.getInputRegisters(start, count);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("startAddress", start);
        result.put("count", count);
        result.put("values", values);
        return result;
    }

    public void setInputRegister(int address, int value) {
        if (value < 0 || value > 65535) {
            throw new IllegalArgumentException("Input Register 값은 0~65535 범위여야 합니다. 입력값: " + value);
        }
        dataStore.setInputRegister(address, value);
    }

    // ==============================
    // 유틸
    // ==============================

    private int[] toIntArray(boolean[] bools) {
        int[] arr = new int[bools.length];
        for (int i = 0; i < bools.length; i++) {
            arr[i] = bools[i] ? 1 : 0;
        }
        return arr;
    }
}
