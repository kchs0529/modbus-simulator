# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

---

## 프로젝트 개요

Modbus TCP/RTU **Slave(장비) 시뮬레이터**.
실제 장비 없이 Modbus Master(PLC, HMI, SCADA 등)의 통신 테스트를 목적으로 한다.

- Spring Boot REST API로 레지스터 값을 동적으로 조작
- React 웹 UI로 설정 관리 및 레지스터 편집
- j2mod 라이브러리로 Modbus TCP/RTU 프로토콜 처리

---

## 모노레포 구조

```
/
├── modbus-simulator/   # Spring Boot 백엔드 (Java 21, Gradle)
└── modbus-ui/          # React 프론트엔드 (Vite)
```

---

## 빌드 / 실행 명령

### 백엔드 (modbus-simulator/)

```bash
# 개발 실행 (Spring Boot 자동 재시작)
./gradlew bootRun

# 테스트
./gradlew test

# 패키징 (테스트 제외)
./gradlew clean build -x test
java -jar build/libs/modbus-simulator-1.0.0.jar
```

> Windows PowerShell에서는 `.\gradlew` 또는 `gradlew.bat` 사용

### 프론트엔드 (modbus-ui/)

```bash
npm install
npm run dev      # 개발 서버 → http://localhost:3000
npm run build    # Spring Boot static 폴더로 출력 (아래 참고)
```

`npm run build` 결과물은 `modbus-simulator/src/main/resources/static/`에 자동 출력된다 (`vite.config.js`의 `build.outDir`).
이후 Spring Boot만 실행하면 `http://localhost:8080`에서 UI까지 서빙된다.

---

## 핵심 아키텍처

### SlaveManager ↔ ModbusDataStore 연동

`SlaveManager.buildProcessImage()`에서 j2mod `ProcessImage`에 **익명 클래스**를 등록한다.
Modbus Master가 요청을 보내면 j2mod이 `getValue()`를 호출하고, 그 내부에서 `ModbusDataStore`를 참조한다.
따라서 REST API나 웹 UI로 DataStore 값을 바꾸면 Modbus 응답에 **즉시** 반영된다.

```java
// SlaveManager.buildProcessImage() - 수정 시 이 구조 유지
image.addRegister(new SimpleRegister(0) {
    @Override public int getValue() { return dataStore.getHoldingRegister(idx); }
    @Override public void setValue(int v) { dataStore.setHoldingRegister(idx, v); }
    @Override public void setValue(short v) { dataStore.setHoldingRegister(idx, v & 0xFFFF); }
});
```

### ModbusDataStore 스레드 안전성

Modbus 요청 스레드와 REST API 스레드가 동시에 접근하므로 `ReentrantReadWriteLock` 사용.
읽기는 동시 허용, 쓰기는 단독 접근. **이 구조는 변경하지 않는다.**

### SlaveManager 재시작 흐름

설정 변경 API → `ModbusProperties` 필드 직접 수정 → `SlaveManager.restart*()` 호출 → 기존 Slave `close()` → 변경된 props로 새 Slave 생성

`SlaveManager`는 싱글톤 빈이고 모든 start/stop/restart 메서드가 `synchronized`이다.

---

## Modbus 레지스터 타입

| FC | 타입 | Master 접근 | 비트 |
|----|------|-------------|------|
| FC01 | Coil | R/W | 1bit |
| FC02 | Discrete Input | R (read-only) | 1bit |
| FC03 | Holding Register | R/W | 16bit (0~65535) |
| FC04 | Input Register | R (read-only) | 16bit (0~65535) |

Slave(시뮬레이터) 내부에서는 REST API로 모든 영역에 쓸 수 있다.

---

## REST API 요약

```
# 레지스터
GET  /api/registers
GET  /api/registers/coils?start=0&count=20
GET  /api/registers/discrete-inputs?start=0&count=20
GET  /api/registers/holding?start=0&count=20
GET  /api/registers/input?start=0&count=20
PUT  /api/registers/coils/{address}          {"value": true}
PUT  /api/registers/discrete-inputs/{address}
PUT  /api/registers/holding/{address}        {"value": 1234}
PUT  /api/registers/input/{address}

# 설정 / 제어
GET  /api/config
PUT  /api/config/tcp    {"port":5020,"unitId":1,"enabled":true}
PUT  /api/config/rtu    {"portName":"COM3","baudRate":9600,...}
POST /api/config/tcp/start|stop|restart
POST /api/config/rtu/start|stop|restart
```

---

## 프론트엔드 구조

- `src/api/modbusApi.js` — 모든 백엔드 API 호출 함수 (axios 래퍼)
- `src/hooks/usePolling.js` — 주기적 API 호출 커스텀 훅 (`usePolling(fn, 2000)`)
- `src/index.css` — 전역 스타일 + 공통 클래스 (`.card`, `.btn`, `.badge` 등) + CSS 변수
- Vite 개발 서버(`:3000`)의 `/api/*` 요청은 `localhost:8080`으로 자동 프록시 (`vite.config.js`)

---

## 미구현: Registers.jsx (2단계)

`src/pages/Registers.jsx`가 현재 스텁 상태다. 구현 예정 스펙:

- 탭: Coil / Discrete Input / Holding Register / Input Register
- 상단 `start`, `count` 입력 필드로 조회 범위 설정
- 테이블: 주소 | 현재값 | 편집 input (bool → checkbox, 16bit → number 0~65535)
- 편집 후 Enter 또는 blur → 즉시 PUT 요청
- `usePolling`으로 2초마다 자동 갱신, 편집 포커스 중에는 일시정지

필요한 의존성(API 함수, 훅, 스타일)은 모두 준비되어 있다.

---

## 주의사항

- `ModbusDataStore` 레지스터 배열 크기는 앱 시작 시 고정된다. **런타임 크기 변경 불가.**
- Modbus TCP 포트 502는 Linux에서 root 권한 필요. 개발 시 5020 이상 사용.
- RTU `enabled=false` 상태에서 `startRtu()`를 호출하면 실제 시작을 시도한다. UI에서 enabled 체크박스와 Start 버튼을 분리 처리할 것.
- React 폴링 중 인라인 편집 시 값이 덮어씌워질 수 있다. 편집 포커스 중 폴링 일시정지 처리 필요.

---

## 테스트 도구 (Modbus Master 역할)

- **Modbus Poll** — Windows GUI 클라이언트
- **modpoll** (CLI): `modpoll -m tcp -a 1 -r 1 -c 10 127.0.0.1`
- **pymodbus** — Python REPL 테스트