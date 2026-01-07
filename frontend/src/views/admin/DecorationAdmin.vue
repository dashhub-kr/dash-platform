<template>
  <div class="space-y-6">
    <!-- Header -->
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-bold text-slate-800">닉네임 꾸미기 관리</h1>
        <p class="text-slate-500">닉네임 효과를 등록하고 사용자에게 부여합니다.</p>
      </div>
      <button 
        @click="showCreateModal = true"
        class="px-4 py-2 bg-brand-600 hover:bg-brand-500 text-white rounded-lg font-bold shadow-sm transition-colors"
      >
        + 새 효과 등록
      </button>
    </div>

    <!-- Effects Grid -->
    <div v-if="loading" class="text-center py-10 text-slate-400">Loading...</div>
    <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
      <div 
        v-for="effect in effects" 
        :key="effect.id"
        class="bg-white rounded-xl border border-slate-200 p-6 shadow-sm hover:shadow-md transition-all"
      >
        <!-- Preview -->
        <div class="bg-slate-50 rounded-lg p-6 flex items-center justify-center mb-4 border border-slate-100">
          <span class="text-xl font-bold" :class="effect.cssClass">
            Preview Text
          </span>
        </div>

        <div class="space-y-2">
          <div class="flex items-center justify-between">
            <h3 class="font-bold text-slate-800">{{ effect.name }}</h3>
            <span 
              class="px-2 py-0.5 text-xs rounded-full font-bold"
              :class="effect.type === 'ADMIN_ONLY' ? 'bg-purple-100 text-purple-700' : 'bg-green-100 text-green-700'"
            >
              {{ effect.type === 'ADMIN_ONLY' ? '관리자용' : '공개' }}
            </span>
          </div>
          <p class="text-sm text-slate-500 line-clamp-2 h-10">{{ effect.description }}</p>
          <div class="text-xs font-mono text-slate-400 bg-slate-50 px-2 py-1 rounded">
            .{{ effect.cssClass }}
          </div>
        </div>

        <!-- Actions -->
        <div class="mt-6 flex gap-2">
          <button 
            @click="openGrantModal(effect)"
            class="flex-1 px-3 py-2 bg-slate-100 hover:bg-slate-200 text-slate-700 rounded-lg text-sm font-bold transition-colors"
          >
            사용자 부여
          </button>
          <button 
            @click="deleteEffect(effect.id)"
            class="px-3 py-2 text-rose-500 hover:bg-rose-50 rounded-lg text-sm font-bold transition-colors"
          >
            삭제
          </button>
        </div>
      </div>
    </div>

    <!-- Create Modal -->
    <div v-if="showCreateModal" class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50 backdrop-blur-sm">
      <div class="bg-white rounded-2xl w-full max-w-md p-6 shadow-2xl">
        <h2 class="text-xl font-bold text-slate-800 mb-4">새 효과 등록</h2>
        <div class="space-y-4">
          <div>
            <label class="block text-sm font-bold text-slate-700 mb-1">이름</label>
            <input v-model="newEffect.name" class="w-full px-3 py-2 border rounded-lg focus:ring-2 focus:ring-brand-500 outline-none" placeholder="예: 무지개 효과">
          </div>
          <div>
            <label class="block text-sm font-bold text-slate-700 mb-1">CSS 클래스</label>
            <input v-model="newEffect.cssClass" class="w-full px-3 py-2 border rounded-lg focus:ring-2 focus:ring-brand-500 outline-none" placeholder="예: effect-rainbow">
             <p class="text-xs text-slate-400 mt-1">effects.css에 정의된 클래스명을 입력하세요.</p>
          </div>
          <div>
            <label class="block text-sm font-bold text-slate-700 mb-1">설명</label>
            <textarea v-model="newEffect.description" class="w-full px-3 py-2 border rounded-lg focus:ring-2 focus:ring-brand-500 outline-none" rows="2"></textarea>
          </div>
          <div>
             <label class="block text-sm font-bold text-slate-700 mb-1">유형</label>
             <select v-model="newEffect.type" class="w-full px-3 py-2 border rounded-lg focus:ring-2 focus:ring-brand-500 outline-none">
                <option value="PUBLIC">공개 (PUBLIC)</option>
                <option value="ADMIN_ONLY">관리자 전용 (ADMIN_ONLY)</option>
             </select>
          </div>
        </div>
        <div class="flex justify-end gap-2 mt-6">
          <button @click="showCreateModal = false" class="px-4 py-2 text-slate-500 hover:bg-slate-50 rounded-lg font-bold">취소</button>
          <button @click="createEffect" class="px-4 py-2 bg-brand-600 hover:bg-brand-500 text-white rounded-lg font-bold">등록</button>
        </div>
      </div>
    </div>

    <!-- Grant Modal -->
    <div v-if="showGrantModal" class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50 backdrop-blur-sm">
      <div class="bg-white rounded-2xl w-full max-w-md p-6 shadow-2xl">
        <h2 class="text-xl font-bold text-slate-800 mb-4">
            '{{ selectedEffect?.name }}' 효과 부여
        </h2>
        <div class="space-y-4">
            <div>
                 <label class="block text-sm font-bold text-slate-700 mb-1">대상 사용자 이름</label>
                 <input v-model="grantTargetUsername" class="w-full px-3 py-2 border rounded-lg focus:ring-2 focus:ring-brand-500 outline-none" placeholder="사용자 닉네임 입력">
            </div>
        </div>
        <div class="flex justify-end gap-2 mt-6">
          <button @click="showGrantModal = false" class="px-4 py-2 text-slate-500 hover:bg-slate-50 rounded-lg font-bold">취소</button>
          <button @click="grantEffect" class="px-4 py-2 bg-brand-600 hover:bg-brand-500 text-white rounded-lg font-bold">부여하기</button>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { decorationApi } from '@/api/decoration';
import { userApi } from '@/api/user';

// Mock API 호출 제거 - 표준 API 클라이언트 사용

const effects = ref([]);
const loading = ref(true);
const showCreateModal = ref(false);
const showGrantModal = ref(false);
const selectedEffect = ref(null);
const grantTargetUsername = ref('');

const newEffect = ref({
    name: '',
    description: '',
    cssClass: '',
    type: 'PUBLIC'
});

const fetchEffects = async () => {
    try {
        const res = await decorationApi.findAll();
        effects.value = res.data;
    } catch (e) {
        console.error(e);
    } finally {
        loading.value = false;
    }
};

const createEffect = async () => {
    try {
        await decorationApi.create(newEffect.value);
        showCreateModal.value = false;
        newEffect.value = { name: '', description: '', cssClass: '', type: 'PUBLIC' };
        fetchEffects();
    } catch (e) {
        alert('Failed to create effect');
    }
};

const deleteEffect = async (id) => {
    if(!confirm('Are you sure?')) return;
    try {
        await decorationApi.delete(id);
        fetchEffects();
    } catch (e) {
        alert('Failed to delete effect');
    }
};

const openGrantModal = (effect) => {
    selectedEffect.value = effect;
    grantTargetUsername.value = '';
    showGrantModal.value = true;
};

const grantEffect = async () => {
    if (!grantTargetUsername.value) return;
    try {
        // 사용자 이름으로 사용자 ID 찾기 필요? 또는 API가 사용자 이름을 받나?
        // 현재 API: /admin/decorations/grant -> body { userId, decorationId }
        // 조회 필요. 일단 사용자를 먼저 검색한다고 가정.
        
        // 단순화: 사용자 이름으로 ID 검색
        const userRes = await userApi.list({ keyword: grantTargetUsername.value });
        if (!userRes.data || userRes.data.length === 0) {
            alert('User not found');
            return;
        }
        // 정확한 일치 또는 첫 번째 일치 가정
        const user = userRes.data.find(u => u.username === grantTargetUsername.value);
        if(!user) {
             alert('User not found (Exact match required)');
             return;
        }

        await decorationApi.grant(user.id, selectedEffect.value.id);
        
        alert(`Granted to ${user.username}`);
        showGrantModal.value = false;
    } catch (e) {
        console.error(e);
        alert('Failed to grant effect');
    }
};

onMounted(() => {
    fetchEffects();
});
</script>
