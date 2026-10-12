import React, { useState, useEffect } from 'react';
import { api } from '../api';
import AiRoadmapGenerator from './AiRoadmapGenerator';

export default function RoadmapView({ onNavigateTab }) {
  const [items, setItems] = useState([]);
  const [readiness, setReadiness] = useState(null);
  const [loading, setLoading] = useState(true);

  const loadData = async () => {
    try {
      setLoading(true);
      const [rmData, rData] = await Promise.all([
        api.getRoadmap(),
        api.getReadiness()
      ]);
      setItems(rmData);
      setReadiness(rData);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const handleStatusToggle = async (roadmapId, newStatus) => {
    if (!roadmapId) return;
    try {
      await api.updateRoadmapStatus(roadmapId, newStatus);
      await loadData();
    } catch (err) {
      alert('Could not update status: ' + err.message);
    }
  };

  if (loading) {
    return <div style={{ padding: '2rem 0' }}>Loading roadmap sequence...</div>;
  }

  return (
    <div>
      <div style={{ marginBottom: '1.5rem', display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap', gap: '1rem' }}>
        <div>
          <p style={{ textTransform: 'uppercase', letterSpacing: '0.06em', fontSize: '0.72rem', fontWeight: 600, color: 'var(--ink-tertiary)', marginBottom: '0.25rem' }}>
            Targeted Progression Workflow
          </p>
          <h1 style={{ marginBottom: '0.25rem' }}>Role Gap Roadmap Tracker</h1>
          <p style={{ fontSize: '0.95rem', margin: 0 }}>
            Mark steps in-progress or completed. Marking a step <strong style={{ color: 'var(--accent-forest)' }}>Completed</strong> automatically acquires the competency and recalculates your readiness score in real time.
          </p>
        </div>

        {onNavigateTab && (
          <button
            type="button"
            className="btn btn-primary"
            onClick={() => onNavigateTab('ai-roadmap')}
            style={{ display: 'inline-flex', alignItems: 'center', gap: '0.5rem', fontSize: '0.85rem' }}
          >
            <span>✦ Launch AI Tech Stack Architect</span>
            <span>&rarr;</span>
          </button>
        )}
      </div>

      {/* Cross-Link Callout Banner */}
      {onNavigateTab && (
        <div style={{
          background: 'linear-gradient(135deg, #FAF8F3 0%, #EBF2EE 100%)',
          border: '1px solid var(--accent-forest)',
          borderRadius: 'var(--radius-md)',
          padding: '1rem 1.25rem',
          marginBottom: '2rem',
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          flexWrap: 'wrap',
          gap: '1rem'
        }}>
          <div>
            <div style={{ fontSize: '0.75rem', fontWeight: 700, color: 'var(--accent-forest)', textTransform: 'uppercase', letterSpacing: '0.06em' }}>
              ✦ NEW: Dedicated AI Curriculum Architect
            </div>
            <div style={{ fontSize: '0.875rem', color: 'var(--ink-primary)', marginTop: '0.2rem' }}>
              Want a custom roadmap for any tech stack (Next.js 15, Go, Rust, PyTorch, Kubernetes)? Try our dedicated AI curriculum generator that adapts to your verified profile skills.
            </div>
          </div>
          <button
            type="button"
            className="btn btn-secondary"
            onClick={() => onNavigateTab('ai-roadmap')}
            style={{ fontSize: '0.8125rem', whiteSpace: 'nowrap' }}
          >
            Open AI Roadmap Generator &rarr;
          </button>
        </div>
      )}

          {/* Live Score Feedback Banner */}
          <div className="editorial-card" style={{ marginBottom: '2rem' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'baseline', marginBottom: '0.75rem' }}>
              <div>
                <span style={{ fontSize: '0.8125rem', fontWeight: 600, textTransform: 'uppercase', letterSpacing: '0.05em', color: 'var(--ink-tertiary)' }}>
                  Live Readiness Score

            </span>
            <div className="readiness-score-block" style={{ marginBottom: 0 }}>
              <span className="readiness-large-number" style={{ fontSize: '3rem' }}>{readiness?.score ?? 0}</span>
              <span className="readiness-percent-sign" style={{ fontSize: '1.25rem' }}>%</span>
            </div>
          </div>
          <div style={{ textAlign: 'right' }}>
            <div style={{ fontSize: '0.875rem', color: 'var(--ink-secondary)' }}>
              Core Gaps: <strong style={{ color: 'var(--signal-orange)' }} className="tabular-nums">{readiness?.missingCoreCount ?? 0}</strong> &bull;
              Good-to-Have: <strong className="tabular-nums">{readiness?.missingGoodToHaveCount ?? 0}</strong>
            </div>
          </div>
        </div>

        <div className="thin-progress-bar-container" style={{ marginBottom: '0.5rem' }}>
          <div className="thin-progress-bar-fill" style={{ width: `${readiness?.score ?? 0}%` }}></div>
        </div>

        <p style={{ fontSize: '0.875rem', color: 'var(--ink-secondary)', margin: 0 }}>
          {readiness?.statusMessage}
        </p>
      </div>

      {/* Ordered Timeline List */}
      <div className="editorial-card">
        <div className="editorial-card-header">
          <div>
            <div className="editorial-card-title">Sequenced Gap Milestones</div>
            <div style={{ fontSize: '0.75rem', color: 'var(--ink-tertiary)', marginTop: '2px' }}>
              Ranked: Core competencies first &rarr; Secondary skills &rarr; Logical sequence
            </div>
          </div>
          <span className="badge-outline">{items.length} Milestones Total</span>
        </div>

        {items.length === 0 ? (
          <div style={{ color: 'var(--ink-secondary)', padding: '1.5rem 0' }}>
            No roadmap steps pending. All competency requirements for your target role are satisfied!
          </div>
        ) : (
          <table className="editorial-table">
            <thead>
              <tr>
                <th style={{ width: '50px' }}>Seq</th>
                <th>Milestone Action</th>
                <th>Associated Competency</th>
                <th>Priority</th>
                <th style={{ textAlign: 'right', width: '260px' }}>Progress State</th>
              </tr>
            </thead>
            <tbody>
              {items.map(item => {
                const rId = item.roadmapId ?? item.item?.roadmapId;
                const rTitle = item.title ?? item.item?.title;
                const rOrder = item.orderIndex ?? item.item?.orderIndex;
                const rSkill = item.skillName ?? item.item?.skill?.skillName;
                const rPriority = item.priority ?? item.item?.priority;
                return (
                  <tr key={rId || rTitle}>
                    <td className="tabular-nums font-mono" style={{ color: 'var(--ink-tertiary)' }}>{rOrder}</td>
                    <td>
                      <div style={{ fontWeight: 600, fontSize: '0.9rem' }}>{rTitle}</div>
                      {item.completedAt && (
                        <div style={{ fontSize: '0.72rem', color: 'var(--accent-forest)', marginTop: '2px' }}>
                          Completed: {new Date(item.completedAt).toLocaleDateString()}
                        </div>
                      )}
                    </td>
                    <td>
                      {rSkill ? (
                        <span style={{ fontWeight: 500 }}>{rSkill}</span>
                      ) : (
                        <span style={{ color: 'var(--ink-tertiary)', fontStyle: 'italic' }}>General Milestone</span>
                      )}
                    </td>
                    <td>
                      {rPriority === 'CORE' && <span className="priority-core-text">Core</span>}
                      {rPriority === 'GOOD_TO_HAVE' && <span className="priority-good-text">Good to have</span>}
                      {!rPriority && <span className="priority-good-text">&mdash;</span>}
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
        )}
      </div>
    </div>
  );
}


