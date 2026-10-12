import React, { useState, useEffect } from 'react';
import { api } from './api';
import Sidebar from './components/Sidebar';
import Landing from './components/Landing';
import Login from './components/Login';
import Register from './components/Register';
import DashboardView from './components/DashboardView';
import ProfileView from './components/ProfileView';
import ReadinessView from './components/ReadinessView';
import RoadmapView from './components/RoadmapView';
import AiRoadmapView from './components/AiRoadmapView';
import AdminView from './components/AdminView';
import AdminAnalyticsView from './components/AdminAnalyticsView';

export default function App() {
  const [currentUser, setCurrentUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const validTabs = ['dashboard', 'profile', 'readiness', 'roadmap', 'ai-roadmap', 'admin', 'analytics'];

  const getTabFromHash = (user) => {
    const raw = (window.location.hash || '').replace(/^#\/?/, '');
    if (validTabs.includes(raw)) return raw;
    return user?.role === 'ADMIN' ? 'analytics' : 'dashboard';
  };

  const [activeTab, setActiveTabState] = useState(() => getTabFromHash(null));
  const [publicView, setPublicView] = useState('landing'); // 'landing' | 'login' | 'register'

  const setActiveTab = (tab) => {
    setActiveTabState(tab);
    window.location.hash = tab;
  };

  useEffect(() => {
    const handleHashChange = () => {
      const raw = (window.location.hash || '').replace(/^#\/?/, '');
      if (validTabs.includes(raw)) {
        setActiveTabState(raw);
      }
    };
    window.addEventListener('hashchange', handleHashChange);
    return () => window.removeEventListener('hashchange', handleHashChange);
  }, []);

  const checkAuth = async () => {
    try {
      setLoading(true);
      const user = await api.getCurrentUser();
      setCurrentUser(user);
      if (user) {
        const currentHashTab = getTabFromHash(user);
        setActiveTabState(currentHashTab);
      } else {
        setPublicView('landing');
      }
    } catch (err) {
      setCurrentUser(null);
      setPublicView('landing');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    checkAuth();
  }, []);

  const handleLoginSuccess = (authData) => {
    checkAuth();
  };

  const handleLogout = async () => {
    await api.logout();
    setCurrentUser(null);
    setPublicView('login');
  };

  if (loading) {
    return (
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', minHeight: '100vh', fontFamily: 'var(--font-sans)', color: 'var(--ink-secondary)' }}>
        Initializing SkillForge...
      </div>
    );
  }

  // Unauthenticated screens
  if (!currentUser) {
    if (publicView === 'login') {
      return (
        <Login
          onLoginSuccess={handleLoginSuccess}
          onNavigateRegister={() => setPublicView('register')}
          onNavigateHome={() => setPublicView('landing')}
        />
      );
    }
    if (publicView === 'register') {
      return (
        <Register
          onRegisterSuccess={handleLoginSuccess}
          onNavigateLogin={() => setPublicView('login')}
          onNavigateHome={() => setPublicView('landing')}
        />
      );
    }
    return (
      <Landing
        onNavigateLogin={() => setPublicView('login')}
        onNavigateRegister={() => setPublicView('register')}
      />
    );
  }

  // Authenticated screens with Sidebar Shell
  return (
    <div className="app-container">
      <Sidebar
        user={currentUser}
        activeTab={activeTab}
        setActiveTab={setActiveTab}
        onLogout={handleLogout}
      />

      <main className="app-main">
        {activeTab === 'dashboard' && (
          <DashboardView
            user={currentUser}
            onNavigateTab={setActiveTab}
          />
        )}
        {activeTab === 'profile' && (
          <ProfileView
            user={currentUser}
            onProfileUpdated={checkAuth}
          />
        )}
        {activeTab === 'readiness' && (
          <ReadinessView
            onNavigateTab={setActiveTab}
          />
        )}
        {activeTab === 'roadmap' && (
          <RoadmapView onNavigateTab={setActiveTab} />
        )}
        {activeTab === 'ai-roadmap' && (
          <AiRoadmapView user={currentUser} />
        )}
        {activeTab === 'admin' && (
          <AdminView />
        )}
        {activeTab === 'analytics' && (
          <AdminAnalyticsView />
        )}
      </main>
    </div>
  );
}

