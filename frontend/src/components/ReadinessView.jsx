import React, { useState, useEffect } from 'react';
import { api } from '../api';

export default function ReadinessView({ onNavigateTab }) {
  const [readiness, setReadiness] = useState(null);
  const [roadmap, setRoadmap] = useState([]);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);

  const loadData = async () => {
    try {
      setRefreshing(true);
      const [rData, rmData] = await Promise.all([
        api.getReadiness(),
        api.getRoadmap()
      ]);
      setReadiness(rData);
      setRoadmap(rmData);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  if (loading) {
    return <div style={{ padding: '2rem 0' }}>Analyzing competency gaps...</div>;
  }

  const {
    roleName,
    score,
    earnedWeight,
    totalWeight,
    acquiredCount,
    totalRequiredCount,
    statusMessage,
    missingCoreSkills = [],
    missingGoodToHaveSkills = [],
    matchedSkills = []
  } = readiness || {};

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '2rem' }}>
        <div>
          <p style={{ textTransform: 'uppercase', letterSpacing: '0.06em', fontSize: '0.72rem', fontWeight: 600, color: 'var(--ink-tertiary)', marginBottom: '0.25rem' }}>
            Objective Evaluation Dossier
          </p>
          <h1 style={{ marginBottom: '0.25rem' }}>Readiness Report</h1>
          <p style={{ fontSize: '0.95rem' }}>
            Target benchmark: <strong style={{ color: 'var(--ink-primary)' }}>{roleName}</strong>
          </p>
        </div>
        <div>
          <button onClick={loadData} disabled={refreshing} className="btn-editorial-primary btn-editorial-sm">
            {refreshing ? 'Evaluating...' : '↻ Check Readiness'}
          </button>
        </div>
      </div>

      {/* Overall Score Card */}
      <div className="editorial-card">
        <div className="editorial-card-header">
          <div>
            <div className="editorial-card-title">Cumulative Competency Index</div>
            <div style={{ fontSize: '0.75rem', color: 'var(--ink-tertiary)', marginTop: '2px' }}>
              Formula: Earned Weight ({earnedWeight}) / Total Benchmark Weight ({totalWeight}) &times; 100
            </div>
          </div>
          <span className="badge-outline">
            {acquiredCount} of {totalRequiredCount} Skills Acquired
          </span>
        </div>

        <div className="readiness-score-block">
          <span className="readiness-large-number">{score}</span>
          <span className="readiness-percent-sign">%</span>
        </div>

        <div className="thin-progress-bar-container">
          <div className="thin-progress-bar-fill" style={{ width: `${score}%` }}></div>
        </div>

        <p style={{ fontSize: '1rem', fontWeight: 500, color: 'var(--ink-primary)', marginBottom: 0 }}>
          {statusMessage}
        </p>
      </div>

      {/* Split Gaps: Missing Core vs Missing Good-to-Have */}
      <div className="grid-2">
        {/* Missing Core Skills (Signal Orange) */}
        <div className="editorial-card">
          <div className="editorial-card-header">
            <div>
              <div className="editorial-card-title" style={{ color: 'var(--signal-orange)' }}>Missing Core Skills</div>
              <div style={{ fontSize: '0.75rem', color: 'var(--ink-tertiary)', marginTop: '2px' }}>
                Non-negotiable foundational requirements (10 pts each)
              </div>
            </div>
            <span className="badge-outline tabular-nums" style={{ color: 'var(--signal-orange)', borderColor: 'var(--signal-orange)' }}>
              {missingCoreSkills.length} Critical
            </span>
          </div>

          {missingCoreSkills.length === 0 ? (
            <div style={{ color: 'var(--accent-forest)', fontSize: '0.875rem' }}>
              ✓ All core skill requirements for this role have been fulfilled!
            </div>
          ) : (
            <table className="editorial-table">
              <thead>
                <tr>
                  <th>Skill</th>
                  <th>Category</th>
                  <th style={{ textAlign: 'right' }}>Weight</th>
                </tr>
              </thead>
              <tbody>
                {missingCoreSkills.map(r => (
                  <tr key={r.id}>
                    <td style={{ fontWeight: 600, color: 'var(--ink-primary)' }}>{r.skill.skillName}</td>
                    <td><span className="badge-outline">{r.skill.category}</span></td>
                    <td style={{ textAlign: 'right' }} className="tabular-nums font-mono">+{r.weight} pts</td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>

        {/* Missing Good-To-Have Skills */}
        <div className="editorial-card">
          <div className="editorial-card-header">
            <div>
              <div className="editorial-card-title">Missing Good-to-Have Skills</div>
              <div style={{ fontSize: '0.75rem', color: 'var(--ink-tertiary)', marginTop: '2px' }}>
                Secondary competencies that strengthen candidate profile (4 pts each)
              </div>
            </div>
            <span className="badge-outline tabular-nums">
              {missingGoodToHaveSkills.length} Secondary
            </span>
          </div>

          {missingGoodToHaveSkills.length === 0 ? (
            <div style={{ color: 'var(--accent-forest)', fontSize: '0.875rem' }}>
              ✓ No secondary skill gaps remaining.
            </div>
          ) : (
            <table className="editorial-table">
              <thead>
                <tr>
                  <th>Skill</th>
                  <th>Category</th>
                  <th style={{ textAlign: 'right' }}>Weight</th>
                </tr>
              </thead>
              <tbody>
                {missingGoodToHaveSkills.map(r => (
                  <tr key={r.id}>
                    <td style={{ fontWeight: 500 }}>{r.skill.skillName}</td>
                    <td><span className="badge-outline">{r.skill.category}</span></td>
                    <td style={{ textAlign: 'right' }} className="tabular-nums font-mono">+{r.weight} pts</td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      </div>

      {/* Matched & Verified Skills */}
      <div className="editorial-card">
        <div className="editorial-card-header">
          <div>
            <div className="editorial-card-title">Acquired & Matched Role Requirements</div>
            <div style={{ fontSize: '0.75rem', color: 'var(--ink-tertiary)', marginTop: '2px' }}>
              Points earned based on self-rated proficiency multiplier
            </div>
          </div>
          <span className="badge-outline">{matchedSkills.length} Matched</span>
        </div>

        {matchedSkills.length === 0 ? (
          <div style={{ color: 'var(--ink-secondary)', fontSize: '0.875rem' }}>
            No matching competencies yet. Update your profile in the Profile Builder.
          </div>
        ) : (
          <table className="editorial-table">
            <thead>
              <tr>
                <th>Skill</th>
                <th>Role Priority</th>
                <th>Category</th>
                <th>Declared Proficiency</th>
                <th>Multiplier</th>
                <th style={{ textAlign: 'right' }}>Earned Points</th>
              </tr>
            </thead>
            <tbody>
              {matchedSkills.map(m => (
                <tr key={m.skillId}>
                  <td style={{ fontWeight: 600 }}>{m.skillName}</td>
                  <td>
                    {m.priority === 'CORE' && <span className="priority-core-text">Core</span>}
                    {m.priority === 'GOOD_TO_HAVE' && <span className="priority-good-text">Good to have</span>}
                  </td>
                  <td><span className="badge-outline">{m.category}</span></td>
                  <td>{m.declaredProficiency}</td>
                  <td className="tabular-nums font-mono">
                    {m.declaredProficiency === 'ADVANCED' ? '1.0' : (m.declaredProficiency === 'INTERMEDIATE' ? '0.8' : '0.5')}
                  </td>
                  <td style={{ textAlign: 'right', fontWeight: 600 }} className="tabular-nums">
                    {m.pointsEarned} / {m.maxWeight}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      {/* Gap-Only Roadmap Summary */}
      <div className="editorial-card">
        <div className="editorial-card-header">
          <div>
            <div className="editorial-card-title">Actionable Gap-Only Roadmap</div>
            <div style={{ fontSize: '0.75rem', color: 'var(--ink-tertiary)', marginTop: '2px' }}>
              Milestones sequenced to close your existing competency gaps
            </div>
          </div>
          <button onClick={() => onNavigateTab('roadmap')} className="btn-editorial-primary btn-editorial-sm">
            Open Interactive Tracker &rarr;
          </button>
        </div>

        {roadmap.length === 0 ? (
          <div style={{ color: 'var(--ink-secondary)', fontSize: '0.875rem' }}>
            No unresolved roadmap items. All milestones satisfied!
          </div>
        ) : (
          <table className="editorial-table">
            <thead>
              <tr>
                <th style={{ width: '50px' }}>Seq</th>
                <th>Milestone Action</th>
                <th>Target Competency</th>
                <th>Priority</th>
                <th style={{ textAlign: 'right' }}>Status</th>
              </tr>
            </thead>
            <tbody>
              {roadmap.map(item => (
                <tr key={item.roadmapId}>
                  <td className="tabular-nums">{item.orderIndex}</td>
                  <td style={{ fontWeight: 500 }}>{item.title}</td>
                  <td>{item.skillName || 'General Milestone'}</td>
                  <td>
                    {item.priority === 'CORE' && <span className="priority-core-text">Core</span>}
                    {item.priority === 'GOOD_TO_HAVE' && <span className="priority-good-text">Good to have</span>}
                    {!item.priority && <span className="priority-good-text">&mdash;</span>}
                  </td>
                  <td style={{ textAlign: 'right' }}>
                    <span className="badge-outline">{item.status}</span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
}
