import { NavLink } from 'react-router-dom'
import { useTheme } from '../context/ThemeContext'

const links = [
  { to: '/',          label: '대시보드' },
  { to: '/registers', label: '레지스터' },
  { to: '/settings',  label: '설정' },
]

export default function Sidebar() {
  const { theme, toggleTheme } = useTheme()

  return (
    <aside style={{
      width: 220,
      minHeight: '100vh',
      background: 'var(--surface)',
      borderRight: '1px solid var(--border)',
      padding: '24px 0',
      flexShrink: 0,
      display: 'flex',
      flexDirection: 'column',
    }}>
      {/* 로고 */}
      <div style={{ padding: '0 20px 20px', borderBottom: '1px solid var(--border)' }}>
        <div style={{ fontSize: 16, fontWeight: 700, color: 'var(--text)' }}>
          Modbus 시뮬레이터
        </div>
        <div style={{ fontSize: 11, color: 'var(--text-muted)', marginTop: 3 }}>
          TCP / RTU Slave
        </div>
      </div>

      {/* 메뉴 */}
      <nav style={{ marginTop: 10, flex: 1 }}>
        {links.map(({ to, label }) => (
          <NavLink
            key={to}
            to={to}
            end={to === '/'}
            style={({ isActive }) => ({
              display: 'block',
              padding: '11px 20px',
              color: isActive ? 'var(--primary)' : 'var(--text-muted)',
              background: isActive ? 'rgba(79,124,255,0.1)' : 'transparent',
              borderLeft: isActive ? '3px solid var(--primary)' : '3px solid transparent',
              fontSize: 14,
              fontWeight: isActive ? 600 : 400,
              transition: 'all 0.15s',
            })}
          >
            {label}
          </NavLink>
        ))}
      </nav>

      {/* 하단: 테마 토글 */}
      <div style={{ padding: '16px 16px 8px', borderTop: '1px solid var(--border)' }}>
        <button
          onClick={toggleTheme}
          className="btn btn-ghost btn-sm"
          style={{ width: '100%', textAlign: 'center' }}
        >
          {theme === 'dark' ? '라이트 모드로 전환' : '다크 모드로 전환'}
        </button>
      </div>
    </aside>
  )
}