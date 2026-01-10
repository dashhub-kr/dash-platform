<template>
  <div class="bg-white rounded-2xl p-4 border border-slate-200 shadow-sm flex items-center justify-around h-[72px] box-border">
    <template v-for="(item, index) in items" :key="index">
      <div 
        class="flex items-center gap-2 group cursor-pointer transition-transform active:scale-95 select-none"
        :title="item.tooltip"
        @click="item.onClick && item.onClick()"
      >
        <!-- Icon Component -->
        <component 
            v-if="item.icon" 
            :is="item.icon" 
            class="w-8 h-8 transition-colors"
            :class="item.iconClass || 'text-slate-700'"
            :stroke-width="2.5" 
            :fill="item.fill || 'none'"
        />
        
        <!-- Image URL -->
        <img 
            v-else-if="item.image" 
            :src="item.image" 
            class="w-8 h-8 object-contain transition-transform group-hover:scale-110" 
            alt="icon"
        />

        <!-- Value -->
        <span 
            class="font-black text-slate-700 whitespace-nowrap"
            :class="item.textClass || 'text-xl'"
        >
            {{ item.value }}
        </span>
      </div>

      <!-- Divider -->
      <div v-if="index < items.length - 1" class="w-px h-8 bg-slate-100 shrink-0 mx-2"></div>
    </template>
  </div>
</template>

<script setup>
defineProps({
  items: {
    type: Array,
    required: true,
    // Expected Item Structure:
    // { 
    //   icon: Component, 
    //   image: String, 
    //   value: String|Number, 
    //   tooltip: String, 
    //   iconClass: String, 
    //   textClass: String,
    //   fill: String,
    //   onClick: Function
    // }
  }
});
</script>
