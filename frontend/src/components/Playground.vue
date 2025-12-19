<template>
  <div class="playground-container">
    <!-- Physics Canvas -->
    <AcornStack ref="stackRef" :totalSolved="currentTotalSolved" />

    <!-- Overlay UI -->
    <div class="ui-overlay">
      <!-- Back Button -->
      <button @click="$router.push('/map')" class="nav-back-btn">
        <span class="icon">⬅️</span>
        <span class="text">월드 맵으로</span>
      </button>

      <!-- Stats Card -->
      <div class="stats-glass-panel">
        <h2 class="storage-title">도토리 창고</h2>
        <div class="total-count">
          <span class="label">보유 도토리</span>
          <span class="number">{{ currentTotalSolved }}</span>
        </div>
        
        <div class="tier-breakdown">
          <div class="tier-item bronze">
            <span class="dot"></span>
            <span class="name">Bronze</span>
            <span class="count">{{ spawnBronze }}</span>
          </div>
          <div class="tier-item silver">
            <span class="dot"></span>
            <span class="name">Silver</span>
            <span class="count">{{ spawnSilver }}</span>
          </div>
          <div class="tier-item gold">
            <span class="dot"></span>
            <span class="name">Gold</span>
            <span class="count">{{ spawnGold }}</span>
          </div>
          <div class="tier-item platinum">
            <span class="dot"></span>
            <span class="name">Platinum</span>
            <span class="count">{{ spawnPlatinum }}</span>
          </div>
        </div>

        <div class="actions">
          <button @click="spawnInit" class="refresh-btn">
            🔄 새로고침
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, onMounted } from "vue";
import AcornStack from "./AcornStack.vue";
import { useAuth } from "../composables/useAuth";
import { studyApi } from "../api/study";

const { user } = useAuth();
const currentTotalSolved = ref(0);
const stackRef = ref(null);
const spawnBronze = ref(0);
const spawnSilver = ref(0);
const spawnGold = ref(0);
const spawnPlatinum = ref(0);

const fetchAndSpawn = async () => {
  if (!user.value || !user.value.studyId) return;
  
  try {
    const res = await studyApi.getStats(user.value.studyId);
    const stats = res.data; 
    if (stats) {
      spawnBronze.value = stats.bronze;
      spawnSilver.value = stats.silver;
      spawnGold.value = stats.gold;
      spawnPlatinum.value = stats.platinum;
      
      const total = stats.bronze + stats.silver + stats.gold + stats.platinum;
      // We update currentTotalSolved after spawning to sync with visual physics if needed,
      // but for the UI counter, we can set it immediately or accumulate.
      // Let's set it to total for the UI display.
      currentTotalSolved.value = total;

      // Physics Spawn
      setTimeout(() => {
        spawnInit();
      }, 500);
    }
  } catch (e) {
    console.error("Failed to fetch study stats:", e);
  }
};

watch(user, (newUser) => {
  if (newUser && newUser.studyId) {
    fetchAndSpawn();
  }
}, { immediate: true });

const spawnInit = async () => {
  if (stackRef.value && typeof stackRef.value.clearSpawned === "function") {
    stackRef.value.clearSpawned();
  }

  const p = Math.max(0, Math.floor(spawnPlatinum.value || 0));
  const g = Math.max(0, Math.floor(spawnGold.value || 0));
  const s = Math.max(0, Math.floor(spawnSilver.value || 0));
  const b = Math.max(0, Math.floor(spawnBronze.value || 0));

  const delay = (ms) => new Promise((r) => setTimeout(r, ms));
  if (p > 0) {
    if (stackRef.value?.spawnTiers) stackRef.value.spawnTiers({ platinum: p });
    await delay(200);
  }
  if (g > 0) {
    if (stackRef.value?.spawnTiers) stackRef.value.spawnTiers({ gold: g });
    await delay(200);
  }
  if (s > 0) {
    if (stackRef.value?.spawnTiers) stackRef.value.spawnTiers({ silver: s });
    await delay(200);
  }
  if (b > 0) {
    if (stackRef.value?.spawnTiers) stackRef.value.spawnTiers({ bronze: b });
  }
};
</script>

<style scoped>

.playground-container {
  position: relative;
  width: 100vw;
  height: 100vh;
  overflow: hidden;
  /* Light & Trendy Gradient Background */
  background: linear-gradient(135deg, #eff6ff 0%, #e0e7ff 100%);
}

.ui-overlay {
  position: absolute;
  inset: 0;
  pointer-events: none;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding: 40px;
}

/* Back Button */
.nav-back-btn {
  pointer-events: auto;
  align-self: flex-start;
  display: flex;
  align-items: center;
  gap: 8px;
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(12px);
  border: 1px solid rgba(255, 255, 255, 0.5);
  padding: 12px 20px;
  border-radius: 9999px;
  color: #475569;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05);
}

.nav-back-btn:hover {
  background: white;
  transform: translateX(-4px);
  color: #2563eb;
  box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1);
}

/* Stats Glass Panel */
.stats-glass-panel {
  pointer-events: auto;
  align-self: flex-end;
  position: absolute;
  top: 40px;
  right: 40px;
  
  width: 280px;
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.6);
  border-radius: 24px;
  padding: 24px;
  box-shadow: 0 20px 50px -12px rgba(0, 0, 0, 0.1);
  color: #1e293b;
  animation: floatPanel 1s ease-out forwards;
}

@keyframes floatPanel {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

.storage-title {
  font-size: 14px;
  text-transform: uppercase;
  letter-spacing: 1px;
  color: #64748b;
  margin-bottom: 8px;
  font-weight: 800;
}

.total-count {
  margin-bottom: 24px;
}

.total-count .number {
  display: block;
  font-size: 48px;
  font-weight: 900;
  line-height: 1;
  background: linear-gradient(to right, #4f46e5, #06b6d4);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
}

.tier-breakdown {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 24px;
}

.tier-item {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 14px;
  font-weight: 600;
}

.tier-item .dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.tier-item .name {
  flex-grow: 1;
  color: #475569;
}

.tier-item .count {
  font-weight: 800;
  color: #1e293b;
}

/* Colors for Tiers */
.bronze .dot { background: #d97706; box-shadow: 0 0 8px rgba(217, 119, 6, 0.4); }
.silver .dot { background: #94a3b8; box-shadow: 0 0 8px rgba(148, 163, 184, 0.4); }
.gold   .dot { background: #eab308; box-shadow: 0 0 8px rgba(234, 179, 8, 0.4); }
.platinum .dot { background: #10b981; box-shadow: 0 0 8px rgba(16, 185, 129, 0.4); }

.actions {
  display: flex;
  justify-content: center;
}

.refresh-btn {
  width: 100%;
  padding: 12px;
  background: white;
  border: 1px solid #e2e8f0;
  border-radius: 16px;
  color: #475569;
  font-size: 13px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.2s;
  box-shadow: 0 2px 4px rgba(0,0,0,0.05);
}

.refresh-btn:hover {
  background: #f8fafc;
  color: #334155;
  border-color: #cbd5e1;
  transform: translateY(-1px);
}
</style>


