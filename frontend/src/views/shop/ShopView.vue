<template>
  <!-- Main Layout Wrapper matching DashboardView -->
  <div class="flex h-screen overflow-hidden bg-white font-['Pretendard']">
    <div class="w-full overflow-y-auto [scrollbar-gutter:stable]">
    <div class="min-h-screen bg-white pb-20">
    <div class="flex justify-center p-4 md:p-8">
      <div class="flex gap-8 max-w-screen-xl w-full items-start">
        <div class="flex-1 min-w-0">
      
      <!-- Header with Currency Badge -->
      <div class="relative flex items-center mb-8 h-7">
        <div class="flex items-center gap-3">
          <ShoppingBag class="w-7 h-7 text-brand-500" stroke-width="2.5" fill="currentColor" />
          <h1 class="text-xl font-black text-slate-800">포인트 상점</h1>
        </div>

        <!-- Currency Badge (Integrated & Absolute) -->
        <div v-if="user" class="absolute right-0 top-1/2 -translate-y-1/2 bg-white rounded-2xl pl-3 pr-5 py-2 shadow-sm border border-slate-200 flex items-center gap-3 transition-transform hover:scale-105 select-none ring-1 ring-slate-100 z-10">
             <div class="relative">
                <div class="w-10 h-10 bg-slate-50 rounded-xl flex items-center justify-center border border-slate-100 shadow-inner">
                    <img src="/images/items/log.png" class="w-6 h-6 object-contain drop-shadow-sm" alt="Logs" />
                </div>
                <div class="absolute -top-1 -right-1 flex h-3 w-3 items-center justify-center rounded-full bg-brand-500 ring-2 ring-white">
                   <div class="h-1 w-1 rounded-full bg-white animate-pulse"></div>
                </div>
             </div>
             
             <div class="flex flex-col">
                 <span class="text-[9px] font-black text-slate-400 uppercase tracking-widest leading-none mb-1">Total Logs</span>
                 <span class="text-xl font-black text-slate-800 font-['Outfit'] leading-none tracking-tight">
                    {{ (user.logCount || 0).toLocaleString() }}<span class="text-sm text-slate-500 ml-1 font-bold font-['Pretendard']">로그</span>
                 </span>
             </div>
        </div>
      </div>

      <!-- Shop Items Grid -->
      <div v-if="loading" class="flex justify-center py-20">
          <Loader2 class="w-10 h-10 text-brand-500 animate-spin" />
      </div>

      <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
        <div 
            v-for="item in items" 
            :key="item.id" 
            class="group bg-white rounded-3xl p-6 border border-slate-200 shadow-sm hover:shadow-xl hover:-translate-y-1 transition-all duration-300 relative overflow-hidden"
            :class="{ 'ring-2 ring-brand-500 ring-offset-2': isOwned(item.id) }"
        >
            <!-- Preview Area -->
            <div class="h-32 bg-slate-50 rounded-2xl mb-6 flex items-center justify-center relative overflow-hidden">
                <div class="relative z-10 px-6 py-3 bg-white rounded-xl shadow-sm border border-slate-100">
                     <span class="font-bold text-slate-800 text-lg" :class="item.cssClass">
                        Sample Nickname
                     </span>
                </div>
                <!-- Background Pattern -->
                <div class="absolute inset-0 opacity-5 bg-[radial-gradient(#cbd5e1_1px,transparent_1px)] [background-size:16px_16px]"></div>
            </div>

            <!-- Item Info -->
            <div class="mb-6">
                <h3 class="font-bold text-slate-900 text-lg mb-1">{{ item.name }}</h3>
                <p class="text-sm text-slate-500 line-clamp-2 min-h-[40px]">{{ item.description }}</p>
            </div>

            <!-- Price & Action -->
            <div class="flex items-center justify-between mt-auto">
                <div class="flex items-center gap-1.5 font-bold text-slate-800">
                    <img src="/images/items/log.png" class="w-5 h-5 object-contain" alt="Logs" />
                    <span>{{ item.price }}</span>
                </div>

                <div class="flex gap-2">
                    <!-- Buy Button -->
                    <button 
                        v-if="!isOwned(item.id)"
                        @click="handleBuy(item)"
                        class="px-5 py-2.5 bg-brand-500 hover:bg-brand-600 text-white text-sm font-bold rounded-xl shadow-lg shadow-brand-500/30 transition-all active:scale-95 flex items-center gap-1.5"
                    >
                        <ShoppingBag :size="16" />
                        구매
                    </button>
                    <button 
                        v-else
                        disabled
                        class="px-5 py-2.5 bg-slate-100 text-slate-400 text-sm font-bold rounded-xl cursor-not-allowed flex items-center gap-1.5"
                    >
                        <Check :size="16" />
                        보유중
                    </button>

                    <!-- Gift Button (Admin Only) -->
                     <button 
                        v-if="isAdmin"
                        @click="openGiftModal(item)"
                        class="px-3 py-2.5 bg-amber-100 hover:bg-amber-200 text-amber-600 text-sm font-bold rounded-xl transition-all active:scale-95"
                        title="선물하기"
                    >
                        <Gift :size="18" />
                    </button>
                </div>
            </div>
        </div>
        </div>
      </div>
    </div>

    <!-- Gift Modal -->
    <Teleport to="body">
        <div v-if="showGiftModal" class="fixed inset-0 z-[9999] flex items-center justify-center p-4">
            <div class="absolute inset-0 bg-slate-900/40 backdrop-blur-sm" @click="closeGiftModal"></div>
            <div class="relative bg-white rounded-3xl w-full max-w-md p-6 shadow-2xl animate-in fade-in zoom-in-95 duration-200">
                <h3 class="text-xl font-bold text-slate-800 mb-6 flex items-center gap-2">
                    <Gift class="w-6 h-6 text-amber-500" />
                    아이템 선물하기
                </h3>
                
                <div class="mb-6">
                    <div class="text-sm font-bold text-slate-500 mb-2">선물할 아이템</div>
                    <div class="p-3 bg-slate-50 rounded-xl border border-slate-200 font-bold text-slate-800 flex items-center gap-3">
                         <span :class="selectedItem?.cssClass">Sample</span>
                         <span class="text-slate-400">|</span>
                         {{ selectedItem?.name }}
                    </div>
                </div>

                <div class="mb-8">
                     <label class="block text-sm font-bold text-slate-600 mb-2">받을 사용자 검색</label>
                     <div class="flex gap-2 mb-2">
                        <input 
                            v-model="searchKeyword" 
                            @keyup.enter="searchUsers"
                            class="flex-1 bg-slate-50 border border-slate-200 rounded-xl px-4 py-2.5 font-medium focus:outline-none focus:border-brand-500 focus:ring-4 focus:ring-brand-500/10"
                            placeholder="이름 또는 이메일"
                        />
                        <button @click="searchUsers" class="px-4 bg-slate-800 text-white rounded-xl font-bold text-sm">검색</button>
                     </div>

                     <!-- Search Results -->
                     <div v-if="users.length > 0" class="max-h-40 overflow-y-auto custom-scrollbar border border-slate-100 rounded-xl">
                        <div 
                            v-for="u in users" 
                            :key="u.id"
                            @click="selectedUser = u"
                            class="p-3 hover:bg-slate-50 cursor-pointer flex items-center justify-between transition-colors border-b border-slate-50 last:border-0"
                            :class="{ 'bg-brand-50': selectedUser?.id === u.id }"
                        >
                            <div class="flex items-center gap-3">
                                <img :src="u.avatarUrl || '/images/items/default-profile.png'" class="w-8 h-8 rounded-full bg-white border border-slate-200"/>
                                <div>
                                    <div class="text-sm font-bold text-slate-800">{{ u.username }}</div>
                                    <div class="text-[10px] text-slate-400">{{ u.email }}</div>
                                </div>
                            </div>
                            <Check v-if="selectedUser?.id === u.id" class="w-4 h-4 text-brand-600" />
                        </div>
                     </div>
                     <div v-else-if="searched" class="text-center py-4 text-xs text-slate-400">검색 결과가 없습니다.</div>
                </div>

                <div class="flex gap-3">
                    <button @click="closeGiftModal" class="flex-1 py-3 rounded-xl font-bold text-slate-500 hover:bg-slate-100 bg-white border border-slate-200">취소</button>
                    <button 
                        @click="handleGift"
                        :disabled="!selectedUser"
                        class="flex-1 py-3 rounded-xl font-bold text-white bg-amber-500 hover:bg-amber-600 shadow-lg shadow-amber-500/30 disabled:opacity-50 disabled:shadow-none"
                    >
                        선물하기
                    </button>
                </div>
            </div>
        </div>
    </Teleport>

    </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { useAuth } from '@/composables/useAuth';

import { shopApi } from '@/api/shop'; // Need to create/update shop.js
import { userApi } from '@/api/user'; // For searching users
import { Loader2, ShoppingBag, Check, Gift } from 'lucide-vue-next';
import '@/assets/css/effects.css'; // Ensure effects are loaded

const { user, refresh } = useAuth();
const items = ref([]);
const loading = ref(true);
const ownedItems = ref(new Set()); // Store IDs of owned items

// Gift Modal State
const showGiftModal = ref(false);
const selectedItem = ref(null);
const searchKeyword = ref('');
const users = ref([]);
const selectedUser = ref(null);
const searched = ref(false);

const isAdmin = computed(() => user.value?.role === 'ROLE_ADMIN');

const fetchItems = async () => {
    try {
        const res = await shopApi.getItems();
        items.value = res.data;
        // In a real app, we should fetch owned items separately or check user's inventory
        // Assuming user.value (from useAuth refresh) could populate this if we added ownedDecorations list to UserResponse
        // But for now, let's just fetch owned items if we have an endpoint, or rely on maybe 'isOwned' flag in item list if backend supported it?
        // Actually backend 'selectShopItems' just returns Deocration.
        // We need to know if user owns it. 
        // Let's assume we can fetch my decorations.
        // Or updated UserResponse to include list of owned decoration IDs?
        // Let's add 'fetchMyDecorations' to shopApi or use existing userApi if available.
        // For now, let's assume we don't know (or implement fetchMyItems).
        // Let's implement fetchMyItems in shopApi quickly or just skip "owned" check visual for now?
        // Better: Fetch my decorations.
        const myDecoRes = await userApi.getMyDecorations(); // Assuming this exists or create it
        ownedItems.value = new Set(myDecoRes.data.map(d => d.decorationId)); 
    } catch (e) {
        console.error(e);
    } finally {
        loading.value = false;
    }
};

const isOwned = (itemId) => ownedItems.value.has(itemId);

const handleBuy = async (item) => {
    if (!confirm(`${item.name}을(를) ${item.price} Log에 구매하시겠습니까?`)) return;
    try {
        await shopApi.buyItem(item.id);
        alert("구매가 완료되었습니다!");
        ownedItems.value.add(item.id);
        if (user.value) {
            user.value.logCount -= item.price; // Optimistic update
        }
        await refresh(); // Refresh user state (logs)
    } catch (e) {
        console.error(e);
        alert(e.response?.data?.message || "구매에 실패했습니다.");
    }
};

// Gift Logic
const openGiftModal = (item) => {
    selectedItem.value = item;
    showGiftModal.value = true;
    searchKeyword.value = '';
    users.value = [];
    selectedUser.value = null;
    searched.value = false;
};

const closeGiftModal = () => {
    showGiftModal.value = false;
    selectedItem.value = null;
};

const searchUsers = async () => {
    if (!searchKeyword.value.trim()) return;
    try {
        const res = await userApi.list({ keyword: searchKeyword.value });
        users.value = res.data;
        searched.value = true;
    } catch (e) {
        console.error(e);
    }
};

const handleGift = async () => {
    if (!selectedUser.value || !selectedItem.value) return;
    if (!confirm(`${selectedUser.value.username}님에게 ${selectedItem.value.name}을(를) 선물하시겠습니까?`)) return;
    
    try {
        await shopApi.giftItem(selectedUser.value.id, selectedItem.value.id);
        alert("선물이 완료되었습니다!");
        closeGiftModal();
    } catch (e) {
        console.error(e);
        alert(e.response?.data?.message || "선물하기 실패");
    }
};

onMounted(() => {
    fetchItems();
});
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Outfit:wght@500;700;900&display=swap');
</style>
