<template>
  <div class="simcity-container font-[Pretendard]">
    <div ref="canvasContainer" class="canvas-container"></div>
    
    <!-- Header Overlay -->
    <div class="absolute top-6 left-6 z-10 animate-fade-in-down">
      <div class="bg-white/80 backdrop-blur-md border border-white/50 rounded-2xl p-4 shadow-lg shadow-indigo-500/10 flex items-center gap-4">
        <div class="w-12 h-12 rounded-xl bg-gradient-to-br from-indigo-400 to-cyan-400 flex items-center justify-center text-2xl shadow-md transform -rotate-6">
          🏙️
        </div>
        <div>
          <h1 class="text-xl font-bold text-slate-800 tracking-tight">스터디 심시티</h1>
          <div class="flex items-center gap-2 text-amber-600 font-bold bg-amber-50 px-2 py-0.5 rounded-lg text-sm mt-1">
            <span>🌰</span>
            <span>{{ acorns }} 도토리</span>
          </div>
        </div>
      </div>
    </div>

    <!-- Controls Overlay -->
    <div class="absolute bottom-10 left-1/2 transform -translate-x-1/2 z-10 animate-fade-in-up">
      <div class="bg-white/90 backdrop-blur-xl border border-white/50 rounded-3xl p-2 shadow-2xl shadow-indigo-500/20 flex items-center gap-2">
        <button 
          @click="setMode('build')" 
          :class="['flex items-center gap-2 px-6 py-3 rounded-2xl font-bold transition-all duration-300', mode === 'build' ? 'bg-indigo-600 text-white shadow-lg scale-105' : 'bg-transparent text-slate-500 hover:bg-slate-100']"
        >
          <span>🏗️</span>
          <span>건설 모드</span>
        </button>
        <div class="w-px h-8 bg-slate-200"></div>
        <button 
          @click="setMode('destroy')" 
          :class="['flex items-center gap-2 px-6 py-3 rounded-2xl font-bold transition-all duration-300', mode === 'destroy' ? 'bg-rose-500 text-white shadow-lg scale-105' : 'bg-transparent text-slate-500 hover:bg-slate-100']"
        >
          <span>💣</span>
          <span>철거 모드</span>
        </button>
      </div>
      
      <!-- Instructions / Toast -->
      <transition name="fade">
        <div v-if="message" class="absolute -top-16 left-1/2 transform -translate-x-1/2 bg-slate-800/90 text-white px-4 py-2 rounded-xl text-sm font-bold shadow-xl backdrop-blur-md flex items-center gap-2 whitespace-nowrap">
          <span>⚠️</span>
          {{ message }}
        </div>
        <div v-else class="absolute -top-12 left-1/2 transform -translate-x-1/2 text-slate-500 text-sm font-medium bg-white/50 px-3 py-1 rounded-full whitespace-nowrap backdrop-blur-sm">
           {{ mode === 'build' ? '빈 땅을 클릭하여 건물을 지으세요' : '건물을 클릭하여 철거하세요' }}
        </div>
      </transition>
    </div>

    <!-- Map Return Button -->
    <button 
      @click="$router.push('/map')"
      class="absolute top-6 right-6 z-10 bg-white/80 backdrop-blur-md p-3 rounded-full text-slate-500 hover:text-indigo-600 hover:bg-white border border-white/50 shadow-md transition-all hover:scale-110 active:scale-95"
      title="지도로 돌아가기"
    >
      <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><path d="m9 18 6-6-6-6"/></svg>
    </button>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue';
import { useRouter } from 'vue-router';
import * as THREE from 'three';
import { OrbitControls } from 'three/examples/jsm/controls/OrbitControls.js';

const router = useRouter();

// 상태 (State)
const acorns = ref(500); // 초기 대략적인 무작위 수량
const mode = ref('build'); // 'build' (건설) | 'destroy' (파괴)
const buildings = ref(new Map()); // 키: "x,z", 값: Mesh
const message = ref('');
const canvasContainer = ref(null);

// Three.js 변수들
let scene, camera, renderer, controls;
let raycaster, pointer;
let gridHelper, groundPlane;
let hoverMesh, cursorMesh;
let homeTexture, homeAspect;

// 상수
const GRID_SIZE = 20;
const GRID_DIVISIONS = 20;
const CELL_SIZE = GRID_SIZE / GRID_DIVISIONS;
const BUILD_COST = 50;
const DESTROY_REFUND = 25;

const setMode = (newMode) => {
  mode.value = newMode;
  message.value = '';
  updateHoverMeshColor();
};

const updateHoverMeshColor = () => {
    if (!hoverMesh) return;
    hoverMesh.material.color.setHex(mode.value === 'build' ? 0x4ade80 : 0xf87171);
    hoverMesh.material.opacity = 0.5;
}

const showMessage = (msg) => {
  message.value = msg;
  setTimeout(() => {
    message.value = '';
  }, 2000);
};

const initScene = () => {
  // 씬 (Scene) - Light & Sky Blue Theme
  scene = new THREE.Scene();
  scene.background = new THREE.Color(0xf0f9ff); // Very light sky blue
  scene.fog = new THREE.Fog(0xf0f9ff, 15, 45); // Soft fog

  // 카메라 (Camera)
  const aspect = window.innerWidth / window.innerHeight;
  const d = 15; // 뷰 크기
  camera = new THREE.OrthographicCamera(-d * aspect, d * aspect, d, -d, 1, 1000);
  
  // 아이소메트릭 위치
  camera.position.set(20, 20, 20);
  camera.lookAt(0, 0, 0);

  // 렌더러 (Renderer)
  renderer = new THREE.WebGLRenderer({ antialias: true, alpha: true });
  renderer.setSize(window.innerWidth, window.innerHeight);
  renderer.shadowMap.enabled = true;
  renderer.shadowMap.type = THREE.SoftShadowMap; // Softer shadows
  canvasContainer.value.appendChild(renderer.domElement);

  // 조명 (Lights)
  const ambientLight = new THREE.AmbientLight(0xffffff, 0.7);
  scene.add(ambientLight);

  const dirLight = new THREE.DirectionalLight(0xffffff, 0.6);
  dirLight.position.set(20, 30, 15);
  dirLight.castShadow = true;
  dirLight.shadow.mapSize.width = 1024;
  dirLight.shadow.mapSize.height = 1024;
  scene.add(dirLight);

  // 헬퍼 (Helper) - Softer grid
  gridHelper = new THREE.GridHelper(GRID_SIZE, GRID_DIVISIONS, 0xcbd5e1, 0xe2e8f0);
  scene.add(gridHelper);

  // 바닥 평면 (레이캐스팅용, 보이지 않음)
  const planeGeo = new THREE.PlaneGeometry(GRID_SIZE, GRID_SIZE);
  const planeMat = new THREE.MeshBasicMaterial({ visible: false });
  groundPlane = new THREE.Mesh(planeGeo, planeMat);
  groundPlane.rotation.x = -Math.PI / 2;
  scene.add(groundPlane);

  // 호버 메쉬 (유령 건물)
  const hoverGeo = new THREE.BoxGeometry(CELL_SIZE, 0.2, CELL_SIZE);
  const hoverMat = new THREE.MeshBasicMaterial({ color: 0x4ade80, transparent: true, opacity: 0.5 });
  hoverMesh = new THREE.Mesh(hoverGeo, hoverMat);
  scene.add(hoverMesh);

  // 커서 하이라이트 (그리드 바닥 선택)
  const cursorGeo = new THREE.PlaneGeometry(CELL_SIZE, CELL_SIZE);
  const cursorMat = new THREE.MeshBasicMaterial({ color: 0x6366f1, transparent: true, opacity: 0.2, side: THREE.DoubleSide }); // Indigo highlight
  cursorMesh = new THREE.Mesh(cursorGeo, cursorMat);
  cursorMesh.rotation.x = -Math.PI / 2;
  cursorMesh.visible = false;
  scene.add(cursorMesh);

  // 집 텍스처 로드
  const textureLoader = new THREE.TextureLoader();
  textureLoader.load('/models/home.png', (t) => {
      homeTexture = t;
      homeAspect = t.image.width / t.image.height;
  });

  // 도토리 나무 (중앙) - 2D 빌보드 스프라이트
  createTreeSprite();

  // 레이캐스터 (Raycaster)
  raycaster = new THREE.Raycaster();
  pointer = new THREE.Vector2();

  // 컨트롤 (Controls)
  controls = new OrbitControls(camera, renderer.domElement);
  controls.enableDamping = true;
  controls.dampingFactor = 0.05;
  controls.enableRotate = false; // 고정 아이소메트릭 뷰
  controls.enableZoom = true;
  controls.enablePan = true;
  controls.minZoom = 0.5;
  controls.maxZoom = 2;

  // 이벤트 리스너
  window.addEventListener('resize', onWindowResize);
  window.addEventListener('mousemove', onMouseMove);
  window.addEventListener('mousedown', onMouseDown);
};

const createTreeSprite = () => {
    const textureLoader = new THREE.TextureLoader();
    textureLoader.load('/models/tree.png', (texture) => {
        // 종횡비 계산
        const imageAspect = texture.image.width / texture.image.height;
        const height = 5;
        const width = height * imageAspect;
        
        const geometry = new THREE.PlaneGeometry(width, height);
        const material = new THREE.MeshBasicMaterial({ 
            map: texture, 
            transparent: true, 
            side: THREE.DoubleSide,
            alphaTest: 0.5, // clear cut
            fog: false // 안개에도 불구하고 나무를 선명하게 유지
        });
        const mesh = new THREE.Mesh(geometry, material);
        
        // 중앙 위치 - 바닥 위에 배치 (y = height/2) + 약간의 오프셋 (0.1)
        mesh.position.set(0, height/2 + 0.1, 0); 
        
        // 카메라를 균일하게 바라보도록 설정
        mesh.quaternion.copy(camera.quaternion);
        
        scene.add(mesh);
        markTreeProtected();
    });
};

const markTreeProtected = () => {
  // GRID_SIZE 20의 중앙 인덱스는 9와 10
  // 예: 0..9 (10칸), 10..19 (10칸)
  // 중앙 2x2 영역 보호
  buildings.value.set("9,9", "tree");
  buildings.value.set("9,10", "tree");
  buildings.value.set("10,9", "tree");
  buildings.value.set("10,10", "tree");
};


const createBuilding = (x, z) => {
  if (!homeTexture) return; // 텍스처 로드 대기

  const height = 1.5; // 크기 축소 (기존 3)
  const width = height * homeAspect;
  
  const geo = new THREE.PlaneGeometry(width, height);
  const mat = new THREE.MeshBasicMaterial({ 
    map: homeTexture,
    transparent: true,
    side: THREE.DoubleSide,
    alphaTest: 0.5,
    fog: false
  });
  
  const mesh = new THREE.Mesh(geo, mat);
  
  // 피벗 조정? 평면 중심은 0,0.
  // 바닥이 0에 오도록 설정.
  // x, z는 그리드 중심.
  mesh.position.set(
      x * CELL_SIZE + CELL_SIZE/2 - GRID_SIZE/2, 
      height/2, 
      z * CELL_SIZE + CELL_SIZE/2 - GRID_SIZE/2
  );
  
  mesh.quaternion.copy(camera.quaternion); // 균일한 빌보드 회전
  mesh.receiveShadow = false; 
  mesh.castShadow = false;

  // 애니메이션 (스케일로 팝업)
  mesh.scale.set(0.1, 0.1, 0.1);
  const targetScale = 1;
  const grow = () => {
      if(mesh.scale.x < targetScale) {
          mesh.scale.x += 0.1;
          mesh.scale.y += 0.1;
          mesh.scale.z += 0.1;
          requestAnimationFrame(grow);
      } else {
          mesh.scale.set(1,1,1);
      }
  };
  grow();

  scene.add(mesh);
  return mesh;
};

const getGridPos = (intersect) => {
    const point = intersect.point;
    // 그리드 좌표로 매핑
    // 그리드는 -GRID_SIZE/2 에서 GRID_SIZE/2 까지
    // 0 에서 GRID_DIVISIONS 까지 필요
    
    // 모서리 기준 로컬 포인트
    const lx = point.x + GRID_SIZE / 2;
    const lz = point.z + GRID_SIZE / 2;

    const gx = Math.floor(lx / CELL_SIZE);
    const gz = Math.floor(lz / CELL_SIZE);

    return { gx, gz };
};

const onMouseMove = (event) => {
  pointer.x = (event.clientX / window.innerWidth) * 2 - 1;
  pointer.y = -(event.clientY / window.innerHeight) * 2 + 1;

  raycaster.setFromCamera(pointer, camera);
  const intersects = raycaster.intersectObject(groundPlane);

  if (intersects.length > 0) {
    const { gx, gz } = getGridPos(intersects[0]);
    if (gx >= 0 && gx < GRID_DIVISIONS && gz >= 0 && gz < GRID_DIVISIONS) {
        hoverMesh.visible = true;
        cursorMesh.visible = true;
        
        const tx = gx * CELL_SIZE + CELL_SIZE/2 - GRID_SIZE/2;
        const tz = gz * CELL_SIZE + CELL_SIZE/2 - GRID_SIZE/2;

        // 그리드에 스냅
        hoverMesh.position.set(tx, 0.1, tz);
        cursorMesh.position.set(tx, 0.05, tz);

    } else {
        hoverMesh.visible = false;
        cursorMesh.visible = false;
    }
  } else {
      hoverMesh.visible = false;
      cursorMesh.visible = false;
  }
};

  const onMouseDown = () => {
  if (!hoverMesh.visible) return;

  // 호버 메쉬 위치에서 그리드 좌표 재계산 (더 쉽고 동기화됨)
  // 위치 설정의 역산:
  // x = gx * CELL_SIZE + CELL_SIZE/2 - GRID_SIZE/2
  // gx = (x + GRID_SIZE/2 - CELL_SIZE/2) / CELL_SIZE
  const gx = Math.round((hoverMesh.position.x + GRID_SIZE/2 - CELL_SIZE/2) / CELL_SIZE);
  const gz = Math.round((hoverMesh.position.z + GRID_SIZE/2 - CELL_SIZE/2) / CELL_SIZE);
  
  const key = `${gx},${gz}`;

  if (buildings.value.has(key) && buildings.value.get(key) === "tree") {
      showMessage("신성한 도토리 나무를 건드릴 수 없습니다!");
      return;
  }

  if (mode.value === 'build') {
      if (buildings.value.has(key)) {
          showMessage("이미 건물이 있습니다!");
          return;
      }
      if (acorns.value < BUILD_COST) {
          showMessage("도토리가 부족합니다!");
          return;
      }
      
      const mesh = createBuilding(gx, gz);
      buildings.value.set(key, mesh);
      acorns.value -= BUILD_COST;

  } else if (mode.value === 'destroy') {
      if (!buildings.value.has(key)) {
          return;
      }
      
      const obj = buildings.value.get(key);
      
      // "tree"와 일치하는지 안전 검사 (위에서 잡히겠지만 좋은 습관)
      if (obj === "tree") {
          showMessage("신성한 도토리 나무를 건드릴 수 없습니다!");
          return;
      }

      // 메쉬임
      scene.remove(obj);
      if(obj.geometry) obj.geometry.dispose();
      if(obj.material) obj.material.dispose();
      buildings.value.delete(key);
      acorns.value += DESTROY_REFUND;
  }
};

const animate = () => {
    requestAnimationFrame(animate);
    
    // 카메라나 나무의 간단한 회전? 일단 정적으로 두거나 나뭇잎을 천천히 회전시킬까?
    // scene.rotation.y += 0.001; // 전체 세상을 회전시킴, 어지러움.
    
    controls.update();
    renderer.render(scene, camera);
};

const onWindowResize = () => {
  const aspect = window.innerWidth / window.innerHeight;
  const d = 15;
  
  camera.left = -d * aspect;
  camera.right = d * aspect;
  camera.top = d;
  camera.bottom = -d;
  
  camera.updateProjectionMatrix();
  renderer.setSize(window.innerWidth, window.innerHeight);
};

onMounted(() => {
  initScene();
  animate();
});

onBeforeUnmount(() => {
  window.removeEventListener('resize', onWindowResize);
  window.removeEventListener('mousemove', onMouseMove);
  window.removeEventListener('mousedown', onMouseDown);
  // Three.js 정리
  renderer.dispose();
});

</script>

<style scoped>
@import url('https://cdn.jsdelivr.net/gh/orioncactus/pretendard/dist/web/static/pretendard.css');

.simcity-container {
  width: 100vw;
  height: 100vh;
  position: relative;
  overflow: hidden;
  background-color: #f0f9ff; /* Fallback */
}

.canvas-container {
  width: 100%;
  height: 100%;
}

.animate-fade-in-down {
  animation: fade-in-down 0.6s cubic-bezier(0.16, 1, 0.3, 1) forwards;
  opacity: 0;
  transform: translateY(-20px);
}

.animate-fade-in-up {
  animation: fade-in-up 0.6s cubic-bezier(0.16, 1, 0.3, 1) forwards;
  opacity: 0;
  transform: translate(-50%, 20px);
}

@keyframes fade-in-down {
  to { opacity: 1; transform: translateY(0); }
}

@keyframes fade-in-up {
  to { opacity: 1; transform: translate(-50%, 0); }
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease, transform 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
  transform: translate(-50%, 10px);
}
</style>

