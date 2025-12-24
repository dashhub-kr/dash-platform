<template>
  <div ref="scene" class="matter-scene" />
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch } from "vue";
import Matter from "matter-js";

const props = defineProps({
  totalSolved: { type: Number, default: 0 },
});

const scene = ref(null);
let engine = null;
let render = null;
let runner = null;
let mouseConstraint = null;
let initialExistingCount = 0;
let dynamicAddedCount = 0;
let spawnSequence = 0;

const ACORN_META = {
  bronze: {
    texture: "/items/acorn-1.png",
    canvasWidth: 609,
    canvasHeight: 609,
    contentWidth: 600,
    contentHeight: 608,
    offsetX: 5,
    offsetY: 0,
    anchorX: 0.5,
    anchorY: 0.5016,
  },
  silver: {
    texture: "/items/acorn-2.png",
    canvasWidth: 786,
    canvasHeight: 786,
    contentWidth: 664,
    contentHeight: 768,
    offsetX: 101,
    offsetY: 10,
    anchorX: 0.4981,
    anchorY: 0.5006,
  },
  gold: {
    texture: "/items/acorn-3.png",
    canvasWidth: 1024,
    canvasHeight: 1024,
    contentWidth: 1014,
    contentHeight: 1023,
    offsetX: 0,
    offsetY: 0,
    anchorX: 0.4995,
    anchorY: 0.4917,
  },
  platinum: {
    texture: "/items/acorn-4.png",
    canvasWidth: 1069,
    canvasHeight: 1069,
    contentWidth: 1024,
    contentHeight: 1069,
    offsetX: 25,
    offsetY: 0,
    anchorX: 0.4935,
    anchorY: 0.4995,
  },
};

// 목표 높이
const TARGET_VISUAL_HEIGHT = 90;
// 물리 바디 크기 비율
const BODY_SQUARE_SCALE = 0.5;
// 스폰 간격
const SPAWN_GAP = TARGET_VISUAL_HEIGHT * 0.75;
// 디버그 드로잉 활성화 여부
const DEBUG_DRAW = true;

const Engine = Matter.Engine,
  Render = Matter.Render,
  World = Matter.World,
  Bodies = Matter.Bodies,
  Body = Matter.Body,
  Runner = Matter.Runner,
  Composite = Matter.Composite,
  Mouse = Matter.Mouse,
  MouseConstraintMod = Matter.MouseConstraint,
  Events = Matter.Events;

const buildSprite = (type, scale) => {
  const meta = ACORN_META[type] || ACORN_META.bronze;

  return {
    texture: meta.texture,
    xScale: scale,
    yScale: scale,
  };
};

const createBox = (x, y, type) => {
  let label;
  if (type === "platinum") label = "100";
  else if (type === "gold") label = "50";
  else if (type === "silver") label = "10";
  else label = "1";

  const meta = ACORN_META[type] || ACORN_META.bronze;
  const spriteScale = TARGET_VISUAL_HEIGHT / meta.contentHeight;
  const displayWidth = meta.contentWidth * spriteScale;
  const displayHeight = TARGET_VISUAL_HEIGHT;
  const bodySide = displayWidth * BODY_SQUARE_SCALE;
  const chamferSize = bodySide * 0.45;

  const body = Bodies.rectangle(x, y, bodySide, bodySide, {
    chamfer: { radius: chamferSize },
    angle: 0,
    render: {
      sprite: buildSprite(type, spriteScale),
    },
    restitution: 0.0,
    friction: 1.0,
    frictionStatic: 1.0,
    friction: 0.8,
    frictionAir: 0.02,
    label: label,
  });

  body.plugin = body.plugin || {};
  body.plugin.acornSpawnOrder = spawnSequence++;
  body.plugin.acornType = type;
  body.plugin.createdAt = Date.now();
  body.plugin.frozen = false;
  Body.setAngularVelocity(body, 0);

  return body;
};

// 정렬 기준 재설정
const reorderBodies = () => {
  if (!engine) return;
  const sorted = engine.world.bodies;
  sorted.sort((a, b) => {
    const orderA = a.plugin?.acornSpawnOrder ?? Number.MIN_SAFE_INTEGER;
    const orderB = b.plugin?.acornSpawnOrder ?? Number.MIN_SAFE_INTEGER;
    return orderB - orderA;
  });
  Composite.setModified(engine.world, true, true, false);
};

// 스폰 위치 결정
const centerX = () => window.innerWidth / 2;
const spawnXForType = (type) => {
  const vw = window.innerWidth;
  const spread = vw;
  let sum = 0;
  for (let i = 0; i < 6; i++) sum += Math.random();
  const rand = (sum - 3) / 6;
  return centerX() + rand * spread;
};

const addBoxes = (diff) => {
  if (!engine) return;
  const capacity = Math.max(0, 500 - initialExistingCount - dynamicAddedCount);
  const allowed = Math.min(diff, capacity);
  if (allowed <= 0) return;
  const newBodies = [];
  let remaining = allowed;
  let currentY = -SPAWN_GAP;

  const platinums = Math.floor(remaining / 100);
  remaining %= 100;
  for (let i = 0; i < platinums; i++) {
    currentY -= SPAWN_GAP;
    newBodies.push(createBox(spawnXForType("platinum"), currentY, "platinum"));
  }

  const golds = Math.floor(remaining / 50);
  remaining %= 50;
  for (let i = 0; i < golds; i++) {
    currentY -= SPAWN_GAP;
    newBodies.push(createBox(spawnXForType("gold"), currentY, "gold"));
  }

  const silvers = Math.floor(remaining / 10);
  remaining %= 10;
  for (let i = 0; i < silvers; i++) {
    currentY -= SPAWN_GAP;
    newBodies.push(createBox(spawnXForType("silver"), currentY, "silver"));
  }

  for (let i = 0; i < remaining; i++) {
    currentY -= SPAWN_GAP;
    newBodies.push(createBox(spawnXForType("bronze"), currentY, "bronze"));
  }

  if (newBodies.length > 0) {
    Composite.add(engine.world, newBodies);
    dynamicAddedCount += newBodies.length;
    reorderBodies();
  }
};

const initMatter = () => {
  if (!scene.value) return;
  engine = Engine.create();
  engine.positionIterations = 10;
  engine.velocityIterations = 8;
  engine.constraintIterations = 2;

  render = Render.create({
    element: scene.value,
    engine: engine,
    options: {
      width: window.innerWidth,
      height: window.innerHeight,
      wireframes: false,
      background: "transparent",
    },
  });

  // 중력 증가
  if (engine.world && engine.world.gravity) {
    engine.world.gravity.y = 1.4;
  }

  const wallOptions = { isStatic: true, render: { fillStyle: "transparent" } };
  const ground = Bodies.rectangle(
    window.innerWidth / 2,
    window.innerHeight + 30,
    window.innerWidth,
    60,
    wallOptions
  );
  const leftWall = Bodies.rectangle(
    -30,
    window.innerHeight / 2,
    60,
    window.innerHeight * 2,
    wallOptions
  );
  const rightWall = Bodies.rectangle(
    window.innerWidth + 30,
    window.innerHeight / 2,
    60,
    window.innerHeight * 2,
    wallOptions
  );

  Composite.add(engine.world, [ground, leftWall, rightWall]);

  const mouse = Mouse.create(render.canvas);
  mouseConstraint = MouseConstraintMod.create(engine, {
    mouse: mouse,
    constraint: { stiffness: 0.2, render: { visible: false } },
  });
  Composite.add(engine.world, mouseConstraint);
  render.mouse = mouse;

  runner = Runner.create();
  Runner.run(runner, engine);
  Render.run(render);

  // 충돌 처리
  Events.on(engine, "collisionStart", (event) => {
    try {
      for (const pair of event.pairs) {
        const a = pair.bodyA;
        const b = pair.bodyB;
        if (!a || !b) continue;
        if (a.isStatic || b.isStatic) continue;
        if (a.label === "Mouse Body" || b.label === "Mouse Body") continue;

        const normal = pair.collision && pair.collision.normal ? pair.collision.normal : { x: 0, y: -1 };

        // 충돌 시 약간의 선형 반발력 적용
        const relVel = (b.velocity.x - a.velocity.x) * normal.x + (b.velocity.y - a.velocity.y) * normal.y;
        const mag = Math.min(0.0025, Math.max(0.0005, Math.abs(relVel) * 0.0006 + 0.0005));
        const biasUp = 0.0002;
        const forceA = { x: -normal.x * mag, y: -normal.y * mag - biasUp };
        const forceB = { x: normal.x * mag, y: normal.y * mag - biasUp };
        Body.applyForce(a, a.position, forceA);
        Body.applyForce(b, b.position, forceB);

        // 접선 방향 상대 속도를 계산하여 작은 각 충격 유도
        const tangent = { x: -normal.y, y: normal.x };
        const relTang = (b.velocity.x - a.velocity.x) * tangent.x + (b.velocity.y - a.velocity.y) * tangent.y;

        // 등급별 민감도: 등급이 낮을수록 더 민감 (더 많은 회전)
        const sensitivityMap = {
          bronze: 1.0,
          silver: 0.85,
          gold: 0.7,
          platinum: 0.5,
        };

        const typeA = (a.plugin && a.plugin.acornType) || null;
        const typeB = (b.plugin && b.plugin.acornType) || null;
        const sensA = sensitivityMap[typeA] || 1.0;
        const sensB = sensitivityMap[typeB] || 1.0;

        // 접선 방향 상대 속도에서 각 델타 계산, 스케일 및 클램프 적용 (전체 민감도 감소)
        const baseFactor = 0.03; // 전체 민감도 감소를 위한 조정 배율 감소
        const maxAngular = 0.45; // 충돌당 최대 각 변화 클램프

        const deltaA = Math.max(-maxAngular, Math.min(maxAngular, -relTang * baseFactor * sensA));
        const deltaB = Math.max(-maxAngular, Math.min(maxAngular, relTang * baseFactor * sensB));

        // 각 속도를 직접 설정하여 적용
        Body.setAngularVelocity(a, (a.angularVelocity || 0) + deltaA);
        Body.setAngularVelocity(b, (b.angularVelocity || 0) + deltaB);
      }
    } catch (err) {
      // 충돌 처리 중 오류 무시
    }
  });

  // 10초 이상된 바디는 고정: 회전 및 미끄럼 없음
  Events.on(engine, "beforeUpdate", () => {
    try {
      const now = Date.now();
      const bodies = Composite.allBodies(engine.world);
      for (const body of bodies) {
        if (!body || body.isStatic) continue;
        const created = body.plugin && body.plugin.createdAt;
        const frozen = body.plugin && body.plugin.frozen;
        if (!created || frozen) continue;
        if (now - created >= 10000) {
          // 속도 0으로 설정하고 위치/회전 고정
          Body.setVelocity(body, { x: 0, y: 0 });
          Body.setAngularVelocity(body, 0);
          Body.setStatic(body, true);
          body.plugin.frozen = true;
        }
      }
    } catch (e) {
      // 무시
    }
  });

//   if (DEBUG_DRAW) {
//     Events.on(render, "afterRender", () => {
//       const ctx = render.context;
//       const bodies = Composite.allBodies(engine.world);
//       ctx.save();
//       ctx.lineWidth = 1;
//       bodies.forEach((body) => {
//         if (body.isStatic) return;
//         ctx.beginPath();
//         ctx.moveTo(body.vertices[0].x, body.vertices[0].y);
//         for (let i = 1; i < body.vertices.length; i++) {
//           ctx.lineTo(body.vertices[i].x, body.vertices[i].y);
//         }
//         ctx.closePath();
//         ctx.strokeStyle = "rgba(0, 200, 255, 0.8)";
//         ctx.stroke();
//
//         ctx.beginPath();
//         ctx.arc(body.position.x, body.position.y, 3, 0, Math.PI * 2);
//         ctx.fillStyle = "rgba(255, 80, 80, 0.9)";
//         ctx.fill();
//       });
//       ctx.restore();
//     });
//   }

  try {
    const allNow = Composite.allBodies(engine.world);
    initialExistingCount = allNow.filter((b) => !b.isStatic).length;
  } catch (e) {
    initialExistingCount = 0;
  }

  if (props.totalSolved > 0) addBoxes(props.totalSolved);
};

const handleResize = () => {
  if (render && render.canvas) {
    render.canvas.width = window.innerWidth;
    render.canvas.height = window.innerHeight;
  }
};

onMounted(() => {
  initMatter();
  window.addEventListener("resize", handleResize);
});

onUnmounted(() => {
  if (render) {
    Render.stop(render);
    Runner.stop(runner);
    if (render.canvas) render.canvas.remove();
  }
  window.removeEventListener("resize", handleResize);
});

watch(
  () => props.totalSolved,
  (newVal, oldVal) => {
    if (!engine) return;
    if (newVal > oldVal) addBoxes(newVal - oldVal);
    else if (newVal === 0) {
      const allBodies = Composite.allBodies(engine.world);
      const boxes = allBodies.filter((b) => !b.isStatic);
      Composite.remove(engine.world, boxes);
    }
  }
);

const addSpecificBoxes = (counts) => {
  if (!engine) return;
  const bodies = [];
  let bBronze = Number(counts.bronze) || 0;
  let bSilver = Number(counts.silver) || 0;
  let bGold = Number(counts.gold) || 0;
  let bPlat = Number(counts.platinum) || 0;

  const remainingCapacity = Math.max(
    0,
    300 - initialExistingCount - dynamicAddedCount
  );

  let toAllocate = remainingCapacity;
  const usePlat = Math.min(bPlat, toAllocate);
  toAllocate -= usePlat;
  bPlat = usePlat;
  const useGold = Math.min(bGold, toAllocate);
  toAllocate -= useGold;
  bGold = useGold;
  const useSilver = Math.min(bSilver, toAllocate);
  toAllocate -= useSilver;
  bSilver = useSilver;
  const useBronze = Math.min(bBronze, toAllocate);
  toAllocate -= useBronze;
  bBronze = useBronze;

  let currentY = -SPAWN_GAP;

  for (let i = 0; i < bPlat; i++) {
    currentY -= SPAWN_GAP;
    bodies.push(createBox(spawnXForType("platinum"), currentY, "platinum"));
  }
  for (let i = 0; i < bGold; i++) {
    currentY -= SPAWN_GAP;
    bodies.push(createBox(spawnXForType("gold"), currentY, "gold"));
  }
  for (let i = 0; i < bSilver; i++) {
    currentY -= SPAWN_GAP;
    bodies.push(createBox(spawnXForType("silver"), currentY, "silver"));
  }
  for (let i = 0; i < bBronze; i++) {
    currentY -= SPAWN_GAP;
    bodies.push(createBox(spawnXForType("bronze"), currentY, "bronze"));
  }

  if (bodies.length > 0) {
    Composite.add(engine.world, bodies);
    dynamicAddedCount += bodies.length;
    reorderBodies();
  }
};

defineExpose({
  spawn(count) {
    const n = Number(count) || 0;
    if (!engine) initMatter();
    if (n > 0) addBoxes(n);
  },
  spawnTiers(counts) {
    if (!engine) initMatter();
    addSpecificBoxes(counts || {});
  },
  clearSpawned() {
    if (!engine) return;
    const allBodies = Composite.allBodies(engine.world);
    const boxes = allBodies.filter((b) => !b.isStatic);
    Composite.remove(engine.world, boxes);

    dynamicAddedCount = 0;
  },
});
</script>

<style scoped>
.matter-scene {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 1;
  pointer-events: auto;
}
</style>
