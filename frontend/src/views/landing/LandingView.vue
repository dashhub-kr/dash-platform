<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { ArrowUp, ArrowDown } from 'lucide-vue-next' // 아이콘 변경됨
import { useAuth } from '@/composables/useAuth'
import { useRouter } from 'vue-router'
import { useMouseInElement, useDebounceFn } from '@vueuse/core'

// 모든 슬라이드 가져오기
import Slide1 from '@/components/landing/Slide1.vue'
import Slide2 from '@/components/landing/Slide2.vue'
import Slide3 from '@/components/landing/Slide3.vue'
import Slide4 from '@/components/landing/Slide4.vue'
import Slide5 from '@/components/landing/Slide5.vue'

// 랜딩 스타일 가져오기
import '@/assets/styles/landing/index.css'

const { user, authChecked } = useAuth()
const router = useRouter()

// 로그인 상태라면 대시보드로 리다이렉트
watch(authChecked, (checked) => {
  if (checked && user.value) {
    router.replace('/dashboard')
  }
}, { immediate: true })

const slides = [
  { component: Slide1, title: 'Intro' },
  { component: Slide2, title: 'Features' },
  { component: Slide3, title: 'Details' },
  { component: Slide4, title: 'Analysis' },
  { component: Slide5, title: 'Join' },
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

// 빠른 스킵 방지를 위한 스크롤 이벤트 디바운스
const handleWheel = useDebounceFn((e: WheelEvent) => {
  if (e.deltaY > 0) {
    nextSlide()
  } else if (e.deltaY < 0) {
    prevSlide()
  }
}, 100) // 100ms 디바운스

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
    <!-- 슬라이드 -->
    <div class="relative w-full h-full">
      <Transition :name="`slide-${direction}`">
        <component
          :is="slides[currentSlide].component"
          :key="currentSlide"
          class="absolute inset-0 w-full h-full"
          @next="nextSlide"
        />
      </Transition>
    </div>

    <!-- 네비게이션 컨트롤 (수직) -->
    <div class="absolute right-8 top-1/2 -translate-y-1/2 z-50 flex flex-col items-center gap-6">
      <button 
        @click="prevSlide"
        class="p-3 bg-white/80 backdrop-blur rounded-full shadow-sm hover:bg-white transition-all disabled:opacity-30 disabled:cursor-not-allowed border border-slate-200"
        :disabled="currentSlide === 0"
      >
        <ArrowUp class="w-6 h-6 text-slate-600" />
      </button>

      <!-- 인디 스타일 진행 표시기 (수직) -->
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
/* 슬라이드 전환 효과 */
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
