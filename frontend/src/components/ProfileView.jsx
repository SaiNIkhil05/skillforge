import React, { useState, useEffect, useRef } from 'react';
import { api } from '../api';

export default function ProfileView({ user, onProfileUpdated }) {
  const [profileData, setProfileData] = useState(null);
  const [roles, setRoles] = useState([]);
  const [allSkills, setAllSkills] = useState([]);
  const [education, setEducation] = useState(user.education || '');
  const [selectedRoleId, setSelectedRoleId] = useState('');

  // Skill Add State
  const [skillSearch, setSkillSearch] = useState('');
  const [skillCategory, setSkillCategory] = useState('');
  const [selectedSkillId, setSelectedSkillId] = useState('');
  const [proficiency, setProficiency] = useState('INTERMEDIATE');

  // Project Add State
  const [projectTitle, setProjectTitle] = useState('');
  const [techUsed, setTechUsed] = useState('');
  const [projectLink, setProjectLink] = useState('');

  // Resume Parser State (Feature Module 1)
  const [isDragging, setIsDragging] = useState(false);
  const [isParsing, setIsParsing] = useState(false);
  const [resumeResult, setResumeResult] = useState(null);
  const [selectedResumeSkills, setSelectedResumeSkills] = useState([]);
  const [isApplyingResume, setIsApplyingResume] = useState(false);
  const fileInputRef = useRef(null);

  const [message, setMessage] = useState(null);
  const [loading, setLoading] = useState(true);

  const loadData = async () => {
    try {
      setLoading(true);
      const [prof, rList, sList] = await Promise.all([
        api.getProfile(),
        api.getRoles(),
        api.getSkills()
      ]);
      setProfileData(prof);
      setRoles(rList);
      setAllSkills(sList);
      setEducation(prof.user.education || '');
      if (prof.user.targetRole) {
        setSelectedRoleId(prof.user.targetRole.roleId);
      }
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const handleUpdateTargetRole = async (e) => {
    e.preventDefault();
    try {
      await api.updateTargetRole(selectedRoleId);
      setMessage({ type: 'success', text: 'Target career role updated. Readiness recalculated.' });
      loadData();
      if (onProfileUpdated) onProfileUpdated();
    } catch (err) {
      setMessage({ type: 'error', text: err.message });
    }
  };

  const handleUpdateEducation = async (e) => {
    e.preventDefault();
    try {
      await api.updateEducation(education);
      setMessage({ type: 'success', text: 'Education details saved.' });
      loadData();
      if (onProfileUpdated) onProfileUpdated();
    } catch (err) {
      setMessage({ type: 'error', text: err.message });
    }
  };

  const handleAddSkill = async (e) => {
    e.preventDefault();
    if (!selectedSkillId) return;
    try {
      await api.addSkill(selectedSkillId, proficiency);
      setMessage({ type: 'success', text: 'Competency added to profile.' });
      setSelectedSkillId('');
      loadData();
      if (onProfileUpdated) onProfileUpdated();
    } catch (err) {
      setMessage({ type: 'error', text: err.message });
    }
  };

  const handleRemoveSkill = async (skillId) => {
    try {
      await api.removeSkill(skillId);
      setMessage({ type: 'success', text: 'Competency removed from profile.' });
      loadData();
      if (onProfileUpdated) onProfileUpdated();
    } catch (err) {
      setMessage({ type: 'error', text: err.message });
    }
  };

  const handleUpdateSkillProficiency = async (skillId, newProficiency) => {
    try {
      await api.addSkill(skillId, newProficiency);
      setMessage({ type: 'success', text: 'Proficiency updated.' });
      loadData();
      if (onProfileUpdated) onProfileUpdated();
    } catch (err) {
      setMessage({ type: 'error', text: err.message });
    }
  };

  const handleAddProject = async (e) => {
    e.preventDefault();
    try {
      await api.addProject(projectTitle, techUsed, projectLink);
      setMessage({ type: 'success', text: 'Project recorded successfully.' });
      setProjectTitle('');
      setTechUsed('');
      setProjectLink('');
      loadData();
    } catch (err) {
      setMessage({ type: 'error', text: err.message });
    }
  };

  const handleRemoveProject = async (projectId) => {
    try {
      await api.removeProject(projectId);
      setMessage({ type: 'success', text: 'Project deleted.' });
      loadData();
    } catch (err) {
      setMessage({ type: 'error', text: err.message });
    }
  };

  // ================= RESUME PARSER HANDLERS =================
  const handleFileDrop = (e) => {
    e.preventDefault();
    setIsDragging(false);
    const files = e.dataTransfer?.files;
    if (files && files.length > 0) {
      processResumeFile(files[0]);
    }
  };

  const handleFileSelect = (e) => {
    const files = e.target.files;
    if (files && files.length > 0) {
      processResumeFile(files[0]);
    }
  };

  const processResumeFile = async (file) => {
    const validExtensions = ['.pdf', '.docx', '.doc', '.txt'];
    const hasValidExt = validExtensions.some(ext => file.name.toLowerCase().endsWith(ext));
    if (!hasValidExt) {
      setMessage({ type: 'error', text: 'Please upload a PDF (.pdf) or Word document (.docx).' });
      return;
    }

    try {
      setIsParsing(true);
      setMessage({ type: 'info', text: `Analyzing ${file.name} using Apache Tika...` });
      const result = await api.uploadResume(file);
      setResumeResult(result);

      // Pre-select skills that are not already in profile
      const initialSelections = (result.detectedSkills || []).map(s => ({
        skillId: s.skillId,
        skillName: s.skillName,
        category: s.category,
        proficiency: s.proficiency || 'BEGINNER',
        confidence: s.confidence,
        snippet: s.snippet,
        alreadyInProfile: s.alreadyInProfile,
        selected: !s.alreadyInProfile
      }));
      setSelectedResumeSkills(initialSelections);
      setMessage(null);
    } catch (err) {
      setMessage({ type: 'error', text: err.message || 'Failed to parse resume.' });
    } finally {
      setIsParsing(false);
      if (fileInputRef.current) fileInputRef.current.value = '';
    }
  };

  const handleToggleSelectSkill = (skillId) => {
    setSelectedResumeSkills(prev => prev.map(s => {
      if (s.skillId === skillId) {
        return { ...s, selected: !s.selected };
      }
      return s;
    }));
  };

  const handleResumeSkillProficiencyChange = (skillId, prof) => {
    setSelectedResumeSkills(prev => prev.map(s => {
      if (s.skillId === skillId) {
        return { ...s, proficiency: prof };
      }
      return s;
    }));
  };

  const handleSelectAllResumeSkills = (selectVal) => {
    setSelectedResumeSkills(prev => prev.map(s => ({ ...s, selected: selectVal })));
  };

  const handleApplyResumeSkills = async () => {
    const approved = selectedResumeSkills.filter(s => s.selected).map(s => ({
      skillId: s.skillId,
      proficiency: s.proficiency
    }));

    if (approved.length === 0) {
      setMessage({ type: 'error', text: 'Select at least one competency to apply to your profile.' });
      return;
    }

    try {
      setIsApplyingResume(true);
      const res = await api.applyResumeSkills(approved);
      setMessage({
        type: 'success',
        text: `${res.message || `Added ${approved.length} competencies.`} Readiness recalculated!`
      });
      setResumeResult(null);
      setSelectedResumeSkills([]);
      loadData();
      if (onProfileUpdated) onProfileUpdated();
    } catch (err) {
      setMessage({ type: 'error', text: err.message || 'Failed to apply skills.' });
    } finally {
      setIsApplyingResume(false);
    }
  };

  if (loading) {
    return <div style={{ padding: '2rem 0' }}>Loading profile builder...</div>;
  }

  const userSkills = profileData?.skills || [];
  const userProjects = profileData?.projects || [];

  const filteredCatalog = allSkills.filter(s => {
    const matchesSearch = !skillSearch || s.skillName.toLowerCase().includes(skillSearch.toLowerCase());
    const matchesCat = !skillCategory || s.category === skillCategory;
    return matchesSearch && matchesCat;
  });

  const selectedCount = selectedResumeSkills.filter(s => s.selected).length;

  return (
    <div>
      <div style={{ marginBottom: '2rem' }}>
        <p style={{ textTransform: 'uppercase', letterSpacing: '0.06em', fontSize: '0.72rem', fontWeight: 600, color: 'var(--ink-tertiary)', marginBottom: '0.25rem' }}>
          Candidate Competency Dossier
        </p>
        <h1 style={{ marginBottom: '0.25rem' }}>Profile Builder</h1>
        <p style={{ fontSize: '0.95rem' }}>
          Maintain your education, declared proficiencies, target career role, and upload resume evidence to automate competency detection.
        </p>
      </div>

      {message && (
        <div className={`editorial-alert ${message.type === 'success' ? 'alert-success' : message.type === 'info' ? 'alert-info' : 'alert-error'}`}>
          <span>{message.text}</span>
        </div>
      )}

      {/* FEATURE MODULE 1: AI RESUME PARSER DROPZONE */}
      <div className="editorial-card" style={{ marginBottom: '2rem', border: isDragging ? '2px dashed var(--accent-forest)' : '1px solid var(--border-stone)', background: isDragging ? 'rgba(31, 77, 58, 0.04)' : 'var(--bg-paper-clean)' }}>
        <div className="editorial-card-header">
          <div>
            <div className="editorial-card-title" style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
              <span>AI-Powered Resume Competency Parser</span>
              <span className="badge-core" style={{ fontSize: '0.68rem', padding: '0.15rem 0.45rem' }}>Auto-Extract</span>
            </div>
            <div style={{ fontSize: '0.82rem', color: 'var(--ink-secondary)', marginTop: '0.25rem' }}>
              Upload your PDF or Word resume. Apache Tika extracts your technical stack, estimates proficiency, and pre-populates your profile.
            </div>
          </div>
        </div>

        <div
          onDragOver={(e) => { e.preventDefault(); setIsDragging(true); }}
          onDragLeave={() => setIsDragging(false)}
          onDrop={handleFileDrop}
          onClick={() => fileInputRef.current?.click()}
          style={{
            border: isDragging ? '2px dashed var(--accent-forest)' : '1px dashed var(--border-stone)',
            borderRadius: '4px',
            padding: '2rem 1.5rem',
            textAlign: 'center',
            cursor: 'pointer',
            background: isDragging ? 'rgba(31, 77, 58, 0.06)' : 'var(--bg-paper)',
            transition: 'all 0.2s ease'
          }}
        >
          <input
            type="file"
            ref={fileInputRef}
            onChange={handleFileSelect}
            accept=".pdf,.docx,.doc,.txt"
            style={{ display: 'none' }}
          />

          <div style={{ fontSize: '2rem', marginBottom: '0.5rem' }}>
            {isParsing ? '⏳' : isDragging ? '📥' : '📄'}
          </div>

          <div style={{ fontWeight: 600, fontSize: '0.98rem', color: isDragging ? 'var(--accent-forest)' : 'var(--ink-primary)' }}>
            {isParsing ? 'Analyzing Document with Apache Tika...' : isDragging ? 'Drop Your Resume Here' : 'Drag & drop your resume here, or click to browse'}
          </div>

          <div style={{ fontSize: '0.8rem', color: 'var(--ink-tertiary)', marginTop: '0.35rem' }}>
            Supports PDF (.pdf) and Microsoft Word (.docx) up to 15MB • Zero manual entry required
          </div>
        </div>
      </div>

      <div className="grid-2">
        {/* LEFT COLUMN: Target Role, Education, Projects */}
        <div>
          {/* Target Role Card */}
          <div className="editorial-card">
            <div className="editorial-card-header">
              <div className="editorial-card-title">Target Career Role</div>
              <span className="badge-outline">Benchmark</span>
            </div>
            <form onSubmit={handleUpdateTargetRole}>
              <div className="editorial-form-group">
                <label className="editorial-label" htmlFor="profTargetRole">Role Track</label>
                <select
                  id="profTargetRole"
                  className="editorial-select"
                  value={selectedRoleId}
                  onChange={(e) => setSelectedRoleId(e.target.value)}
                  required
                >
                  <option value="">-- Choose Career Role --</option>
                  {roles.map(r => (
                    <option key={r.roleId} value={r.roleId}>{r.roleName}</option>
                  ))}
                </select>
              </div>
              <button type="submit" className="btn-editorial-primary btn-editorial-sm">
                Update Benchmark Role
              </button>
            </form>
          </div>

          {/* Education Card */}
          <div className="editorial-card">
            <div className="editorial-card-header">
              <div className="editorial-card-title">Education & Graduation</div>
            </div>
            <form onSubmit={handleUpdateEducation}>
              <div className="editorial-form-group">
                <label className="editorial-label" htmlFor="profEdu">Degree, Branch & Batch</label>
                <input
                  type="text"
                  id="profEdu"
                  className="editorial-input"
                  value={education}
                  onChange={(e) => setEducation(e.target.value)}
                  placeholder="e.g. B.Tech Computer Science, 2026"
                  required
                />
              </div>
              <button type="submit" className="btn-editorial-secondary btn-editorial-sm">
                Save Education
              </button>
            </form>
          </div>

          {/* Practical Projects Card */}
          <div className="editorial-card">
            <div className="editorial-card-header">
              <div className="editorial-card-title">Practical Projects</div>
              <span className="badge-outline">{userProjects.length} Added</span>
            </div>

            <form onSubmit={handleAddProject} style={{ marginBottom: '1.5rem', paddingBottom: '1.5rem', borderBottom: '1px solid var(--border-subtle)' }}>
              <div className="editorial-form-group">
                <label className="editorial-label" htmlFor="pTitle">Project Title</label>
                <input
                  type="text"
                  id="pTitle"
                  className="editorial-input"
                  placeholder="Distributed Cache Engine"
                  value={projectTitle}
                  onChange={(e) => setProjectTitle(e.target.value)}
                  required
                />
              </div>
              <div className="editorial-form-group">
                <label className="editorial-label" htmlFor="pTech">Technologies Used</label>
                <input
                  type="text"
                  id="pTech"
                  className="editorial-input"
                  placeholder="Java 17, Spring Boot, Redis, Docker"
                  value={techUsed}
                  onChange={(e) => setTechUsed(e.target.value)}
                  required
                />
              </div>
              <div className="editorial-form-group">
                <label className="editorial-label" htmlFor="pLink">Repository / Demo Link (Optional)</label>
                <input
                  type="url"
                  id="pLink"
                  className="editorial-input"
                  placeholder="https://github.com/..."
                  value={projectLink}
                  onChange={(e) => setProjectLink(e.target.value)}
                />
              </div>
              <button type="submit" className="btn-editorial-secondary btn-editorial-sm">
                + Add Project
              </button>
            </form>

            {userProjects.length === 0 ? (
              <div style={{ fontSize: '0.85rem', color: 'var(--ink-secondary)' }}>
                No projects declared yet.
              </div>
            ) : (
              userProjects.map(proj => (
                <div key={proj.projectId} style={{ padding: '0.75rem 0', borderBottom: '1px solid var(--border-subtle)', display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                  <div>
                    <div style={{ fontWeight: 600, fontSize: '0.9rem' }}>{proj.title}</div>
                    <div style={{ fontSize: '0.8rem', color: 'var(--ink-secondary)', marginTop: '2px' }}>{proj.techUsed}</div>
                    {proj.projectLink && (
                      <a href={proj.projectLink} target="_blank" rel="noopener noreferrer" style={{ fontSize: '0.75rem', color: 'var(--accent-forest)', textDecoration: 'underline', marginTop: '2px', display: 'inline-block' }}>
                        View Repository &nearr;
                      </a>
                    )}
                  </div>
                  <button onClick={() => handleRemoveProject(proj.projectId)} className="btn-editorial-danger btn-editorial-sm">
                    Remove
                  </button>
                </div>
              ))
            )}
          </div>
        </div>

        {/* RIGHT COLUMN: Skills Catalog Search & Declared Skills */}
        <div>
          {/* Add Competency Card */}
          <div className="editorial-card">
            <div className="editorial-card-header">
              <div className="editorial-card-title">Add Competency to Profile</div>
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0.75rem', marginBottom: '1rem' }}>
              <div>
                <label className="editorial-label">Filter Category</label>
                <select
                  className="editorial-select"
                  value={skillCategory}
                  onChange={(e) => setSkillCategory(e.target.value)}
                >
                  <option value="">All Categories</option>
                  <option value="LANGUAGE">Language</option>
                  <option value="FRAMEWORK">Framework</option>
                  <option value="DATABASE">Database</option>
                  <option value="TOOL">Tool</option>
                  <option value="CONCEPT">Concept</option>
                </select>
              </div>

              <div>
                <label className="editorial-label">Search Skills</label>
                <input
                  type="text"
                  className="editorial-input"
                  placeholder="e.g. Docker, Java..."
                  value={skillSearch}
                  onChange={(e) => setSkillSearch(e.target.value)}
                />
              </div>
            </div>

            <form onSubmit={handleAddSkill}>
              <div className="editorial-form-group">
                <label className="editorial-label" htmlFor="catalogSelect">Select Skill</label>
                <select
                  id="catalogSelect"
                  className="editorial-select"
                  value={selectedSkillId}
                  onChange={(e) => setSelectedSkillId(e.target.value)}
                  required
                >
                  <option value="">-- Choose from Catalog ({filteredCatalog.length} available) --</option>
                  {filteredCatalog.map(s => (
                    <option key={s.skillId} value={s.skillId}>
                      {s.skillName} ({s.category})
                    </option>
                  ))}
                </select>
              </div>

              <div className="editorial-form-group">
                <label className="editorial-label">Your Proficiency</label>
                <div style={{ display: 'flex', gap: '1rem' }}>
                  {['BEGINNER', 'INTERMEDIATE', 'ADVANCED'].map(p => (
                    <label key={p} style={{ display: 'flex', alignItems: 'center', gap: '0.4rem', fontSize: '0.85rem', cursor: 'pointer' }}>
                      <input
                        type="radio"
                        name="profChoice"
                        value={p}
                        checked={proficiency === p}
                        onChange={(e) => setProficiency(e.target.value)}
                      />
                      {p.charAt(0) + p.slice(1).toLowerCase()}
                    </label>
                  ))}
                </div>
              </div>

              <button type="submit" className="btn-editorial-primary btn-editorial-sm">
                + Add Competency
              </button>
            </form>
          </div>

          {/* Current Profile Skills List */}
          <div className="editorial-card">
            <div className="editorial-card-header">
              <div className="editorial-card-title">Declared Competencies</div>
              <span className="badge-outline">{userSkills.length} Total</span>
            </div>

            {userSkills.length === 0 ? (
              <div style={{ fontSize: '0.85rem', color: 'var(--ink-secondary)' }}>
                No skills added yet. Use the selector above or upload your resume to auto-populate your competencies.
              </div>
            ) : (
              <table className="editorial-table">
                <thead>
                  <tr>
                    <th>Skill</th>
                    <th>Category</th>
                    <th>Proficiency</th>
                    <th style={{ textAlign: 'right' }}>Action</th>
                  </tr>
                </thead>
                <tbody>
                  {userSkills.map(us => (
                    <tr key={us.id}>
                      <td style={{ fontWeight: 600 }}>{us.skill.skillName}</td>
                      <td>
                        <span className="badge-outline">{us.skill.category}</span>
                      </td>
                      <td>
                        <select
                          className="editorial-select"
                          style={{ padding: '0.2rem 0.4rem', fontSize: '0.78rem', width: 'auto' }}
                          value={us.proficiency}
                          onChange={(e) => handleUpdateSkillProficiency(us.skill.skillId, e.target.value)}
                        >
                          <option value="BEGINNER">Beginner (50%)</option>
                          <option value="INTERMEDIATE">Intermediate (80%)</option>
                          <option value="ADVANCED">Advanced (100%)</option>
                        </select>
                      </td>
                      <td style={{ textAlign: 'right' }}>
                        <button
                          onClick={() => handleRemoveSkill(us.skill.skillId)}
                          className="btn-editorial-danger btn-editorial-sm"
                        >
                          Delete
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        </div>
      </div>

      {/* FEATURE 1: RESUME EXTRACTION PREVIEW MODAL */}
      {resumeResult && (
        <div style={{
          position: 'fixed',
          top: 0,
          left: 0,
          right: 0,
          bottom: 0,
          backgroundColor: 'rgba(28, 27, 25, 0.65)',
          backdropFilter: 'blur(3px)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          zIndex: 1000,
          padding: '1.5rem'
        }}>
          <div style={{
            backgroundColor: 'var(--bg-paper-clean)',
            borderRadius: '6px',
            border: '1px solid var(--border-stone)',
            width: '100%',
            maxWidth: '850px',
            maxHeight: '90vh',
            display: 'flex',
            flexDirection: 'column',
            boxShadow: '0 20px 40px rgba(0, 0, 0, 0.2)'
          }}>
            {/* Modal Header */}
            <div style={{
              padding: '1.25rem 1.75rem',
              borderBottom: '1px solid var(--border-subtle)',
              display: 'flex',
              justifyContent: 'space-between',
              alignItems: 'center'
            }}>
              <div>
                <h3 style={{ margin: 0, fontSize: '1.25rem' }}>Resume Competencies Detected</h3>
                <div style={{ fontSize: '0.82rem', color: 'var(--ink-secondary)', marginTop: '0.2rem' }}>
                  Parsed from <strong>{resumeResult.filename}</strong> ({resumeResult.newSkillsCount} new • {resumeResult.existingSkillsCount} already in profile)
                </div>
              </div>
              <button
                onClick={() => setResumeResult(null)}
                style={{ background: 'transparent', border: 'none', fontSize: '1.3rem', cursor: 'pointer', color: 'var(--ink-secondary)' }}
              >
                &times;
              </button>
            </div>

            {/* Modal Body */}
            <div style={{ padding: '1.5rem 1.75rem', overflowY: 'auto', flex: 1 }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
                <div style={{ fontSize: '0.85rem', color: 'var(--ink-secondary)' }}>
                  Confirm which skills to apply to your SkillForge profile. Adjust estimated proficiency if needed.
                </div>
                <div style={{ display: 'flex', gap: '0.5rem' }}>
                  <button
                    onClick={() => handleSelectAllResumeSkills(true)}
                    className="btn-editorial-secondary btn-editorial-sm"
                    style={{ fontSize: '0.75rem', padding: '0.2rem 0.5rem' }}
                  >
                    Select All
                  </button>
                  <button
                    onClick={() => handleSelectAllResumeSkills(false)}
                    className="btn-editorial-secondary btn-editorial-sm"
                    style={{ fontSize: '0.75rem', padding: '0.2rem 0.5rem' }}
                  >
                    Deselect All
                  </button>
                </div>
              </div>

              {selectedResumeSkills.length === 0 ? (
                <div style={{ padding: '2rem', textAlign: 'center', color: 'var(--ink-secondary)' }}>
                  No catalog competencies detected in the uploaded resume.
                </div>
              ) : (
                <div style={{ border: '1px solid var(--border-subtle)', borderRadius: '4px', overflow: 'hidden' }}>
                  <table className="editorial-table" style={{ margin: 0 }}>
                    <thead>
                      <tr>
                        <th style={{ width: '40px', textAlign: 'center' }}>✓</th>
                        <th>Competency</th>
                        <th>Category</th>
                        <th>Estimated Proficiency</th>
                        <th>Confidence</th>
                        <th>Resume Context Evidence</th>
                      </tr>
                    </thead>
                    <tbody>
                      {selectedResumeSkills.map(item => (
                        <tr key={item.skillId} style={{ backgroundColor: item.selected ? 'rgba(31, 77, 58, 0.02)' : 'transparent' }}>
                          <td style={{ textAlign: 'center' }}>
                            <input
                              type="checkbox"
                              checked={item.selected}
                              onChange={() => handleToggleSelectSkill(item.skillId)}
                            />
                          </td>
                          <td>
                            <div style={{ fontWeight: 600 }}>{item.skillName}</div>
                            {item.alreadyInProfile && (
                              <span style={{ fontSize: '0.68rem', color: 'var(--accent-burnt)', display: 'block' }}>
                                Already in profile
                              </span>
                            )}
                          </td>
                          <td>
                            <span className="badge-outline">{item.category}</span>
                          </td>
                          <td>
                            <select
                              className="editorial-select"
                              value={item.proficiency}
                              onChange={(e) => handleResumeSkillProficiencyChange(item.skillId, e.target.value)}
                              style={{ padding: '0.2rem 0.4rem', fontSize: '0.78rem' }}
                            >
                              <option value="BEGINNER">Beginner (50%)</option>
                              <option value="INTERMEDIATE">Intermediate (80%)</option>
                              <option value="ADVANCED">Advanced (100%)</option>
                            </select>
                          </td>
                          <td>
                            <span style={{ fontSize: '0.8rem', fontWeight: 600, color: 'var(--accent-forest)' }}>
                              {Math.round(item.confidence * 100)}%
                            </span>
                          </td>
                          <td style={{ fontSize: '0.78rem', color: 'var(--ink-secondary)', maxWidth: '240px' }}>
                            <em>{item.snippet || 'Referenced in skills inventory'}</em>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              )}
            </div>

            {/* Modal Footer */}
            <div style={{
              padding: '1.25rem 1.75rem',
              borderTop: '1px solid var(--border-subtle)',
              display: 'flex',
              justifyContent: 'space-between',
              alignItems: 'center',
              backgroundColor: 'var(--bg-paper)'
            }}>
              <span style={{ fontSize: '0.85rem', color: 'var(--ink-secondary)' }}>
                <strong>{selectedCount}</strong> competencies selected to merge
              </span>

              <div style={{ display: 'flex', gap: '0.75rem' }}>
                <button
                  type="button"
                  onClick={() => setResumeResult(null)}
                  className="btn-editorial-secondary"
                  disabled={isApplyingResume}
                >
                  Cancel
                </button>
                <button
                  type="button"
                  onClick={handleApplyResumeSkills}
                  className="btn-editorial-primary"
                  disabled={isApplyingResume || selectedCount === 0}
                >
                  {isApplyingResume ? 'Applying & Recalculating...' : `Apply ${selectedCount} Competencies`}
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
