# Modbus Simulator

실제 장비 없이 Modbus Master(PLC, HMI, SCADA 등)의 통신 테스트를 위한 **Modbus TCP/RTU Slave 시뮬레이터**.

- Spring Boot REST API로 레지스터 값을 동적으로 조작
- React 웹 UI로 설정 관리 및 레지스터 편집
- Windows 실행파일(.exe)로 배포 가능 (Java 설치 불필요)

---

## 요구사항 (개발 환경)

| 항목 | 버전 |
|------|------|
| JDK | 21 이상 |
| Node.js | 18 이상 |

---

## 실행 방법

### 개발 서버

```bash
# 백엔드 (http://localhost:8080)
cd modbus-simulator
./gradlew bootRun

# 프론트엔드 개발 서버 (http://localhost:3000, 별도 터미널)
cd modbus-ui
npm install
npm run dev
```

### JAR 빌드 후 실행

```bash
cd modbus-simulator
./gradlew build          # React 빌드 포함
java -jar build/libs/modbus-simulator-1.0.0.jar
```

> React 빌드를 건너뛰려면: `./gradlew build -PskipNpm`

---

## Windows 배포용 실행파일 생성

```bash
cd modbus-simulator
./gradlew release
```

`dist/ModbusSimulator/ModbusSimulator.exe` 생성.  
폴더째로 zip으로 묶어서 배포. Java 설치 불필요.

실행하면 기본 브라우저가 자동으로 열리고 시스템 트레이 아이콘으로 관리.

---

## 프로젝트 구조

```
/
├── modbus-simulator/   # Spring Boot 백엔드 (Java 21, Gradle)
└── modbus-ui/          # React 프론트엔드 (Vite)
```

---

## 주요 기능

- **Modbus TCP Slave**: 포트, Unit ID 설정 / 시작·중지·재시작
- **Modbus RTU Slave**: 시리얼 포트(PC 연결 포트 자동 감지), 보드레이트 등 설정
- **레지스터 편집**: FC01~FC04 전 타입, 인라인 편집, 2초 자동 갱신
- **포트 충돌 자동 처리**: 8080 사용 중이면 8081, 8082... 순서로 자동 탐색

---

## REST API

```
GET  /api/config                        # 전체 설정 및 상태 조회
PUT  /api/config/tcp                    # TCP 설정 변경
PUT  /api/config/rtu                    # RTU 설정 변경
POST /api/config/tcp/start|stop|restart
POST /api/config/rtu/start|stop|restart
GET  /api/config/serial-ports           # 현재 PC의 시리얼 포트 목록

GET  /api/registers/coils?start=0&count=20
GET  /api/registers/discrete-inputs?start=0&count=20
GET  /api/registers/holding?start=0&count=20
GET  /api/registers/input?start=0&count=20
PUT  /api/registers/coils/{address}          {"value": true}
PUT  /api/registers/holding/{address}        {"value": 1234}
```

---

## Modbus 레지스터 타입

| FC | 타입 | Master 접근 | 비트폭 |
|----|------|-------------|--------|
| FC01 | Coil | R/W | 1bit |
| FC02 | Discrete Input | R | 1bit |
| FC03 | Holding Register | R/W | 16bit |
| FC04 | Input Register | R | 16bit |

---

## 테스트 도구

- **Modbus Poll** — Windows GUI 클라이언트
- **modpoll** (CLI): `modpoll -m tcp -a 1 -r 1 -c 10 127.0.0.1`
- **pymodbus** — Python REPL 테스트