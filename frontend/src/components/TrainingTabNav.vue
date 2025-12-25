<template>
  <div class="flex justify-center mb-8">
    <div class="flex gap-1 bg-white/50 backdrop-blur-sm p-1.5 rounded-3xl border border-white/60 shadow-sm">
      <router-link 
        to="/training/roadmap"
        class="px-6 py-2.5 rounded-2xl text-sm font-bold transition-all duration-200"
        :class="isRoadmapActive ? 'bg-white text-brand-600 shadow-sm ring-1 ring-black/5' : 'text-slate-500 hover:text-slate-700'"
      >
        로드맵
      </router-link>
      <router-link 
        to="/training/skilltree"
        class="px-6 py-2.5 rounded-2xl text-sm font-bold transition-all duration-200"
        :class="isSkillTreeActive ? 'bg-white text-brand-600 shadow-sm ring-1 ring-black/5' : 'text-slate-500 hover:text-slate-700'"
      >
        스킬 트리
      </router-link>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue';
import { useRoute } from 'vue-router';

const route = useRoute();

const isRoadmapActive = computed(() => 
  route.path === '/training/roadmap' || route.path === '/training'
);

const isSkillTreeActive = computed(() => 
  route.path === '/training/skilltree'
);
</script>
