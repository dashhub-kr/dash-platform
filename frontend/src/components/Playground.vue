<template>
  <div class="playground-container">
    <!-- Physics Canvas -->
    <AcornStack ref="stackRef" :totalSolved="currentTotalSolved" />

    <!-- Overlay UI -->
    <div class="ui-overlay">
      <!-- Back Button -->
      <button @click="$router.push('/')" class="nav-back-btn">
        <span class="icon">⬅️</span>
        <span class="text">대시보드로</span>
      </button>

      <!-- Recent Activity Panel -->
      <transition name="float-panel">
        <div v-if="acornLogs.length > 0" class="recent-activity-panel">
          <h3 class="panel-title flex items-center gap-2">
            <Activity :size="16" class="text-indigo-500" />
            <span class="text-slate-500 font-bold text-xs uppercase tracking-wider">Recent Activity</span>
          </h3>
          <div class="activity-list custom-scrollbar">
            <div v-for="log in acornLogs" :key="log.id" class="activity-item group">
              <div class="flex items-center gap-3">
                <div class="avatar-circle">
                  {{ (log.username || '?').charAt(0).toUpperCase() }}
                </div>
                <div class="flex flex-col">
                  <span class="username">{{ log.username }}</span>
                  <span class="timestamp">{{ formatDate(log.createdAt) }}</span>
                </div>
              </div>
              <span class="amount" :class="log.amount > 0 ? 'text-emerald-500' : 'text-rose-500'">
                {{ log.amount > 0 ? '+' : '' }}{{ log.amount }}
              </span>
            </div>
          </div>
        </div>
      </transition>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, onMounted } from "vue";
import AcornStack from "./AcornStack.vue";
import { useAuth } from "../composables/useAuth";
import { studyApi } from "../api/study";
import { Activity } from "lucide-vue-next";

const { user } = useAuth();
const currentTotalSolved = ref(0);
const stackRef = ref(null);
const spawnBronze = ref(0);
const spawnSilver = ref(0);
const spawnGold = ref(0);
const spawnPlatinum = ref(0);
const acornLogs = ref([]);

const fetchAndSpawn = async () => {
  if (!user.value || !user.value.studyId) return;
  
  try {
    const studyRes = await studyApi.get(user.value.studyId);
    const study = studyRes.data;
    if (study) {
      const acornCount = study.acornCount || 0;
      currentTotalSolved.value = acornCount;
      
      // 도토리 티어 계산: Platinum(10000000), Gold(100000), Silver(1000), Bronze(10)
      let remaining = acornCount;
      
      const platinum = Math.floor(remaining / 10_000_000);
      remaining %= 10_000_000;
      
      const gold = Math.floor(remaining / 100_000);
      remaining %= 100_000;
      
      const silver = Math.floor(remaining / 1_000);
      remaining %= 1_000;
      
      const bronze = Math.floor(remaining / 10);
      
      spawnPlatinum.value = platinum;
      spawnGold.value = gold;
      spawnSilver.value = silver;
      spawnBronze.value = bronze;

      // Physics Spawn
      setTimeout(() => {
        spawnInit();
      }, 500);
    }

    // Fetch Acorn Logs
    const logsRes = await studyApi.getAcornLogs(user.value.studyId);
    acornLogs.value = logsRes.data || [];

  } catch (e) {
    console.error("Failed to fetch study info:", e);
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

const formatDate = (dateString) => {
    if (!dateString) return '';
    const date = new Date(dateString);
    const now = new Date();
    const diff = now - date;
    const minutes = Math.floor(diff / 60000);
    const hours = Math.floor(diff / 3600000);
    
    if (minutes < 1) return '방금 전';
    if (minutes < 60) return `${minutes}분 전`;
    if (hours < 24) return `${hours}시간 전`;
    return date.toLocaleDateString('ko-KR', { month: 'short', day: 'numeric' });
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
  z-index: 100;
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

/* Recent Activity Panel (Center Stage) */
.recent-activity-panel {
  pointer-events: auto;
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%); /* Centered */
  
  width: 360px;
  max-height: 480px;
  background: rgba(255, 255, 255, 0.65); /* More transparent */
  backdrop-filter: blur(24px);
  -webkit-backdrop-filter: blur(24px);
  border: 1px solid rgba(255, 255, 255, 0.4);
  border-radius: 32px;
  padding: 24px;
  box-shadow: 
    0 20px 40px -12px rgba(0, 0, 0, 0.1),
    0 0 0 1px rgba(255, 255, 255, 0.5) inset;
  color: #1e293b;
  display: flex;
  flex-direction: column;
  animation: floatPanel 0.8s cubic-bezier(0.16, 1, 0.3, 1) forwards;
}

.float-panel-enter-active,
.float-panel-leave-active {
  transition: all 0.5s cubic-bezier(0.16, 1, 0.3, 1);
}

.float-panel-enter-from,
.float-panel-leave-to {
  opacity: 0;
  transform: translate(-50%, -40%) scale(0.95);
}

.panel-title {
  padding-bottom: 16px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
  margin-bottom: 16px;
}

.activity-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  overflow-y: auto;
  padding-right: 4px;
  /* Hide scrollbar logic if needed or custom scrollbar */
}

.activity-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.4);
  border: 1px solid rgba(255, 255, 255, 0.4);
  transition: all 0.2s;
}

.activity-item:hover {
  background: rgba(255, 255, 255, 0.8);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.avatar-circle {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: linear-gradient(135deg, #6366f1, #a855f7);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: 800;
  font-size: 14px;
  box-shadow: 0 4px 10px rgba(99, 102, 241, 0.3);
}

.username {
  font-size: 14px;
  font-weight: 700;
  color: #334155;
}

.timestamp {
  font-size: 11px;
  font-weight: 500;
  color: #94a3b8;
}

.amount {
  font-family: monospace;
  font-size: 16px;
  font-weight: 800;
}

/* Custom Scrollbar for the list */
.custom-scrollbar::-webkit-scrollbar {
  width: 4px;
}
.custom-scrollbar::-webkit-scrollbar-track {
  background: transparent;
}
.custom-scrollbar::-webkit-scrollbar-thumb {
  background: rgba(0,0,0,0.1);
  border-radius: 4px;
}
.custom-scrollbar::-webkit-scrollbar-thumb:hover {
  background: rgba(0,0,0,0.2);
}

@keyframes floatPanel {
  from { opacity: 0; transform: translate(-50%, -45%); }
  to { opacity: 1; transform: translate(-50%, -50%); }
}
</style>


