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

const Engine = Matter.Engine,
  Render = Matter.Render,
  World = Matter.World,
  Bodies = Matter.Bodies,
  Runner = Matter.Runner,
  Composite = Matter.Composite,
  Mouse = Matter.Mouse,
  MouseConstraintMod = Matter.MouseConstraint,
  Events = Matter.Events;

const createBox = (x, y, type) => {
  let size, color, label;
  if (type === "platinum") {
    size = 60;
    color = "#27e2a4";
    label = "100";
  } else if (type === "gold") {
    size = 45;
    color = "#ec9a00";
    label = "50";
  } else if (type === "silver") {
    size = 35;
    color = "#435f7a";
    label = "10";
  } else {
    size = 25;
    color = "#ad5600";
    label = "1";
  }

  return Bodies.rectangle(x, y, size, size, {
    render: { fillStyle: color, strokeStyle: "#ffffff", lineWidth: 2 },
    restitution: 0.6,
    friction: 0.5,
    label: label,
  });
};

const centerX = () => window.innerWidth / 2;
const spawnXForType = (type) => {
  const vw = window.innerWidth;
  const base = Math.min(700, Math.max(200, Math.floor(vw * 0.35)));
  let spread;
  if (type === "platinum") spread = base * 1.1;
  else if (type === "gold") spread = base * 0.9;
  else if (type === "silver") spread = base * 0.6;
  else spread = base * 0.4;
  return centerX() + (Math.random() - 0.5) * spread;
};

const addBoxes = (diff) => {
  if (!engine) return;
  const capacity = Math.max(0, 300 - initialExistingCount - dynamicAddedCount);
  const allowed = Math.min(diff, capacity);
  if (allowed <= 0) return;
  const newBodies = [];
  let remaining = allowed;

  const platinums = Math.floor(remaining / 100);
  remaining %= 100;
  for (let i = 0; i < platinums; i++)
    newBodies.push(
      createBox(
        spawnXForType("platinum"),
        -Math.random() * 500 - 100,
        "platinum"
      )
    );

  const golds = Math.floor(remaining / 50);
  remaining %= 50;
  for (let i = 0; i < golds; i++)
    newBodies.push(
      createBox(spawnXForType("gold"), -Math.random() * 400 - 50, "gold")
    );

  const silvers = Math.floor(remaining / 10);
  remaining %= 10;
  for (let i = 0; i < silvers; i++)
    newBodies.push(
      createBox(spawnXForType("silver"), -Math.random() * 300 - 50, "silver")
    );

  for (let i = 0; i < remaining; i++)
    newBodies.push(
      createBox(spawnXForType("bronze"), -Math.random() * 200 - 50, "bronze")
    );

  if (newBodies.length > 0) {
    Composite.add(engine.world, newBodies);
    dynamicAddedCount += newBodies.length;
  }
};

const initMatter = () => {
  if (!scene.value) return;
  engine = Engine.create();

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

  for (let i = 0; i < bPlat; i++)
    bodies.push(
      createBox(
        spawnXForType("platinum"),
        -Math.random() * 500 - 100,
        "platinum"
      )
    );
  for (let i = 0; i < bGold; i++)
    bodies.push(
      createBox(spawnXForType("gold"), -Math.random() * 400 - 50, "gold")
    );
  for (let i = 0; i < bSilver; i++)
    bodies.push(
      createBox(spawnXForType("silver"), -Math.random() * 300 - 50, "silver")
    );
  for (let i = 0; i < bBronze; i++)
    bodies.push(
      createBox(spawnXForType("bronze"), -Math.random() * 200 - 50, "bronze")
    );

  if (bodies.length > 0) {
    Composite.add(engine.world, bodies);

    dynamicAddedCount += bodies.length;
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
