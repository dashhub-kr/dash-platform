<template>
  <div class="min-h-screen bg-slate-50 text-slate-800 selection:bg-indigo-500/30 font-[Pretendard]">
    <!-- Background Decor -->
    <div class="fixed inset-0 z-0 pointer-events-none overflow-hidden">
        <div class="absolute top-[-10%] right-[-5%] w-[40vw] h-[40vw] bg-indigo-200/40 rounded-full blur-[100px] mix-blend-multiply opacity-70 animate-blob"></div>
        <div class="absolute bottom-[-10%] left-[-5%] w-[40vw] h-[40vw] bg-blue-200/40 rounded-full blur-[100px] mix-blend-multiply opacity-70 animate-blob animation-delay-2000"></div>
    </div>

    <main class="relative z-10 container mx-auto px-6 py-12 max-w-2xl">
      <div class="mb-10 text-center animate-fade-in-up">
        <h1 class="text-4xl font-extrabold mb-3 text-slate-900 tracking-tight">마이페이지</h1>
        <p class="text-lg text-slate-500 font-medium">내 정보를 확인하고 수정할 수 있습니다.</p>
      </div>

      <div class="bg-white/70 backdrop-blur-xl border border-white/40 rounded-3xl p-8 shadow-xl shadow-indigo-100/50 animate-fade-in-up delay-100">
        <div class="flex flex-col sm:flex-row items-center gap-8 mb-10 pb-10 border-b border-slate-100">
          <div class="relative group">
            <div class="w-24 h-24 rounded-full bg-gradient-to-br from-indigo-500 to-purple-600 flex items-center justify-center text-4xl font-bold text-white shadow-lg group-hover:scale-105 transition-transform duration-300">
              {{ userInitial }}
            </div>
            <div class="absolute -bottom-2 -right-2 w-8 h-8 bg-white rounded-full flex items-center justify-center shadow-md border border-slate-100 text-indigo-500">
                <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"><path d="M20 6 9 17l-5-5"/></svg>
            </div>
          </div>
          <div class="text-center sm:text-left">
            <h2 class="text-2xl font-bold text-slate-900 mb-1">{{ userData.username }}</h2>
            <p class="text-slate-500 font-medium bg-slate-100 px-3 py-1 rounded-full text-sm inline-block">{{ userData.email }}</p>
          </div>
        </div>

        <form @submit.prevent="handleUpdate" class="space-y-8">
          <div class="space-y-3">
            <label for="username" class="text-sm font-bold text-slate-700 ml-1">이름</label>
            <input
              id="username"
              v-model="userData.username"
              type="text"
              class="w-full bg-slate-50 border border-slate-200 rounded-2xl px-5 py-4 text-slate-900 font-medium placeholder-slate-400 focus:outline-none focus:ring-4 focus:ring-indigo-500/10 focus:border-indigo-500 transition-all shadow-sm"
              placeholder="이름을 입력하세요"
            />
          </div>

           <div class="space-y-3">
            <label for="email" class="text-sm font-bold text-slate-700 ml-1">이메일</label>
            <input
              id="email"
              v-model="userData.email"
              type="email"
              disabled
              class="w-full bg-slate-50/50 border border-slate-200/60 rounded-2xl px-5 py-4 text-slate-400 font-medium cursor-not-allowed"
            />
            <p class="text-xs text-slate-400 font-medium ml-1">🔒 이메일은 변경할 수 없습니다.</p>
          </div>

          <div class="pt-4 flex justify-end">
            <button
              type="submit"
              :disabled="updating"
              class="w-full sm:w-auto px-8 py-4 bg-slate-900 hover:bg-slate-800 text-white rounded-2xl font-bold shadow-lg shadow-slate-200 transition-all hover:-translate-y-1 disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2 group"
            >
              <div v-if="updating" class="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin"></div>
              <span>변경사항 저장</span>
              <svg v-if="!updating" xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round" class="group-hover:translate-x-1 transition-transform"><path d="M5 12h14"/><path d="m12 5 7 7-7 7"/></svg>
            </button>
          </div>
        </form>

        <div class="mt-12 pt-8 border-t border-slate-100">
            <div class="flex items-center justify-between">
                <div>
                     <h3 class="text-slate-900 font-bold mb-1">계정 관리</h3>
                     <p class="text-xs text-slate-400">서비스 이용을 중단하고 싶으신가요?</p>
                </div>
                <button 
                    @click="handleDelete"
                    class="px-4 py-2 border border-red-200 bg-red-50 text-red-500 hover:bg-red-100 hover:border-red-300 rounded-xl transition-colors text-sm font-bold"
                >
                    회원 탈퇴
                </button>
            </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import { userApi } from '../api/user';
import { useAuth } from '../composables/useAuth';

const router = useRouter();
const { refresh, user } = useAuth();

const userData = ref({
    id: null,
    username: '',
    email: ''
});
const updating = ref(false);

const userInitial = computed(() => {
    return userData.value.username ? userData.value.username.charAt(0).toUpperCase() : 'U';
});

onMounted(async () => {
    try {
        const res = await userApi.getMyProfile();
        userData.value = res.data;
    } catch (e) {
        console.error("Failed to load profile", e);
    }
});

const handleUpdate = async () => {
    updating.value = true;
    try {
        await userApi.update(userData.value.id, { 
            username: userData.value.username,
            email: userData.value.email 
        });
        alert("정보가 수정되었습니다.");
        // Should refresh global auth state if useAuth stores user info
        window.location.reload(); 
    } catch (e) {
        console.error("Update failed", e);
        alert("수정에 실패했습니다.");
    } finally {
        updating.value = false;
    }
};

const handleDelete = async () => {
    if(!confirm("정말로 탈퇴하시겠습니까? 이 작업은 되돌릴 수 없습니다.")) return;
    try {
        await userApi.delete(userData.value.id);
        alert("탈퇴 처리되었습니다.");
        window.location.href = "/";
    } catch (e) {
        console.error("Delete failed", e);
        alert("실패했습니다.");
    }
};
</script>

<style scoped>
@import url('https://cdn.jsdelivr.net/gh/orioncactus/pretendard/dist/web/static/pretendard.css');

.animate-fade-in-up {
  animation: fade-in-up 0.8s cubic-bezier(0.16, 1, 0.3, 1) forwards;
  opacity: 0;
  transform: translateY(20px);
}
.delay-100 { animation-delay: 0.1s; }
@keyframes fade-in-up {
  to { opacity: 1; transform: translateY(0); }
}

.animate-blob {
  animation: blob 7s infinite;
}
.animation-delay-2000 {
  animation-delay: 2s;
}
@keyframes blob {
  0% { transform: translate(0px, 0px) scale(1); }
  33% { transform: translate(30px, -50px) scale(1.1); }
  66% { transform: translate(-20px, 20px) scale(0.9); }
  100% { transform: translate(0px, 0px) scale(1); }
}
</style>

