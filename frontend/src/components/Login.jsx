import React, { useState } from 'react';
import { api } from '../api';

export default function Login({ onLoginSuccess, onNavigateRegister, onNavigateHome }) {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);
    setLoading(true);
    try {
      const data = await api.login(email, password);
      onLoginSuccess(data);
    } catch (err) {
      setError(err.message || 'Invalid email or password.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', minHeight: '100vh', padding: '2rem 1rem' }}>
      <div style={{ width: '100%', maxWidth: '440px' }}>
        <div style={{ marginBottom: '2rem', textAlign: 'left' }}>
          <button onClick={onNavigateHome} style={{ background: 'none', border: 'none', padding: 0, fontFamily: 'var(--font-serif)', fontSize: '1.5rem', fontWeight: 700, color: 'var(--accent-forest)', cursor: 'pointer' }}>
            SkillForge
          </button>
          <h1 style={{ fontSize: '1.75rem', marginTop: '1rem', marginBottom: '0.25rem' }}>Sign In</h1>
          <p style={{ fontSize: '0.875rem' }}>Access your career readiness evaluation and dynamic roadmaps.</p>
        </div>

        {error && (
          <div className="editorial-alert alert-error">
            <span>{error}</span>
          </div>
        )}

        <div className="editorial-card">
          <form onSubmit={handleSubmit}>
            <div className="editorial-form-group">
              <label className="editorial-label" htmlFor="loginEmail">College Email Address</label>
              <input
                type="email"
                id="loginEmail"
                className="editorial-input"
                placeholder="student@college.edu"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
                autoFocus
              />
            </div>

            <div className="editorial-form-group">
              <label className="editorial-label" htmlFor="loginPassword">Password</label>
              <input
                type="password"
                id="loginPassword"
                className="editorial-input"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
              />
            </div>

            <button type="submit" disabled={loading} className="btn-editorial-primary w-100" style={{ display: 'block', textAlign: 'center', padding: '0.65rem' }}>
              {loading ? 'Authenticating...' : 'Sign In'}
            </button>
          </form>
        </div>

        <div style={{ fontSize: '0.8125rem', color: 'var(--ink-secondary)', marginTop: '1.5rem', textAlign: 'center' }}>
          Don't have an account?{' '}
          <button onClick={onNavigateRegister} style={{ background: 'none', border: 'none', color: 'var(--accent-forest)', fontWeight: 600, cursor: 'pointer', padding: 0 }}>
            Register with your college email
          </button>
        </div>

        <div style={{ marginTop: '2rem', padding: '1rem', border: '1px dashed var(--border-stone)', fontSize: '0.75rem', color: 'var(--ink-secondary)', background: 'var(--bg-surface)' }}>
          <div style={{ fontWeight: 600, marginBottom: '0.5rem', color: 'var(--ink-primary)' }}>Quick Test Accounts:</div>
          <div style={{ display: 'flex', gap: '0.5rem', marginBottom: '0.5rem' }}>
            <button
              type="button"
              onClick={() => { setEmail('student@college.edu'); setPassword('Student@123'); setError(null); }}
              className="btn-editorial-secondary"
              style={{ padding: '0.25rem 0.6rem', fontSize: '0.75rem' }}
            >
              Fill Student
            </button>
            <button
              type="button"
              onClick={() => { setEmail('admin@college.edu'); setPassword('Admin@123'); setError(null); }}
              className="btn-editorial-secondary"
              style={{ padding: '0.25rem 0.6rem', fontSize: '0.75rem' }}
            >
              Fill Admin
            </button>
          </div>
          • Student: <code>student@college.edu</code> (or username <code>student</code>) / <code>Student@123</code><br />
          • Admin: <code>admin@college.edu</code> (or username <code>admin</code>) / <code>Admin@123</code>
        </div>
      </div>
    </div>
  );
}
