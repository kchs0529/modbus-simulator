import { useState, useCallback } from 'react'
import { getConfig, tcpStart, tcpStop, tcpRestart, rtuStart, rtuStop, rtuRestart } from '../api/modbusApi'
import { usePolling } from '../hooks/usePolling'
import StatusBadge from '../components/StatusBadge'

export default function Dashboard() {
  const [config, setConfig] = useState(null)
  const [loading, setLoading] = useState(false)

  const fetchConfig = useCallback(async () => {
    try {
      const data = await getConfig()
      setConfig(data)
    } catch (e) {
      console.error('설정 조회 실패', e)
    }
  }, [])

  usePolling(fetchConfig, 2000)

  const action = async (fn) => {
    setLoading(true)
    try {
      const data = await fn()
      setConfig(data)
    } catch (e) {
      alert('실패: ' + (e.response?.data?.error ?? e.message))
    } finally {
      setLoading(false)
    }
  }

  if (!config) {
    return <div style={{ padding: 32, color: 'var(--text-muted)' }}>서버에 연결 중...</div>
  }

  return (
    <div style={{ padding: '28px 40px', maxWidth: 1100, margin: '0 auto' }}>
      <h1 style={{ fontSize: 22, fontWeight: 700, marginBottom: 24 }}>대시보드</h1>

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(380px, 1fr))', gap: 20 }}>
        <SlaveCard
          title="Modbus TCP"
          status={config.tcp.status}
          error={config.tcp.error}
          startedAt={config.tcp.startedAt}
          rows={[
            ['포트',      config.tcp.port],
            ['Unit ID',   config.tcp.unitId],
            ['활성화',    config.tcp.enabled ? '예' : '아니오'],
          ]}
          loading={loading}
          onStart={() => action(tcpStart)}
          onStop={() => action(tcpStop)}
          onRestart={() => action(tcpRestart)}
        />

        <SlaveCard
          title="Modbus RTU"
          status={config.rtu.status}
          error={config.rtu.error}
          startedAt={config.rtu.startedAt}
          rows={[
            ['시리얼 포트', config.rtu.portName],
            ['전송 속도',   config.rtu.baudRate],
            ['Unit ID',    config.rtu.unitId],
          ]}
          loading={loading}
          onStart={() => action(rtuStart)}
          onStop={() => action(rtuStop)}
          onRestart={() => action(rtuRestart)}
        />
      </div>

      {/* 레지스터 맵 요약 */}
      <div className="card" style={{ marginTop: 20 }}>
        <div style={{ fontWeight: 600, marginBottom: 14 }}>레지스터 맵</div>
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(180px, 1fr))', gap: 14 }}>
          {[
            ['코일 (FC01)',           config.register.coilCount],
            ['접점 입력 (FC02)',       config.register.discreteInputCount],
            ['보유 레지스터 (FC03)',    config.register.holdingRegisterCount],
            ['입력 레지스터 (FC04)',    config.register.inputRegisterCount],
          ].map(([label, count]) => (
            <div key={label} style={{
              background: 'var(--surface2)',
              border: '1px solid var(--border)',
              borderRadius: 6,
              padding: '12px 16px',
            }}>
              <div style={{ fontSize: 11, color: 'var(--text-muted)', marginBottom: 4 }}>{label}</div>
              <div style={{ fontSize: 22, fontWeight: 700, color: 'var(--primary)' }}>{count}</div>
            </div>
          ))}
        </div>
      </div>

      <div style={{ marginTop: 12, fontSize: 11, color: 'var(--text-muted)', textAlign: 'right' }}>
        서버 시각: {config.serverTime?.replace('T', ' ').slice(0, 19)}
      </div>
    </div>
  )
}

function SlaveCard({ title, status, error, startedAt, rows, loading, onStart, onStop, onRestart }) {
  return (
    <div className="card">
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 }}>
        <span style={{ fontWeight: 600, fontSize: 15 }}>{title}</span>
        <StatusBadge status={status} />
      </div>

      <table style={{ marginBottom: 14 }}>
        <tbody>
          {rows.map(([k, v]) => (
            <tr key={k}>
              <td style={{ color: 'var(--text-muted)', width: '45%' }}>{k}</td>
              <td>{v}</td>
            </tr>
          ))}
          {startedAt && (
            <tr>
              <td style={{ color: 'var(--text-muted)' }}>시작 시각</td>
              <td>{startedAt.replace('T', ' ').slice(0, 19)}</td>
            </tr>
          )}
        </tbody>
      </table>

      {error && (
        <div style={{
          background: 'var(--toast-err-bg)',
          color: 'var(--toast-err-text)',
          borderRadius: 6,
          padding: '8px 12px',
          fontSize: 12,
          marginBottom: 12,
        }}>
          {error}
        </div>
      )}

      <div style={{ display: 'flex', gap: 8 }}>
        <button className="btn btn-primary btn-sm" onClick={onStart}   disabled={loading || status === 'RUNNING'}>시작</button>
        <button className="btn btn-ghost   btn-sm" onClick={onRestart} disabled={loading}>재시작</button>
        <button className="btn btn-danger  btn-sm" onClick={onStop}    disabled={loading || status === 'STOPPED'}>중지</button>
      </div>
    </div>
  )
}