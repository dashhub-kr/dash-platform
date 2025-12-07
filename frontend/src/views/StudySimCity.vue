<template>
  <div class="simcity-container">
    <div ref="canvasContainer" class="canvas-container"></div>
    
    <div class="ui-overlay">
      <div class="header">
        <h1>스터디 심시티</h1>
        <div class="acorn-display">
          <span class="icon">🌰</span>
          <span class="count">{{ acorns }} 도토리</span>
        </div>
      </div>
      
      <div class="controls">
        <button 
          @click="setMode('build')" 
          :class="{ active: mode === 'build' }"
          class="control-btn build-btn"
        >
          건설
        </button>
        <button 
          @click="setMode('destroy')" 
          :class="{ active: mode === 'destroy' }"
          class="control-btn destroy-btn"
        >
          철거
        </button>
      </div>

      <div class="instructions">
        <p>{{ mode === 'build' ? '빈 칸을 눌러 건설하세요.' : '건물을 눌러 철거하세요.' }}</p>
        <p class="warning" v-if="message">{{ message }}</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue';
import * as THREE from 'three';
import { OrbitControls } from 'three/examples/jsm/controls/OrbitControls.js';

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
  // 씬 (Scene)
  scene = new THREE.Scene();
  scene.background = new THREE.Color(0xe0f2fe); // 하늘색
  scene = new THREE.Scene();
  scene.background = new THREE.Color(0xe0f2fe); // 하늘색
  scene.fog = new THREE.Fog(0xe0f2fe, 10, 50); // 깊이감을 위해 안개 복원

  // 카메라 (Camera)
  // 카메라 - 아이소메트릭 뷰를 위한 직교 투영
  const aspect = window.innerWidth / window.innerHeight;
  const d = 15; // 뷰 크기
  camera = new THREE.OrthographicCamera(-d * aspect, d * aspect, d, -d, 1, 1000);
  
  // 아이소메트릭 위치
  camera.position.set(20, 20, 20);
  camera.lookAt(0, 0, 0);

  // 렌더러 (Renderer)
  renderer = new THREE.WebGLRenderer({ antialias: true });
  renderer.setSize(window.innerWidth, window.innerHeight);
  renderer.shadowMap.enabled = true;
  canvasContainer.value.appendChild(renderer.domElement);

  // 조명 (Lights)
  const ambientLight = new THREE.AmbientLight(0xffffff, 0.6);
  scene.add(ambientLight);

  const dirLight = new THREE.DirectionalLight(0xffffff, 0.8);
  dirLight.position.set(10, 20, 10);
  dirLight.castShadow = true;
  scene.add(dirLight);

  // 헬퍼 (Helper)
  gridHelper = new THREE.GridHelper(GRID_SIZE, GRID_DIVISIONS, 0x888888, 0xcccccc);
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
  const cursorMat = new THREE.MeshBasicMaterial({ color: 0xffff00, transparent: true, opacity: 0.3, side: THREE.DoubleSide });
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
  controls.enableRotate = false; // 고정 아이소메트릭 뷰를 위해 회전 잠금
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
    fog: false // 선택사항: 집도 선명하게 유지할까? 선명하게 유지하자.
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
  mesh.receiveShadow = false; // 이 스타일에서는 평면이 그림자를 예쁘게 드리우지 않음
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
.simcity-container {
  width: 100vw;
  height: 100vh;
  position: relative;
  overflow: hidden;
}

.canvas-container {
  width: 100%;
  height: 100%;
}

.ui-overlay {
  position: absolute;
  top: 15px;
  left: 15px;
  background: rgba(255, 255, 255, 0.95);
  padding: 12px;
  border-radius: 12px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.05);
  width: 220px;
  font-family: 'Inter', sans-serif;
  backdrop-filter: blur(2px);
}

.header {
  margin-bottom: 12px;
  border-bottom: 1px solid #f3f4f6;
  padding-bottom: 8px;
}

h1 {
  font-size: 1rem;
  font-weight: 700;
  color: #374151;
  margin: 0;
  letter-spacing: -0.01em;
}

.acorn-display {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: 4px;
  font-size: 0.9rem;
  color: #8B4513;
  font-weight: 600;
}

.controls {
  display: flex;
  gap: 8px;
  margin-bottom: 10px;
}

.control-btn {
  flex: 1;
  padding: 6px 4px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 500;
  font-size: 0.75rem;
  transition: all 0.2s;
}

.build-btn {
  background: #d1fae5;
  color: #065f46;
}
.build-btn.active {
  background: #10b981;
  color: white;
  box-shadow: 0 0 0 2px #065f46;
}

.destroy-btn {
  background: #fee2e2;
  color: #991b1b;
}
.destroy-btn.active {
  background: #ef4444;
  color: white;
  box-shadow: 0 0 0 2px #991b1b;
}

.instructions {
  font-size: 0.9rem;
  color: #6b7280;
}

.warning {
  color: #dc2626;
  font-weight: 600;
  margin-top: 5px;
  animation: shake 0.5s;
}

@keyframes shake {
  0% { transform: translateX(0); }
  25% { transform: translateX(-5px); }
  50% { transform: translateX(5px); }
  75% { transform: translateX(-5px); }
  100% { transform: translateX(0); }
}
</style>
