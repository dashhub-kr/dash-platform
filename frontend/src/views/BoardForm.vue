<template>
  <div class="min-h-screen bg-slate-950 text-slate-100 selection:bg-indigo-500/30">

    <main class="container mx-auto px-6 py-10 max-w-4xl">
      <div class="mb-8 animate-fade-in-up">
        <button 
          @click="$router.back()" 
          class="flex items-center gap-2 text-slate-400 hover:text-white mb-4 transition-colors"
        >
          <ArrowLeft :size="20" />
          돌아가기
        </button>
        <h1 class="text-3xl font-bold">{{ isEdit ? '글 수정하기' : '새 글 작성하기' }}</h1>
      </div>

      <div class="bg-slate-900/50 border border-white/10 rounded-2xl p-8 animate-fade-in-up delay-100">
        <form @submit.prevent="handleSubmit" class="space-y-6">
          <!-- Title -->
          <div class="space-y-2">
            <label for="title" class="text-sm font-medium text-slate-300">제목</label>
            <input
              id="title"
              v-model="form.title"
              type="text"
              required
              placeholder="제목을 입력하세요"
              class="w-full bg-slate-800/50 border border-white/10 rounded-lg px-4 py-3 text-white placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-indigo-500/50 focus:border-transparent transition-all"
            />
          </div>

          <!-- Content -->
          <div class="space-y-2">
            <label for="content" class="text-sm font-medium text-slate-300">내용</label>
            <textarea
              id="content"
              v-model="form.content"
              required
              rows="12"
              placeholder="내용을 입력하세요"
              class="w-full bg-slate-800/50 border border-white/10 rounded-lg px-4 py-3 text-white placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-indigo-500/50 focus:border-transparent transition-all resize-none"
            ></textarea>
          </div>

          <!-- Actions -->
          <div class="flex justify-end gap-4 pt-4 border-t border-white/5">
            <button
              type="button"
              @click="$router.back()"
              class="px-6 py-3 rounded-lg text-slate-300 hover:bg-slate-800 transition-colors font-medium"
            >
              취소
            </button>
            <button
              type="submit"
              :disabled="submitting"
              class="px-6 py-3 bg-indigo-600 hover:bg-indigo-500 text-white rounded-lg font-bold shadow-lg shadow-indigo-500/20 transition-all hover:scale-105 disabled:opacity-50 disabled:cursor-not-allowed flex items-center gap-2"
            >
              <div v-if="submitting" class="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin"></div>
              {{ isEdit ? '수정 완료' : '작성 완료' }}
            </button>
          </div>
        </form>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { ArrowLeft } from 'lucide-vue-next';
import { boardApi } from '../api/board';
import { useAuth } from '../composables/useAuth'; // Ensure imported

const router = useRouter();
const route = useRoute();
const { user } = useAuth(); // Add this

const isEdit = computed(() => !!route.params.id);
const submitting = ref(false);
const form = ref({
    title: '',
    content: ''
});

onMounted(async () => {
    if (isEdit.value) {
        try {
            const res = await boardApi.findById(route.params.id);
            if (res.data) {
                form.value = {
                    title: res.data.title,
                    content: res.data.content
                };
            }
        } catch (e) {
            console.error("Failed to load post", e);
            alert("게시글을 불러오는 데 실패했습니다.");
            router.push('/boards');
        }
    }
});


const handleSubmit = async () => {
    submitting.value = true;
    try {
        if (isEdit.value) {
            await boardApi.update(route.params.id, form.value);
            alert("게시글이 수정되었습니다.");
        } else {
            // Include userId for creation
            await boardApi.create({
                ...form.value,
                userId: user.value?.id
            });
            alert("게시글이 등록되었습니다.");
        }
        router.push('/boards');
    } catch (e) {
        console.error("Failed to submit", e);
        alert("처리에 실패했습니다.");
    } finally {
        submitting.value = false;
    }
};
</script>

<style scoped>
.animate-fade-in-up {
  animation: fade-in-up 0.8s cubic-bezier(0.16, 1, 0.3, 1) forwards;
  opacity: 0;
  transform: translateY(20px);
}
.delay-100 { animation-delay: 0.1s; }
@keyframes fade-in-up {
  to { opacity: 1; transform: translateY(0); }
}
</style>
