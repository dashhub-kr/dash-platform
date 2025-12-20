<template>
  <div class="world-map-container font-[Pretendard]">
    <!-- Ambient Background -->
    <div class="map-bg">
      <div class="cloud-1"></div>
      <div class="cloud-2"></div>
      <div class="cloud-3"></div>
    </div>

    <!-- 3D Scene -->
    <div class="scene">
      <div class="map-plane">
        
        <!-- Zone 1: The Warehouse (Playground) -->
        <div class="island warehouse" @click="enterZone('warehouse')">
          <div class="island-base"></div>
          <div class="island-top">
            <div class="building-warehouse">
              <div class="roof"></div>
              <div class="body">
                <span class="icon transform hover:scale-110 transition-transform">🎪</span>
              </div>
            </div>
            <div class="label-tag">도토리 창고</div>
          </div>
        </div>

        <!-- Zone 2: Tower (Coming Soon) -->
        <div class="island tower" @click="enterZone('tower')">
          <div class="island-base"></div>
          <div class="island-top">
            <div class="building-tower">
              <span class="icon transform hover:scale-110 transition-transform">⛲</span>
            </div>
            <div class="label-tag">시련의 탑</div>
          </div>
        </div>

        <!-- Zone 3: Defense (Coming Soon) -->
        <div class="island defense" @click="enterZone('defense')">
          <div class="island-base"></div>
          <div class="island-top">
            <div class="building-defense">
              <span class="icon transform hover:scale-110 transition-transform">🎡</span>
            </div>
            <div class="label-tag">랜덤 디펜스</div>
          </div>
        </div>

        <!-- Zone 4: Academy (Coming Soon) -->
        <div class="island academy" @click="enterZone('academy')">
          <div class="island-base"></div>
          <div class="island-top">
            <div class="building-academy">
              <span class="icon transform hover:scale-110 transition-transform">🏫</span>
            </div>
            <div class="label-tag">알고리즘 학당</div>
          </div>
        </div>

        <!-- Connecting Paths (Glowing Lines) -->
        <svg class="paths">
          <!-- Center to Warehouse -->
          <path d="M400 300 L400 150" class="path-line" />
          <!-- Center to Tower -->
          <path d="M400 300 L200 300" class="path-line" />
          <!-- Center to Defense -->
          <path d="M400 300 L600 300" class="path-line" />
          <!-- Center to Academy -->
          <path d="M400 300 L400 450" class="path-line" />
        </svg>

        <!-- Avatar (Improved Design) -->
        <div class="avatar-container">
          <div class="avatar-body">
             <div class="character">
                <div class="hat"></div>
                <div class="face">
                    <div class="eyes"></div>
                </div>
             </div>
            <div class="avatar-shadow"></div>
          </div>
        </div>

      </div>
    </div>

    <!-- UI HUD -->
    <div class="hud">
      <button @click="$router.push('/dashboard')" class="hud-btn back group">
        <span class="group-hover:-translate-x-1 transition-transform">⬅️</span>
        <span class="font-bold text-slate-600 group-hover:text-indigo-600 transition-colors">대시보드</span>
      </button>
      <div class="title-container bg-white/80 backdrop-blur-md px-6 py-2 rounded-full shadow-lg border border-white/50">
          <h1 class="map-title text-xl font-extrabold text-transparent bg-clip-text bg-gradient-to-r from-indigo-500 to-sky-500">DASH WORLD</h1>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter();

const enterZone = (zoneId) => {
  if (zoneId === 'warehouse') {
    router.push('/playground');
  } else if (zoneId === 'defense') {
    router.push('/defense');
  } else {
    // Shake animation or toast
    alert("🚧 공사 중인 구역입니다.");
  }
};
</script>

<style scoped>
@import url('https://cdn.jsdelivr.net/gh/orioncactus/pretendard/dist/web/static/pretendard.css');

/* 1. Environment & Container */
.world-map-container {
  width: 100vw;
  height: 100vh;
  /* Light Sky Gradient */
  background: linear-gradient(to bottom, #f0f9ff 0%, #e0f2fe 100%);
  overflow: hidden;
  position: relative;
}

.map-bg {
  position: absolute;
  inset: 0;
  z-index: 0;
  overflow: hidden;
}

/* Clouds Decoration */
.cloud-1, .cloud-2, .cloud-3 {
    position: absolute;
    background: white;
    border-radius: 999px;
    opacity: 0.6;
    filter: blur(20px);
}
.cloud-1 { width: 300px; height: 100px; top: 10%; left: 10%; animation: floatCloud 20s infinite alternate; }
.cloud-2 { width: 400px; height: 120px; top: 20%; right: 15%; animation: floatCloud 25s infinite alternate-reverse; }
.cloud-3 { width: 250px; height: 80px; bottom: 15%; left: 20%; animation: floatCloud 18s infinite alternate; }

@keyframes floatCloud {
    from { transform: translateX(0); }
    to { transform: translateX(30px); }
}

/* 2. 3D Scene Setup */
.scene {
  width: 100%;
  height: 100%;
  perspective: 1200px;
  display: flex;
  justify-content: center;
  align-items: center;
}

.map-plane {
  width: 800px;
  height: 600px;
  position: relative;
  transform-style: preserve-3d;
  transform: rotateX(45deg) rotateZ(0deg); /* Iso-like view */
}

/* 3. Islands (Common) */
.island {
  position: absolute;
  width: 120px;
  height: 120px;
  transform-style: preserve-3d;
  cursor: pointer;
  transition: transform 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}

.island:hover {
  transform: translateZ(30px) scale(1.1);
}

.island-base {
  position: absolute;
  inset: 0;
  background: #ffffff;
  border-radius: 50%;
  transform: translateZ(0);
  box-shadow: 0 10px 30px rgba(99, 102, 241, 0.15); /* Soft Indigo Shadow */
  border: 4px solid #e0e7ff; /* Light Indigo Border */
}

.island-top {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  transform: translateZ(40px); /* Lift content up */
}

/* Labels */
.label-tag {
  margin-top: 10px;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(4px);
  padding: 6px 14px;
  border-radius: 99px;
  color: #475569;
  font-weight: 800;
  font-size: 14px;
  border: 1px solid rgba(255,255,255,0.5);
  box-shadow: 0 4px 10px rgba(0,0,0,0.05);
  pointer-events: none;
  opacity: 0.9;
  transition: all 0.3s;
}

.island:hover .label-tag {
  opacity: 1;
  transform: translateY(-5px);
  background: #4f46e5;
  color: white;
  box-shadow: 0 8px 20px rgba(79, 70, 229, 0.3);
}

/* Specific Buildings (Visuals) */
.icon {
  font-size: 48px;
  filter: drop-shadow(0 4px 6px rgba(0,0,0,0.1));
}

/* Coordinate Positioning */
.warehouse { top: 0; left: 50%; margin-left: -60px; } /* Top Center */
.tower { top: 50%; left: 0; margin-top: -60px; } /* Left Center */
.defense { top: 50%; right: 0; margin-top: -60px; } /* Right Center */
.academy { bottom: 0; left: 50%; margin-left: -60px; } /* Bottom Center */

/* 4. Paths */
.paths {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  transform: translateZ(5px);
}

.path-line {
  stroke: #cbd5e1; /* Slate-300 */
  stroke-width: 4;
  stroke-dasharray: 10;
  stroke-linecap: round;
  /* filter: drop-shadow(0 0 2px rgba(255,255,255,0.8)); */
  opacity: 0.6;
}

/* 5. Avatar (CSS Character) */
.avatar-container {
  position: absolute;
  top: 50%;
  left: 50%;
  width: 60px;
  height: 60px;
  margin-top: -30px;
  margin-left: -30px;
  transform-style: preserve-3d;
  transform: translateZ(20px);
  z-index: 10;
}

.avatar-body {
  position: relative;
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  animation: floatAvatar 2s ease-in-out infinite;
}

.character {
    width: 44px;
    height: 44px;
    background: #4f46e5; /* Indigo body */
    border-radius: 12px;
    position: relative;
    box-shadow: inset 0 -4px 0 rgba(0,0,0,0.2);
}

.character::before { /* Backpack */
    content: '';
    position: absolute;
    top: 5px;
    left: -6px;
    width: 6px;
    height: 25px;
    background: #4338ca;
    border-radius: 4px 0 0 4px;
}

/* Explorer Hat */
.hat {
    position: absolute;
    top: -12px;
    left: 50%;
    transform: translateX(-50%);
    width: 54px;
    height: 16px;
    background: #fcd34d; /* Yellow hat */
    border-radius: 20px;
    box-shadow: 0 4px 0 rgba(0,0,0,0.1);
}
.hat::after { /* Hat Dome */
    content: '';
    position: absolute;
    bottom: 8px;
    left: 50%;
    transform: translateX(-50%);
    width: 30px;
    height: 20px;
    background: #fcd34d;
    border-radius: 20px 20px 0 0;
}

.face {
    position: absolute;
    top: 10px;
    left: 50%;
    transform: translateX(-50%);
    width: 28px;
    height: 20px;
    background: #ffedd5; /* Skin tone */
    border-radius: 8px;
}

.eyes {
    position: absolute;
    top: 8px;
    left: 6px;
    width: 4px;
    height: 4px;
    background: #1e293b;
    border-radius: 50%;
    box-shadow: 12px 0 0 #1e293b; /* Right eye */
}

.avatar-shadow {
  width: 30px;
  height: 10px;
  background: rgba(0,0,0,0.1); /* Softer shadow for light mode */
  filter: blur(4px);
  border-radius: 50%;
  margin-top: 15px;
  transform: scale(1);
  animation: shadowScale 2s ease-in-out infinite;
}

@keyframes floatAvatar {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-10px); }
}

@keyframes shadowScale {
  0%, 100% { transform: scale(1); opacity: 0.3; }
  50% { transform: scale(0.8); opacity: 0.1; }
}

@keyframes bounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-5px); }
}

/* 6. HUD */
.hud {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  padding: 40px;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  pointer-events: none;
}

.hud-btn {
  pointer-events: auto;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255,255,255,0.2);
  color: white;
  padding: 10px 20px;
  border-radius: 12px;
  font-weight: bold;
  cursor: pointer;
  transition: all 0.2s;
}

.hud-btn:hover {
  background: white;
  color: #0f172a;
}

.map-title {
  font-size: 32px;
  font-weight: 900;
  letter-spacing: -1px;
  background: linear-gradient(to right, #818cf8, #c084fc);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  text-shadow: 0 10px 30px rgba(192, 132, 252, 0.3);
}
</style>
      
