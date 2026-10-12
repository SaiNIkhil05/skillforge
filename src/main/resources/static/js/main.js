/**
 * SkillForge Editorial Client Interactions
 */

document.addEventListener('DOMContentLoaded', () => {
  initRoadmapToggles();
  initSkillSearchFilter();
});

/**
 * Handles 3-state segmented control for roadmap items:
 * NOT_STARTED, IN_PROGRESS, COMPLETED
 */
function initRoadmapToggles() {
  const toggleButtons = document.querySelectorAll('.segmented-option[data-roadmap-id]');
  if (!toggleButtons.length) return;

  toggleButtons.forEach(btn => {
    btn.addEventListener('click', async (e) => {
      e.preventDefault();
      const roadmapId = btn.getAttribute('data-roadmap-id');
      const targetStatus = btn.getAttribute('data-status');
      const parentControl = btn.closest('.segmented-control');

      try {
        const formData = new URLSearchParams();
        formData.append('roadmapId', roadmapId);
        formData.append('status', targetStatus);

        const response = await fetch('/roadmap/update', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
          },
          body: formData.toString()
        });

        if (!response.ok) {
          throw new Error('Failed to update milestone status');
        }

        const data = await response.json();

        // Update UI state in segmented control
        parentControl.querySelectorAll('.segmented-option').forEach(opt => {
          opt.classList.remove('active', 'active-completed');
        });

        if (targetStatus === 'COMPLETED') {
          btn.classList.add('active-completed');
        } else {
          btn.classList.add('active');
        }

        // Dynamically update readiness score indicators if present on page
        updateScoreIndicators(data);

      } catch (err) {
        console.error('Error toggling milestone status:', err);
        alert('Could not update status: ' + err.message);
      }
    });
  });
}

function updateScoreIndicators(data) {
  const scoreDisplay = document.getElementById('readinessScoreValue');
  const scoreBar = document.getElementById('readinessProgressBar');
  const statusMsg = document.getElementById('readinessStatusMessage');
  const coreGapCount = document.getElementById('missingCoreCountValue');
  const goodGapCount = document.getElementById('missingGoodCountValue');

  if (scoreDisplay && data.updatedScore !== undefined) {
    scoreDisplay.textContent = data.updatedScore;
  }
  if (scoreBar && data.updatedScore !== undefined) {
    scoreBar.style.width = data.updatedScore + '%';
  }
  if (statusMsg && data.statusMessage) {
    statusMsg.textContent = data.statusMessage;
  }
  if (coreGapCount && data.missingCoreCount !== undefined) {
    coreGapCount.textContent = data.missingCoreCount;
  }
  if (goodGapCount && data.missingGoodToHaveCount !== undefined) {
    goodGapCount.textContent = data.missingGoodToHaveCount;
  }
}

/**
 * Live search and category filter for skills in Profile Builder
 */
function initSkillSearchFilter() {
  const searchInput = document.getElementById('skillSearchInput');
  const categoryFilter = document.getElementById('skillCategoryFilter');
  const skillSelect = document.getElementById('skillSelectDropdown');

  if (!searchInput && !categoryFilter) return;

  function filterOptions() {
    const query = (searchInput ? searchInput.value : '').toLowerCase().trim();
    const selectedCategory = categoryFilter ? categoryFilter.value : '';

    if (!skillSelect) return;

    let visibleCount = 0;
    Array.from(skillSelect.options).forEach(opt => {
      if (!opt.value) return; // Keep placeholder option

      const name = opt.getAttribute('data-name') ? opt.getAttribute('data-name').toLowerCase() : opt.text.toLowerCase();
      const cat = opt.getAttribute('data-category') || '';

      const matchesSearch = !query || name.includes(query);
      const matchesCategory = !selectedCategory || cat === selectedCategory;

      if (matchesSearch && matchesCategory) {
        opt.style.display = '';
        visibleCount++;
      } else {
        opt.style.display = 'none';
      }
    });
  }

  if (searchInput) {
    searchInput.addEventListener('input', filterOptions);
  }
  if (categoryFilter) {
    categoryFilter.addEventListener('change', filterOptions);
  }
}
