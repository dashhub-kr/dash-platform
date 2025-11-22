<template>
  <div class="playground-container">
    <AcornStack ref="stackRef" :totalSolved="currentTotalSolved" />

    <div class="controls-panel">
      <h1>도토리 물리 엔진 테스트</h1>

      <div
        class="tier-grid"
        style="
          display: grid;
          grid-template-columns: repeat(2, 1fr);
          gap: 10px;
          margin-top: 12px;
        "
      >
        <div
          style="
            display: flex;
            gap: 8px;
            align-items: center;
            justify-content: space-between;
          "
        >
          <div style="display: flex; gap: 8px; align-items: center">
            <label style="font-size: 13px; width: 70px">브론즈</label>
            <input
              v-model.number="spawnBronze"
              type="number"
              min="0"
              style="
                width: 80px;
                padding: 6px;
                border-radius: 6px;
                border: 1px solid #ddd;
                text-align: center;
              "
            />
          </div>
          <button
            @click="spawnOne('bronze')"
            class="btn btn-bronze"
            style="padding: 6px 10px"
          >
            +1
          </button>
        </div>

        <div
          style="
            display: flex;
            gap: 8px;
            align-items: center;
            justify-content: space-between;
          "
        >
          <div style="display: flex; gap: 8px; align-items: center">
            <label style="font-size: 13px; width: 70px">실버</label>
            <input
              v-model.number="spawnSilver"
              type="number"
              min="0"
              style="
                width: 80px;
                padding: 6px;
                border-radius: 6px;
                border: 1px solid #ddd;
                text-align: center;
              "
            />
          </div>
          <button
            @click="spawnOne('silver')"
            class="btn btn-silver"
            style="padding: 6px 10px"
          >
            +1
          </button>
        </div>

        <div
          style="
            display: flex;
            gap: 8px;
            align-items: center;
            justify-content: space-between;
          "
        >
          <div style="display: flex; gap: 8px; align-items: center">
            <label style="font-size: 13px; width: 70px">골드</label>
            <input
              v-model.number="spawnGold"
              type="number"
              min="0"
              style="
                width: 80px;
                padding: 6px;
                border-radius: 6px;
                border: 1px solid #ddd;
                text-align: center;
              "
            />
          </div>
          <button
            @click="spawnOne('gold')"
            class="btn btn-gold"
            style="padding: 6px 10px"
          >
            +1
          </button>
        </div>

        <div
          style="
            display: flex;
            gap: 8px;
            align-items: center;
            justify-content: space-between;
          "
        >
          <div style="display: flex; gap: 8px; align-items: center">
            <label style="font-size: 13px; width: 70px">플래티넘</label>
            <input
              v-model.number="spawnPlatinum"
              type="number"
              min="0"
              style="
                width: 80px;
                padding: 6px;
                border-radius: 6px;
                border: 1px solid #ddd;
                text-align: center;
              "
            />
          </div>
          <button
            @click="spawnOne('platinum')"
            class="btn btn-platinum"
            style="padding: 6px 10px"
          >
            +1
          </button>
        </div>
      </div>

      <div
        style="
          display: flex;
          gap: 10px;
          justify-content: center;
          margin-top: 14px;
        "
      >
        <button @click="spawnInit" class="btn btn-platinum">
          최초 도토리 로드
        </button>
        <button @click="resetAll" class="btn btn-danger">초기화</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from "vue";
import AcornStack from "./AcornStack.vue";

const currentTotalSolved = ref(0);
const spawnCount = ref(10);
const stackRef = ref(null);
const spawnBronze = ref(0);
const spawnSilver = ref(0);
const spawnGold = ref(0);
const spawnPlatinum = ref(0);

const addProblems = (count) => {
  currentTotalSolved.value += count;
};

const reset = () => {
  currentTotalSolved.value = 0;
  if (stackRef.value && typeof stackRef.value.clearSpawned === "function")
    stackRef.value.clearSpawned();
};

const spawnNow = () => {
  const n = Math.max(0, Math.floor(spawnCount.value || 0));
  if (n <= 0) return;
  if (stackRef.value && typeof stackRef.value.spawn === "function") {
    stackRef.value.spawn(n);
  } else {
    currentTotalSolved.value += n;
  }
};

const clearNow = () => {
  if (stackRef.value && typeof stackRef.value.clearSpawned === "function") {
    stackRef.value.clearSpawned();
  } else {
    currentTotalSolved.value = 0;
  }
};

const spawnTiersNow = () => {
  const b = Math.max(0, Math.floor(spawnBronze.value || 0));
  const s = Math.max(0, Math.floor(spawnSilver.value || 0));
  const g = Math.max(0, Math.floor(spawnGold.value || 0));
  const p = Math.max(0, Math.floor(spawnPlatinum.value || 0));
  const total = b + s + g + p;
  if (total <= 0) return;
  if (stackRef.value && typeof stackRef.value.spawnTiers === "function") {
    stackRef.value.spawnTiers({ bronze: b, silver: s, gold: g, platinum: p });
  } else {
    currentTotalSolved.value += total;
  }
};

const spawnOne = (tier) => {
  if (!tier) return;
  if (stackRef.value && typeof stackRef.value.spawnTiers === "function") {
    const counts = { bronze: 0, silver: 0, gold: 0, platinum: 0 };
    counts[tier] = 1;
    stackRef.value.spawnTiers(counts);
  } else {
    currentTotalSolved.value += 1;
  }
};

const spawnInit = async () => {
  if (stackRef.value && typeof stackRef.value.clearSpawned === "function") {
    stackRef.value.clearSpawned();
  } else {
    currentTotalSolved.value = 0;
  }

  const p = Math.max(0, Math.floor(spawnPlatinum.value || 0));
  const g = Math.max(0, Math.floor(spawnGold.value || 0));
  const s = Math.max(0, Math.floor(spawnSilver.value || 0));
  const b = Math.max(0, Math.floor(spawnBronze.value || 0));

  const delay = (ms) => new Promise((r) => setTimeout(r, ms));
  if (p > 0) {
    if (stackRef.value?.spawnTiers) stackRef.value.spawnTiers({ platinum: p });
    await delay(150);
  }
  if (g > 0) {
    if (stackRef.value?.spawnTiers) stackRef.value.spawnTiers({ gold: g });
    await delay(120);
  }
  if (s > 0) {
    if (stackRef.value?.spawnTiers) stackRef.value.spawnTiers({ silver: s });
    await delay(90);
  }
  if (b > 0) {
    if (stackRef.value?.spawnTiers) stackRef.value.spawnTiers({ bronze: b });
  }
};

const resetAll = () => {
  spawnBronze.value = 0;
  spawnSilver.value = 0;
  spawnGold.value = 0;
  spawnPlatinum.value = 0;
  currentTotalSolved.value = 0;
  if (stackRef.value && typeof stackRef.value.clearSpawned === "function") {
    stackRef.value.clearSpawned();
  }
};
</script>

<style scoped>
.playground-container {
  position: relative;
  width: 100vw;
  height: 100vh;
  overflow: hidden;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
}

.controls-panel {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  z-index: 10;
  background: rgba(255, 255, 255, 0.9);
  padding: 2rem;
  border-radius: 16px;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.1);
  text-align: center;
  backdrop-filter: blur(5px);
  min-width: 400px;
}

h1 {
  margin-bottom: 1.5rem;
  font-size: 1.8rem;
  color: #333;
}

.stats {
  font-size: 1.2rem;
  margin-bottom: 2rem;
}

.buttons {
  display: flex;
  gap: 10px;
  justify-content: center;
  flex-wrap: wrap;
  margin-bottom: 1.5rem;
}

.btn {
  padding: 10px 20px;
  border: none;
  border-radius: 8px;
  font-weight: bold;
  cursor: pointer;
  transition: transform 0.1s, filter 0.1s;
  color: white;
}
.btn:active {
  transform: scale(0.95);
}
.btn:hover {
  filter: brightness(1.1);
}

.btn-bronze {
  background-color: #ad5600;
}
.btn-silver {
  background-color: #435f7a;
}
.btn-gold {
  background-color: #ec9a00;
}
.btn-platinum {
  background-color: #27e2a4;
}
.btn-danger {
  background-color: #ff4757;
  margin-top: 10px;
}

.description {
  margin-top: 20px;
  font-size: 0.9rem;
  color: #666;
  line-height: 1.5;
}
</style>
