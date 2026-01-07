<template>
  <div class="min-h-screen bg-slate-50 p-6 md:p-12">
    <div class="max-w-4xl mx-auto space-y-8">
      
      <!-- Header -->
      <div class="bg-white rounded-3xl p-8 shadow-sm flex items-center justify-between">
        <div>
          <h1 class="text-2xl font-bold text-slate-800">닉네임 꾸미기</h1>
          <p class="text-slate-500 mt-1">획득한 효과를 장착하여 닉네임을 돋보이게 만드세요!</p>
        </div>
        <div class="text-right">
             <div class="text-sm text-slate-400 mb-1">현재 적용 중</div>
             <div class="text-2xl font-bold">
                <NicknameRenderer 
                    :nickname="user?.username || 'User'"
                    :role="user?.role"
                    :decorationClass="user?.equippedDecorationClass"
                    class="text-2xl"
                />
             </div>
        </div>
      </div>

      <!-- Inventory Grid -->
      <div v-if="loading" class="text-center py-10 text-slate-400">Loading...</div>
      <div v-else-if="decorations.length === 0" class="text-center py-20 bg-white rounded-3xl shadow-sm">
        <p class="text-slate-500 text-lg">보유한 닉네임 효과가 없습니다.</p>
        <p class="text-slate-400 text-sm mt-2">이벤트나 업적을 통해 효과를 획득해보세요!</p>
      </div>
      <div v-else class="grid grid-cols-1 md:grid-cols-2 gap-6">
        <!-- Default (Unequip) Option -->
        <div 
            class="bg-white rounded-2xl p-6 border-2 transition-all cursor-pointer hover:shadow-md flex items-center justify-between"
            :class="!user?.equippedDecorationClass ? 'border-brand-500 ring-2 ring-brand-100' : 'border-slate-100 hover:border-brand-200'"
            @click="unequip"
        >
            <div class="flex items-center gap-4">
                 <div class="w-12 h-12 rounded-full bg-slate-100 flex items-center justify-center text-slate-400">
                    <span class="font-bold text-xs">OFF</span>
                 </div>
                 <div>
                    <h3 class="font-bold text-slate-800">기본 (효과 없음)</h3>
                    <p class="text-xs text-slate-400">일반 텍스트로 표시됩니다.</p>
                 </div>
            </div>
            <div v-if="!user?.equippedDecorationClass" class="text-brand-600 font-bold text-sm">
                장착 중
            </div>
        </div>

        <!-- Owned Items -->
        <div 
            v-for="item in decorations" 
            :key="item.id"
            class="bg-white rounded-2xl p-6 border-2 transition-all cursor-pointer hover:shadow-md flex items-center justify-between"
            :class="isEquipped(item) ? 'border-brand-500 ring-2 ring-brand-100' : 'border-slate-100 hover:border-brand-200'"
            @click="equip(item)"
        >
             <div class="flex items-center gap-4">
                 <!-- Preview Circle -->
                 <div class="w-12 h-12 rounded-full bg-slate-50 flex items-center justify-center border border-slate-100 overflow-hidden">
                    <span :class="item.decoration.cssClass" class="text-lg font-bold">Aa</span>
                 </div>
                 <div>
                    <h3 class="font-bold text-slate-800">{{ item.decoration.name }}</h3>
                    <p class="text-xs text-slate-400">{{ item.decoration.description }}</p>
                 </div>
            </div>
            <div v-if="isEquipped(item)" class="text-brand-600 font-bold text-sm">
                장착 중
            </div>
        </div>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useAuth } from '@/composables/useAuth'; // 현재 사용자 정보 가져오기 & 새로고침을 위해 auth composable 사용
import NicknameRenderer from '@/components/common/NicknameRenderer.vue';
import { decorationApi } from '@/api/decoration';

// API 설정 제거 - 표준 클라이언트 사용

const { user, refresh } = useAuth();
const decorations = ref([]);
const loading = ref(true);

const fetchInventory = async () => {
    try {
        const res = await decorationApi.getMyInventory();
        decorations.value = res.data;
    } catch (e) {
        console.error(e);
    } finally {
        loading.value = false;
    }
};

const isEquipped = (item) => {
    return user.value?.equippedDecorationClass === item.decoration.cssClass; 
    // 사용자 객체에 decorationId가 없을 수 있으므로 cssClass로 비교
    // cssClass가 고유하다면 안전함
};

const equip = async (item) => {
    try {
        await decorationApi.equip(item.decorationId);
        // 장착 상태 업데이트를 위해 사용자 정보 새로고침
        await refresh();
    } catch (e) {
        alert('Failed to equip');
    }
};

const unequip = async () => {
    try {
        await decorationApi.unequip();
        await refresh();
    } catch (e) {
        alert('Failed to unequip');
    }
};

onMounted(() => {
    fetchInventory();
    if (!user.value) refresh();
});
</script>
