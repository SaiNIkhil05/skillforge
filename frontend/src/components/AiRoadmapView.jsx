import React, { useState, useEffect, useRef } from 'react';
import { api } from '../api';

const QUICK_PROMPTS = [
  {
    icon: '⚡',
    label: 'Next.js 15 & FastAPI Roadmap',
    prompt: 'Please generate a comprehensive 12-week roadmap for Next.js 15, FastAPI, and PostgreSQL for a Full-Stack Engineer role.'
  },
  {
    icon: '⚙️',
    label: 'Go & Kubernetes Backend',
    prompt: 'Create a learning path for Go microservices, Apache Kafka, and Kubernetes for a Backend Systems Engineer.'
  },
  {
    icon: '🦀',
    label: 'Compare Rust vs Go',
    prompt: 'Compare Rust vs Go for high-throughput backend microservices and explain when to choose each.'
  },
  {
    icon: '🧠',
    label: 'AI Agents & PyTorch',
    prompt: 'Generate an AI systems roadmap covering Python, PyTorch, LangChain, and pgvector for an AI Engineer role.'
  },
  {
    icon: '💡',
    label: 'What makes FastAPI fast?',
    prompt: 'Explain what FastAPI is, why it is faster than Flask/Django, and how Starlette and Pydantic work under the hood.'
  },
  {
    icon: '☕',
    label: 'Spring Boot 3 Enterprise Path',
    prompt: 'Provide a production-ready roadmap for Java 17, Spring Boot 3, and Angular 18 for Enterprise Full-Stack development.'
  },
  {
    icon: '🚀',
    label: 'System Design Interview Prep',
    prompt: 'What are the most crucial distributed systems patterns I should master for junior to mid-level engineering interviews?'
  }
];

const THINKING_STEPS = [
  'Analyzing inquiry with live intelligence model...',
  'Cross-referencing your SkillForge competencies and career goals...',
  'Reasoning through architectural layers and dependencies...',
  'Formulating structured guidance and verified deliverables...',
  'Finalizing response...'
];

// Helper to format chat message text with markdown-like styling
function FormatChatContent({ content }) {
  if (!content) return null;

  // Split by double line breaks into paragraphs
  const blocks = content.split(/\n\n+/);

  return (
    <div className="ai-msg-body">
      {blocks.map((block, bIdx) => {
        const trimmed = block.trim();
        if (!trimmed) return null;

        // Check if block is a bulleted list
        const lines = trimmed.split('\n');
        const isBulletList = lines.every((line) => line.trim().startsWith('* ') || line.trim().startsWith('- ') || line.trim().startsWith('• '));
        const isNumberedList = lines.every((line) => /^\d+\.\s/.test(line.trim()));

        if (isBulletList) {
          return (
            <ul key={bIdx}>
              {lines.map((line, lIdx) => {
                const text = line.trim().replace(/^[-*•]\s+/, '');
                return <li key={lIdx}>{formatInlineText(text)}</li>;
              })}
            </ul>
          );
        }

        if (isNumberedList) {
          return (
            <ol key={bIdx}>
              {lines.map((line, lIdx) => {
                const text = line.trim().replace(/^\d+\.\s+/, '');
                return <li key={lIdx}>{formatInlineText(text)}</li>;
              })}
            </ol>
          );
        }

        // Regular paragraph (may have single line breaks inside)
        return (
          <p key={bIdx}>
            {lines.map((line, lIdx) => (
              <React.Fragment key={lIdx}>
                {formatInlineText(line)}
                {lIdx < lines.length - 1 && <br />}
              </React.Fragment>
            ))}
          </p>
        );
      })}
    </div>
  );
}

// Formats **bold** and `code` inline
function formatInlineText(text) {
  if (!text) return null;

  // Split by inline code: `code`
  const codeParts = text.split(/(`[^`]+`)/g);

  return codeParts.map((part, pIdx) => {
    if (part.startsWith('`') && part.endsWith('`') && part.length > 2) {
      return (
        <code key={pIdx}>
          {part.slice(1, -1)}
        </code>
      );
    }

    // Split by bold: **bold**
    const boldParts = part.split(/(\*\*[^*]+\*\*)/g);
    return boldParts.map((bPart, bIdx) => {
      if (bPart.startsWith('**') && bPart.endsWith('**') && bPart.length > 4) {
        return <strong key={bIdx}>{bPart.slice(2, -2)}</strong>;
      }
      return bPart;
    });
  });
}

// Embedded Interactive Roadmap Blueprint Component
function EmbeddedRoadmapBlueprint({ roadmap, onSave, isSaving, onMilestoneStatusChange }) {
  if (!roadmap) return null;

  return (
    <div className="ai-chat-embedded-roadmap">
      {/* Blueprint Header */}
      <div className="ai-blueprint-card" style={{ marginBottom: '1.25rem' }}>
        <div className="ai-blueprint-header">
          <div>
            <div className="ai-section-eyebrow">✦ GENERATED LEARNING BLUEPRINT</div>
            <h3 className="ai-blueprint-title" style={{ fontSize: '1.45rem' }}>{roadmap.techStack}</h3>
            <div className="ai-meta-pills">
              {roadmap.targetRole && <span className="ai-meta-pill role">{roadmap.targetRole}</span>}
              {roadmap.experienceLevel && <span className="ai-meta-pill level">{roadmap.experienceLevel}</span>}
              {roadmap.timeframeWeeks && <span className="ai-meta-pill time">{roadmap.timeframeWeeks} Weeks</span>}
              {roadmap.learningFocus && <span className="ai-meta-pill focus">{roadmap.learningFocus}</span>}
              <span className="ai-meta-pill engine">Engine: {roadmap.generatedBy || 'SkillForge Live AI'}</span>
            </div>
          </div>

          <div className="ai-save-action-box">
            {roadmap.saved ? (
              <span className="ai-saved-indicator">
                ✓ Saved in Library
              </span>
            ) : (
              <button
                type="button"
                className="btn btn-primary"
                onClick={() => onSave(roadmap)}
                disabled={isSaving}
              >
                {isSaving ? 'Saving...' : 'Save Roadmap to Library'}
              </button>
            )}
          </div>
        </div>

        {roadmap.executiveSummary && (
          <div className="ai-blueprint-summary">
            <p>{roadmap.executiveSummary}</p>
          </div>
        )}

        {roadmap.aiThinkingNotes && (
          <div className="ai-synergy-callout">
            <div className="ai-synergy-header">
              <span className="sparkle-symbol">✦</span>
              <span>AI Reasoning & Profile Synergy</span>
            </div>
            <p className="ai-synergy-content">{roadmap.aiThinkingNotes}</p>
          </div>
        )}

        {roadmap.architectureOverview && (
          <div className="ai-arch-overview">
            <strong>System Architecture Overview:</strong> {roadmap.architectureOverview}
          </div>
        )}
      </div>

      {/* Prerequisites Checklist */}
      {roadmap.prerequisites && roadmap.prerequisites.length > 0 && (
        <div className="ai-prereq-card" style={{ marginBottom: '1.25rem' }}>
          <div className="ai-card-title-bar">
            <div className="ai-card-heading">Foundational Prerequisites</div>
            <span className="badge-outline">{roadmap.prerequisites.length} Checklist Items</span>
          </div>
          <div className="ai-prereq-grid">
            {roadmap.prerequisites.map((p, idx) => (
              <div key={idx} className="ai-prereq-item">
                <span className="ai-check-circle">✓</span>
                <span>{p}</span>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Sequenced Learning Phases */}
      {roadmap.phases && roadmap.phases.length > 0 && (
        <div className="ai-phases-timeline">
          <div className="ai-timeline-header">
            <h4 className="ai-timeline-title">Sequenced Learning Phases</h4>
            <span className="badge-outline">{roadmap.phases.length} Progressive Phases</span>
          </div>

          {roadmap.phases.map((phase, pIdx) => (
            <div key={pIdx} className="ai-timeline-phase-card">
              <div className="ai-phase-banner">
                <div className="ai-phase-badge-col">
                  <span className="ai-phase-number">PHASE {phase.phaseNumber}</span>
                  <span className="ai-phase-duration font-mono">{phase.estimatedWeeks} WEEKS</span>
                </div>
                <div className="ai-phase-title-col">
                  <h5 className="ai-phase-heading">{phase.phaseTitle}</h5>
                  <div className="ai-phase-goal">{phase.phaseGoal}</div>
                </div>
                <div className="ai-phase-milestones-count">
                  <span className="badge-outline">
                    {phase.milestones ? phase.milestones.length : 0} Milestones
                  </span>
                </div>
              </div>

              {/* Milestones inside Phase */}
              <div className="ai-milestones-list">
                {phase.milestones && phase.milestones.map((m) => (
                  <div key={m.id} className="ai-milestone-row">
                    <div className="ai-milestone-details">
                      <div className="ai-milestone-title-row">
                        <span className="ai-milestone-code font-mono">#{m.id}</span>
                        <h6 className="ai-milestone-title">{m.title}</h6>
                        <span className="ai-hours-badge">~{m.estimatedHours} hrs</span>
                      </div>

                      <p className="ai-milestone-summary">{m.summary}</p>

                      {m.competencies && m.competencies.length > 0 && (
                        <div className="ai-competency-chips">
                          {m.competencies.map((c, cIdx) => (
                            <span key={cIdx} className="tag-badge">{c}</span>
                          ))}
                        </div>
                      )}

                      {m.keyTopics && m.keyTopics.length > 0 && (
                        <div className="ai-key-topics">
                          <strong>Core Topics:</strong> {m.keyTopics.join(' • ')}
                        </div>
                      )}

                      {m.projectDeliverable && (
                        <div className="ai-deliverable-box">
                          <span className="ai-deliverable-tag">HANDS-ON DELIVERABLE</span>
                          <div>{m.projectDeliverable}</div>
                        </div>
                      )}

                      {m.recommendedResources && m.recommendedResources.length > 0 && (
                        <div className="ai-resources-row">
                          <span className="ai-res-label">Resources:</span>
                          <span>{m.recommendedResources.join(' | ')}</span>
                        </div>
                      )}
                    </div>

                    {/* Milestone Status Toggle */}
                    <div className="ai-milestone-action-col">
                      <div className="segmented-control">
                        <button
                          type="button"
                          className={`segmented-option ${m.status === 'NOT_STARTED' ? 'active' : ''}`}
                          onClick={() => onMilestoneStatusChange(roadmap, pIdx, m.id, 'NOT_STARTED')}
                        >
                          Not started
                        </button>
                        <button
                          type="button"
                          className={`segmented-option ${m.status === 'IN_PROGRESS' ? 'active' : ''}`}
                          onClick={() => onMilestoneStatusChange(roadmap, pIdx, m.id, 'IN_PROGRESS')}
                        >
                          In progress
                        </button>
                        <button
                          type="button"
                          className={`segmented-option ${m.status === 'COMPLETED' ? 'active-completed' : ''}`}
                          onClick={() => onMilestoneStatusChange(roadmap, pIdx, m.id, 'COMPLETED')}
                        >
                          Completed
                        </button>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Capstone Projects Section */}
      {roadmap.recommendedProjects && roadmap.recommendedProjects.length > 0 && (
        <div className="ai-capstones-wrapper" style={{ marginTop: '1.25rem' }}>
          <div className="ai-card-title-bar">
            <div>
              <div className="ai-card-heading">Recommended Production Capstone Projects</div>
              <div className="ai-card-subheading">
                Enterprise-grade projects designed for portfolio showcase
              </div>
            </div>
            <span className="badge-outline">Portfolio Highlights</span>
          </div>

          <div className="ai-capstones-grid">
            {roadmap.recommendedProjects.map((proj, idx) => (
              <div key={idx} className="ai-capstone-item">
                <div className="ai-capstone-number">CAPSTONE #{idx + 1}</div>
                <p className="ai-capstone-desc">{proj}</p>
                <div className="ai-capstone-seal">✓ Verified for Career Readiness</div>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
}

export default function AiRoadmapView({ user }) {
  const [activeSubTab, setActiveSubTab] = useState('chat'); // 'chat' | 'library'
  const [messages, setMessages] = useState([
    {
      id: 'welcome',
      role: 'assistant',
      content: `Hello ${user?.fullName ? user.fullName.split(' ')[0] : 'there'}! I am your **SkillForge AI Assistant**, connected to a live intelligence engine.\n\nYou can chat with me naturally about any technical topic — languages, architectures, databases, system design, or interview advice.\n\nWhenever you need a **custom tech stack roadmap**, just ask me (e.g., *"Build me a 12-week roadmap for Next.js 15 & FastAPI"*), and I will generate an interactive, milestone-tracked blueprint directly in our chat!`,
      modelUsed: 'Live AI Engine (GPT-4o)',
      roadmap: null,
      timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
    }
  ]);

  const [inputMessage, setInputMessage] = useState('');
  const [isThinking, setIsThinking] = useState(false);
  const [thinkingStepIndex, setThinkingStepIndex] = useState(0);
  const [customApiKey, setCustomApiKey] = useState('');
  const [showApiKeyDrawer, setShowApiKeyDrawer] = useState(false);
  const [savedRoadmaps, setSavedRoadmaps] = useState([]);
  const [savingRoadmapIds, setSavingRoadmapIds] = useState(new Set());
  const [notification, setNotification] = useState(null);

  const messagesEndRef = useRef(null);
  const textareaRef = useRef(null);

  useEffect(() => {
    loadSavedRoadmaps();
  }, []);

  useEffect(() => {
    scrollToBottom();
  }, [messages, isThinking]);

  useEffect(() => {
    let interval;
    if (isThinking) {
      setThinkingStepIndex(0);
      interval = setInterval(() => {
        setThinkingStepIndex((prev) => (prev + 1) % THINKING_STEPS.length);
      }, 1600);
    }
    return () => clearInterval(interval);
  }, [isThinking]);

  const scrollToBottom = () => {
    if (messagesEndRef.current) {
      messagesEndRef.current.scrollIntoView({ behavior: 'smooth' });
    }
  };

  const loadSavedRoadmaps = async () => {
    try {
      const roadmaps = await api.getSavedAiRoadmaps();
      setSavedRoadmaps(roadmaps || []);
    } catch (err) {
      console.warn('Could not load saved roadmaps:', err.message);
    }
  };

  const showNotice = (msg, type = 'info') => {
    setNotification({ msg, type });
    setTimeout(() => {
      setNotification((curr) => (curr?.msg === msg ? null : curr));
    }, 4500);
  };

  const handleSendMessage = async (customText = null) => {
    const textToSend = (customText !== null ? customText : inputMessage).trim();
    if (!textToSend || isThinking) return;

    const userMsg = {
      id: 'msg-' + Date.now(),
      role: 'user',
      content: textToSend,
      roadmap: null,
      timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
    };

    const nextHistory = [...messages, userMsg];
    setMessages(nextHistory);
    setInputMessage('');
    setIsThinking(true);

    try {
      // Build conversation turns for LLM
      const payloadMessages = nextHistory
        .filter((m) => m.role === 'user' || m.role === 'assistant')
        .map((m) => ({ role: m.role, content: m.content }));

      const response = await api.chatWithAi(payloadMessages, customApiKey.trim() || null);

      const assistantMsg = {
        id: 'reply-' + Date.now(),
        role: 'assistant',
        content: response.reply || 'Here is the requested information.',
        modelUsed: response.modelUsed || 'Live AI Engine',
        roadmap: response.roadmap || null,
        timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
      };

      setMessages((prev) => [...prev, assistantMsg]);

      if (response.roadmap) {
        showNotice('Generated interactive roadmap! You can toggle milestone progress or save it to your library below.', 'success');
      }
    } catch (err) {
      console.error('AI Chat error:', err);
      const errMsg = {
        id: 'err-' + Date.now(),
        role: 'assistant',
        content: `I encountered an issue communicating with the AI service: ${err.message}. Please check your connection or try again.`,
        modelUsed: 'SkillForge System',
        roadmap: null,
        timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
      };
      setMessages((prev) => [...prev, errMsg]);
      showNotice('Communication error: ' + err.message, 'error');
    } finally {
      setIsThinking(false);
      if (textareaRef.current) {
        textareaRef.current.focus();
      }
    }
  };

  const handleKeyDown = (e) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleSendMessage();
    }
  };

  const handleNewChat = () => {
    setMessages([
      {
        id: 'welcome-' + Date.now(),
        role: 'assistant',
        content: `Fresh conversation started! Ask me any engineering question or ask for a personalized tech stack roadmap.`,
        modelUsed: 'Live AI Engine (GPT-4o)',
        roadmap: null,
        timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
      }
    ]);
    setInputMessage('');
    showNotice('Started new conversation thread.', 'info');
  };

  const handleSaveRoadmap = async (roadmapObj) => {
    if (!roadmapObj) return;
    const tempKey = roadmapObj.techStack || 'curr';
    setSavingRoadmapIds((prev) => new Set([...prev, tempKey]));

    try {
      const saved = await api.saveAiRoadmap(roadmapObj);

      // Update message state so this roadmap is marked as saved
      setMessages((prev) =>
        prev.map((m) => {
          if (m.roadmap && (m.roadmap === roadmapObj || m.roadmap.techStack === roadmapObj.techStack)) {
            return {
              ...m,
              roadmap: {
                ...m.roadmap,
                id: saved.id,
                saved: true
              }
            };
          }
          return m;
        })
      );

      await loadSavedRoadmaps();
      showNotice(`"${saved.title}" saved to your Library!`, 'success');
    } catch (err) {
      showNotice('Failed to save roadmap: ' + err.message, 'error');
    } finally {
      setSavingRoadmapIds((prev) => {
        const next = new Set(prev);
        next.delete(tempKey);
        return next;
      });
    }
  };

  const handleMilestoneStatusChange = async (targetRoadmap, phaseIndex, milestoneId, newStatus) => {
    // Optimistically update inside messages
    setMessages((prev) =>
      prev.map((msg) => {
        if (!msg.roadmap || msg.roadmap !== targetRoadmap) return msg;

        const updatedPhases = msg.roadmap.phases.map((phase, pIdx) => {
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

        return {
          ...msg,
          roadmap: {
            ...msg.roadmap,
            phases: updatedPhases
          }
        };
      })
    );

    // If already saved in DB, persist milestone status to backend
    if (targetRoadmap.id) {
      try {
        await api.updateAiMilestoneStatus(targetRoadmap.id, milestoneId, newStatus);
        await loadSavedRoadmaps();
      } catch (err) {
        console.warn('Backend milestone status update error:', err.message);
      }
    }
  };

  const handleLoadSavedRoadmapIntoChat = (savedItem) => {
    try {
      const parsed = JSON.parse(savedItem.roadmapJson);
      parsed.id = savedItem.id;
      parsed.saved = true;

      const loadedMsg = {
        id: 'loaded-' + Date.now(),
        role: 'assistant',
        content: `Here is your saved roadmap **"${savedItem.title}"** loaded from your library. You can continue updating your milestone progress below or ask me any follow-up questions about it!`,
        modelUsed: savedItem.aiModelUsed || 'SkillForge Saved Library',
        roadmap: parsed,
        timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
      };

      setMessages((prev) => [...prev, loadedMsg]);
      setActiveSubTab('chat');
      showNotice(`Loaded "${savedItem.title}" into chat.`, 'info');
    } catch (err) {
      showNotice('Could not open saved roadmap: ' + err.message, 'error');
    }
  };

  const handleDeleteSavedRoadmap = async (id, title, e) => {
    e.stopPropagation();
    if (!window.confirm(`Delete "${title}" from your library?`)) return;
    try {
      await api.deleteAiRoadmap(id);
      await loadSavedRoadmaps();
      showNotice('Roadmap deleted from library.', 'info');
    } catch (err) {
      showNotice('Delete failed: ' + err.message, 'error');
    }
  };

  return (
    <div className="ai-page-wrapper">
      {/* Page Header */}
      <div className="ai-page-header">
        <div className="ai-page-header-text">
          <div className="ai-pill-badge">
            <span className="sparkle-symbol">✦</span> AUTONOMOUS ARCHITECT & CHATBOT
          </div>
          <h1 className="ai-page-title">SkillForge AI Chat & Roadmap Architect</h1>
          <p className="ai-page-subtitle">
            Chat like ChatGPT or Gemini about any technical concept, or ask for dynamic learning roadmaps tailored to your target tech stack and verified profile competencies.
          </p>
        </div>

        {/* View Switcher Controls */}
        <div className="ai-header-controls">
          <div className="ai-view-switch">
            <button
              type="button"
              className={`ai-view-switch-btn ${activeSubTab === 'chat' ? 'active' : ''}`}
              onClick={() => setActiveSubTab('chat')}
            >
              <span className="sparkle-symbol">✦</span>
              <span>AI Chatbot & Architect</span>
            </button>
            <button
              type="button"
              className={`ai-view-switch-btn ${activeSubTab === 'library' ? 'active' : ''}`}
              onClick={() => setActiveSubTab('library')}
            >
              <span>Saved Roadmaps</span>
              <span className="ai-count-chip">{savedRoadmaps.length}</span>
            </button>
          </div>
        </div>
      </div>

      {/* Floating Notification */}
      {notification && (
        <div className={`ai-notification-banner ${notification.type}`}>
          <span>{notification.msg}</span>
          <button type="button" onClick={() => setNotification(null)} className="ai-notification-close">×</button>
        </div>
      )}

      {/* ========================================================
          SUB-TAB 1: AI CHATBOT & ARCHITECT INTERFACE
         ======================================================== */}
      {activeSubTab === 'chat' && (
        <div className="ai-chat-card">
          {/* Chat Topbar */}
          <div className="ai-chat-topbar">
            <div className="ai-chat-topbar-left">
              <div className="ai-avatar-badge">✦</div>
              <div>
                <h2 className="ai-chat-topbar-title">
                  SkillForge Conversational AI
                  <span className="ai-model-status-pill">
                    <span className="ai-online-dot" /> Live Model Connected
                  </span>
                </h2>
                <div style={{ fontSize: '0.75rem', color: 'var(--ink-tertiary)', marginTop: '2px' }}>
                  Powered by Live GPT-4o / Gemini with Adaptive Domain Decomposition
                </div>
              </div>
            </div>

            <div className="ai-chat-topbar-actions">
              <button
                type="button"
                className="ai-new-chat-btn"
                onClick={handleNewChat}
                title="Start a fresh conversation thread"
              >
                <span>+</span> New Chat
              </button>
            </div>
          </div>

          {/* Chat Message Stream */}
          <div className="ai-chat-stream">
            {messages.map((msg) => {
              const isUser = msg.role === 'user';
              return (
                <div key={msg.id} className={`ai-msg-row ${isUser ? 'user' : 'assistant'}`}>
                  {isUser ? (
                    <div className="ai-msg-user-avatar" title={user?.fullName || 'Student'}>
                      {user?.fullName ? user.fullName.charAt(0).toUpperCase() : 'U'}
                    </div>
                  ) : (
                    <div className="ai-msg-assistant-avatar" title="SkillForge AI">
                      ✦
                    </div>
                  )}

                  <div className="ai-msg-bubble-wrap">
                    <div className="ai-msg-bubble">
                      <FormatChatContent content={msg.content} />

                      {/* Embedded Interactive Roadmap if present */}
                      {msg.roadmap && (
                        <EmbeddedRoadmapBlueprint
                          roadmap={msg.roadmap}
                          onSave={handleSaveRoadmap}
                          isSaving={savingRoadmapIds.has(msg.roadmap.techStack || 'curr')}
                          onMilestoneStatusChange={handleMilestoneStatusChange}
                        />
                      )}
                    </div>

                    <div className="ai-msg-meta">
                      <span>{msg.timestamp}</span>
                      {!isUser && msg.modelUsed && (
                        <span>• {msg.modelUsed}</span>
                      )}
                    </div>
                  </div>
                </div>
              );
            })}

            {/* Thinking / Reasoning Bubble */}
            {isThinking && (
              <div className="ai-msg-row assistant">
                <div className="ai-msg-assistant-avatar">✦</div>
                <div className="ai-msg-bubble-wrap">
                  <div className="ai-thinking-bubble">
                    <div className="ai-typing-dots">
                      <span className="ai-typing-dot" />
                      <span className="ai-typing-dot" />
                      <span className="ai-typing-dot" />
                    </div>
                    <span className="ai-thinking-text">{THINKING_STEPS[thinkingStepIndex]}</span>
                  </div>
                </div>
              </div>
            )}

            <div ref={messagesEndRef} />
          </div>

          {/* Quick Inspiration Prompt Chips */}
          <div className="ai-chat-suggestions-bar">
            <div className="ai-suggestions-label">
              <span className="sparkle-symbol">✦</span> Quick Inquiry & Roadmap Prompts:
            </div>
            <div className="ai-suggestions-chips">
              {QUICK_PROMPTS.map((item, idx) => (
                <button
                  key={idx}
                  type="button"
                  className="ai-suggestion-chip"
                  onClick={() => handleSendMessage(item.prompt)}
                  disabled={isThinking}
                >
                  <span>{item.icon}</span>
                  <span>{item.label}</span>
                </button>
              ))}
            </div>
          </div>

          {/* Collapsible Custom Gemini Key Drawer */}
          <div style={{ padding: '0.4rem 1.25rem', backgroundColor: '#FAF8F5', borderTop: '1px solid var(--border-stone)' }}>
            <button
              type="button"
              className="ai-accordion-trigger"
              style={{ fontSize: '0.78rem', padding: '0.2rem 0' }}
              onClick={() => setShowApiKeyDrawer(!showApiKeyDrawer)}
            >
              <span>{showApiKeyDrawer ? '▾ Hide' : '▸ Optional'} Custom Google Gemini API Key</span>
              <span className="badge-outline" style={{ fontSize: '0.68rem' }}>Default: Live AI Active</span>
            </button>

            {showApiKeyDrawer && (
              <div style={{ marginTop: '0.5rem', paddingBottom: '0.5rem' }}>
                <input
                  type="password"
                  className="ai-config-input font-mono"
                  style={{ fontSize: '0.8rem', padding: '0.4rem 0.6rem' }}
                  value={customApiKey}
                  onChange={(e) => setCustomApiKey(e.target.value)}
                  placeholder="AIzaSy... (leave blank to use SkillForge's built-in live AI engine)"
                />
              </div>
            )}
          </div>

          {/* Chat Input Bar */}
          <div className="ai-chat-input-bar">
            <form
              onSubmit={(e) => {
                e.preventDefault();
                handleSendMessage();
              }}
              className="ai-chat-input-form"
            >
              <div className="ai-input-flex-row">
                <textarea
                  ref={textareaRef}
                  className="ai-chat-textarea"
                  value={inputMessage}
                  onChange={(e) => setInputMessage(e.target.value)}
                  onKeyDown={handleKeyDown}
                  placeholder="Ask a programming question or request a roadmap (e.g. 'Build a 12-week roadmap for Next.js and FastAPI')..."
                  rows={2}
                  disabled={isThinking}
                />

                <button
                  type="submit"
                  className="ai-send-btn"
                  disabled={!inputMessage.trim() || isThinking}
                >
                  {isThinking ? (
                    <span className="spinner-inline" />
                  ) : (
                    <>
                      <span>Send</span>
                      <span>➤</span>
                    </>
                  )}
                </button>
              </div>

              <div className="ai-input-hint-row">
                <span>
                  Press <span className="ai-key-badge">Enter</span> to send, <span className="ai-key-badge">Shift + Enter</span> for new line
                </span>
                <span>Linked to Live Intelligence Engine</span>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* ========================================================
          SUB-TAB 2: SAVED ROADMAPS LIBRARY
         ======================================================== */}
      {activeSubTab === 'library' && (
        <div className="ai-library-card">
          <div className="ai-card-title-bar">
            <div>
              <h2 className="ai-card-heading">My Saved AI Roadmaps Library</h2>
              <div className="ai-card-subheading">
                Track custom tech stack learning paths created for your student profile
              </div>
            </div>
            <span className="badge-outline">{savedRoadmaps.length} Saved</span>
          </div>

          {savedRoadmaps.length === 0 ? (
            <div className="ai-empty-library">
              <div className="ai-empty-icon">✦</div>
              <h3>No saved roadmaps yet</h3>
              <p>Ask the AI Chatbot to generate a roadmap and click <strong>"Save Roadmap to Library"</strong> to track your progress here.</p>
              <button
                type="button"
                className="btn btn-primary"
                onClick={() => setActiveSubTab('chat')}
                style={{ marginTop: '0.75rem' }}
              >
                Go to AI Chatbot
              </button>
            </div>
          ) : (
            <div className="ai-library-grid">
              {savedRoadmaps.map((item) => {
                const percent = item.totalMilestones > 0
                  ? Math.round((item.completedMilestones / item.totalMilestones) * 100)
                  : 0;
                return (
                  <div
                    key={item.id}
                    className="ai-library-item-card"
                    onClick={() => handleLoadSavedRoadmapIntoChat(item)}
                  >
                    <div className="ai-lib-top-row">
                      <div className="ai-lib-title-box">
                        <h4 className="ai-lib-title">{item.title}</h4>
                        <div className="ai-lib-tags">
                          {item.targetLevel && <span className="badge-outline">{item.targetLevel}</span>}
                          {item.timeframeWeeks && <span className="badge-outline">{item.timeframeWeeks} Weeks</span>}
                          {item.learningFocus && <span className="badge-outline">{item.learningFocus}</span>}
                        </div>
                      </div>

                      <button
                        type="button"
                        className="ai-lib-delete-btn"
                        onClick={(e) => handleDeleteSavedRoadmap(item.id, item.title, e)}
                        title="Delete this roadmap"
                      >
                        Delete
                      </button>
                    </div>

                    <div className="ai-lib-meta-text">
                      Generated by {item.aiModelUsed}
                    </div>

                    {/* Progress Bar */}
                    <div className="ai-lib-progress-section">
                      <div className="ai-lib-progress-labels">
                        <span>Milestones Completed</span>
                        <span className="font-mono">
                          {item.completedMilestones} / {item.totalMilestones} ({percent}%)
                        </span>
                      </div>
                      <div className="thin-progress-bar-container">
                        <div
                          className="thin-progress-bar-fill"
                          style={{ width: `${percent}%` }}
                        />
                      </div>
                    </div>

                    <div className="ai-lib-open-prompt">
                      <span>Click to open in Chat & resume progress</span>
                      <span>→</span>
                    </div>
                  </div>
                );
              })}
            </div>
          )}
        </div>
      )}
    </div>
  );
}
