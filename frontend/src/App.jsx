import { useEffect, useMemo, useState } from 'react'

const rawBase = import.meta.env.VITE_API_BASE_URL || ''
const API = rawBase ? `${rawBase.replace(/\/+$/, '')}/api` : '/api'
const START = '2026-11-12'
const END = '2027-07-11'
const fmt = (s, opts={day:'numeric', month:'short', year:'numeric'}) => new Date(`${s}T00:00:00`).toLocaleDateString('en-IN', opts)
const todayISO = () => {
  const d = new Date()
  return `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')}`
}
const duration = m => m >= 60 ? `${Math.floor(m/60)}h${m%60 ? ` ${m%60}m` : ''}` : `${m} min`

export default function App() {
  const [days, setDays] = useState([])
  const [summary, setSummary] = useState(null)
  const [loading, setLoading] = useState(true)
  const [busy, setBusy] = useState(false)
  const [filter, setFilter] = useState('ALL')
  const [month, setMonth] = useState('ALL')
  const [error, setError] = useState('')

  async function refresh() {
    setLoading(true); setError('')
    try {
      const [d, s] = await Promise.all([
        fetch(`${API}/study-days?from=${START}&to=${END}`).then(r => { if(!r.ok) throw new Error('Could not load schedule'); return r.json() }),
        fetch(`${API}/summary`).then(r => { if(!r.ok) throw new Error('Could not load progress'); return r.json() })
      ])
      setDays(d); setSummary(s)
    } catch(e) { setError('Backend is not reachable. If on Render free tier, the backend may take ~30-50s to wake up from sleep.'); }
    finally { setLoading(false) }
  }
  useEffect(() => { refresh() }, [])

  const today = todayISO()
  const activeIncomplete = days.find(d => !d.completed)
  const completedToday = days.find(d => d.completed && d.completedAt && d.completedAt.startsWith(today))
  const todayTask = completedToday || activeIncomplete || days[0]

  const todayIndex = days.findIndex(d => d.date === todayTask?.date)
  const nextTask = todayIndex !== -1 ? days.slice(todayIndex + 1).find(d => !d.completed) : null
  const months = useMemo(() => [...new Set(days.map(d => d.date.slice(0,7)))], [days])
  const filtered = days.filter(d => (filter === 'ALL' || d.status === filter) && (month === 'ALL' || d.date.startsWith(month)))
  const completed = summary?.completed || 0
  const rate = summary?.completionRate || 0
  const doneToday = todayTask?.completed

  async function mark(date, done) {
    setBusy(true)
    try {
      const method = done ? 'DELETE' : 'POST'
      const res = await fetch(`${API}/study-days/${date}/complete`, { method })
      if (!res.ok) throw new Error('Could not update completion')
      await refresh()
    } catch(e) { setError('Could not save your progress. Check that the backend is running.'); }
    finally { setBusy(false) }
  }

  const domainStats = useMemo(() => {
    const map = {}
    days.forEach(d => { map[d.domain] ||= { total:0, done:0 }; map[d.domain].total++; if(d.completed) map[d.domain].done++ })
    return Object.entries(map).sort((a,b) => b[1].total-a[1].total)
  }, [days])

  return <div className="app-shell">
    <aside className="sidebar">
      <div className="brand"><div className="brand-mark">✦</div><div><b>GrowthOS</b><span>PERSONAL LEARNING TRACKER</span></div></div>
      <div className="side-label">WORKSPACE</div>
      <a className="nav active" href="#overview"><span>▦</span> Overview</a>
      <a className="nav" href="#schedule"><span>☷</span> Study schedule</a>
      <a className="nav" href="#domains"><span>◷</span> Domain progress</a>
      <div className="side-bottom"><div className="avatar">VD</div><div><b>My learning plan</b><small>Nov 2026 – Jul 2027</small></div></div>
    </aside>

    <main className="main" id="overview">
      <header className="topbar"><div><div className="eyebrow">YOUR PERSONAL ROADMAP</div><h1>Developer Growth Tracker</h1><p>Build strong engineering fundamentals. Add practical AI skills. Track every day.</p></div><div className="date-chip">◷ &nbsp; {fmt(today)}</div></header>
      {error && <div className="error">{error} <button onClick={refresh}>Retry</button></div>}
      {loading ? <div className="loading">Loading your learning plan…</div> : <>
        <section className="stats-grid">
          <Stat label="TOPICS COMPLETED" value={`${completed} / ${days.length}`} note="Roadmap progress" icon="✓"/>
          <Stat label="COMPLETION RATE" value={`${rate}%`} note="Of full 242-day curriculum" icon="◔"/>
          <Stat label="CURRENT STREAK" value={`${summary?.currentStreak || 0} days`} note="Consecutive study days" icon="♨"/>
          <Stat label="LONGEST STREAK" value={`${summary?.longestStreak || 0} days`} note="Your personal best" icon="↗"/>
        </section>

        <section className="today-layout">
          <div className="today-card">
            <div className="card-top"><span className="pill purple">CURRENT FOCUS</span><span className="muted">{fmt(today, {weekday:'long', day:'numeric', month:'long'})}</span></div>
            {todayTask ? <>
              <div className="today-domain">{todayTask.domain} <span>·</span> {todayTask.phase}</div>
              <h2>{todayTask.topic}</h2>
              <p className="task-detail">{todayTask.detail}</p>
              <div className="task-meta"><span>◷ &nbsp;{duration(todayTask.minutes)}</span><span>▣ &nbsp;Week {todayTask.week}</span></div>
              {doneToday ? <button className="complete-btn done" onClick={() => mark(todayTask.date, true)} disabled={busy}>✓ &nbsp; Completed · Undo</button>
                : <button className="complete-btn" onClick={() => mark(todayTask.date, false)} disabled={busy}>✓ &nbsp; I've studied this</button>}
            </> : <div className="empty-today"><h2>All topics completed!</h2><p>Congratulations, you've completed the full 8-month roadmap!</p></div>}
          </div>
          <div className="side-card">
            <div className="card-heading">UP NEXT <span>↗</span></div>
            {nextTask ? <><div className="next-date">{fmt(nextTask.date, {weekday:'short', day:'numeric', month:'short'})}</div><h3>{nextTask.topic}</h3><p>{nextTask.domain}</p><div className="thin-line"></div><div className="small-row"><span>Estimated time</span><b>{duration(nextTask.minutes)}</b></div></> : <p className="muted">You've reached the end of this plan.</p>}
            <div className="tip"><b>Master at your own pace</b><p>Topics advance only when you complete them. If you take a break, this topic will still be waiting for you tomorrow.</p></div>
          </div>
        </section>

        <section className="progress-panel">
          <div className="section-title"><div><div className="eyebrow">THE BIG PICTURE</div><h2>8-month journey</h2></div><span className="muted">{fmt(START)} — {fmt(END)}</span></div>
          <div className="progress-track"><div className="progress-fill" style={{width:`${rate}%`}}/></div>
          <div className="progress-labels"><span>{completed} completed</span><span>{days.length - completed} remaining</span><span>{summary?.missed ? `${summary.missed} days behind schedule` : 'On track'}</span></div>
        </section>

        <section className="domains-panel" id="domains">
          <div className="section-title"><div><div className="eyebrow">BALANCED LEARNING</div><h2>Domain progress</h2></div><span className="muted">Completed study days</span></div>
          <div className="domain-grid">{domainStats.map(([name, val]) => <div className="domain-item" key={name}><div className="domain-head"><b>{name}</b><span>{val.done}/{val.total}</span></div><div className="mini-track"><div style={{width:`${val.total ? val.done/val.total*100 : 0}%`}}/></div></div>)}</div>
        </section>

        <section className="schedule-panel" id="schedule">
          <div className="section-title schedule-title"><div><div className="eyebrow">YOUR DAILY PLAN</div><h2>Study schedule</h2><p className="muted">Every date has one planned focus. Completion is tracked independently for each day.</p></div></div>
          <div className="filters">
            <select value={month} onChange={e=>setMonth(e.target.value)}><option value="ALL">All months</option>{months.map(m=><option key={m} value={m}>{new Date(`${m}-01T00:00:00`).toLocaleDateString('en-IN',{month:'long',year:'numeric'})}</option>)}</select>
            <select value={filter} onChange={e=>setFilter(e.target.value)}><option value="ALL">All statuses</option><option value="COMPLETED">Completed</option><option value="TODAY">Current focus</option><option value="UPCOMING">Upcoming</option></select>
            <span className="row-count">{filtered.length} days</span>
          </div>
          <div className="table-wrap"><table><thead><tr><th>DATE</th><th>DOMAIN</th><th>TOPIC</th><th>TIME</th><th>STATUS</th><th>ACTION</th></tr></thead>
            <tbody>{filtered.map(d=><tr key={d.date} className={d.date===todayTask?.date?'today-row':''}>
              <td className="date-cell">{fmt(d.date,{day:'2-digit',month:'short'})}<small>{new Date(`${d.date}T00:00:00`).toLocaleDateString('en-IN',{weekday:'short'})}</small></td>
              <td><span className="domain-tag">{d.domain}</span></td><td className="topic-cell"><b>{d.topic}</b><small>{d.phase}</small></td><td>{duration(d.minutes)}</td>
              <td><Status status={d.status}/></td><td><button className={`table-action ${d.completed?'undo':''}`} disabled={busy} onClick={()=>mark(d.date,d.completed)}>{d.completed?'Undo':'Mark studied'}</button></td>
            </tr>)}</tbody></table>{filtered.length===0&&<div className="no-rows">No study days match these filters.</div>}</div>
        </section>
      </>}
      <footer>Made for consistent learning · Java + System Design + Applied AI</footer>
    </main>
  </div>
}

function Stat({label,value,note,icon}) { return <div className="stat-card"><div className="stat-icon">{icon}</div><div className="stat-label">{label}</div><div className="stat-value">{value}</div><div className="stat-note">{note}</div></div> }
function Status({status}) {
  const map = {COMPLETED:['Completed','green'],MISSED:['Missed','red'],TODAY:['Current focus','purple'],UPCOMING:['Upcoming','gray']}
  const [label,cls] = map[status] || map.UPCOMING
  return <span className={`status ${cls}`}><i/>{label}</span>
}
