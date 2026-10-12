import React, { useState, useEffect } from 'react';
import { api } from '../api';

export default function Register({ onRegisterSuccess, onNavigateLogin, onNavigateHome }) {
  const [fullName, setFullName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [education, setEducation] = useState('B.Tech Computer Science & Engineering, 2025');
  const [targetRoleId, setTargetRoleId] = useState('');
  const [roles, setRoles] = useState([]);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    api.getRoles().then(setRoles).catch(() => {});
  }, []);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);
    setSuccess(null);

    if (password !== confirmPassword) {
      setError('Passwords do not match.');
      return;
    }

    setLoading(true);
    try {
      const authData = await api.register(fullName, email, password, confirmPassword, education, targetRoleId);
      setSuccess('Account created successfully! Loading your dashboard...');
      setTimeout(() => {
        if (onRegisterSuccess) {
          onRegisterSuccess(authData);
        } else {
          onNavigateLogin();
        }
      }, 500);
    } catch (err) {
      setError(err.message || 'Registration failed.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', minHeight: '100vh', padding: '2rem 1rem' }}>
      <div style={{ width: '100%', maxWidth: '520px' }}>
        <div style={{ marginBottom: '2rem', textAlign: 'left' }}>
          <button onClick={onNavigateHome} style={{ background: 'none', border: 'none', padding: 0, fontFamily: 'var(--font-serif)', fontSize: '1.5rem', fontWeight: 700, color: 'var(--accent-forest)', cursor: 'pointer' }}>
            SkillForge
          </button>
          <h1 style={{ fontSize: '1.75rem', marginTop: '1rem', marginBottom: '0.25rem' }}>Create Student Profile</h1>
          <p style={{ fontSize: '0.875rem' }}>Supports college domains (<code>@college.edu</code>, <code>*.edu</code>, <code>*.ac.in</code>) and <code>@gmail.com</code>.</p>
        </div>

        {error && (
          <div className="editorial-alert alert-error">
            <span>{error}</span>
          </div>
        )}
        {success && (
          <div className="editorial-alert alert-success">
            <span>{success}</span>
          </div>
        )}

        <div className="editorial-card">
          <form onSubmit={handleSubmit}>
            <div className="editorial-form-group">
              <label className="editorial-label" htmlFor="regName">Full Name</label>
              <input
                type="text"
                id="regName"
                className="editorial-input"
                placeholder="Sai Nikhil"
                value={fullName}
                onChange={(e) => setFullName(e.target.value)}
                required
              />
            </div>

            <div className="editorial-form-group">
              <label className="editorial-label" htmlFor="regEmail">College / Student Email Address</label>
              <input
                type="email"
                id="regEmail"
                className="editorial-input"
                placeholder="student.name@college.edu or name@gmail.com"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
              />
              <div className="editorial-field-hint">Allowed: @college.edu, @gmail.com, *.edu, *.ac.in</div>
            </div>

            <div className="grid-2">
              <div className="editorial-form-group">
                <label className="editorial-label" htmlFor="regPass">Password</label>
                <input
                  type="password"
                  id="regPass"
                  className="editorial-input"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  required
                  minLength={6}
                />
              </div>
              <div className="editorial-form-group">
                <label className="editorial-label" htmlFor="regConfirm">Confirm Password</label>
                <input
                  type="password"
                  id="regConfirm"
                  className="editorial-input"
                  value={confirmPassword}
                  onChange={(e) => setConfirmPassword(e.target.value)}
                  required
                  minLength={6}
                />
              </div>
            </div>

            <div className="editorial-form-group">
              <label className="editorial-label" htmlFor="regEdu">Degree & Graduation Year</label>
              <input
                type="text"
                id="regEdu"
                className="editorial-input"
                placeholder="B.Tech Computer Science & Engineering, 2025"
                value={education}
                onChange={(e) => setEducation(e.target.value)}
              />
            </div>

            <div className="editorial-form-group">
              <label className="editorial-label" htmlFor="regRole">Initial Target Role</label>
              <select
                id="regRole"
                className="editorial-select"
                value={targetRoleId}
                onChange={(e) => setTargetRoleId(e.target.value)}
              >
                <option value="">-- Choose Role --</option>
                {roles.map(r => (
                  <option key={r.roleId} value={r.roleId}>{r.roleName}</option>
                ))}
              </select>
            </div>

            <button type="submit" disabled={loading} className="btn-editorial-primary w-100" style={{ display: 'block', textAlign: 'center', padding: '0.65rem' }}>
              {loading ? 'Registering...' : 'Complete Registration'}
            </button>
          </form>
        </div>

        <div style={{ fontSize: '0.8125rem', color: 'var(--ink-secondary)', marginTop: '1.5rem', textAlign: 'center' }}>
          Already have an account?{' '}
          <button onClick={onNavigateLogin} style={{ background: 'none', border: 'none', color: 'var(--accent-forest)', fontWeight: 600, cursor: 'pointer', padding: 0 }}>
            Sign In
          </button>
        </div>
      </div>
    </div>
  );
}
