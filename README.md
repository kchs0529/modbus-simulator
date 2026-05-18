# Modbus Simulator

Modbus TCP/RTU Slave 시뮬레이터. 실제 장비 없이 PLC, HMI, SCADA 등 Modbus Master 장비의 통신을 테스트할 수 있습니다.

웹 브라우저에서 레지스터 값을 직접 편집하면 Modbus 응답에 즉시 반영됩니다.

## 시작하기

JDK 21, Node.js 18 이상 필요.

```bash
# 백엔드
cd modbus-simulator
./gradlew bootRun

# 프론트엔드 (별도 터미널, 개발 시만)
cd modbus-ui
npm install && npm run dev
```

브라우저에서 `http://localhost:8080` 접속. 8080이 사용 중이면 8081, 8082... 순으로 자동 탐색합니다.

## 빌드

```bash
cd modbus-simulator
./gradlew build   # React 빌드 포함, JAR 생성
```

## Windows 배포용 exe 만들기

```bash
cd modbus-simulator
./gradlew release
```

`dist/ModbusSimulator/` 폴더에 실행파일이 생성됩니다. Java 설치 없이 바로 실행 가능하며, 실행하면 브라우저가 자동으로 열리고 트레이 아이콘으로 종료할 수 있습니다.

폴더째로 zip으로 묶어서 배포하면 됩니다.

## 기능

- Modbus TCP / RTU Slave 동작 (포트, Unit ID, 시리얼 설정 등 웹에서 변경)
- FC01 Coil, FC02 Discrete Input, FC03 Holding Register, FC04 Input Register 지원
- 레지스터 인라인 편집, 2초 자동 갱신
- 시리얼 포트 목록 자동 감지 (RTU 설정 시 드롭다운으로 선택)

