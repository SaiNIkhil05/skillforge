const rawBase = import.meta.env.VITE_API_URL || '';
const API_BASE = rawBase 
  ? (rawBase.endsWith('/api') ? rawBase : `${rawBase.replace(/\/$/, '')}/api`)
  : '/api';


function getAccessToken() {
  return localStorage.getItem('sf_token');
}

function getRefreshToken() {
  return localStorage.getItem('sf_refresh_token');
}

function setTokens(accessToken, refreshToken) {
  if (accessToken) localStorage.setItem('sf_token', accessToken);
  if (refreshToken) localStorage.setItem('sf_refresh_token', refreshToken);
}

function clearTokens() {
  localStorage.removeItem('sf_token');
  localStorage.removeItem('sf_refresh_token');
}

function getAuthHeaders(isJson = true) {
  const token = getAccessToken();
  const headers = {};
  if (isJson) {
    headers['Content-Type'] = 'application/json';
  }
  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }
  return headers;
}

// Token refresh mutex
let isRefreshing = false;
let refreshQueue = [];

function subscribeTokenRefresh(cb) {
  refreshQueue.push(cb);
}

function onRefreshed(token) {
  refreshQueue.forEach(cb => cb(token));
  refreshQueue = [];
}

async function executeTokenRefresh() {
  const currentRefresh = getRefreshToken();
  const res = await fetch(`${API_BASE}/auth/refresh`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    credentials: 'include',
    body: JSON.stringify({ refreshToken: currentRefresh })
  });

  if (!res.ok) {
    clearTokens();
    throw new Error('Session expired. Please log in again.');
  }

  const data = await res.json();
  setTokens(data.accessToken, data.refreshToken);
  return data.accessToken;
}

// Interceptor-wrapped fetch with auto token refresh rotation
async function authFetch(url, options = {}) {
  const headers = options.headers || {};
  const token = getAccessToken();
  if (token && !headers['Authorization']) {
    headers['Authorization'] = `Bearer ${token}`;
  }
  options.headers = headers;
  options.credentials = 'include';

  let res = await fetch(url, options);

  // If 401 Unauthorized, attempt refresh token rotation and retry
  if (res.status === 401 && !url.includes('/auth/login') && !url.includes('/auth/refresh')) {
    if (!isRefreshing) {
      isRefreshing = true;
      try {
        const newToken = await executeTokenRefresh();
        isRefreshing = false;
        onRefreshed(newToken);
      } catch (err) {
        isRefreshing = false;
        refreshQueue = [];
        return res;
      }
    }

    // Wait for the active refresh to complete
    return new Promise((resolve) => {
      subscribeTokenRefresh(async (newToken) => {
        options.headers['Authorization'] = `Bearer ${newToken}`;
        const retryRes = await fetch(url, options);
        resolve(retryRes);
      });
    });
  }

  return res;
}

export const api = {
  // ================= AUTH =================
  async login(email, password) {
    try {
      const res = await fetch(`${API_BASE}/auth/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include',
        body: JSON.stringify({ email: (email || '').trim(), password })
      });
      if (!res.ok) {
        const err = await res.json().catch(() => ({ message: 'Invalid credentials or server unavailable.' }));
        const msg = err.message || (err.fieldErrors ? Object.values(err.fieldErrors).join(', ') : null) || err.error || 'Login failed';
        throw new Error(msg);
      }
      const data = await res.json();
      setTokens(data.token, data.refreshToken);
      return data;
    } catch (e) {
      if (e.message && e.message.includes('Failed to fetch')) {
        throw new Error('Backend server is not responding. Ensure Spring Boot is running on port 8080.');
      }
      throw e;
    }
  },

  async register(fullName, email, password, confirmPassword, education, targetRoleId) {
    try {
      const res = await fetch(`${API_BASE}/auth/register`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include',
        body: JSON.stringify({
          fullName: (fullName || '').trim(),
          email: (email || '').trim().toLowerCase(),
          password,
          confirmPassword,
          education,
          targetRoleId: targetRoleId ? parseInt(targetRoleId, 10) : null
        })
      });
      if (!res.ok) {
        const err = await res.json().catch(() => ({ message: 'Registration failed' }));
        const msg = err.message || (err.fieldErrors ? Object.values(err.fieldErrors).join(', ') : null) || err.error || 'Registration failed';
        throw new Error(msg);
      }
      const data = await res.json();
      setTokens(data.token, data.refreshToken);
      return data;
    } catch (e) {
      if (e.message && e.message.includes('Failed to fetch')) {
        throw new Error('Backend server is not responding. Ensure Spring Boot is running on port 8080.');
      }
      throw e;
    }
  },

  async refreshToken() {
    return executeTokenRefresh();
  },

  async verify(token) {
    const res = await fetch(`${API_BASE}/auth/verify?token=${encodeURIComponent(token)}`);
    if (!res.ok) throw new Error('Verification failed');
    return res.json();
  },

  async getCurrentUser() {
    const res = await authFetch(`${API_BASE}/auth/me`, {
      headers: getAuthHeaders()
    });
    if (!res.ok) {
      clearTokens();
      return null;
    }
    return res.json();
  },

  async logout() {
    try {
      await authFetch(`${API_BASE}/auth/logout`, {
        method: 'POST',
        headers: getAuthHeaders()
      });
    } catch (e) {
      // ignore
    }
    clearTokens();
  },

  // ================= DASHBOARD & PROFILE =================
  async getDashboard() {
    const res = await authFetch(`${API_BASE}/dashboard`, {
      headers: getAuthHeaders()
    });
    if (!res.ok) throw new Error('Failed to load dashboard data');
    return res.json();
  },

  async getProfile() {
    const res = await authFetch(`${API_BASE}/profile`, {
      headers: getAuthHeaders()
    });
    if (!res.ok) throw new Error('Failed to load profile');
    return res.json();
  },

  async updateEducation(education) {
    const res = await authFetch(`${API_BASE}/profile/education?education=${encodeURIComponent(education)}`, {
      method: 'POST',
      headers: getAuthHeaders()
    });
    if (!res.ok) throw new Error('Failed to update education');
    return res.json();
  },

  async updateTargetRole(roleId) {
    const res = await authFetch(`${API_BASE}/profile/target-role?roleId=${roleId}`, {
      method: 'POST',
      headers: getAuthHeaders()
    });
    if (!res.ok) throw new Error('Failed to update target role');
    return res.json();
  },

  async addSkill(skillId, proficiency) {
    const res = await authFetch(`${API_BASE}/profile/skills`, {
      method: 'POST',
      headers: getAuthHeaders(),
      body: JSON.stringify({ skillId: parseInt(skillId, 10), proficiency })
    });
    if (!res.ok) throw new Error('Failed to add skill');
    return res.json();
  },

  async removeSkill(skillId) {
    const res = await authFetch(`${API_BASE}/profile/skills/${skillId}`, {
      method: 'DELETE',
      headers: getAuthHeaders()
    });
    if (!res.ok) throw new Error('Failed to remove skill');
    return res.json();
  },

  async addProject(title, techUsed, projectLink) {
    const res = await authFetch(`${API_BASE}/profile/projects`, {
      method: 'POST',
      headers: getAuthHeaders(),
      body: JSON.stringify({ title, techUsed, projectLink })
    });
    if (!res.ok) throw new Error('Failed to add project');
    return res.json();
  },

  async removeProject(projectId) {
    const res = await authFetch(`${API_BASE}/profile/projects/${projectId}`, {
      method: 'DELETE',
      headers: getAuthHeaders()
    });
    if (!res.ok) throw new Error('Failed to remove project');
    return res.json();
  },

  // ================= RESUME PARSER (FEATURE 1) =================
  async uploadResume(file) {
    const formData = new FormData();
    formData.append('file', file);

    const headers = getAuthHeaders(false); // multipart boundary set by browser
    const res = await authFetch(`${API_BASE}/profile/upload-resume`, {
      method: 'POST',
      headers,
      body: formData
    });

    if (!res.ok) {
      const err = await res.json().catch(() => ({ message: 'Failed to parse resume.' }));
      throw new Error(err.message || 'Error parsing resume');
    }
    return res.json();
  },

  async applyResumeSkills(skills) {
    const res = await authFetch(`${API_BASE}/profile/apply-resume-skills`, {
      method: 'POST',
      headers: getAuthHeaders(),
      body: JSON.stringify({ skills })
    });

    if (!res.ok) {
      const err = await res.json().catch(() => ({ message: 'Failed to apply resume skills.' }));
      throw new Error(err.message || 'Error applying resume skills');
    }
    return res.json();
  },

  // ================= READINESS & ROADMAP =================
  async getReadiness() {
    const res = await authFetch(`${API_BASE}/readiness`, {
      headers: getAuthHeaders()
    });
    if (!res.ok) throw new Error('Failed to load readiness');
    return res.json();
  },

  async getRoadmap() {
    const res = await authFetch(`${API_BASE}/roadmap`, {
      headers: getAuthHeaders()
    });
    if (!res.ok) throw new Error('Failed to load roadmap');
    return res.json();
  },

  async updateRoadmapStatus(roadmapId, status) {
    const res = await authFetch(`${API_BASE}/roadmap/update?roadmapId=${roadmapId}&status=${status}`, {
      method: 'POST',
      headers: getAuthHeaders()
    });
    if (!res.ok) throw new Error('Failed to update milestone status');
    return res.json();
  },

  // ================= CATALOG & ROLES =================
  async getSkills(category = '', search = '') {
    const params = new URLSearchParams();
    if (category) params.append('category', category);
    if (search) params.append('search', search);
    const res = await authFetch(`${API_BASE}/skills?${params.toString()}`, {
      headers: getAuthHeaders()
    });
    if (!res.ok) throw new Error('Failed to fetch skills catalog');
    return res.json();
  },

  async getRoles() {
    const res = await fetch(`${API_BASE}/roles`);
    if (!res.ok) throw new Error('Failed to fetch career roles');
    return res.json();
  },

  // ================= ADMIN ROLE & SKILL MANAGEMENT =================
  async createRole(roleName, description) {
    const res = await authFetch(`${API_BASE}/admin/roles`, {
      method: 'POST',
      headers: getAuthHeaders(),
      body: JSON.stringify({ roleName, description })
    });
    if (!res.ok) throw new Error('Failed to create career role');
    return res.json();
  },

  async addRequiredSkill(roleId, skillId, priority, weight) {
    const res = await authFetch(`${API_BASE}/admin/roles/${roleId}/skills`, {
      method: 'POST',
      headers: getAuthHeaders(),
      body: JSON.stringify({ skillId: parseInt(skillId, 10), priority, weight: parseInt(weight, 10) })
    });
    if (!res.ok) throw new Error('Failed to add required skill');
    return res.json();
  },

  async deleteRequiredSkill(id) {
    const res = await authFetch(`${API_BASE}/admin/roles/skills/${id}`, {
      method: 'DELETE',
      headers: getAuthHeaders()
    });
    if (!res.ok) throw new Error('Failed to delete required skill');
    return res.json();
  },

  async addRoadmapItem(roleId, skillId, title, orderIndex) {
    const res = await authFetch(`${API_BASE}/admin/roles/${roleId}/roadmap`, {
      method: 'POST',
      headers: getAuthHeaders(),
      body: JSON.stringify({
        skillId: skillId ? parseInt(skillId, 10) : null,
        title,
        orderIndex: parseInt(orderIndex, 10)
      })
    });
    if (!res.ok) throw new Error('Failed to add roadmap item');
    return res.json();
  },

  async deleteRoadmapItem(id) {
    const res = await authFetch(`${API_BASE}/admin/roadmap/${id}`, {
      method: 'DELETE',
      headers: getAuthHeaders()
    });
    if (!res.ok) throw new Error('Failed to delete roadmap item');
    return res.json();
  },

  async createCatalogSkill(skillName, category) {
    const res = await authFetch(`${API_BASE}/admin/skills?skillName=${encodeURIComponent(skillName)}&category=${encodeURIComponent(category)}`, {
      method: 'POST',
      headers: getAuthHeaders()
    });
    if (!res.ok) throw new Error('Failed to add skill to catalog');
    return res.json();
  },

  // ================= PLACEMENT ADMIN BATCH ANALYTICS & HEATMAP (FEATURE 2) =================
  async getBatchSummary() {
    const res = await authFetch(`${API_BASE}/admin/analytics/batch-summary`, {
      headers: getAuthHeaders()
    });
    if (!res.ok) throw new Error('Failed to load batch summary analytics');
    return res.json();
  },

  async getHeatmap() {
    const res = await authFetch(`${API_BASE}/admin/analytics/heatmap`, {
      headers: getAuthHeaders()
    });
    if (!res.ok) throw new Error('Failed to load department vs role heatmap');
    return res.json();
  },

  async getCandidateRoster(minScore = '', roleId = '') {
    const params = new URLSearchParams();
    if (minScore !== '' && minScore !== null) params.append('minScore', minScore);
    if (roleId !== '' && roleId !== null) params.append('roleId', roleId);
    const res = await authFetch(`${API_BASE}/admin/analytics/candidates?${params.toString()}`, {
      headers: getAuthHeaders()
    });
    if (!res.ok) throw new Error('Failed to fetch candidate roster');
    return res.json();
  },

  async exportCandidatesCsv(minScore = '', roleId = '') {
    const params = new URLSearchParams();
    if (minScore !== '' && minScore !== null) params.append('minScore', minScore);
    if (roleId !== '' && roleId !== null) params.append('roleId', roleId);

    const res = await authFetch(`${API_BASE}/admin/export/candidates?${params.toString()}`, {
      headers: getAuthHeaders()
    });
    if (!res.ok) throw new Error('Failed to export candidate roster CSV');

    const blob = await res.blob();
    const downloadUrl = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = downloadUrl;
    a.download = `SkillForge_Candidate_Roster_${new Date().toISOString().slice(0, 10)}.csv`;
    document.body.appendChild(a);
    a.click();
    a.remove();
    window.URL.revokeObjectURL(downloadUrl);
  },

  // ================= AI TECH STACK ROADMAP GENERATOR =================
  async generateAiRoadmap(requestData) {
    const res = await authFetch(`${API_BASE}/roadmap/ai/generate`, {
      method: 'POST',
      headers: getAuthHeaders(),
      body: JSON.stringify(requestData)
    });
    if (!res.ok) {
      const err = await res.json().catch(() => ({}));
      throw new Error(err.message || 'Failed to generate AI roadmap');
    }
    return res.json();
  },

  async chatWithAi(messages, customApiKey = null) {
    const res = await authFetch(`${API_BASE}/roadmap/ai/chat`, {
      method: 'POST',
      headers: getAuthHeaders(),
      body: JSON.stringify({ messages, customApiKey })
    });
    if (!res.ok) {
      const err = await res.json().catch(() => ({}));
      throw new Error(err.message || 'Failed to communicate with AI Chatbot');
    }
    return res.json();
  },


  async saveAiRoadmap(roadmapData) {
    const res = await authFetch(`${API_BASE}/roadmap/ai/save`, {
      method: 'POST',
      headers: getAuthHeaders(),
      body: JSON.stringify(roadmapData)
    });
    if (!res.ok) throw new Error('Failed to save AI roadmap');
    return res.json();
  },

  async getSavedAiRoadmaps() {
    const res = await authFetch(`${API_BASE}/roadmap/ai/saved`, {
      headers: getAuthHeaders()
    });
    if (!res.ok) throw new Error('Failed to load saved roadmaps');
    return res.json();
  },

  async getSavedAiRoadmap(id) {
    const res = await authFetch(`${API_BASE}/roadmap/ai/saved/${id}`, {
      headers: getAuthHeaders()
    });
    if (!res.ok) throw new Error('Failed to load roadmap details');
    return res.json();
  },

  async updateAiMilestoneStatus(roadmapId, milestoneId, status) {
    const res = await authFetch(`${API_BASE}/roadmap/ai/${roadmapId}/milestones/${milestoneId}/status?status=${encodeURIComponent(status)}`, {
      method: 'POST',
      headers: getAuthHeaders()
    });
    if (!res.ok) throw new Error('Failed to update milestone progress');
    return res.json();
  },

  async deleteAiRoadmap(id) {
    const res = await authFetch(`${API_BASE}/roadmap/ai/${id}`, {
      method: 'DELETE',
      headers: getAuthHeaders()
    });
    if (!res.ok) throw new Error('Failed to delete custom roadmap');
    return res.json();
  }
};

