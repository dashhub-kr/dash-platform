<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { ArrowUp, ArrowDown } from 'lucide-vue-next' // Changed icons
import { useAuth } from '../composables/useAuth'
import { useRouter } from 'vue-router'
import { useMouseInElement, useDebounceFn } from '@vueuse/core'

// Import all slides
import Slide1 from '../components/landing/Slide1.vue'
import Slide2 from '../components/landing/Slide2.vue'
import Slide3 from '../components/landing/Slide3.vue'
import Slide4 from '../components/landing/Slide4.vue'
import Slide5 from '../components/landing/Slide5.vue'

// Import landing styles
import '../assets/styles/landing/index.css'

const { user, authChecked } = useAuth()
const router = useRouter()

// Redirect to dashboard if logged in
watch(authChecked, (checked) => {
  if (checked && user.value) {
    router.replace('/dashboard')
  }
}, { immediate: true })

const slides = [
  { component: Slide1, title: 'Intro' },
  { component: Slide2, title: 'Features' },
  { component: Slide3, title: 'Details' },
  { component: Slide4, title: 'Benefits' },
  { component: Slide5, title: 'Outro' },
]

const currentSlide = ref(0)
const direction = ref('next')

const nextSlide = () => {
  if (currentSlide.value < slides.length - 1) {
    direction.value = 'next'
    currentSlide.value++
  }
}

const prevSlide = () => {
  if (currentSlide.value > 0) {
    direction.value = 'prev'
    currentSlide.value--
  }
}

// Debounce scroll events to prevent rapid skipping
const handleWheel = useDebounceFn((e: WheelEvent) => {
  if (e.deltaY > 0) {
    nextSlide()
  } else if (e.deltaY < 0) {
    prevSlide()
  }
}, 100) // 100ms debounce

const handleKeydown = (e: KeyboardEvent) => {
  if (e.key === 'ArrowDown' || e.key === ' ' || e.key === 'PageDown') {
    nextSlide()
  } else if (e.key === 'ArrowUp' || e.key === 'PageUp') {
    prevSlide()
  }
}

onMounted(() => {
  window.addEventListener('keydown', handleKeydown)
  window.addEventListener('wheel', handleWheel, { passive: false })
})

onUnmounted(() => {
  window.removeEventListener('keydown', handleKeydown)
  window.removeEventListener('wheel', handleWheel)
})
</script>

<template>
  <div class="fixed inset-0 overflow-hidden bg-slate-50 font-[Pretendard]">
    <!-- Slides -->
    <div class="relative w-full h-full">
      <Transition :name="`slide-${direction}`">
        <component
          :is="slides[currentSlide].component"
          :key="currentSlide"
          class="absolute inset-0 w-full h-full"
        />
      </Transition>
    </div>

    <!-- Navigation Controls (Vertical) -->
    <div class="absolute right-8 top-1/2 -translate-y-1/2 z-50 flex flex-col items-center gap-6">
      <button 
        @click="prevSlide"
        class="p-3 bg-white/80 backdrop-blur rounded-full shadow-sm hover:bg-white transition-all disabled:opacity-30 disabled:cursor-not-allowed border border-slate-200"
        :disabled="currentSlide === 0"
      >
        <ArrowUp class="w-6 h-6 text-slate-600" />
      </button>

      <!-- Indie-style Progress Indicators (Vertical) -->
      <div class="flex flex-col gap-2">
        <button
          v-for="(_, index) in slides"
          :key="index"
          @click="() => {
            direction = index > currentSlide ? 'next' : 'prev'
            currentSlide = index
          }"
          class="w-2.5 h-2.5 rounded-full transition-all duration-300"
          :class="[
            currentSlide === index 
              ? 'bg-slate-800 h-8' 
              : 'bg-slate-300 hover:bg-slate-400'
          ]"
        />
      </div>

      <button 
        @click="nextSlide"
        class="p-3 bg-white/80 backdrop-blur rounded-full shadow-sm hover:bg-white transition-all disabled:opacity-30 disabled:cursor-not-allowed border border-slate-200"
        :disabled="currentSlide === slides.length - 1"
      >
        <ArrowDown class="w-6 h-6 text-slate-600" />
      </button>
    </div>



  </div>
</template>

<style>
/* Slide Transitions */
.slide-next-enter-active,
.slide-next-leave-active,
.slide-prev-enter-active,
.slide-prev-leave-active {
  transition: all 0.5s cubic-bezier(0.25, 1, 0.5, 1);
  position: absolute;
  width: 100%;
  height: 100%;
}

.slide-next-enter-from {
  transform: translateY(100%);
  opacity: 0;
}
.slide-next-leave-to {
  transform: translateY(-100%);
  opacity: 0;
}

.slide-prev-enter-from {
  transform: translateY(-100%);
  opacity: 0;
}
.slide-prev-leave-to {
  transform: translateY(100%);
  opacity: 0;
}
</style>
