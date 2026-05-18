import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  headers: { 'Content-Type': 'application/json' },
})

// ==============================
// 설정 / 상태
// ==============================

export const getConfig = () =>
  api.get('/config').then(r => r.data)

export const updateTcpConfig = (body) =>
  api.put('/config/tcp', body).then(r => r.data)

export const updateRtuConfig = (body) =>
  api.put('/config/rtu', body).then(r => r.data)

export const getSerialPorts = () =>
  api.get('/config/serial-ports').then(r => r.data.ports)

export const tcpStart   = () => api.post('/config/tcp/start').then(r => r.data)
export const tcpStop    = () => api.post('/config/tcp/stop').then(r => r.data)
export const tcpRestart = () => api.post('/config/tcp/restart').then(r => r.data)

export const rtuStart   = () => api.post('/config/rtu/start').then(r => r.data)
export const rtuStop    = () => api.post('/config/rtu/stop').then(r => r.data)
export const rtuRestart = () => api.post('/config/rtu/restart').then(r => r.data)

// ==============================
// 레지스터 조회
// ==============================

export const getAllRegisters = () =>
  api.get('/registers').then(r => r.data)

export const getCoils = (start = 0, count = 20) =>
  api.get('/registers/coils', { params: { start, count } }).then(r => r.data)

export const getDiscreteInputs = (start = 0, count = 20) =>
  api.get('/registers/discrete-inputs', { params: { start, count } }).then(r => r.data)

export const getHoldingRegisters = (start = 0, count = 20) =>
  api.get('/registers/holding', { params: { start, count } }).then(r => r.data)

export const getInputRegisters = (start = 0, count = 20) =>
  api.get('/registers/input', { params: { start, count } }).then(r => r.data)

// ==============================
// 레지스터 쓰기
// ==============================

export const setCoil = (address, value) =>
  api.put(`/registers/coils/${address}`, { value }).then(r => r.data)

export const setDiscreteInput = (address, value) =>
  api.put(`/registers/discrete-inputs/${address}`, { value }).then(r => r.data)

export const setHoldingRegister = (address, value) =>
  api.put(`/registers/holding/${address}`, { value }).then(r => r.data)

export const setInputRegister = (address, value) =>
  api.put(`/registers/input/${address}`, { value }).then(r => r.data)
