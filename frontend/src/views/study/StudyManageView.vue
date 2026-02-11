<script setup>
import { ref, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import { studyApi } from '@/api/study';
import { useAuth } from '@/composables/useAuth';
import { 
  Settings, Users, Crown, Trash2, UserCheck, 
  ChevronLeft, Loader2, Save, AlertTriangle, X, Check
} from 'lucide-vue-next';
import BaseIconBadge from '@/components/common/BaseIconBadge.vue';

const router = useRouter();
const { user, refresh } = useAuth();

// State
const loading = ref(true);
const saving = ref(false);
const study = ref({
  id: null,
  name: '',
  description: '',
  creatorId: null
});

// Member Management
const memberList = ref([]);
const loadingMembers = ref(false);
const showDelegateModal = ref(false);
const selectedMemberId = ref(null);

// Dissolution
const showDeleteConfirmModal = ref(false);
const deleteInput = ref('');

onMounted(async () => {
  if (!user.value?.studyId) {
    router.replace('/training/roadmap');
    return;
  }

  try {
    const res = await studyApi.get(user.value.studyId);
    study.value = res.data;
    
    // Check leadership
    if (study.value.creatorId !== user.value.id && user.value.role !== 'ROLE_ADMIN') {
      alert("스터디장만 접근 가능합니다.");
      router.replace('/dashboard');
    }
  } catch (e) {
    console.error("Failed to load study info", e);
    alert("스터디 정보를 불러오지 못했습니다.");
    router.replace('/dashboard');
  } finally {
    loading.value = false;
  }
});

const handleSave = async () => {
  if (!study.value.name.trim()) return;
  saving.value = true;
  try {
    await studyApi.update(study.value.id, {
      name: study.value.name,
      description: study.value.description
    });
    alert("스터디 정보가 수정되었습니다.");
    await refresh(); 
    router.push('/profile');
  } catch (e) {
    console.error(e);
    alert("수정에 실패했습니다.");
  } finally {
    saving.value = false;
  }
};

const openDelegateModal = async () => {
  showDelegateModal.value = true;
  loadingMembers.value = true;
  selectedMemberId.value = null;
  try {
    const res = await studyApi.getMembers(study.value.id);
    // Exclude self
    memberList.value = res.data.filter(m => m.id !== user.value.id);
  } catch (e) {
    console.error(e);
    alert("멤버 목록을 불러오지 못했습니다.");
    showDelegateModal.value = false;
  } finally {
    loadingMembers.value = false;
  }
};

const handleDelegate = async () => {
  if (!selectedMemberId.value) return;
  if (!confirm("정말로 스터디장 권한을 위임하시겠습니까?\n위임 후에는 일반 멤버로 전환되며, 이 페이지에 다시 접근할 수 없습니다.")) return;
  
  try {
    await studyApi.delegateLeader(study.value.id, selectedMemberId.value);
    alert("스터디장이 변경되었습니다.");
    router.replace('/profile');
  } catch(e) {
    console.error(e);
    alert("위임에 실패했습니다.");
  }
};

const openDeleteModal = () => {
  deleteInput.value = '';
  showDeleteConfirmModal.value = true;
};

const handleConfirmDelete = async () => {
  if (deleteInput.value !== study.value.name) return;
  
  if (!confirm("정말로 스터디를 해체하시겠습니까?\n이 작업은 되돌릴 수 없으며, 모든 미션과 기록이 영구적으로 삭제됩니다.")) return;
  
  try {
    await studyApi.deleteStudy(study.value.id);
    alert("스터디가 해체되었습니다.");
    router.replace('/training/roadmap');
  } catch (e) {
    console.error(e);
    alert(e.response?.data?.message || "해체에 실패했습니다.");
  }
};

const goBack = () => {
  router.back();
};
</script>

<template>
  <div class="min-h-screen bg-slate-50 text-slate-700 font-[Pretendard] pb-20">
    <!-- Header -->
    <header class="bg-white border-b border-slate-200 sticky top-0 z-30">
      <div class="container mx-auto px-6 h-16 flex items-center justify-between max-w-5xl">
        <div class="flex items-center gap-4">
          <button @click="goBack" class="p-2 hover:bg-slate-100 rounded-xl transition-colors text-slate-500">
            <ChevronLeft :size="24" />
          </button>
          <h1 class="text-xl font-black text-slate-800 tracking-tight">스터디 관리</h1>
        </div>
        <button 
          @click="handleSave"
          :disabled="saving || !study.name.trim()"
          class="px-5 py-2.5 bg-brand-600 hover:bg-brand-700 text-white rounded-xl font-bold transition-all shadow-md shadow-brand-200 disabled:opacity-50 flex items-center gap-2"
        >
          <Loader2 v-if="saving" class="animate-spin w-4 h-4" />
          <Save v-else :size="18" />
          저장하기
        </button>
      </div>
    </header>

    <main class="container mx-auto px-6 py-10 max-w-5xl">
      <div v-if="loading" class="flex flex-col items-center justify-center py-20">
        <Loader2 class="animate-spin text-brand-500 mb-4" :size="48" />
        <p class="text-slate-400 font-bold">스터디 정보를 불러오는 중...</p>
      </div>

      <div v-else class="grid grid-cols-1 lg:grid-cols-12 gap-8 items-start">
        <!-- Left Side: Basic Info -->
        <div class="lg:col-span-7 space-y-6">
          <section class="bg-white rounded-3xl p-8 shadow-sm border border-slate-100">
            <h2 class="text-lg font-bold text-slate-800 mb-6 flex items-center gap-2">
              <Settings :size="20" class="text-brand-500" />
              기본 정보 수정
            </h2>
            
            <div class="space-y-6">
              <div>
                <label class="block text-xs font-bold text-slate-400 mb-2 uppercase">스터디 이름</label>
                <input 
                  v-model="study.name"
                  type="text"
                  placeholder="스터디 이름을 입력하세요"
                  class="w-full bg-slate-50 border-2 border-slate-100 rounded-2xl px-4 py-3.5 font-bold text-slate-700 focus:outline-none focus:border-brand-500 focus:bg-white transition-all"
                />
              </div>

              <div>
                <label class="block text-xs font-bold text-slate-400 mb-2 uppercase">스터디 소개</label>
                <textarea 
                  v-model="study.description"
                  rows="4"
                  placeholder="스터디를 소개하는 글을 적어주세요"
                  class="w-full bg-slate-50 border-2 border-slate-100 rounded-2xl px-4 py-3.5 font-medium text-slate-700 focus:outline-none focus:border-brand-500 focus:bg-white transition-all resize-none"
                ></textarea>
              </div>
            </div>
          </section>

          <!-- Integration Tips -->
          <div class="bg-indigo-50 border border-indigo-100 rounded-3xl p-6 flex gap-4">
            <div class="bg-indigo-100 p-3 rounded-2xl h-fit">
              <Settings class="w-6 h-6 text-indigo-600" />
            </div>
            <div>
              <h3 class="font-bold text-indigo-900 mb-1">스터디 관리 팁</h3>
              <p class="text-sm text-indigo-700 leading-relaxed break-keep">
                <span class="block">스터디 이름이나 소개글을 변경하면 모든 스터디원에게 즉시 반영됩니다.</span>
                <span class="block">멋진 소개글로 새로운 팀원을 모집해보세요!</span>
              </p>
            </div>
          </div>
        </div>

        <!-- Right Side: Leader Actions -->
        <div class="lg:col-span-5 space-y-6">
          <section class="bg-white rounded-3xl p-8 shadow-sm border border-slate-100">
            <h2 class="text-lg font-bold text-slate-800 mb-6 flex items-center gap-2">
              <Crown :size="20" class="text-orange-500" />
              스터디장 전용 작업
            </h2>

            <div class="space-y-4">
              <!-- Delegation -->
              <button 
                @click="openDelegateModal"
                class="w-full group px-6 py-5 bg-slate-50 hover:bg-brand-50 border border-slate-100 hover:border-brand-100 rounded-2xl transition-all text-left flex items-center justify-between"
              >
                <div class="flex items-center gap-4">
                  <div class="p-3 bg-white group-hover:bg-brand-100 rounded-xl transition-colors shadow-sm">
                    <UserCheck :size="20" class="text-slate-400 group-hover:text-brand-600" />
                  </div>
                  <div>
                    <div class="font-bold text-slate-700 group-hover:text-brand-900">스터디장 위임</div>
                    <div class="text-xs text-slate-400 group-hover:text-brand-600">권한을 다른 멤버에게 넘깁니다</div>
                  </div>
                </div>
              </button>

              <!-- Dissolution -->
              <button 
                @click="openDeleteModal"
                class="w-full group px-6 py-5 bg-slate-50 hover:bg-red-50 border border-slate-100 hover:border-red-100 rounded-2xl transition-all text-left flex items-center justify-between"
              >
                <div class="flex items-center gap-4">
                  <div class="p-3 bg-white group-hover:bg-red-100 rounded-xl transition-colors shadow-sm">
                    <Trash2 :size="20" class="text-slate-400 group-hover:text-red-500" />
                  </div>
                  <div>
                    <div class="font-bold text-slate-700 group-hover:text-red-900">스터디 해체</div>
                    <div class="text-xs text-slate-400 group-hover:text-red-500">모든 정보와 기록을 삭제합니다</div>
                  </div>
                </div>
              </button>
            </div>
          </section>

          <!-- Warning Zone -->
          <div class="bg-amber-50 border border-amber-100 rounded-3xl p-6 flex gap-4">
            <AlertTriangle class="w-6 h-6 text-amber-500 shrink-0" />
            <p class="text-xs text-amber-700 font-medium leading-relaxed break-keep">
              <span class="block">스터디장 권한 위임이나 스터디 해체는 되돌릴 수 없는 작업입니다.</span>
              <span class="block">신중하게 결정해주세요.</span>
            </p>
          </div>
        </div>
      </div>
    </main>

    <!-- Delegation Modal -->
    <Teleport to="body">
      <div v-if="showDelegateModal" class="fixed inset-0 z-[9000] flex items-center justify-center p-4">
        <div class="absolute inset-0 bg-slate-900/60 backdrop-blur-sm" @click="showDelegateModal = false"></div>
        <div class="relative bg-white rounded-3xl w-full max-w-md p-8 shadow-2xl animate-in fade-in zoom-in-95 duration-200">
          <div class="flex items-center justify-between mb-6">
            <h3 class="text-xl font-bold text-slate-800">스터디장 위임</h3>
            <button @click="showDelegateModal = false" class="p-2 hover:bg-slate-100 rounded-full transition-colors">
              <X :size="20" class="text-slate-400" />
            </button>
          </div>
          
          <p class="text-sm text-slate-500 mb-6">새로운 스터디장을 선택해주세요.</p>
          
          <div v-if="loadingMembers" class="py-12 flex flex-col items-center gap-3 mb-10">
            <Loader2 class="animate-spin text-brand-500" :size="32"/>
            <p class="text-xs text-slate-400 font-bold">멤버 목록 불러오는 중...</p>
          </div>
          
          <div v-else-if="memberList.length === 0" class="py-12 text-center text-slate-400 font-bold bg-slate-50 rounded-2xl px-6 mb-10">
            <Users class="w-12 h-12 text-slate-200 mx-auto mb-3" />
            위임할 수 있는 다른 멤버가 없습니다.<br>
            <span class="text-xs font-normal mt-2 block">멤버가 최소 2명 이상이어야 위임이 가능합니다.</span>
          </div>

          <div v-else class="space-y-3 max-h-[300px] overflow-y-auto mb-10 pr-1 custom-scrollbar">
            <label 
              v-for="member in memberList" 
              :key="member.id"
              class="flex items-center gap-4 p-4 rounded-2xl border-2 cursor-pointer transition-all"
              :class="selectedMemberId === member.id ? 'border-brand-500 bg-brand-50 shadow-sm shadow-brand-100' : 'border-slate-50 hover:border-slate-200'"
            >
              <input type="radio" :value="member.id" v-model="selectedMemberId" class="hidden">
              <img :src="member.avatarUrl || '/images/profiles/default-profile.png'" class="w-12 h-12 rounded-2xl bg-slate-100 object-cover border border-slate-100 shadow-sm" />
              <div class="flex-1">
                <div class="font-bold text-slate-700">{{ member.username }}</div>
                <div class="text-xs text-slate-400">Tier: {{ member.solvedacTier }}</div>
              </div>
              <div v-if="selectedMemberId === member.id" class="w-6 h-6 bg-brand-500 text-white rounded-full flex items-center justify-center">
                <Check :size="14" stroke-width="3" />
              </div>
            </label>
          </div>

          <div class="flex gap-3">
            <button 
                @click="showDelegateModal = false"
                class="flex-1 py-4 rounded-2xl font-bold text-slate-500 border border-slate-200 hover:bg-slate-50 transition-colors"
            >
                취소
            </button>
            <button 
                @click="handleDelegate"
                :disabled="!selectedMemberId"
                class="flex-1 py-4 rounded-2xl font-bold text-white bg-brand-400 hover:bg-brand-500 transition-all disabled:opacity-50 disabled:cursor-not-allowed shadow-lg shadow-brand-100"
            >
                위임하기
            </button>
          </div>
        </div>
      </div>
    </Teleport>

    <!-- Dissolution Confirmation Modal -->
    <Teleport to="body">
      <div v-if="showDeleteConfirmModal" class="fixed inset-0 z-[9000] flex items-center justify-center p-4">
        <div class="absolute inset-0 bg-slate-900/70 backdrop-blur-sm" @click="showDeleteConfirmModal = false"></div>
        <div class="relative bg-white rounded-3xl w-full max-w-lg p-8 shadow-2xl animate-in fade-in zoom-in-95 duration-200 border border-red-50">
          <h3 class="text-2xl font-black text-slate-800 mb-2">스터디 해체</h3>
          <p class="text-sm text-slate-500 leading-relaxed mb-8 break-keep">
            <span class="block">정말 <span class="bg-red-50 text-red-600 px-1.5 py-0.5 rounded font-bold underline decoration-2 underline-offset-4">{{ study.name }}</span> 스터디를 해체하시겠습니까?</span>
            <span class="block">이 작업은 되돌릴 수 없으며, 모든 미션과 기록이 영구적으로 삭제됩니다.</span>
          </p>
          
          <div class="mb-8">
            <label class="block text-xs font-bold text-slate-400 mb-3 uppercase tracking-widest">스터디 이름을 입력하세요</label>
            <input 
              v-model="deleteInput"
              type="text"
              class="w-full bg-slate-50 border-2 border-slate-100 rounded-2xl px-5 py-4 font-black text-slate-800 focus:outline-none focus:border-red-500 focus:bg-white transition-all placeholder:text-slate-200"
              :placeholder="study.name"
            />
          </div>

          <div class="flex gap-4">
            <button 
              @click="showDeleteConfirmModal = false"
              class="flex-1 py-4 rounded-2xl font-bold text-slate-500 border border-slate-200 hover:bg-slate-50 transition-colors"
            >
              취소
            </button>
            <button 
              @click="handleConfirmDelete"
              :disabled="deleteInput !== study.name"
              class="flex-1 py-4 rounded-2xl font-bold text-white bg-red-500 hover:bg-red-600 transition-all disabled:opacity-50 disabled:cursor-not-allowed shadow-lg shadow-red-200"
            >
              해체하기
            </button>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<style scoped>
.custom-scrollbar::-webkit-scrollbar {
  width: 4px;
}
.custom-scrollbar::-webkit-scrollbar-track {
  background: transparent;
}
.custom-scrollbar::-webkit-scrollbar-thumb {
  background: #e2e8f0;
  border-radius: 10px;
}
.custom-scrollbar::-webkit-scrollbar-thumb:hover {
  background: #cbd5e1;
}

/* Base Decoration Classes (from project CSS if available) */
.text-brand-gradient {
  background: linear-gradient(to right, #6366f1, #06b6d4);
  background-clip: text;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}
</style>
