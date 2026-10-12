import React, { useState, useEffect } from 'react';
import { api } from '../api';

const PRESET_STACKS = [
  { label: 'Next.js 15 + FastAPI + PostgreSQL', value: 'Next.js 15, FastAPI, PostgreSQL, Tailwind CSS, Docker' },
  { label: 'Go Microservices + Kafka + K8s', value: 'Go (Golang), Apache Kafka, Kubernetes, Docker, Prometheus' },
  { label: 'Rust Systems + Tokio + Axum', value: 'Rust, Tokio, Axum, PostgreSQL, WebAssembly' },
  { label: 'AI Agent Stack: PyTorch + LangChain', value: 'Python, PyTorch, LangChain, pgvector, FastAPI, Streamlit' },
  { label: 'Enterprise Spring Boot 3 + Angular', value: 'Java 17, Spring Boot 3, Angular 18, MySQL, Docker' },
  { label: 'Mobile: React Native + Supabase', value: 'React Native, Expo, Supabase, TypeScript, Tailwind' }
];

const THINKING_STEPS = [
  'Decomposing tech stack into runtime, framework, data, and cloud layers...',
  'Analyzing your SkillForge profile competencies to eliminate redundant prerequisites...',
  'Calibrating milestone velocity for your target experience level and timeframe...',
  'Generating hands-on code deliverables and verified architectural blueprints...',
  'Synthesizing production capstone projects and documentation references...'
];

export default function AiRoadmapGenerator() {
  const [desiredTechStack, setDesiredTechStack] = useState('');
  const [targetRole, setTargetRole] = useState('Full-Stack Software Engineer');
  const [targetLevel, setTargetLevel] = useState('Junior to Mid-Level');
  const [timeframeWeeks, setTimeframeWeeks] = useState(12);
  const [learningFocus, setLearningFocus] = useState('Project-Driven');
  const [customApiKey, setCustomApiKey] = useState('');
  const [showApiKeyInput, setShowApiKeyInput] = useState(false);

  const [generating, setGenerating] = useState(false);
  const [thinkingStepIndex, setThinkingStepIndex] = useState(0);
  const [generatedRoadmap, setGeneratedRoadmap] = useState(null);
  const [savedRoadmaps, setSavedRoadmaps] = useState([]);
  const [savingRoadmap, setSavingRoadmap] = useState(false);
  const [saveSuccessMessage, setSaveSuccessMessage] = useState('');
  const [activeTab, setActiveTab] = useState('generator'); // 'generator' | 'library'

  // Load saved roadmaps on mount
  useEffect(() => {
    loadSavedRoadmaps();
  }, []);

  // Animate thinking steps during generation
  useEffect(() => {
    let interval;
    if (generating) {
      setThinkingStepIndex(0);
      interval = setInterval(() => {
        setThinkingStepIndex((prev) => (prev + 1) % THINKING_STEPS.length);
      }, 1600);
    }
    return () => clearInterval(interval);
  }, [generating]);

  const loadSavedRoadmaps = async () => {
    try {
      const roadmaps = await api.getSavedAiRoadmaps();
      setSavedRoadmaps(roadmaps || []);
    } catch (err) {
      console.warn('Could not load saved roadmaps:', err.message);
    }
  };

  const handleGenerate = async (e) => {
    if (e) e.preventDefault();
    if (!desiredTechStack.trim()) {
      alert('Please enter your desired tech stack or choose one of the quick suggestions.');
      return;
    }

    setGenerating(true);
    setSaveSuccessMessage('');
    try {
      const response = await api.generateAiRoadmap({
        desiredTechStack: desiredTechStack.trim(),
        targetRole,
        targetLevel,
        timeframeWeeks: parseInt(timeframeWeeks, 10),
        learningFocus,
        customApiKey: customApiKey.trim() || null
      });
      setGeneratedRoadmap(response);
      setActiveTab('generator');
    } catch (err) {
      alert('AI Roadmap generation error: ' + err.message);
    } finally {
      setGenerating(false);
    }
  };

  const handleSaveRoadmap = async () => {
    if (!generatedRoadmap) return;
    setSavingRoadmap(true);
    setSaveSuccessMessage('');
    try {
      const saved = await api.saveAiRoadmap(generatedRoadmap);
      setGeneratedRoadmap((prev) => ({
        ...prev,
        id: saved.id,
        saved: true
      }));
      setSaveSuccessMessage('Roadmap saved to your personal library!');
      await loadSavedRoadmaps();
    } catch (err) {
      alert('Could not save roadmap: ' + err.message);
    } finally {
      setSavingRoadmap(false);
    }
  };

  const handleMilestoneStatusChange = async (phaseIndex, milestoneId, newStatus) => {
    if (!generatedRoadmap) return;

    // Optimistically update local state
    const updatedPhases = generatedRoadmap.phases.map((phase, pIdx) => {
      if (pIdx !== phaseIndex) return phase;
      return {
        ...phase,
        milestones: phase.milestones.map((m) => {
          if (m.id === milestoneId) {
            return { ...m, status: newStatus };
          }
          return m;
        })
      };
    });

    setGeneratedRoadmap((prev) => ({
      ...prev,
      phases: updatedPhases
    }));

    // If already saved on server, update via backend API
    if (generatedRoadmap.id) {
      try {
        await api.updateAiMilestoneStatus(generatedRoadmap.id, milestoneId, newStatus);
        await loadSavedRoadmaps();
      } catch (err) {
        console.warn('Failed to update milestone on backend:', err.message);
      }
    }
  };

  const handleLoadSavedRoadmap = (savedItem) => {
    try {
      const parsed = JSON.parse(savedItem.roadmapJson);
      parsed.id = savedItem.id;
      parsed.saved = true;
      setGeneratedRoadmap(parsed);
      setActiveTab('generator');
      window.scrollTo({ top: 300, behavior: 'smooth' });
    } catch (err) {
      alert('Could not load saved roadmap details: ' + err.message);
    }
  };

  const handleDeleteSavedRoadmap = async (id, e) => {
    e.stopPropagation();
    if (!window.confirm('Are you sure you want to delete this saved roadmap?')) return;
    try {
      await api.deleteAiRoadmap(id);
      if (generatedRoadmap && generatedRoadmap.id === id) {
        setGeneratedRoadmap((prev) => ({ ...prev, saved: false, id: null }));
      }
      await loadSavedRoadmaps();
    } catch (err) {
      alert('Failed to delete roadmap: ' + err.message);
    }
  };

  return (
    <div>
      {/* Sub-tab navigation: Generator Studio vs Saved Library */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.25rem' }}>
        <div style={{ display: 'flex', gap: '0.75rem' }}>
          <button
            type="button"
            className={`btn ${activeTab === 'generator' ? 'btn-primary' : 'btn-secondary'}`}
            style={{ fontSize: '0.85rem', padding: '0.45rem 1rem' }}
            onClick={() => setActiveTab('generator')}
          >
            AI Curriculum Studio
          </button>
          <button
            type="button"
            className={`btn ${activeTab === 'library' ? 'btn-primary' : 'btn-secondary'}`}
            style={{ fontSize: '0.85rem', padding: '0.45rem 1rem' }}
            onClick={() => setActiveTab('library')}
          >
            Saved Roadmaps ({savedRoadmaps.length})
          </button>
        </div>
        {generatedRoadmap && activeTab === 'generator' && (
          <div>
            {generatedRoadmap.saved ? (
              <span className="badge-outline" style={{ color: 'var(--accent-forest)', borderColor: 'var(--accent-forest)', fontWeight: 600 }}>
                Saved in Your Library
              </span>
            ) : (
              <button
                type="button"
                className="btn btn-primary"
                style={{ fontSize: '0.8125rem', padding: '0.4rem 0.9rem' }}
                onClick={handleSaveRoadmap}
                disabled={savingRoadmap}
              >
                {savingRoadmap ? 'Saving...' : 'Save Roadmap to Library'}
              </button>
            )}
          </div>
        )}
      </div>

      {saveSuccessMessage && (
        <div className="alert alert-info" style={{ marginBottom: '1.25rem' }}>
          {saveSuccessMessage}
        </div>
      )}

      {/* VIEW: Saved Roadmaps Library */}
      {activeTab === 'library' && (
        <div className="editorial-card">
          <div className="editorial-card-header">
            <div>
              <div className="editorial-card-title">My Saved AI Roadmaps</div>
              <div style={{ fontSize: '0.75rem', color: 'var(--ink-tertiary)' }}>
                Track custom tech stack learning journeys created for your profile
              </div>
            </div>
            <span className="badge-outline">{savedRoadmaps.length} Saved</span>
          </div>

          {savedRoadmaps.length === 0 ? (
            <div style={{ padding: '2rem 0', textAlign: 'center', color: 'var(--ink-secondary)' }}>
              No custom roadmaps saved yet. Use the <strong>AI Curriculum Studio</strong> to generate and save your desired tech stack!
            </div>
          ) : (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
              {savedRoadmaps.map((item) => {
                const percent = item.totalMilestones > 0
                  ? Math.round((item.completedMilestones / item.totalMilestones) * 100)
                  : 0;
                return (
                  <div
                    key={item.id}
                    className="editorial-card"
                    style={{
                      margin: 0,
                      cursor: 'pointer',
                      border: '1px solid var(--border-stone)',
                      backgroundColor: 'var(--bg-surface)',
                      transition: 'border-color 0.15s ease'
                    }}
                    onClick={() => handleLoadSavedRoadmap(item)}
                  >
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                      <div>
                        <div style={{ fontWeight: 600, fontSize: '1rem', color: 'var(--ink-primary)' }}>
                          {item.title}
                        </div>
                        <div style={{ fontSize: '0.8rem', color: 'var(--ink-secondary)', marginTop: '0.2rem' }}>
                          Target: <strong>{item.targetLevel}</strong> &bull; {item.timeframeWeeks} Weeks &bull; {item.learningFocus}
                        </div>
                        <div style={{ fontSize: '0.75rem', color: 'var(--ink-tertiary)', marginTop: '0.25rem' }}>
                          Architected by: {item.aiModelUsed}
                        </div>
                      </div>
                      <div style={{ textAlign: 'right' }}>
                        <button
                          type="button"
                          className="btn btn-secondary"
                          style={{ fontSize: '0.72rem', padding: '0.25rem 0.55rem', color: 'var(--signal-orange)' }}
                          onClick={(e) => handleDeleteSavedRoadmap(item.id, e)}
                        >
                          Delete
                        </button>
                      </div>
                    </div>

                    <div style={{ marginTop: '0.85rem' }}>
                      <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.75rem', marginBottom: '0.35rem' }}>
                        <span style={{ color: 'var(--ink-secondary)' }}>Milestones Completed</span>
                        <span className="tabular-nums font-mono" style={{ fontWeight: 600 }}>
                          {item.completedMilestones} / {item.totalMilestones} ({percent}%)
                        </span>
                      </div>
                      <div className="thin-progress-bar-container">
                        <div className="thin-progress-bar-fill" style={{ width: `${percent}%` }}></div>
                      </div>
                    </div>
                  </div>
                );
              })}
            </div>
          )}
        </div>
      )}

      {/* VIEW: Curriculum Studio */}
      {activeTab === 'generator' && (
        <>
          {/* Prompt Studio Card */}
          <div className="editorial-card" style={{ marginBottom: '2rem' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '1rem' }}>
              <div>
                <span style={{ fontSize: '0.75rem', fontWeight: 700, textTransform: 'uppercase', letterSpacing: '0.06em', color: 'var(--accent-forest)' }}>
                  Generative Career Engine
                </span>
                <h2 style={{ fontSize: '1.35rem', margin: '0.2rem 0 0.4rem 0' }}>AI Tech Stack Roadmap Architect</h2>
                <p style={{ fontSize: '0.875rem', margin: 0 }}>
                  Specify any modern technologies, languages, and frameworks. The AI reasons over architectural dependencies, evaluates your current acquired competencies, and synthesizes a production-grade curriculum.
                </p>
              </div>
              <span className="badge-outline" style={{ background: 'var(--accent-forest-subtle)', color: 'var(--accent-forest)', borderColor: 'var(--accent-forest)' }}>
                Gemini 1.5 & Adaptive AI
              </span>
            </div>

            {/* Quick Inspiration Chips */}
            <div style={{ marginBottom: '1.25rem' }}>
              <div style={{ fontSize: '0.775rem', fontWeight: 600, color: 'var(--ink-tertiary)', textTransform: 'uppercase', letterSpacing: '0.04em' }}>
                Quick Inspiration Stacks:
              </div>
              <div className="chip-stack-container">
                {PRESET_STACKS.map((preset) => (
                  <button
                    key={preset.label}
                    type="button"
                    className="chip-preset"
                    onClick={() => {
                      setDesiredTechStack(preset.value);
                    }}
                  >
                    + {preset.label}
                  </button>
                ))}
              </div>
            </div>

            {/* Generator Form */}
            <form onSubmit={handleGenerate}>
              <div className="form-group" style={{ marginBottom: '1.25rem' }}>
                <label className="form-label" style={{ fontWeight: 600 }}>
                  Desired Tech Stack or Architecture <span style={{ color: 'var(--signal-orange)' }}>*</span>
                </label>
                <textarea
                  className="form-input"
                  rows={2}
                  value={desiredTechStack}
                  onChange={(e) => setDesiredTechStack(e.target.value)}
                  placeholder="e.g. Next.js 15, FastAPI, PostgreSQL, Tailwind CSS, Docker, Redis"
                  required
                  style={{ fontFamily: 'var(--font-sans)', fontSize: '0.925rem' }}
                />
                <div style={{ fontSize: '0.75rem', color: 'var(--ink-tertiary)', marginTop: '0.25rem' }}>
                  Combine any frameworks, languages, databases, or cloud tools. Separate with commas or plus signs.
                </div>
              </div>

              <div className="grid-2" style={{ marginBottom: '1.25rem' }}>
                <div className="form-group">
                  <label className="form-label">Target Role / Specialization</label>
                  <input
                    type="text"
                    className="form-input"
                    value={targetRole}
                    onChange={(e) => setTargetRole(e.target.value)}
                    placeholder="e.g. Full-Stack Software Engineer"
                  />
                </div>

                <div className="form-group">
                  <label className="form-label">Target Proficiency Level</label>
                  <select
                    className="form-input"
                    value={targetLevel}
                    onChange={(e) => setTargetLevel(e.target.value)}
                  >
                    <option value="Beginner to Junior">Beginner to Junior (Foundations & Syntax)</option>
                    <option value="Junior to Mid-Level">Junior to Mid-Level (Production Engineering)</option>
                    <option value="Mid-Level to Senior">Mid-Level to Senior (System Design & Scale)</option>
                    <option value="Staff / Systems Architect">Staff / Systems Architect (Resilience & Enterprise)</option>
                  </select>
                </div>
              </div>

              <div className="grid-2" style={{ marginBottom: '1.25rem' }}>
                <div className="form-group">
                  <label className="form-label">Estimated Timeframe</label>
                  <select
                    className="form-input"
                    value={timeframeWeeks}
                    onChange={(e) => setTimeframeWeeks(e.target.value)}
                  >
                    <option value="4">4 Weeks (Intensive Sprint)</option>
                    <option value="8">8 Weeks (Accelerated Immersion)</option>
                    <option value="12">12 Weeks (Quarterly Mastery - Recommended)</option>
                    <option value="16">16 Weeks (Semester-Length Depth)</option>
                    <option value="24">24 Weeks (Comprehensive Engineering Capstone)</option>
                  </select>
                </div>

                <div className="form-group">
                  <label className="form-label">Learning Focus Strategy</label>
                  <select
                    className="form-input"
                    value={learningFocus}
                    onChange={(e) => setLearningFocus(e.target.value)}
                  >
                    <option value="Project-Driven">Project-Driven (Build Portfolio Capstones)</option>
                    <option value="System Internals & Architecture">System Internals & Memory/Concurrency</option>
                    <option value="Enterprise Production Hardening">Enterprise Production Hardening (CI/CD, Tests)</option>
                    <option value="Technical Interview Readiness">Technical Interview & Algorithmic Design</option>
                  </select>
                </div>
              </div>

              {/* Collapsible Custom Gemini Key */}
              <div style={{ marginBottom: '1.5rem', borderTop: '1px solid var(--border-stone)', paddingTop: '0.85rem' }}>
                <button
                  type="button"
                  style={{
                    background: 'none',
                    border: 'none',
                    color: 'var(--ink-secondary)',
                    fontSize: '0.8125rem',
                    cursor: 'pointer',
                    padding: 0,
                    textDecoration: 'underline'
                  }}
                  onClick={() => setShowApiKeyInput(!showApiKeyInput)}
                >
                  {showApiKeyInput ? 'Hide Custom Gemini Key Configuration' : 'Provide Custom Gemini API Key (Optional)'}
                </button>

                {showApiKeyInput && (
                  <div style={{ marginTop: '0.75rem', background: 'var(--bg-subtle)', padding: '0.85rem', borderRadius: 'var(--radius-sm)' }}>
                    <label className="form-label" style={{ fontSize: '0.8rem' }}>
                      Google Gemini API Key
                    </label>
                    <input
                      type="password"
                      className="form-input"
                      value={customApiKey}
                      onChange={(e) => setCustomApiKey(e.target.value)}
                      placeholder="AIzaSy..."
                      style={{ fontSize: '0.85rem', fontFamily: 'var(--font-mono)' }}
                    />
                    <div style={{ fontSize: '0.72rem', color: 'var(--ink-tertiary)', marginTop: '0.25rem' }}>
                      Leave empty to use the server-configured Gemini key or SkillForge Cognitive Architecture Engine.
                    </div>
                  </div>
                )}
              </div>

              <div style={{ display: 'flex', gap: '1rem', alignItems: 'center' }}>
                <button
                  type="submit"
                  className="btn btn-primary"
                  disabled={generating}
                  style={{
                    padding: '0.65rem 1.5rem',
                    fontSize: '0.925rem',
                    display: 'inline-flex',
                    alignItems: 'center',
                    gap: '0.5rem'
                  }}
                >
                  {generating ? (
                    <>
                      <span className="spinner-inline" />
                      Synthesizing Roadmap...
                    </>
                  ) : (
                    <>
                      <span>Generate Dynamic AI Roadmap</span> &rarr;
                    </>
                  )}
                </button>
              </div>
            </form>
          </div>

          {/* Dynamic AI Thinking State */}
          {generating && (
            <div className="ai-thinking-box">
              <div className="ai-pulse-dot" />
              <div>
                <div style={{ fontWeight: 700, fontSize: '0.95rem', color: 'var(--accent-forest)' }}>
                  SkillForge Cognitive Architecture Engine in Progress
                </div>
                <div style={{ fontSize: '0.875rem', color: 'var(--ink-primary)', marginTop: '0.25rem' }}>
                  {THINKING_STEPS[thinkingStepIndex]}
                </div>
                <div style={{ fontSize: '0.75rem', color: 'var(--ink-tertiary)', marginTop: '0.35rem' }}>
                  Building individualized step-by-step milestones tailored to {desiredTechStack}...
                </div>
              </div>
            </div>
          )}

          {/* Generated Roadmap Result */}
          {generatedRoadmap && !generating && (
            <div>
              {/* Executive Summary Card */}
              <div className="editorial-card" style={{ marginBottom: '1.75rem' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap', gap: '1rem', marginBottom: '1rem' }}>
                  <div>
                    <span style={{ fontSize: '0.72rem', fontWeight: 700, textTransform: 'uppercase', letterSpacing: '0.06em', color: 'var(--accent-forest)' }}>
                      Architectural Blueprint
                    </span>
                    <h2 style={{ fontSize: '1.5rem', margin: '0.2rem 0 0.5rem 0' }}>
                      {generatedRoadmap.techStack}
                    </h2>
                    <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.5rem', alignItems: 'center' }}>
                      <span className="badge-outline">
                        {generatedRoadmap.targetRole}
                      </span>
                      <span className="badge-outline">
                        {generatedRoadmap.experienceLevel}
                      </span>
                      <span className="badge-outline">
                        {generatedRoadmap.timeframeWeeks} Weeks
                      </span>
                      <span className="badge-outline">
                        {generatedRoadmap.learningFocus}
                      </span>
                      <span className="badge-outline" style={{ background: 'var(--accent-forest-subtle)', color: 'var(--accent-forest)' }}>
                        Generated by: {generatedRoadmap.generatedBy}
                      </span>
                    </div>
                  </div>

                  <div>
                    {generatedRoadmap.saved ? (
                      <span className="badge-outline" style={{ color: 'var(--accent-forest)', borderColor: 'var(--accent-forest)', fontWeight: 600 }}>
                        Saved in Your Library
                      </span>
                    ) : (
                      <button
                        type="button"
                        className="btn btn-primary"
                        onClick={handleSaveRoadmap}
                        disabled={savingRoadmap}
                        style={{ fontSize: '0.85rem' }}
                      >
                        {savingRoadmap ? 'Saving...' : 'Save to My Roadmaps'}
                      </button>
                    )}
                  </div>
                </div>

                <p style={{ fontSize: '0.95rem', lineHeight: 1.6, color: 'var(--ink-primary)', marginBottom: '1rem' }}>
                  {generatedRoadmap.executiveSummary}
                </p>

                {/* AI Thinking Notes & Profile Synergy */}
                {generatedRoadmap.aiThinkingNotes && (
                  <div style={{
                    backgroundColor: 'var(--bg-subtle)',
                    borderLeft: '4px solid var(--accent-forest)',
                    padding: '0.85rem 1rem',
                    borderRadius: '0 var(--radius-sm) var(--radius-sm) 0',
                    marginBottom: '1rem'
                  }}>
                    <div style={{ fontSize: '0.75rem', fontWeight: 700, textTransform: 'uppercase', letterSpacing: '0.05em', color: 'var(--accent-forest)', marginBottom: '0.25rem' }}>
                      AI Cognitive Reasoning & Profile Synergy
                    </div>
                    <div style={{ fontSize: '0.875rem', color: 'var(--ink-primary)', lineHeight: 1.55 }}>
                      {generatedRoadmap.aiThinkingNotes}
                    </div>
                  </div>
                )}

                {/* System Architecture Overview */}
                {generatedRoadmap.architectureOverview && (
                  <div style={{ fontSize: '0.85rem', color: 'var(--ink-secondary)', borderTop: '1px solid var(--border-stone)', paddingTop: '0.75rem' }}>
                    <strong style={{ color: 'var(--ink-primary)' }}>Architecture Overview:</strong> {generatedRoadmap.architectureOverview}
                  </div>
                )}
              </div>

              {/* Prerequisites checklist */}
              {generatedRoadmap.prerequisites && generatedRoadmap.prerequisites.length > 0 && (
                <div className="editorial-card" style={{ marginBottom: '1.75rem' }}>
                  <div className="editorial-card-header">
                    <div className="editorial-card-title">Foundational Prerequisites</div>
                    <span className="badge-outline">{generatedRoadmap.prerequisites.length} Items</span>
                  </div>
                  <ul style={{ margin: 0, paddingLeft: '1.25rem', fontSize: '0.875rem', color: 'var(--ink-secondary)' }}>
                    {generatedRoadmap.prerequisites.map((p, idx) => (
                      <li key={idx} style={{ marginBottom: '0.35rem' }}>{p}</li>
                    ))}
                  </ul>
                </div>
              )}

              {/* Phases and Milestones */}
              <div style={{ marginBottom: '2rem' }}>
                <h3 style={{ fontSize: '1.25rem', marginBottom: '1rem' }}>Sequenced Learning Phases</h3>
                {generatedRoadmap.phases && generatedRoadmap.phases.map((phase, pIdx) => (
                  <div key={pIdx} className="phase-card">
                    <div className="phase-header">
                      <div>
                        <span style={{ fontSize: '0.72rem', fontWeight: 700, textTransform: 'uppercase', letterSpacing: '0.05em', color: 'var(--accent-forest)' }}>
                          Phase {phase.phaseNumber} ({phase.estimatedWeeks} Weeks)
                        </span>
                        <div style={{ fontSize: '1.1rem', fontWeight: 600, color: 'var(--ink-primary)', marginTop: '0.15rem' }}>
                          {phase.phaseTitle}
                        </div>
                        <div style={{ fontSize: '0.825rem', color: 'var(--ink-secondary)', marginTop: '0.2rem' }}>
                          {phase.phaseGoal}
                        </div>
                      </div>
                      <span className="badge-outline">
                        {phase.milestones ? phase.milestones.length : 0} Milestones
                      </span>
                    </div>

                    {/* Milestones in this phase */}
                    <div>
                      {phase.milestones && phase.milestones.map((milestone) => (
                        <div key={milestone.id} className="milestone-card">
                          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap', gap: '0.75rem' }}>
                            <div style={{ flex: 1, minWidth: '280px' }}>
                              <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '0.25rem' }}>
                                <span className="tabular-nums font-mono" style={{ fontSize: '0.75rem', color: 'var(--ink-tertiary)', fontWeight: 600 }}>
                                  #{milestone.id}
                                </span>
                                <h4 style={{ margin: 0, fontSize: '0.975rem' }}>{milestone.title}</h4>
                                <span className="badge-outline" style={{ fontSize: '0.68rem' }}>
                                  ~{milestone.estimatedHours} hrs
                                </span>
                              </div>
                              <p style={{ fontSize: '0.85rem', margin: '0 0 0.5rem 0', color: 'var(--ink-secondary)' }}>
                                {milestone.summary}
                              </p>

                              {/* Competencies Chips */}
                              {milestone.competencies && milestone.competencies.length > 0 && (
                                <div style={{ marginBottom: '0.45rem' }}>
                                  {milestone.competencies.map((c, cIdx) => (
                                    <span key={cIdx} className="tag-badge">{c}</span>
                                  ))}
                                </div>
                              )}

                              {/* Key Topics */}
                              {milestone.keyTopics && milestone.keyTopics.length > 0 && (
                                <div style={{ fontSize: '0.8rem', color: 'var(--ink-secondary)', marginBottom: '0.5rem' }}>
                                  <strong>Key Topics:</strong> {milestone.keyTopics.join(' &bull; ')}
                                </div>
                              )}

                              {/* Project Deliverable */}
                              {milestone.projectDeliverable && (
                                <div className="deliverable-callout">
                                  <strong>Hands-on Deliverable:</strong> {milestone.projectDeliverable}
                                </div>
                              )}

                              {/* Resources */}
                              {milestone.recommendedResources && milestone.recommendedResources.length > 0 && (
                                <div style={{ fontSize: '0.75rem', color: 'var(--ink-tertiary)', marginTop: '0.45rem' }}>
                                  Resources: {milestone.recommendedResources.join(' | ')}
                                </div>
                              )}
                            </div>

                            {/* Milestone Status Toggle */}
                            <div style={{ alignSelf: 'flex-start' }}>
                              <div className="segmented-control">
                                <button
                                  type="button"
                                  className={`segmented-option ${milestone.status === 'NOT_STARTED' ? 'active' : ''}`}
                                  onClick={() => handleMilestoneStatusChange(pIdx, milestone.id, 'NOT_STARTED')}
                                >
                                  Not started
                                </button>
                                <button
                                  type="button"
                                  className={`segmented-option ${milestone.status === 'IN_PROGRESS' ? 'active' : ''}`}
                                  onClick={() => handleMilestoneStatusChange(pIdx, milestone.id, 'IN_PROGRESS')}
                                >
                                  In progress
                                </button>
                                <button
                                  type="button"
                                  className={`segmented-option ${milestone.status === 'COMPLETED' ? 'active-completed' : ''}`}
                                  onClick={() => handleMilestoneStatusChange(pIdx, milestone.id, 'COMPLETED')}
                                >
                                  Completed
                                </button>
                              </div>
                            </div>
                          </div>
                        </div>
                      ))}
                    </div>
                  </div>
                ))}
              </div>

              {/* Capstone Projects Section */}
              {generatedRoadmap.recommendedProjects && generatedRoadmap.recommendedProjects.length > 0 && (
                <div className="editorial-card" style={{ marginBottom: '2rem' }}>
                  <div className="editorial-card-header">
                    <div>
                      <div className="editorial-card-title">Recommended Production Capstone Projects</div>
                      <div style={{ fontSize: '0.75rem', color: 'var(--ink-tertiary)' }}>
                        Build and showcase these architectural projects on GitHub to prove your mastery
                      </div>
                    </div>
                    <span className="badge-outline">Portfolio Highlights</span>
                  </div>

                  <div className="capstone-grid">
                    {generatedRoadmap.recommendedProjects.map((proj, pIdx) => (
                      <div key={pIdx} className="capstone-card">
                        <div>
                          <div style={{ fontSize: '0.72rem', fontWeight: 700, color: 'var(--accent-forest)', textTransform: 'uppercase', letterSpacing: '0.04em' }}>
                            Capstone Spec #{pIdx + 1}
                          </div>
                          <p style={{ fontSize: '0.875rem', color: 'var(--ink-primary)', marginTop: '0.35rem', lineHeight: 1.5 }}>
                            {proj}
                          </p>
                        </div>
                        <div style={{ fontSize: '0.72rem', color: 'var(--ink-tertiary)', borderTop: '1px solid var(--border-stone)', paddingTop: '0.5rem', marginTop: '0.75rem' }}>
                          Verified for Portfolio Readiness
                        </div>
                      </div>
                    ))}
                  </div>
                </div>
              )}
            </div>
          )}
        </>
      )}
    </div>
  );
}
