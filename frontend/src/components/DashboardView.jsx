import React, { useState, useEffect } from 'react';
import { api } from '../api';

export default function DashboardView({ user, onNavigateTab }) {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const loadData = async () => {
    try {
      setLoading(true);
      const res = await api.getDashboard();
      setData(res);
      setError(null);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const handleStatusToggle = async (roadmapId, status) => {
    if (!roadmapId) return;
    try {
      await api.updateRoadmapStatus(roadmapId, status);
      await loadData();
    } catch (err) {
      alert('Could not update status: ' + err.message);
    }
  };

  const handleSelectRole = async (roleId) => {
    try {
      await api.updateTargetRole(roleId);
      loadData();
    } catch (err) {
      alert('Failed to set target role: ' + err.message);
    }
  };

  if (loading) {
    return <div style={{ padding: '2rem 0' }}>Loading student dossier...</div>;
  }

  if (error) {
    return (
      <div className="editorial-alert alert-error">
        <span>{error}</span>
      </div>
    );
  }

  if (!data) {
    return <div style={{ padding: '2rem 0' }}>Loading student dossier...</div>;
  }

  const { targetRoleName, targetRoleId, readiness, roadmapPreview, skillsCount, projectsCount, availableRoles } = data;

  return (
    <div>
      <div style={{ marginBottom: '2rem' }}>
        <p style={{ textTransform: 'uppercase', letterSpacing: '0.06em', fontSize: '0.72rem', fontWeight: 600, color: 'var(--ink-tertiary)', marginBottom: '0.25rem' }}>
          Student Overview &bull; {user?.education || 'Academic Candidate'}
        </p>
        <h1 style={{ marginBottom: '0.25rem' }}>Welcome, {user?.fullName || 'Student'}</h1>
        <p style={{ fontSize: '0.95rem' }}>
          Target role: <strong style={{ color: 'var(--ink-primary)' }}>{targetRoleName || 'No role selected'}</strong>
        </p>
      </div>

      {targetRoleId ? (
        <>
          {/* Readiness Score Card */}
          <div className="editorial-card">
            <div className="editorial-card-header">
              <div>
                <div className="editorial-card-title">Target Readiness Score</div>
                <div style={{ fontSize: '0.75rem', color: 'var(--ink-tertiary)', marginTop: '2px' }}>
                  Calculated across weighted core and secondary requirements
                </div>
              </div>
              <button onClick={() => onNavigateTab('readiness')} className="btn-editorial-secondary btn-editorial-sm">
                Inspect Gap Analysis &rarr;
              </button>
            </div>

            <div className="readiness-score-block">
              <span className="readiness-large-number">{readiness?.score ?? 0}</span>
              <span className="readiness-percent-sign">%</span>
            </div>

            <div className="thin-progress-bar-container">
              <div
                className="thin-progress-bar-fill"
                style={{ width: `${readiness?.score ?? 0}%` }}
              ></div>
            </div>

            <p style={{ fontSize: '0.95rem', fontWeight: 500, color: 'var(--ink-primary)', marginBottom: '1.25rem' }}>
              {readiness?.statusMessage}
            </p>

            <div className="stat-grid-4">
              <div className="metric-tile metric-tile-warning">
                <div className="metric-number tabular-nums" style={{ color: 'var(--signal-orange)' }}>
                  {readiness?.missingCoreCount ?? 0}
                </div>
                <div className="metric-label">Missing Core Skills</div>
              </div>
              <div className="metric-tile">
                <div className="metric-number tabular-nums">
                  {readiness?.missingGoodToHaveCount ?? 0}
                </div>
                <div className="metric-label">Good-to-Have Gaps</div>
              </div>
              <div className="metric-tile metric-tile-success">
                <div className="metric-number tabular-nums" style={{ color: 'var(--accent-forest)' }}>
                  {skillsCount}
                </div>
                <div className="metric-label">Acquired Skills</div>
              </div>
              <div className="metric-tile">
                <div className="metric-number tabular-nums">{projectsCount}</div>
                <div className="metric-label">Verified Projects</div>
              </div>
            </div>
          </div>

          {/* Gap Roadmap Preview */}
          <div className="editorial-card">
            <div className="editorial-card-header">
              <div>
                <div className="editorial-card-title">Immediate Learning Roadmap</div>
                <div style={{ fontSize: '0.75rem', color: 'var(--ink-tertiary)', marginTop: '2px' }}>
                  Filtered strictly to your current competency gaps (core milestones prioritized)
                </div>
              </div>
              <button onClick={() => onNavigateTab('roadmap')} className="btn-editorial-primary btn-editorial-sm">
                Open Roadmap Tracker &rarr;
              </button>
            </div>

            {!roadmapPreview || roadmapPreview.length === 0 ? (
              <div style={{ padding: '1rem 0', color: 'var(--ink-secondary)' }}>
                You have cleared all prioritized milestone gaps for this career role!
              </div>
            ) : (
              <div className="editorial-table-container">
                <table className="editorial-table">
                  <thead>
                    <tr>
                      <th style={{ width: '60px' }}>Seq</th>
                      <th>Milestone</th>
                      <th>Associated Skill</th>
                      <th>Priority</th>
                      <th style={{ textAlign: 'right' }}>Status Action</th>
                    </tr>
                  </thead>
                  <tbody>
                    {roadmapPreview.map(item => {
                      const rId = item.roadmapId ?? item.item?.roadmapId;
                      const rTitle = item.title ?? item.item?.title;
                      const rOrder = item.orderIndex ?? item.item?.orderIndex;
                      const rSkill = item.skillName ?? item.item?.skill?.skillName ?? 'General Milestone';
                      const rPriority = item.priority ?? item.item?.priority;
                      return (
                        <tr key={rId || rTitle}>
                          <td className="tabular-nums" style={{ fontWeight: 600, color: 'var(--ink-tertiary)' }}>{rOrder}</td>
                          <td style={{ fontWeight: 500 }}>{rTitle}</td>
                          <td><span className="badge-outline" style={{ background: 'var(--bg-subtle)' }}>{rSkill}</span></td>
                          <td>
                            {rPriority === 'CORE' && <span className="priority-core-badge">● Core</span>}
                            {rPriority === 'GOOD_TO_HAVE' && <span className="priority-good-badge">○ Secondary</span>}
                            {!rPriority && <span className="priority-good-badge">&mdash;</span>}
                          </td>
                          <td style={{ textAlign: 'right' }}>
                            <div className="segmented-control">
                              <button
                                type="button"
                                className={`segmented-option ${item.status === 'NOT_STARTED' ? 'active' : ''}`}
                                onClick={() => handleStatusToggle(rId, 'NOT_STARTED')}
                              >
                                Not started
                              </button>
                              <button
                                type="button"
                                className={`segmented-option ${item.status === 'IN_PROGRESS' ? 'active' : ''}`}
                                onClick={() => handleStatusToggle(rId, 'IN_PROGRESS')}
                              >
                                In progress
                              </button>
                              <button
                                type="button"
                                className={`segmented-option ${item.status === 'COMPLETED' ? 'active-completed' : ''}`}
                                onClick={() => handleStatusToggle(rId, 'COMPLETED')}
                              >
                                Completed
                              </button>
                            </div>
                          </td>
                        </tr>
                      );
                    })}
                  </tbody>
                </table>
              </div>
            )}
          </div>

          {/* AI Roadmap Generator Discovery Card */}
          <div className="editorial-card" style={{
            background: 'linear-gradient(135deg, #FAF8F3 0%, #EBF2EE 100%)',
            border: '1.5px solid var(--accent-forest)',
            marginBottom: '2rem',
            padding: '1.5rem 1.75rem'
          }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '1.25rem' }}>
              <div style={{ flex: 1, minWidth: '300px' }}>
                <div style={{ display: 'inline-flex', alignItems: 'center', gap: '0.4rem', fontSize: '0.72rem', fontWeight: 700, color: 'var(--accent-forest)', letterSpacing: '0.08em', textTransform: 'uppercase', marginBottom: '0.35rem' }}>
                  <span>✦</span> AUTONOMOUS CURRICULUM ARCHITECT
                </div>
                <h3 style={{ margin: '0 0 0.35rem 0', fontSize: '1.25rem' }}>Explore Generative Tech Stack Roadmaps</h3>
                <p style={{ margin: 0, fontSize: '0.875rem', color: 'var(--ink-secondary)', lineHeight: 1.5 }}>
                  Want to master a specific technology combination outside your benchmark career role (e.g., Next.js 15, Go Microservices, Rust, or PyTorch)? Our AI dynamically architects a multi-phase curriculum that factors in your verified profile skills.
                </p>
              </div>
              <button
                type="button"
                className="btn btn-primary"
                onClick={() => onNavigateTab('ai-roadmap')}
                style={{ whiteSpace: 'nowrap', display: 'inline-flex', alignItems: 'center', gap: '0.5rem' }}
              >
                <span>Launch AI Architect</span>
                <span>&rarr;</span>
              </button>
            </div>
          </div>
        </>
      ) : (
        /* No Target Role Chosen */
        <div className="editorial-card" style={{ padding: '2.5rem 2rem' }}>
          <h2 style={{ fontSize: '1.4rem', marginBottom: '0.5rem' }}>Select Your Target Role</h2>
          <p style={{ maxWidth: '600px', marginBottom: '1.5rem' }}>
            SkillForge needs a benchmark role to calculate your readiness score and generate your personalized gap analysis.
          </p>
          <div style={{ maxWidth: '450px' }}>
            <div className="editorial-form-group">
              <label className="editorial-label" htmlFor="dashSelectRole">Available Career Paths</label>
              <select
                id="dashSelectRole"
                className="editorial-select"
                onChange={(e) => { if (e.target.value) handleSelectRole(e.target.value); }}
              >
                <option value="">-- Choose Target Role --</option>
                {(availableRoles || []).map(r => (
                  <option key={r.roleId} value={r.roleId}>{r.roleName}</option>
                ))}
              </select>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
