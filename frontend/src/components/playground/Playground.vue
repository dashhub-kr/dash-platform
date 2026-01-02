<template>
  <div class="playground-container">
    <!-- 물리 캔버스 -->
    <AcornStack ref="stackRef" />

    <!-- 오버레이 UI -->
    <div class="ui-overlay">
      <!-- 상단 네비게이션 -->
      <div class="nav-bar">
        <button @click="$router.push('/')" class="nav-back-btn">
          <span class="icon">⬅️</span>
          <span class="text">대시보드로</span>
        </button>
      </div>

      <!-- 메인 콘텐츠 영역 -->
      <div class="hud-container">
        
        <!-- 왼쪽: 미니멀 가이드 카드 -->
        <div class="guide-card animate-fade-in-left">
           <div class="card-header">
              <div class="icon-wrapper">
                 <img src="/images/items/acorn-1.png" class="w-8 h-8 object-contain drop-shadow-sm" alt="acorn" />
              </div>
              <div class="header-text">
                 <h3 class="font-black text-slate-800 text-lg">도토리 창고</h3>
                 <p class="text-xs text-slate-500 font-bold uppercase tracking-wider">Acorn Warehouse</p>
              </div>
           </div>
           
           <div class="stat-box">
              <span class="label">수집된 도토리 수</span>
              <span class="value">{{ currentTotalSolved.toLocaleString() }}</span>
           </div>

           <div class="hint-text">
              도토리들이 모여<br/>어떤 모습으로 변할지 지켜보세요.
           </div>
        </div>

        <!-- 오른쪽 하단: 라이브 로그 피드 -->
        <div class="log-feed animate-fade-in-up">
           <div class="feed-header">
              <span class="live-badge">LIVE ACTIVITY</span>
           </div>
           
           <transition-group name="feed-anim" tag="div" class="feed-list">
             <div v-for="log in acornLogs.slice(0, 5)" :key="log.id" class="feed-item">
                <div class="avatar-wrapper">
                   <img v-if="log.avatarUrl" :src="log.avatarUrl" class="avatar-img" alt="profile" />
                   <div v-else class="avatar-circle small">
                      {{ (log.username || '?').charAt(0).toUpperCase() }}
                   </div>
                </div>
                <div class="feed-content">
                   <div class="feed-row top">
                      <span class="username">{{ log.username }}</span>
                      <span class="timestamp">{{ formatDate(log.createdAt) }}</span>
                   </div>
                   <div class="feed-row bottom">
                      <span class="reason">{{ log.reason || 'Activity' }}</span>
                      <span class="amount" :class="log.amount > 0 ? 'plus' : 'minus'">
                         {{ log.amount > 0 ? '+' : '' }}{{ log.amount }}
                      </span>
                   </div>
                </div>
             </div>
           </transition-group>
        </div>

      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, onMounted } from "vue";
import AcornStack from "./AcornStack.vue";
import { useAuth } from "@/composables/useAuth";
import { studyApi } from "@/api/study";
import { Activity } from "lucide-vue-next";

// ... (keep logic same)
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

      // 물리 엔진 생성
      setTimeout(() => {
        spawnInit();
      }, 500);
    }

    // 도토리 로그 가져오기
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
  position: fixed;
  inset: 0;
  overflow: hidden;
  /* 도토리 창고 배경 이미지 */
  background: url('/images/background/storage.png') no-repeat center center;
  background-size: cover;
  z-index: 50; /* Ensure it stays on top of any potential layout wrappers */
}

.ui-overlay {
  position: absolute;
  inset: 0;
  pointer-events: none;
  z-index: 100;
  display: flex;
  flex-direction: column;
  padding: 32px;
}

/* Navigation Bar */
.nav-bar {
  display: flex;
  margin-bottom: 24px;
}

.nav-back-btn {
  pointer-events: auto;
  display: flex;
  align-items: center;
  gap: 8px;
  background: rgba(255, 255, 255, 0.85); /* Slightly more opaque */
  backdrop-filter: blur(12px);
  border: 1px solid rgba(251, 191, 36, 0.3); /* Amber border */
  padding: 10px 18px;
  border-radius: 9999px;
  color: #78350f; /* Amber-900 */
  font-weight: 700;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s ease;
  box-shadow: 0 4px 6px -1px rgba(69, 26, 3, 0.1); /* Brown shadow */
}

.nav-back-btn:hover {
  background: #fffbeb; /* Amber-50 */
  transform: translateY(-2px);
  box-shadow: 0 8px 15px -3px rgba(69, 26, 3, 0.15);
  color: #d97706; /* Amber-600 */
}

/* HUD Container */
.hud-container {
  flex: 1;
  position: relative;
  display: flex;
  flex-direction: column;
}

/* Guide Card (Left Top) */
.guide-card {
  pointer-events: auto;
  width: 260px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(251, 191, 36, 0.2); /* Subtle amber border */
  border-radius: 24px;
  padding: 24px;
  box-shadow: 0 20px 50px -12px rgba(69, 26, 3, 0.15), 0 0 0 1px rgba(251, 191, 36, 0.1); /* Warm shadow */
  animation: fadeInLeft 0.6s cubic-bezier(0.2, 0.8, 0.2, 1);
}

.card-header {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 24px;
}

.icon-wrapper {
  width: 46px;
  height: 46px;
  background: #fff7ed; /* Orange-50 */
  border: 1px solid #fed7aa; /* Orange-200 */
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 6px rgba(194, 65, 12, 0.1); /* Orange-700 tint shadow */
}

.header-text {
  display: flex;
  flex-direction: column;
}

.header-text h3 {
  color: #451a03; /* Amber-950 */
}

.header-text p {
  color: #a16207; /* Yellow-700 */
}


.stat-box {
  background: #fffbeb; /* Amber-50 */
  border: 1px solid #fde68a; /* Amber-200 */
  border-radius: 18px;
  padding: 16px 20px;
  margin-bottom: 20px;
  box-shadow: inset 0 2px 4px rgba(69, 26, 3, 0.03);
}

.stat-box .label {
  display: block;
  font-size: 11px;
  font-weight: 800;
  color: #92400e; /* Amber-800 */
  margin-bottom: 4px;
  letter-spacing: 0.05em;
  text-transform: uppercase;
}

.stat-box .value {
  display: block;
  font-size: 28px;
  font-weight: 900;
  color: #451a03; /* Amber-950 */
  font-family: monospace;
  letter-spacing: -0.05em;
  line-height: 1;
}

.hint-text {
  font-size: 13px;
  color: #78350f; /* Amber-900 */
  font-weight: 600;
  line-height: 1.6;
  opacity: 0.9;
}

/* Log Feed (Right Bottom) */
.log-feed {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 320px;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  pointer-events: none; 
}

.feed-header {
  margin-bottom: 12px;
  text-align: right;
  opacity: 0.8;
}

.live-badge {
  background: rgba(69, 26, 3, 0.6); /* Dark brown */
  color: #fef3c7; /* Amber-100 */
  font-size: 9px;
  font-weight: 800;
  padding: 4px 8px;
  border-radius: 99px;
  backdrop-filter: blur(4px);
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.feed-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  align-items: flex-end;
  pointer-events: auto;
  width: 100%;
}

.feed-item {
  width: 100%;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(12px);
  border: 1px solid rgba(251, 191, 36, 0.3); /* Amber border */
  padding: 12px;
  border-radius: 16px;
  box-shadow: 0 4px 15px rgba(69, 26, 3, 0.08);
  display: flex;
  align-items: center;
  gap: 12px;
  transform-origin: bottom right;
  transition: all 0.3s ease;
}

.feed-item:hover {
  transform: scale(1.02) translateX(-4px);
  background: #fffbeb; /* Amber-50 */
  box-shadow: 0 8px 25px rgba(69, 26, 3, 0.12);
  border-color: #fcd34d; /* Amber-300 */
}

.avatar-wrapper {
  width: 36px;
  height: 36px;
  flex-shrink: 0;
}

.avatar-img {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  object-fit: cover;
  box-shadow: 0 2px 5px rgba(0,0,0,0.1);
  border: 1px solid rgba(251, 191, 36, 0.2);
}

.avatar-circle.small {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  background: linear-gradient(135deg, #d97706, #b45309); /* Amber gradient */
  color: white;
  font-size: 13px;
  font-weight: 800;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 5px rgba(180, 83, 9, 0.3);
}

.feed-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}

.feed-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
}

.username {
  font-size: 13px;
  font-weight: 700;
  color: #451a03; /* Amber-950 */
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.timestamp {
  font-size: 10px;
  color: #a16207; /* Yellow-700 */
  font-weight: 600;
}

.reason {
  font-size: 11px;
  color: #78350f; /* Amber-900 */
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 140px;
}

.amount {
  font-family: monospace;
  font-weight: 800;
  font-size: 14px;
}
.amount.plus { color: #d97706; /* Amber-600 (Darker than green to fit theme) */ }
.amount.minus { color: #dc2626; }

/* Animations */
.feed-anim-enter-active,
.feed-anim-leave-active {
  transition: all 0.4s cubic-bezier(0.16, 1, 0.3, 1);
}
.feed-anim-enter-from {
  opacity: 0;
  transform: translateY(20px) scale(0.9);
}
.feed-anim-leave-to {
  opacity: 0;
  transform: translateY(-20px) scale(0.9);
}

@keyframes fadeInLeft {
  from { opacity: 0; transform: translateX(-30px); }
  to { opacity: 1; transform: translateX(0); }
}

@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(30px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>


