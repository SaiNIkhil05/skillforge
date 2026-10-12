import React, { useState, useEffect } from 'react';
import { api } from '../api';

export default function AdminAnalyticsView() {
  const [summary, setSummary] = useState(null);
  const [heatmap, setHeatmap] = useState([]);
  const [roles, setRoles] = useState([]);
  const [roster, setRoster] = useState([]);
  const [filterRole, setFilterRole] = useState('');
  const [filterMinScore, setFilterMinScore] = useState('');
  const [loading, setLoading] = useState(true);
  const [exporting, setExporting] = useState(false);
  const [error, setError] = useState(null);

  const loadData = async () => {
    try {
      setLoading(true);
      setError(null);
      const [sumData, heatData, rolesData, rosterData] = await Promise.all([
        api.getBatchSummary(),
        api.getHeatmap(),
        api.getRoles(),
        api.getCandidateRoster(filterMinScore, filterRole)
      ]);
      setSummary(sumData);
      setHeatmap(heatData);
      setRoles(rolesData);
      setRoster(rosterData);
    } catch (err) {
      setError(err.message || 'Failed to load placement analytics');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, [filterMinScore, filterRole]);

  const handleExportCsv = async () => {
    try {
      setExporting(true);
      await api.exportCandidatesCsv(filterMinScore, filterRole);
    } catch (err) {
      alert('Failed to export CSV: ' + err.message);
    } finally {
      setExporting(false);
    }
  };

  if (loading && !summary) {
    return <div style={{ padding: '2rem 0' }}>Loading placement batch analytics and heatmap...</div>;
  }

  // Group heatmap data into unique departments and roles for matrix visualization
  const departments = Array.from(new Set(heatmap.map(h => h.department)));
  const uniqueRoles = Array.from(
    new Map(heatmap.map(h => [h.roleId, { roleId: h.roleId, roleName: h.roleName }])).values()
  );

  const getHeatColor = (avgScore, count) => {
    if (count === 0) return { bg: 'var(--bg-paper)', text: 'var(--ink-tertiary)', border: 'transparent' };
    if (avgScore >= 70) return { bg: 'rgba(31, 77, 58, 0.16)', text: 'var(--accent-forest)', border: 'var(--accent-forest)' };
    if (avgScore >= 45) return { bg: 'rgba(194, 86, 31, 0.14)', text: 'var(--accent-burnt)', border: 'var(--accent-burnt)' };
    return { bg: 'rgba(28, 27, 25, 0.05)', text: 'var(--ink-secondary)', border: 'var(--border-subtle)' };
  };

  return (
    <div>
      {/* Header */}
      <div style={{ marginBottom: '2rem', display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap', gap: '1rem' }}>
        <div>
          <p style={{ textTransform: 'uppercase', letterSpacing: '0.06em', fontSize: '0.72rem', fontWeight: 600, color: 'var(--ink-tertiary)', marginBottom: '0.25rem' }}>
            Campus Placement Intelligence
          </p>
          <h1 style={{ marginBottom: '0.25rem' }}>Placement Admin Analytics & Heatmap</h1>
          <p style={{ fontSize: '0.95rem', maxWidth: '750px' }}>
            Aggregate career readiness evaluation across departments, branch-level role fit matrix, and filterable candidate roster for campus placement drives.
          </p>
        </div>

        <button
          onClick={handleExportCsv}
          disabled={exporting}
          className="btn-editorial-primary"
          style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}
        >
          <span>{exporting ? 'Generating Roster...' : '📥 Export Candidate Roster (CSV)'}</span>
        </button>
      </div>

      {error && (
        <div className="editorial-alert alert-error">
          <span>{error}</span>
        </div>
      )}

      {/* 1. EXECUTIVE METRIC COUNTER CARDS */}
      <div className="grid-4" style={{ marginBottom: '2rem' }}>
        {/* Metric 1: Avg Score */}
        <div className="editorial-card" style={{ marginBottom: 0 }}>
          <div style={{ fontSize: '0.75rem', textTransform: 'uppercase', letterSpacing: '0.05em', color: 'var(--ink-tertiary)', fontWeight: 600 }}>
            Batch Average Readiness
          </div>
          <div style={{ fontFamily: 'var(--font-serif)', fontSize: '2.4rem', fontWeight: 700, color: 'var(--ink-primary)', margin: '0.35rem 0' }}>
            {summary?.averageReadinessScore || 0}%
          </div>
          <div style={{ height: '6px', background: 'var(--border-stone)', borderRadius: '3px', overflow: 'hidden' }}>
            <div style={{ width: `${summary?.averageReadinessScore || 0}%`, height: '100%', background: 'var(--accent-forest)' }} />
          </div>
          <div style={{ fontSize: '0.78rem', color: 'var(--ink-secondary)', marginTop: '0.5rem' }}>
            Across all enrolled candidates
          </div>
        </div>

        {/* Metric 2: Job Ready % */}
        <div className="editorial-card" style={{ marginBottom: 0 }}>
          <div style={{ fontSize: '0.75rem', textTransform: 'uppercase', letterSpacing: '0.05em', color: 'var(--ink-tertiary)', fontWeight: 600 }}>
            Job-Ready Candidates (≥ 70%)
          </div>
          <div style={{ fontFamily: 'var(--font-serif)', fontSize: '2.4rem', fontWeight: 700, color: 'var(--accent-forest)', margin: '0.35rem 0' }}>
            {summary?.jobReadyPercentage || 0}%
          </div>
          <div style={{ fontSize: '0.85rem', fontWeight: 600, color: 'var(--ink-primary)' }}>
            {summary?.jobReadyCount || 0} of {summary?.totalStudents || 0} candidates
          </div>
          <div style={{ fontSize: '0.78rem', color: 'var(--ink-secondary)', marginTop: '0.25rem' }}>
            Exceed industry benchmark
          </div>
        </div>

        {/* Metric 3: Critical Gaps */}
        <div className="editorial-card" style={{ marginBottom: 0 }}>
          <div style={{ fontSize: '0.75rem', textTransform: 'uppercase', letterSpacing: '0.05em', color: 'var(--ink-tertiary)', fontWeight: 600 }}>
            Critical Core Gaps
          </div>
          <div style={{ fontFamily: 'var(--font-serif)', fontSize: '2.4rem', fontWeight: 700, color: 'var(--accent-burnt)', margin: '0.35rem 0' }}>
            {summary?.criticalGapCount || 0}
          </div>
          <div style={{ fontSize: '0.85rem', fontWeight: 600, color: 'var(--ink-primary)' }}>
            Missing Core Skills
          </div>
          <div style={{ fontSize: '0.78rem', color: 'var(--ink-secondary)', marginTop: '0.25rem' }}>
            Blocking interview qualification
          </div>
        </div>

        {/* Metric 4: Total Candidates */}
        <div className="editorial-card" style={{ marginBottom: 0 }}>
          <div style={{ fontSize: '0.75rem', textTransform: 'uppercase', letterSpacing: '0.05em', color: 'var(--ink-tertiary)', fontWeight: 600 }}>
            Active Candidate Roster
          </div>
          <div style={{ fontFamily: 'var(--font-serif)', fontSize: '2.4rem', fontWeight: 700, color: 'var(--ink-primary)', margin: '0.35rem 0' }}>
            {summary?.totalStudents || 0}
          </div>
          <div style={{ fontSize: '0.85rem', fontWeight: 600, color: 'var(--ink-primary)' }}>
            {roles.length} Benchmark Tracks
          </div>
          <div style={{ fontSize: '0.78rem', color: 'var(--ink-secondary)', marginTop: '0.25rem' }}>
            Target role distribution active
          </div>
        </div>
      </div>

      {/* 2. DEPARTMENT VS ROLE READINESS MATRIX / HEATMAP */}
      <div className="editorial-card" style={{ marginBottom: '2rem' }}>
        <div className="editorial-card-header">
          <div>
            <div className="editorial-card-title">Department vs. Career Role Readiness Matrix (Heatmap)</div>
            <div style={{ fontSize: '0.82rem', color: 'var(--ink-secondary)', marginTop: '0.25rem' }}>
              Color density indicates average competency match score. Each cell shows [Avg Score % • Student Count • Placement-Ready Count].
            </div>
          </div>

          <div style={{ display: 'flex', gap: '1rem', alignItems: 'center', fontSize: '0.75rem' }}>
            <span style={{ display: 'flex', alignItems: 'center', gap: '0.35rem' }}>
              <span style={{ width: '12px', height: '12px', background: 'rgba(31, 77, 58, 0.25)', border: '1px solid var(--accent-forest)', display: 'inline-block' }} />
              <strong>≥ 70%</strong> Job Ready
            </span>
            <span style={{ display: 'flex', alignItems: 'center', gap: '0.35rem' }}>
              <span style={{ width: '12px', height: '12px', background: 'rgba(194, 86, 31, 0.2)', border: '1px solid var(--accent-burnt)', display: 'inline-block' }} />
              <strong>45-69%</strong> Developing
            </span>
            <span style={{ display: 'flex', alignItems: 'center', gap: '0.35rem' }}>
              <span style={{ width: '12px', height: '12px', background: 'rgba(28, 27, 25, 0.05)', border: '1px solid var(--border-subtle)', display: 'inline-block' }} />
              <strong>&lt; 45%</strong> Early Stage
            </span>
          </div>
        </div>

        <div style={{ overflowX: 'auto' }}>
          <table className="editorial-table" style={{ margin: 0, minWidth: '700px' }}>
            <thead>
              <tr>
                <th style={{ width: '180px' }}>Department / Branch</th>
                {uniqueRoles.map(r => (
                  <th key={r.roleId} style={{ textAlign: 'center' }}>
                    {r.roleName}
                  </th>
                ))}
              </tr>
            </thead>
            <tbody>
              {departments.map(dept => (
                <tr key={dept}>
                  <td style={{ fontWeight: 600, verticalAlign: 'middle' }}>{dept}</td>
                  {uniqueRoles.map(r => {
                    const cell = heatmap.find(h => h.department === dept && h.roleId === r.roleId) || {
                      studentCount: 0,
                      averageScore: 0,
                      jobReadyCount: 0,
                      jobReadyRate: 0
                    };
                    const color = getHeatColor(cell.averageScore, cell.studentCount);

                    return (
                      <td
                        key={r.roleId}
                        style={{
                          backgroundColor: color.bg,
                          textAlign: 'center',
                          padding: '1rem 0.75rem',
                          border: `1px solid ${color.border}`
                        }}
                      >
                        {cell.studentCount > 0 ? (
                          <div>
                            <div style={{ fontFamily: 'var(--font-serif)', fontSize: '1.25rem', fontWeight: 700, color: color.text }}>
                              {cell.averageScore}%
                            </div>
                            <div style={{ fontSize: '0.72rem', color: 'var(--ink-secondary)', marginTop: '0.15rem' }}>
                              {cell.studentCount} cand. • {cell.jobReadyCount} ready
                            </div>
                          </div>
                        ) : (
                          <span style={{ fontSize: '0.75rem', color: 'var(--ink-tertiary)' }}>—</span>
                        )}
                      </td>
                    );
                  })}
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {/* 3. COMPETENCY GAPS BY CATEGORY BREAKDOWN */}
      <div className="grid-2" style={{ marginBottom: '2rem' }}>
        <div className="editorial-card">
          <div className="editorial-card-header">
            <div className="editorial-card-title">Missing Skill Gaps by Category</div>
            <span className="badge-outline">Intervention Target</span>
          </div>
          <div style={{ fontSize: '0.85rem', color: 'var(--ink-secondary)', marginBottom: '1rem' }}>
            Highest concentration of competency deficits identified during student evaluations:
          </div>

          {summary?.gapsByCategory && Object.keys(summary.gapsByCategory).length > 0 ? (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
              {Object.entries(summary.gapsByCategory).map(([cat, count]) => {
                const max = Math.max(...Object.values(summary.gapsByCategory));
                const pct = max > 0 ? Math.round((count / max) * 100) : 0;
                return (
                  <div key={cat}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.82rem', marginBottom: '0.2rem' }}>
                      <span style={{ fontWeight: 600 }}>{cat}</span>
                      <span style={{ color: 'var(--ink-secondary)' }}>{count} gap instances</span>
                    </div>
                    <div style={{ height: '8px', background: 'var(--border-subtle)', borderRadius: '4px', overflow: 'hidden' }}>
                      <div style={{ width: `${pct}%`, height: '100%', background: 'var(--accent-burnt)' }} />
                    </div>
                  </div>
                );
              })}
            </div>
          ) : (
            <div style={{ color: 'var(--ink-secondary)', fontSize: '0.85rem' }}>No gap data logged.</div>
          )}
        </div>

        <div className="editorial-card">
          <div className="editorial-card-header">
            <div className="editorial-card-title">Candidate Target Role Distribution</div>
            <span className="badge-outline">Aspirations</span>
          </div>
          <div style={{ fontSize: '0.85rem', color: 'var(--ink-secondary)', marginBottom: '1rem' }}>
            Where final-year students are orienting their preparation:
          </div>

          {summary?.studentDistributionByRole && Object.keys(summary.studentDistributionByRole).length > 0 ? (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
              {Object.entries(summary.studentDistributionByRole).map(([roleName, count]) => {
                const total = summary.totalStudents || 1;
                const pct = Math.round((count / total) * 100);
                const avgScore = summary.averageScoreByRole?.[roleName] || 0;
                return (
                  <div key={roleName}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.82rem', marginBottom: '0.2rem' }}>
                      <span style={{ fontWeight: 600 }}>{roleName}</span>
                      <span style={{ color: 'var(--ink-secondary)' }}>{count} students ({pct}%) • Avg {avgScore}%</span>
                    </div>
                    <div style={{ height: '8px', background: 'var(--border-subtle)', borderRadius: '4px', overflow: 'hidden' }}>
                      <div style={{ width: `${pct}%`, height: '100%', background: 'var(--accent-forest)' }} />
                    </div>
                  </div>
                );
              })}
            </div>
          ) : (
            <div style={{ color: 'var(--ink-secondary)', fontSize: '0.85rem' }}>No candidates enrolled.</div>
          )}
        </div>
      </div>

      {/* 4. FILTERABLE CANDIDATE ROSTER TABLE & CSV EXPORT */}
      <div className="editorial-card">
        <div className="editorial-card-header" style={{ flexWrap: 'wrap', gap: '1rem' }}>
          <div>
            <div className="editorial-card-title">Candidate Placement Roster</div>
            <div style={{ fontSize: '0.82rem', color: 'var(--ink-secondary)', marginTop: '0.25rem' }}>
              Recruiter shortlisting roster with real-time score filters and one-click CSV export.
            </div>
          </div>

          {/* Filter Controls */}
          <div style={{ display: 'flex', gap: '0.75rem', flexWrap: 'wrap', alignItems: 'center' }}>
            <div>
              <select
                className="editorial-select"
                value={filterRole}
                onChange={(e) => setFilterRole(e.target.value)}
                style={{ padding: '0.35rem 0.65rem', fontSize: '0.82rem' }}
              >
                <option value="">All Career Roles</option>
                {roles.map(r => (
                  <option key={r.roleId} value={r.roleId}>{r.roleName}</option>
                ))}
              </select>
            </div>

            <div>
              <select
                className="editorial-select"
                value={filterMinScore}
                onChange={(e) => setFilterMinScore(e.target.value)}
                style={{ padding: '0.35rem 0.65rem', fontSize: '0.82rem' }}
              >
                <option value="">All Scores</option>
                <option value="50">≥ 50% Emerging</option>
                <option value="70">≥ 70% Placement Ready</option>
                <option value="85">≥ 85% Top Tier</option>
              </select>
            </div>

            <button
              onClick={handleExportCsv}
              disabled={exporting}
              className="btn-editorial-secondary btn-editorial-sm"
            >
              {exporting ? 'Exporting...' : 'Export CSV'}
            </button>
          </div>
        </div>

        {roster.length === 0 ? (
          <div style={{ padding: '2rem 0', textAlign: 'center', color: 'var(--ink-secondary)' }}>
            No candidates matched the selected filter criteria.
          </div>
        ) : (
          <div style={{ overflowX: 'auto' }}>
            <table className="editorial-table">
              <thead>
                <tr>
                  <th>Candidate</th>
                  <th>Department</th>
                  <th>Target Role</th>
                  <th>Readiness Score</th>
                  <th>Acquired Skills</th>
                  <th>Missing Core</th>
                  <th>Status</th>
                </tr>
              </thead>
              <tbody>
                {roster.map(c => (
                  <tr key={c.userId}>
                    <td>
                      <div style={{ fontWeight: 600 }}>{c.fullName}</div>
                      <div style={{ fontSize: '0.75rem', color: 'var(--ink-secondary)' }}>{c.email}</div>
                    </td>
                    <td>{c.department}</td>
                    <td style={{ fontWeight: 500 }}>{c.targetRoleName}</td>
                    <td>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                        <span style={{
                          fontFamily: 'var(--font-serif)',
                          fontSize: '1.05rem',
                          fontWeight: 700,
                          color: c.score >= 70 ? 'var(--accent-forest)' : c.score >= 40 ? 'var(--accent-burnt)' : 'var(--ink-secondary)'
                        }}>
                          {c.score}%
                        </span>
                        <div style={{ width: '50px', height: '5px', background: 'var(--border-stone)', borderRadius: '2px', overflow: 'hidden' }}>
                          <div style={{
                            width: `${c.score}%`,
                            height: '100%',
                            background: c.score >= 70 ? 'var(--accent-forest)' : 'var(--accent-burnt)'
                          }} />
                        </div>
                      </div>
                    </td>
                    <td>{c.acquiredCount} / {c.totalRequiredCount}</td>
                    <td>
                      {c.missingCoreCount === 0 ? (
                        <span style={{ color: 'var(--accent-forest)', fontWeight: 600 }}>0 (Cleared)</span>
                      ) : (
                        <span style={{ color: 'var(--accent-burnt)', fontWeight: 600 }}>{c.missingCoreCount} Core Missing</span>
                      )}
                    </td>
                    <td>
                      {c.jobReady ? (
                        <span className="badge-core">Placement Ready</span>
                      ) : (
                        <span className="badge-outline">In Training</span>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
}
