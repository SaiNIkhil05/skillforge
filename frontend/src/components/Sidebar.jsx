import React from 'react';

export default function Sidebar({ user, activeTab, setActiveTab, onLogout }) {
  const isStudent = user?.role === 'STUDENT';
  const isAdmin = user?.role === 'ADMIN';

  return (
    <aside className="app-sidebar">
      <div className="sidebar-header">
        <a href="#dashboard" onClick={(e) => { e.preventDefault(); setActiveTab(isStudent ? 'dashboard' : 'admin'); }} className="brand-title">
          SkillForge
        </a>
        <div className="brand-subtitle">Readiness & Gap Platform</div>
      </div>

      <nav className="sidebar-nav">
        {isStudent && (
          <>
            <div className="nav-section-label">Readiness</div>
            <button
              className={`sidebar-link ${activeTab === 'dashboard' ? 'active' : ''}`}
              onClick={() => setActiveTab('dashboard')}
            >
              Dashboard
            </button>
            <button
              className={`sidebar-link ${activeTab === 'profile' ? 'active' : ''}`}
              onClick={() => setActiveTab('profile')}
            >
              Profile Builder
            </button>
            <button
              className={`sidebar-link ${activeTab === 'readiness' ? 'active' : ''}`}
              onClick={() => setActiveTab('readiness')}
            >
              Readiness Report
            </button>
            <button
              className={`sidebar-link ${activeTab === 'roadmap' ? 'active' : ''}`}
              onClick={() => setActiveTab('roadmap')}
            >
              Roadmap Tracker
            </button>
            <button
              id="nav-ai-roadmap"
              data-testid="nav-ai-roadmap"
              className={`sidebar-link ${activeTab === 'ai-roadmap' ? 'active' : ''}`}
              onClick={() => setActiveTab('ai-roadmap')}
              style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}
            >
              <span>AI Roadmap Generator</span>
              <span className="badge-outline" style={{ fontSize: '0.625rem', padding: '1px 5px', background: 'var(--accent-forest-subtle)', color: 'var(--accent-forest)', borderColor: 'var(--accent-forest)', fontWeight: 600 }}>
                ✦ AI
              </span>
            </button>
          </>
        )}

        {isAdmin && (
          <>
            <div className="nav-section-label">Administration</div>
            <button
              className={`sidebar-link ${activeTab === 'analytics' ? 'active' : ''}`}
              onClick={() => setActiveTab('analytics')}
            >
              Placement Analytics & Heatmap
            </button>
            <button
              className={`sidebar-link ${activeTab === 'admin' ? 'active' : ''}`}
              onClick={() => setActiveTab('admin')}
            >
              Role & Skill Controls
            </button>
          </>
        )}
      </nav>

      <div className="sidebar-footer">
        {user && (
          <div className="user-profile-brief">
            <div className="user-name">{user.fullName}</div>
            <div className="user-role-badge">{user.role}</div>
          </div>
        )}
        <button onClick={onLogout} className="btn-editorial-secondary btn-editorial-sm w-100" style={{ display: 'block', textAlign: 'center' }}>
          Sign Out
        </button>
      </div>
    </aside>
  );
}
