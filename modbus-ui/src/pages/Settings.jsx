import { useState, useEffect } from 'react'
import { getConfig, updateTcpConfig, updateRtuConfig, tcpRestart, rtuRestart, getSerialPorts } from '../api/modbusApi'

export default function Settings() {
  const [config, setConfig] = useState(null)
  const [tcp, setTcp] = useState({})
  const [rtu, setRtu] = useState({})
  const [serialPorts, setSerialPorts] = useState([])
  const [saving, setSaving] = useState(false)
  const [msg, setMsg] = useState(null)  // { type: 'ok'|'err', text }

  useEffect(() => {
    getConfig().then(data => {
      setConfig(data)
      setTcp({ ...data.tcp })
      setRtu({ ...data.rtu })
    })
    getSerialPorts().then(setSerialPorts).catch(() => setSerialPorts([]))
  }, [])

  const flash = (type, text) => {
    setMsg({ type, text })
    setTimeout(() => setMsg(null), 3000)
  }

  const saveTcp = async () => {
    setSaving(true)
    try {
      await updateTcpConfig({
        port:    Number(tcp.port),
        unitId:  Number(tcp.unitId),
        enabled: tcp.enabled,
      })
      await tcpRestart()
      flash('ok', 'TCP 설정이 저장되고 재시작되었습니다.')
    } catch (e) {
      flash('err', e.response?.data?.error ?? e.message)
    } finally {
      setSaving(false)
    }
  }

  const saveRtu = async () => {
    setSaving(true)
    try {
      await updateRtuConfig({
        portName: rtu.portName,
        baudRate: Number(rtu.baudRate),
        dataBits: Number(rtu.dataBits),
        stopBits: Number(rtu.stopBits),
        parity:   Number(rtu.parity),
        unitId:   Number(rtu.unitId),
        enabled:  rtu.enabled,
      })
      if (rtu.enabled) await rtuRestart()
      flash('ok', 'RTU 설정이 저장되었습니다.')
    } catch (e) {
      flash('err', e.response?.data?.error ?? e.message)
    } finally {
      setSaving(false)
    }
  }

  if (!config) return <div style={{ padding: 32, color: 'var(--text-muted)' }}>불러오는 중...</div>

  return (
    <div style={{ padding: 28, maxWidth: 720, margin: '0 auto' }}>
      <h1 style={{ fontSize: 22, fontWeight: 700, marginBottom: 24 }}>설정</h1>

      {msg && (
        <div style={{
          background: msg.type === 'ok' ? 'var(--toast-ok-bg)' : 'var(--toast-err-bg)',
          color:      msg.type === 'ok' ? 'var(--toast-ok-text)' : 'var(--toast-err-text)',
          borderRadius: 8,
          padding: '10px 16px',
          marginBottom: 20,
          fontSize: 13,
        }}>
          {msg.text}
        </div>
      )}

      {/* TCP 설정 */}
      <div className="card" style={{ marginBottom: 20 }}>
        <div style={{ fontWeight: 600, fontSize: 15, marginBottom: 18 }}>Modbus TCP</div>

        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 16, marginBottom: 16 }}>
          <div className="form-group">
            <label>포트 번호</label>
            <input
              type="number"
              value={tcp.port ?? ''}
              onChange={e => setTcp(p => ({ ...p, port: e.target.value }))}
              min={1} max={65535}
            />
          </div>
          <div className="form-group">
            <label>Unit ID (1~247)</label>
            <input
              type="number"
              value={tcp.unitId ?? ''}
              onChange={e => setTcp(p => ({ ...p, unitId: e.target.value }))}
              min={1} max={247}
            />
          </div>
        </div>

        <div className="form-group" style={{ marginBottom: 18 }}>
          <label style={{ display: 'flex', alignItems: 'center', gap: 8, flexDirection: 'row' }}>
            <input
              type="checkbox"
              style={{ width: 'auto' }}
              checked={tcp.enabled ?? false}
              onChange={e => setTcp(p => ({ ...p, enabled: e.target.checked }))}
            />
            활성화
          </label>
        </div>

        <button className="btn btn-primary" onClick={saveTcp} disabled={saving}>
          저장 및 재시작
        </button>
      </div>

      {/* RTU 설정 */}
      <div className="card">
        <div style={{ fontWeight: 600, fontSize: 15, marginBottom: 18 }}>Modbus RTU (시리얼)</div>

        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 16, marginBottom: 16 }}>
          <div className="form-group">
            <label>시리얼 포트</label>
            {serialPorts.length > 0 ? (
              <select
                value={rtu.portName ?? ''}
                onChange={e => setRtu(p => ({ ...p, portName: e.target.value }))}
              >
                {!serialPorts.includes(rtu.portName) && rtu.portName && (
                  <option value={rtu.portName}>{rtu.portName} (저장된 값)</option>
                )}
                {serialPorts.map(p => (
                  <option key={p} value={p}>{p}</option>
                ))}
              </select>
            ) : (
              <input
                type="text"
                value={rtu.portName ?? ''}
                onChange={e => setRtu(p => ({ ...p, portName: e.target.value }))}
                placeholder="사용 가능한 포트 없음 — 직접 입력 (예: COM3)"
              />
            )}
          </div>
          <div className="form-group">
            <label>전송 속도 (bps)</label>
            <select value={rtu.baudRate ?? 9600} onChange={e => setRtu(p => ({ ...p, baudRate: e.target.value }))}>
              {[1200,2400,4800,9600,19200,38400,57600,115200].map(b => (
                <option key={b} value={b}>{b}</option>
              ))}
            </select>
          </div>
          <div className="form-group">
            <label>데이터 비트</label>
            <select value={rtu.dataBits ?? 8} onChange={e => setRtu(p => ({ ...p, dataBits: e.target.value }))}>
              {[7, 8].map(b => <option key={b} value={b}>{b}비트</option>)}
            </select>
          </div>
          <div className="form-group">
            <label>정지 비트</label>
            <select value={rtu.stopBits ?? 1} onChange={e => setRtu(p => ({ ...p, stopBits: e.target.value }))}>
              {[1, 2].map(b => <option key={b} value={b}>{b}비트</option>)}
            </select>
          </div>
          <div className="form-group">
            <label>패리티</label>
            <select value={rtu.parity ?? 0} onChange={e => setRtu(p => ({ ...p, parity: e.target.value }))}>
              <option value={0}>없음 (None)</option>
              <option value={1}>홀수 (Odd)</option>
              <option value={2}>짝수 (Even)</option>
            </select>
          </div>
          <div className="form-group">
            <label>Unit ID (1~247)</label>
            <input
              type="number"
              value={rtu.unitId ?? ''}
              onChange={e => setRtu(p => ({ ...p, unitId: e.target.value }))}
              min={1} max={247}
            />
          </div>
        </div>

        <div className="form-group" style={{ marginBottom: 18 }}>
          <label style={{ display: 'flex', alignItems: 'center', gap: 8, flexDirection: 'row' }}>
            <input
              type="checkbox"
              style={{ width: 'auto' }}
              checked={rtu.enabled ?? false}
              onChange={e => setRtu(p => ({ ...p, enabled: e.target.checked }))}
            />
            활성화
          </label>
        </div>

        <button className="btn btn-primary" onClick={saveRtu} disabled={saving}>
          저장{rtu.enabled ? ' 및 재시작' : ''}
        </button>
      </div>
    </div>
  )
}