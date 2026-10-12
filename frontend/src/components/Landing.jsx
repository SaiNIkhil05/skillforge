import React, { useState, useEffect } from 'react';
import { api } from '../api';

export default function Landing({ onNavigateLogin, onNavigateRegister }) {
  const [roles, setRoles] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    api.getRoles()
      .then(data => {
        setRoles(data);
        setLoading(false);
      })
      .catch(() => setLoading(false));
  }, []);

  return (
    <div>
      <header style={{ borderBottom: '1px solid var(--border-stone)', background: 'var(--bg-surface)', padding: '1rem 2rem' }}>
        <div style={{ maxWidth: '1100px', margin: '0 auto', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <div>
            <span style={{ fontFamily: 'var(--font-serif)', fontSize: '1.35rem', fontWeight: 700, color: 'var(--accent-forest)' }}>
              SkillForge
            </span>
            <span style={{ fontSize: '0.75rem', textTransform: 'uppercase', letterSpacing: '0.05em', color: 'var(--ink-tertiary)', marginLeft: '0.5rem' }}>
              Career Readiness Platform
            </span>
          </div>
          <div>
            <button onClick={onNavigateLogin} className="btn-editorial-secondary" style={{ marginRight: '0.75rem' }}>
              Sign In
            </button>
            <button onClick={onNavigateRegister} className="btn-editorial-primary">
              Register
            </button>
          </div>
        </div>
      </header>

      <main style={{ maxWidth: '1100px', margin: '3.5rem auto', padding: '0 1.5rem' }}>
        <div style={{ marginBottom: '3.5rem' }}>
          <p style={{ textTransform: 'uppercase', letterSpacing: '0.08em', fontSize: '0.75rem', fontWeight: 600, color: 'var(--accent-forest)', marginBottom: '0.5rem' }}>
            Objective Readiness Evaluation
          </p>
          <h1 style={{ fontSize: '2.75rem', marginBottom: '1rem', maxWidth: '780px' }}>
            Campus guidance is a generic list. SkillForge gives you a number.
          </h1>
          <p style={{ fontSize: '1.1rem', maxWidth: '720px', color: 'var(--ink-secondary)', lineHeight: 1.6 }}>
            Compare your actual coursework, proficiencies, and projects against authoritative industry role specifications. Identify your exact gap, split into core and good-to-have competencies, and execute a dynamic, gap-only learning roadmap.
          </p>
          <div style={{ marginTop: '1.75rem' }}>
            <button onClick={onNavigateRegister} className="btn-editorial-primary" style={{ padding: '0.7rem 1.4rem', fontSize: '0.95rem' }}>
              Build Your Readiness Profile
            </button>
            <button onClick={onNavigateLogin} className="btn-editorial-secondary" style={{ padding: '0.7rem 1.4rem', fontSize: '0.95rem', marginLeft: '0.75rem' }}>
              Sign In with College ID
            </button>
          </div>
        </div>

        <div style={{ display: 'flex', gap: '2.5rem', marginBottom: '3.5rem', padding: '1.5rem 0', borderTop: '1px solid var(--border-stone)', borderBottom: '1px solid var(--border-stone)' }}>
          <div className="metric-stat">
            <div className="metric-number tabular-nums">{roles.length || 4}</div>
            <div className="metric-label">Target Career Roles</div>
          </div>
          <div className="metric-stat">
            <div className="metric-number tabular-nums">40+</div>
            <div className="metric-label">Cataloged Competencies</div>
          </div>
          <div className="metric-stat">
            <div className="metric-number">Weighted</div>
            <div className="metric-label">Readiness Formula</div>
          </div>
        </div>

        <div>
          <h2 style={{ fontSize: '1.6rem', marginBottom: '1.5rem' }}>Available Target Roles</h2>
          {loading ? (
            <p>Loading benchmark roles...</p>
          ) : (
            <div className="grid-2">
              {roles.map(r => (
                <div key={r.roleId} className="editorial-card" style={{ marginBottom: 0 }}>
                  <h3 style={{ marginBottom: '0.5rem', fontSize: '1.25rem' }}>{r.roleName}</h3>
                  <p style={{ fontSize: '0.875rem', color: 'var(--ink-secondary)', marginBottom: '1.25rem' }}>
                    {r.description}
                  </p>
                  <button onClick={onNavigateLogin} className="btn-editorial-secondary btn-editorial-sm">
                    Evaluate Readiness &rarr;
                  </button>
                </div>
              ))}
            </div>
          )}
        </div>
      </main>
    </div>
  );
}
