package com.drimsys.modbus.model;

import com.drimsys.modbus.config.ModbusProperties;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Modbus 4가지 데이터 영역을 인메모리로 관리하는 저장소.
 *
 * FC01 - Coil (읽기/쓰기, 1bit)
 * FC02 - Discrete Input (읽기 전용, 1bit)
 * FC03 - Holding Register (읽기/쓰기, 16bit)
 * FC04 - Input Register (읽기 전용, 16bit)
 *
 * 멀티스레드 환경(Modbus 요청 + REST API 동시 접근)에서 안전하게 동작하도록
 * ReadWriteLock을 사용한다.
 */
@Component
public class ModbusDataStore {

    private final boolean[] coils;
    private final boolean[] discreteInputs;
    private final int[] holdingRegisters;
    private final int[] inputRegisters;

    // 읽기는 동시에 여러 스레드, 쓰기는 단독 접근
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    public ModbusDataStore(ModbusProperties props) {
        ModbusProperties.Register reg = props.getRegister();
        this.coils             = new boolean[reg.getCoilCount()];
        this.discreteInputs    = new boolean[reg.getDiscreteInputCount()];
        this.holdingRegisters  = new int[reg.getHoldingRegisterCount()];
        this.inputRegisters    = new int[reg.getInputRegisterCount()];
    }

    // ==============================
    // Coil (FC01/FC05/FC0F)
    // ==============================
    public boolean getCoil(int address) {
        lock.readLock().lock();
        try {
            checkBounds(address, coils.length, "Coil");
            return coils[address];
        } finally {
            lock.readLock().unlock();
        }
    }

    public boolean[] getCoils(int startAddress, int count) {
        lock.readLock().lock();
        try {
            checkBounds(startAddress + count - 1, coils.length, "Coil");
            return Arrays.copyOfRange(coils, startAddress, startAddress + count);
        } finally {
            lock.readLock().unlock();
        }
    }

    public void setCoil(int address, boolean value) {
        lock.writeLock().lock();
        try {
            checkBounds(address, coils.length, "Coil");
            coils[address] = value;
        } finally {
            lock.writeLock().unlock();
        }
    }

    // ==============================
    // Discrete Input (FC02)
    // ==============================
    public boolean getDiscreteInput(int address) {
        lock.readLock().lock();
        try {
            checkBounds(address, discreteInputs.length, "DiscreteInput");
            return discreteInputs[address];
        } finally {
            lock.readLock().unlock();
        }
    }

    public boolean[] getDiscreteInputs(int startAddress, int count) {
        lock.readLock().lock();
        try {
            checkBounds(startAddress + count - 1, discreteInputs.length, "DiscreteInput");
            return Arrays.copyOfRange(discreteInputs, startAddress, startAddress + count);
        } finally {
            lock.readLock().unlock();
        }
    }

    public void setDiscreteInput(int address, boolean value) {
        lock.writeLock().lock();
        try {
            checkBounds(address, discreteInputs.length, "DiscreteInput");
            discreteInputs[address] = value;
        } finally {
            lock.writeLock().unlock();
        }
    }

    // ==============================
    // Holding Register (FC03/FC06/FC10)
    // ==============================
    public int getHoldingRegister(int address) {
        lock.readLock().lock();
        try {
            checkBounds(address, holdingRegisters.length, "HoldingRegister");
            return holdingRegisters[address];
        } finally {
            lock.readLock().unlock();
        }
    }

    public int[] getHoldingRegisters(int startAddress, int count) {
        lock.readLock().lock();
        try {
            checkBounds(startAddress + count - 1, holdingRegisters.length, "HoldingRegister");
            return Arrays.copyOfRange(holdingRegisters, startAddress, startAddress + count);
        } finally {
            lock.readLock().unlock();
        }
    }

    public void setHoldingRegister(int address, int value) {
        lock.writeLock().lock();
        try {
            checkBounds(address, holdingRegisters.length, "HoldingRegister");
            // Modbus 레지스터는 0~65535 (unsigned 16bit)
            holdingRegisters[address] = value & 0xFFFF;
        } finally {
            lock.writeLock().unlock();
        }
    }

    // ==============================
    // Input Register (FC04)
    // ==============================
    public int getInputRegister(int address) {
        lock.readLock().lock();
        try {
            checkBounds(address, inputRegisters.length, "InputRegister");
            return inputRegisters[address];
        } finally {
            lock.readLock().unlock();
        }
    }

    public int[] getInputRegisters(int startAddress, int count) {
        lock.readLock().lock();
        try {
            checkBounds(startAddress + count - 1, inputRegisters.length, "InputRegister");
            return Arrays.copyOfRange(inputRegisters, startAddress, startAddress + count);
        } finally {
            lock.readLock().unlock();
        }
    }

    public void setInputRegister(int address, int value) {
        lock.writeLock().lock();
        try {
            checkBounds(address, inputRegisters.length, "InputRegister");
            inputRegisters[address] = value & 0xFFFF;
        } finally {
            lock.writeLock().unlock();
        }
    }

    // ==============================
    // 전체 덤프 (REST API 조회용)
    // ==============================
    public boolean[] getAllCoils() {
        lock.readLock().lock();
        try { return Arrays.copyOf(coils, coils.length); }
        finally { lock.readLock().unlock(); }
    }

    public boolean[] getAllDiscreteInputs() {
        lock.readLock().lock();
        try { return Arrays.copyOf(discreteInputs, discreteInputs.length); }
        finally { lock.readLock().unlock(); }
    }

    public int[] getAllHoldingRegisters() {
        lock.readLock().lock();
        try { return Arrays.copyOf(holdingRegisters, holdingRegisters.length); }
        finally { lock.readLock().unlock(); }
    }

    public int[] getAllInputRegisters() {
        lock.readLock().lock();
        try { return Arrays.copyOf(inputRegisters, inputRegisters.length); }
        finally { lock.readLock().unlock(); }
    }

    // ==============================
    // 범위 체크
    // ==============================
    private void checkBounds(int address, int length, String type) {
        if (address < 0 || address >= length) {
            throw new IllegalArgumentException(
                type + " address " + address + " out of range [0, " + (length - 1) + "]"
            );
        }
    }
}
