import { useState, useEffect } from 'react'
import {
  getCoils, getDiscreteInputs, getHoldingRegisters, getInputRegisters,
  setCoil, setDiscreteInput, setHoldingRegister, setInputRegister,
} from '../api/modbusApi'

const TABS = [
  { key: 'coil', label: '코일',           fc: 'FC01', detail: '1비트 · R/W',      desc: '디지털 출력',           addrBase: 1,     getter: getCoils,            setter: setCoil,            bool: true  },
  { key: 'di',   label: '접점 입력',      fc: 'FC02', detail: '1비트 · 읽기전용', desc: '디지털 입력',           addrBase: 10001, getter: getDiscreteInputs,   setter: setDiscreteInput,   bool: true  },
  { key: 'hr',   label: '보유 레지스터',  fc: 'FC03', detail: '16비트 · R/W',     desc: '아날로그 출력 / 설정값', addrBase: 40001, getter: getHoldingRegisters, setter: setHoldingRegister, bool: false },
  { key: 'ir',   label: '입력 레지스터',  fc: 'FC04', detail: '16비트 · 읽기전용', desc: '아날로그 입력 / 측정값', addrBase: 30001, getter: getInputRegisters,   setter: setInputRegister,   bool: false },
]

const PAGE_SIZE = 20

function toModbusAddr(tab, pduAddr) {
  return String(tab.addrBase + pduAddr).padStart(5, '0')
}

export default function Registers() {
  const [tabKey, setTabKey]         = useState('coil')
  const [start, setStart]           = useState(0)
  const [count, setCount]           = useState(9999)
  const [draftStart, setDraftStart] = useState('0')
  const [draftCount, setDraftCount] = useState('9999')
  const [data, setData]             = useState(null)
  const [page, setPage]             = useState(0)
  const [editing, setEditing]       = useState(null)
  const [draft, setDraft]           = useState('')
  const [error, setError]           = useState(null)

  useEffect(() => {
    if (editing !== null) return

    const tab = TABS.find(t => t.key === tabKey)
    let cancelled = false

    const doFetch = async () => {
      if (cancelled) return
      try {
        const res = await tab.getter(start, count)
        if (!cancelled) { setData(res); setError(null) }
      } catch (e) {
        if (!cancelled) setError(e.response?.data?.error ?? e.message)
      }
    }

    doFetch()
    const id = setInterval(doFetch, 2000)
    return () => { cancelled = true; clearInterval(id) }
  }, [tabKey, start, count, editing])

  const changeTab = (key) => {
    if (key === tabKey) return
    setTabKey(key)
    setData(null)
    setEditing(null)
    setError(null)
    setPage(0)
  }

  const applyRange = () => {
    const s = Math.max(0, parseInt(draftStart) || 0)
    const c = Math.min(9999, Math.max(1, parseInt(draftCount) || 1))
    setDraftStart(String(s))
    setDraftCount(String(c))
    setStart(s)
    setCount(c)
    setData(null)
    setEditing(null)
    setPage(0)
  }

  const handleBoolToggle = async (address, checked) => {
    const tab = TABS.find(t => t.key === tabKey)
    try {
      await tab.setter(address, checked)
      setData(prev => {
        if (!prev) return prev
        const values = [...prev.values]
        values[address - prev.startAddress] = checked ? 1 : 0
        return { ...prev, values }
      })
      setError(null)
    } catch (e) {
      setError(e.response?.data?.error ?? e.message)
    }
  }

  const startEdit = (address, value) => { setEditing(address); setDraft(String(value)) }
  const cancelEdit = () => setEditing(null)

  const commitEdit = async (address) => {
    const num = parseInt(draft, 10)
    if (!isNaN(num) && num >= 0 && num <= 65535) {
      const tab = TABS.find(t => t.key === tabKey)
      try {
        await tab.setter(address, num)
        setData(prev => {
          if (!prev) return prev
          const values = [...prev.values]
          values[address - prev.startAddress] = num
          return { ...prev, values }
        })
        setError(null)
      } catch (e) {
        setError(e.response?.data?.error ?? e.message)
      }
    }
    setEditing(null)
  }

  const tab = TABS.find(t => t.key === tabKey)

  // 페이지네이션 계산
  const totalItems  = data?.values.length ?? 0
  const totalPages  = Math.max(1, Math.ceil(totalItems / PAGE_SIZE))
  const safePage    = Math.min(page, totalPages - 1)
  const pageValues  = data?.values.slice(safePage * PAGE_SIZE, (safePage + 1) * PAGE_SIZE) ?? []
  const pageStart   = (data?.startAddress ?? 0) + safePage * PAGE_SIZE

  return (
    <div style={{ padding: '28px 40px', maxWidth: 1100, margin: '0 auto' }}>
      <h1 style={{ fontSize: 22, fontWeight: 700, marginBottom: 20 }}>레지스터</h1>

      {/* ── 탭 ── */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: 10, marginBottom: 20 }}>
        {TABS.map(t => {
          const active = tabKey === t.key
          return (
            <button
              key={t.key}
              onClick={() => changeTab(t.key)}
              style={{
                background: active ? 'var(--primary)' : 'var(--surface)',
                border: `1px solid ${active ? 'var(--primary)' : 'var(--border)'}`,
                borderRadius: 'var(--radius)',
                padding: '10px 14px',
                cursor: 'pointer',
                textAlign: 'left',
                transition: 'all 0.15s',
                color: active ? '#fff' : 'var(--text)',
              }}
            >
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'baseline', marginBottom: 3 }}>
                <span style={{ fontWeight: 600, fontSize: 13 }}>{t.label}</span>
                <span style={{
                  fontSize: 11, fontFamily: 'Consolas, monospace',
                  background: active ? 'rgba(255,255,255,0.2)' : 'var(--surface2)',
                  padding: '1px 6px', borderRadius: 4,
                  color: active ? '#fff' : 'var(--text-muted)',
                }}>
                  {t.fc}
                </span>
              </div>
              <div style={{ fontSize: 11, color: active ? 'rgba(255,255,255,0.8)' : 'var(--text-muted)' }}>{t.detail}</div>
              <div style={{ fontSize: 10, marginTop: 2, color: active ? 'rgba(255,255,255,0.65)' : 'var(--text-muted)' }}>{t.desc}</div>
            </button>
          )
        })}
      </div>

      {/* ── 범위 설정 ── */}
      <div className="card" style={{ marginBottom: 16, display: 'flex', alignItems: 'flex-end', gap: 14, flexWrap: 'wrap' }}>
        <div className="form-group" style={{ marginBottom: 0 }}>
          <label>시작 주소</label>
          <input type="number" min={0} style={{ width: 100 }}
            value={draftStart}
            onChange={e => setDraftStart(e.target.value)}
            onKeyDown={e => e.key === 'Enter' && applyRange()}
          />
        </div>
        <div className="form-group" style={{ marginBottom: 0 }}>
          <label>개수 (최대 9999)</label>
          <input type="number" min={1} max={9999} style={{ width: 80 }}
            value={draftCount}
            onChange={e => setDraftCount(e.target.value)}
            onKeyDown={e => e.key === 'Enter' && applyRange()}
          />
        </div>
        <button className="btn btn-ghost btn-sm" style={{ marginBottom: 2 }} onClick={applyRange}>
          조회
        </button>
        <span style={{ fontSize: 11, color: 'var(--text-muted)', marginBottom: 4, whiteSpace: 'nowrap' }}>
          {editing !== null ? '⏸ 편집 중 — 자동 갱신 일시정지' : '↺ 2초 자동 갱신'}
        </span>
      </div>

      {error && (
        <div style={{
          background: 'var(--toast-err-bg)', color: 'var(--toast-err-text)',
          borderRadius: 8, padding: '10px 16px', marginBottom: 16, fontSize: 13,
        }}>
          {error}
        </div>
      )}

      {/* ── 테이블 ── */}
      <div className="card" style={{ padding: 0, overflow: 'hidden' }}>
        {!data ? (
          <div style={{ padding: 48, textAlign: 'center', color: 'var(--text-muted)' }}>불러오는 중...</div>
        ) : totalItems === 0 ? (
          <div style={{ padding: 48, textAlign: 'center', color: 'var(--text-muted)' }}>데이터 없음</div>
        ) : (
          <>
            <table style={{ tableLayout: 'fixed' }}>
              <colgroup>
                <col style={{ width: '20%' }} />
                <col style={{ width: '14%' }} />
                <col style={{ width: '18%' }} />
                <col style={{ width: '48%' }} />
              </colgroup>
              <thead>
                <tr>
                  <th>Modbus 주소</th>
                  <th>PDU 주소</th>
                  <th>현재 값</th>
                  <th>편집</th>
                </tr>
              </thead>
              <tbody>
                {pageValues.map((val, i) => {
                  const address = pageStart + i
                  return (
                    <tr key={address} style={{ height: 50 }}>
                      <td style={{ fontFamily: 'Consolas, monospace', fontSize: 14, color: 'var(--primary)', fontWeight: 600 }}>
                        {toModbusAddr(tab, address)}
                      </td>
                      <td style={{ color: 'var(--text-muted)', fontSize: 13 }}>{address}</td>
                      <td style={{ fontSize: 14, fontWeight: 600 }}>
                        {tab.bool
                          ? <span style={{ color: val ? 'var(--success)' : 'var(--text-muted)' }}>{val ? 'ON' : 'OFF'}</span>
                          : <span style={{ fontFamily: 'Consolas, monospace' }}>{val}</span>
                        }
                      </td>
                      <td style={{ paddingRight: 20 }}>
                        {tab.bool ? (
                          <label style={{ display: 'flex', alignItems: 'center', gap: 10, cursor: 'pointer', width: 'fit-content' }}>
                            <input
                              type="checkbox"
                              style={{ width: 18, height: 18, accentColor: 'var(--primary)', cursor: 'pointer' }}
                              checked={val === 1}
                              onChange={e => handleBoolToggle(address, e.target.checked)}
                            />
                            <span style={{ fontSize: 13, color: val ? 'var(--success)' : 'var(--text-muted)', fontWeight: 500 }}>
                              {val ? 'ON으로 설정' : 'OFF로 설정'}
                            </span>
                          </label>
                        ) : editing === address ? (
                          <input
                            type="number" autoFocus
                            style={{ width: '100%', fontSize: 14, padding: '6px 10px' }}
                            value={draft} min={0} max={65535}
                            onChange={e => setDraft(e.target.value)}
                            onBlur={() => commitEdit(address)}
                            onKeyDown={e => {
                              if (e.key === 'Enter') commitEdit(address)
                              if (e.key === 'Escape') cancelEdit()
                            }}
                          />
                        ) : (
                          <span
                            onClick={() => startEdit(address, val)}
                            title="클릭하여 편집 (Enter 저장 / Esc 취소)"
                            style={{
                              display: 'block', width: '100%',
                              padding: '6px 10px',
                              background: 'var(--surface2)',
                              border: '1px solid var(--border)',
                              borderRadius: 6, cursor: 'text',
                              fontSize: 14, fontFamily: 'Consolas, monospace',
                              color: 'var(--text)', boxSizing: 'border-box',
                            }}
                          >
                            {val}
                          </span>
                        )}
                      </td>
                    </tr>
                  )
                })}
              </tbody>
            </table>

            {/* ── 페이지네이션 ── */}
            <div style={{
              display: 'flex', alignItems: 'center', justifyContent: 'space-between',
              padding: '12px 16px',
              borderTop: '1px solid var(--border)',
              background: 'var(--surface2)',
            }}>
              <span style={{ fontSize: 12, color: 'var(--text-muted)' }}>
                전체 {totalItems}개 · {safePage * PAGE_SIZE + 1}~{Math.min((safePage + 1) * PAGE_SIZE, totalItems)}번째
              </span>

              <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                <button
                  className="btn btn-ghost btn-sm"
                  onClick={() => setPage(0)}
                  disabled={safePage === 0}
                >
                  처음
                </button>
                <button
                  className="btn btn-ghost btn-sm"
                  onClick={() => setPage(p => Math.max(0, p - 1))}
                  disabled={safePage === 0}
                >
                  이전
                </button>

                <span style={{ fontSize: 13, fontWeight: 600, padding: '0 8px', color: 'var(--text)' }}>
                  {safePage + 1} / {totalPages}
                </span>

                <button
                  className="btn btn-ghost btn-sm"
                  onClick={() => setPage(p => Math.min(totalPages - 1, p + 1))}
                  disabled={safePage >= totalPages - 1}
                >
                  다음
                </button>
                <button
                  className="btn btn-ghost btn-sm"
                  onClick={() => setPage(totalPages - 1)}
                  disabled={safePage >= totalPages - 1}
                >
                  마지막
                </button>
              </div>
            </div>
          </>
        )}
      </div>
    </div>
  )
}