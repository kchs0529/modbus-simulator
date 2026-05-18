const STATUS_MAP = {
  RUNNING: { cls: 'badge badge-running', label: '실행 중' },
  STOPPED: { cls: 'badge badge-stopped', label: '중지됨' },
  ERROR:   { cls: 'badge badge-error',   label: '오류'   },
}

export default function StatusBadge({ status }) {
  const { cls, label } = STATUS_MAP[status] ?? { cls: 'badge badge-stopped', label: '알 수 없음' }
  return <span className={cls}>{label}</span>
}