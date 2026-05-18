import { Routes, Route } from 'react-router-dom'
import Sidebar from './components/Sidebar'
import Dashboard from './pages/Dashboard'
import Registers from './pages/Registers'
import Settings from './pages/Settings'

export default function App() {
  return (
    <div style={{ display: 'flex', minHeight: '100vh' }}>
      <Sidebar />
      <main style={{ flex: 1, overflowY: 'auto' }}>
        <Routes>
          <Route path="/"          element={<Dashboard />} />
          <Route path="/registers" element={<Registers />} />
          <Route path="/settings"  element={<Settings />} />
        </Routes>
      </main>
    </div>
  )
}
