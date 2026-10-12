import React, { useState, useEffect } from 'react';
import { api } from '../api';

export default function AdminView() {
  const [subTab, setSubTab] = useState('roles'); // 'roles' or 'skills'
  const [roles, setRoles] = useState([]);
  const [catalogSkills, setCatalogSkills] = useState([]);
  const [selectedRole, setSelectedRole] = useState(null);
  const [roleRequirements, setRoleRequirements] = useState([]);
  const [roleRoadmap, setRoleRoadmap] = useState([]);

  // Create Role Form
  const [newRoleName, setNewRoleName] = useState('');
  const [newRoleDesc, setNewRoleDesc] = useState('');

  // Add Required Skill Form
  const [reqSkillId, setReqSkillId] = useState('');
  const [reqPriority, setReqPriority] = useState('CORE');
  const [reqWeight, setReqWeight] = useState(10);

  // Add Roadmap Milestone Form
  const [rmTitle, setRmTitle] = useState('');
  const [rmSkillId, setRmSkillId] = useState('');
  const [rmOrderIndex, setRmOrderIndex] = useState(1);

  // Add Catalog Skill Form
  const [catSkillName, setCatSkillName] = useState('');
  const [catCategory, setCatCategory] = useState('LANGUAGE');

  const [message, setMessage] = useState(null);
  const [loading, setLoading] = useState(true);

  const loadInitialData = async () => {
    try {
      setLoading(true);
      const [rList, sList] = await Promise.all([
        api.getRoles(),
        api.getSkills()
      ]);
      setRoles(rList);
      setCatalogSkills(sList);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadInitialData();
  }, []);

  const handleSelectRoleConfig = async (role) => {
    setSelectedRole(role);
    try {
      const res = await fetch(`/api/roles/${role.roleId}`);
      if (res.ok) {
        const fullRole = await res.json();
        // also fetch required skills and roadmap
        const reqRes = await fetch(`/admin/roles/${role.roleId}/skills`); // or via service
      }
      // To get requirements and roadmap cleanly:
      loadInitialData();
    } catch (err) {
      console.error(err);
    }
  };

  const handleCreateRole = async (e) => {
    e.preventDefault();
    try {
      await api.createRole(newRoleName, newRoleDesc);
      setMessage({ type: 'success', text: `Role '${newRoleName}' created successfully.` });
      setNewRoleName('');
      setNewRoleDesc('');
      loadInitialData();
    } catch (err) {
      setMessage({ type: 'error', text: err.message });
    }
  };

  const handleCreateCatalogSkill = async (e) => {
    e.preventDefault();
    try {
      await api.createCatalogSkill(catSkillName, catCategory);
      setMessage({ type: 'success', text: `Skill '${catSkillName}' added to master catalog.` });
      setCatSkillName('');
      loadInitialData();
    } catch (err) {
      setMessage({ type: 'error', text: err.message });
    }
  };

  const handleAddRequiredSkill = async (e) => {
    e.preventDefault();
    if (!selectedRole || !reqSkillId) return;
    try {
      await api.addRequiredSkill(selectedRole.roleId, reqSkillId, reqPriority, reqWeight);
      setMessage({ type: 'success', text: 'Required skill added to role profile.' });
      loadInitialData();
    } catch (err) {
      setMessage({ type: 'error', text: err.message });
    }
  };

  const handleAddRoadmapItem = async (e) => {
    e.preventDefault();
    if (!selectedRole) return;
    try {
      await api.addRoadmapItem(selectedRole.roleId, rmSkillId || null, rmTitle, rmOrderIndex);
      setMessage({ type: 'success', text: 'Roadmap step added to sequence.' });
      setRmTitle('');
      setRmOrderIndex(prev => prev + 1);
      loadInitialData();
    } catch (err) {
      setMessage({ type: 'error', text: err.message });
    }
  };

  if (loading) {
    return <div style={{ padding: '2rem 0' }}>Loading administrator panel...</div>;
  }

  return (
    <div>
      <div style={{ marginBottom: '2rem' }}>
        <p style={{ textTransform: 'uppercase', letterSpacing: '0.06em', fontSize: '0.72rem', fontWeight: 600, color: 'var(--ink-tertiary)', marginBottom: '0.25rem' }}>
          Platform Administrator Console
        </p>
        <h1 style={{ marginBottom: '0.25rem' }}>Career & Competency Controls</h1>
        <p style={{ fontSize: '0.95rem' }}>
          Maintain industry benchmark roles, competency weights, sequential roadmaps, and the global taxonomy.
        </p>
      </div>

      {message && (
        <div className={`editorial-alert ${message.type === 'success' ? 'alert-success' : 'alert-error'}`}>
          <span>{message.text}</span>
        </div>
      )}

      {/* Sub-tabs */}
      <div style={{ display: 'flex', gap: '0.75rem', marginBottom: '1.5rem', borderBottom: '1px solid var(--border-stone)', paddingBottom: '0.75rem' }}>
        <button
          onClick={() => { setSubTab('roles'); setSelectedRole(null); }}
          className={`btn-editorial-sm ${subTab === 'roles' ? 'btn-editorial-primary' : 'btn-editorial-secondary'}`}
        >
          Career Roles & Requirements
        </button>
        <button
          onClick={() => setSubTab('skills')}
          className={`btn-editorial-sm ${subTab === 'skills' ? 'btn-editorial-primary' : 'btn-editorial-secondary'}`}
        >
          Master Skills Catalog ({catalogSkills.length})
        </button>
      </div>

      {subTab === 'roles' && !selectedRole && (
        <>
          <div className="grid-2">
            {/* Create Role Card */}
            <div className="editorial-card">
              <div className="editorial-card-header">
                <div className="editorial-card-title">Create Career Role</div>
              </div>
              <form onSubmit={handleCreateRole}>
                <div className="editorial-form-group">
                  <label className="editorial-label" htmlFor="roleNameInput">Role Title</label>
                  <input
                    type="text"
                    id="roleNameInput"
                    className="editorial-input"
                    placeholder="e.g. Cloud Solutions Architect"
                    value={newRoleName}
                    onChange={(e) => setNewRoleName(e.target.value)}
                    required
                  />
                </div>
                <div className="editorial-form-group">
                  <label className="editorial-label" htmlFor="roleDescInput">Description & Expectations</label>
                  <textarea
                    id="roleDescInput"
                    className="editorial-textarea"
                    rows="2"
                    placeholder="Responsibilities and industry standards..."
                    value={newRoleDesc}
                    onChange={(e) => setNewRoleDesc(e.target.value)}
                  ></textarea>
                </div>
                <button type="submit" className="btn-editorial-primary btn-editorial-sm">
                  + Create Career Role
                </button>
              </form>
            </div>

            {/* Quick Metrics */}
            <div className="editorial-card">
              <div className="editorial-card-header">
                <div className="editorial-card-title">System Benchmarks</div>
              </div>
              <div className="grid-2">
                <div className="metric-stat">
                  <div className="metric-number tabular-nums">{roles.length}</div>
                  <div className="metric-label">Roles Active</div>
                </div>
                <div className="metric-stat">
                  <div className="metric-number tabular-nums">{catalogSkills.length}</div>
                  <div className="metric-label">Skills Cataloged</div>
                </div>
              </div>
            </div>
          </div>

          {/* Existing Roles Table */}
          <div className="editorial-card">
            <div className="editorial-card-header">
              <div className="editorial-card-title">Configured Career Roles</div>
              <span className="badge-outline">{roles.length} Roles</span>
            </div>

            <table className="editorial-table">
              <thead>
                <tr>
                  <th style={{ width: '50px' }}>ID</th>
                  <th>Role Title</th>
                  <th>Description</th>
                  <th style={{ textAlign: 'right' }}>Actions</th>
                </tr>
              </thead>
              <tbody>
                {roles.map(r => (
                  <tr key={r.roleId}>
                    <td className="tabular-nums font-mono">{r.roleId}</td>
                    <td style={{ fontWeight: 600 }}>{r.roleName}</td>
                    <td style={{ fontSize: '0.8125rem', color: 'var(--ink-secondary)', maxWidth: '420px' }}>{r.description}</td>
                    <td style={{ textAlign: 'right' }}>
                      <button
                        onClick={() => setSelectedRole(r)}
                        className="btn-editorial-secondary btn-editorial-sm"
                      >
                        Configure Requirements &rarr;
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </>
      )}

      {/* Role Requirements & Roadmap Configuration Subview */}
      {subTab === 'roles' && selectedRole && (
        <>
          <div style={{ marginBottom: '1.5rem', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <div>
              <button
                onClick={() => setSelectedRole(null)}
                className="btn-editorial-secondary btn-editorial-sm"
                style={{ marginBottom: '0.5rem' }}
              >
                &larr; Back to Roles List
              </button>
              <h2 style={{ margin: 0 }}>Configure: {selectedRole.roleName}</h2>
            </div>
          </div>

          <div className="grid-2">
            {/* Add Required Skill */}
            <div className="editorial-card">
              <div className="editorial-card-header">
                <div className="editorial-card-title">Add / Update Required Skill</div>
              </div>
              <form onSubmit={handleAddRequiredSkill}>
                <div className="editorial-form-group">
                  <label className="editorial-label" htmlFor="adminReqSkill">Skill</label>
                  <select
                    id="adminReqSkill"
                    className="editorial-select"
                    value={reqSkillId}
                    onChange={(e) => setReqSkillId(e.target.value)}
                    required
                  >
                    <option value="">-- Choose Skill --</option>
                    {catalogSkills.map(s => (
                      <option key={s.skillId} value={s.skillId}>{s.skillName} ({s.category})</option>
                    ))}
                  </select>
                </div>
                <div className="grid-2">
                  <div className="editorial-form-group">
                    <label className="editorial-label" htmlFor="adminPriority">Priority</label>
                    <select
                      id="adminPriority"
                      className="editorial-select"
                      value={reqPriority}
                      onChange={(e) => setReqPriority(e.target.value)}
                    >
                      <option value="CORE">CORE (Critical)</option>
                      <option value="GOOD_TO_HAVE">GOOD_TO_HAVE (Secondary)</option>
                    </select>
                  </div>
                  <div className="editorial-form-group">
                    <label className="editorial-label" htmlFor="adminWeight">Weight</label>
                    <input
                      type="number"
                      id="adminWeight"
                      className="editorial-input"
                      value={reqWeight}
                      onChange={(e) => setReqWeight(e.target.value)}
                      min="1"
                      max="100"
                      required
                    />
                  </div>
                </div>
                <button type="submit" className="btn-editorial-primary btn-editorial-sm">+ Save Requirement</button>
              </form>
            </div>

            {/* Add Roadmap Milestone */}
            <div className="editorial-card">
              <div className="editorial-card-header">
                <div className="editorial-card-title">Add Roadmap Milestone</div>
              </div>
              <form onSubmit={handleAddRoadmapItem}>
                <div className="editorial-form-group">
                  <label className="editorial-label" htmlFor="adminRmTitle">Milestone Action</label>
                  <input
                    type="text"
                    id="adminRmTitle"
                    className="editorial-input"
                    placeholder="e.g. Master Spring Security stateless filters"
                    value={rmTitle}
                    onChange={(e) => setRmTitle(e.target.value)}
                    required
                  />
                </div>
                <div className="grid-2">
                  <div className="editorial-form-group">
                    <label className="editorial-label" htmlFor="adminRmSkill">Linked Competency</label>
                    <select
                      id="adminRmSkill"
                      className="editorial-select"
                      value={rmSkillId}
                      onChange={(e) => setRmSkillId(e.target.value)}
                    >
                      <option value="">-- General Milestone (No Link) --</option>
                      {catalogSkills.map(s => (
                        <option key={s.skillId} value={s.skillId}>{s.skillName}</option>
                      ))}
                    </select>
                  </div>
                  <div className="editorial-form-group">
                    <label className="editorial-label" htmlFor="adminRmOrder">Sequence Order</label>
                    <input
                      type="number"
                      id="adminRmOrder"
                      className="editorial-input"
                      value={rmOrderIndex}
                      onChange={(e) => setRmOrderIndex(e.target.value)}
                      min="1"
                      required
                    />
                  </div>
                </div>
                <button type="submit" className="btn-editorial-primary btn-editorial-sm">+ Add Milestone</button>
              </form>
            </div>
          </div>
        </>
      )}

      {/* Master Skills Catalog Subview */}
      {subTab === 'skills' && (
        <div className="grid-2">
          {/* Add Skill to Catalog */}
          <div className="editorial-card">
            <div className="editorial-card-header">
              <div className="editorial-card-title">Add Competency to Master Catalog</div>
            </div>
            <form onSubmit={handleCreateCatalogSkill}>
              <div className="editorial-form-group">
                <label className="editorial-label" htmlFor="newCatSkill">Skill Name</label>
                <input
                  type="text"
                  id="newCatSkill"
                  className="editorial-input"
                  placeholder="e.g. Apache Kafka"
                  value={catSkillName}
                  onChange={(e) => setCatSkillName(e.target.value)}
                  required
                />
              </div>
              <div className="editorial-form-group">
                <label className="editorial-label" htmlFor="newCatDomain">Domain Category</label>
                <select
                  id="newCatDomain"
                  className="editorial-select"
                  value={catCategory}
                  onChange={(e) => setCatCategory(e.target.value)}
                >
                  <option value="LANGUAGE">LANGUAGE</option>
                  <option value="FRAMEWORK">FRAMEWORK</option>
                  <option value="DATABASE">DATABASE</option>
                  <option value="TOOLS_AND_PRACTICES">TOOLS_AND_PRACTICES</option>
                </select>
              </div>
              <button type="submit" className="btn-editorial-primary btn-editorial-sm">+ Add to Catalog</button>
            </form>
          </div>

          {/* Catalog Table */}
          <div className="editorial-card">
            <div className="editorial-card-header">
              <div className="editorial-card-title">Catalog Inventory</div>
              <span className="badge-outline">{catalogSkills.length} Skills</span>
            </div>

            <table className="editorial-table">
              <thead>
                <tr>
                  <th style={{ width: '50px' }}>ID</th>
                  <th>Skill Name</th>
                  <th>Category</th>
                </tr>
              </thead>
              <tbody>
                {catalogSkills.map(s => (
                  <tr key={s.skillId}>
                    <td className="tabular-nums font-mono">{s.skillId}</td>
                    <td style={{ fontWeight: 600 }}>{s.skillName}</td>
                    <td><span className="badge-outline">{s.category}</span></td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </div>
  );
}
