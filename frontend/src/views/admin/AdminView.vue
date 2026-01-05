<template>
  <div class="min-h-screen bg-white text-slate-800 pb-20 font-['Pretendard']">
    <div class="max-w-4xl mx-auto px-6 py-10">
      
      <!-- Header -->
      <header class="mb-10">
        <h1 class="text-3xl font-black text-slate-900 flex items-center gap-3 mb-2">
            <div class="w-12 h-12 bg-slate-900 rounded-2xl flex items-center justify-center shadow-lg shadow-slate-200">
                <ShieldAlert class="w-6 h-6 text-white" />
            </div>
            관리자 대시보드
        </h1>
        <p class="text-slate-500 font-medium ml-[60px]">시스템 관리 및 운영 작업을 수행합니다.</p>
      </header>

      <!-- Main Grid -->
      <div class="grid gap-8">
        
        <!-- Acorn Gift Section -->
        <section class="bg-white rounded-3xl border border-slate-200 shadow-sm p-8 relative overflow-hidden">
            <div class="absolute top-0 right-0 w-32 h-32 bg-amber-50 rounded-bl-full -mr-10 -mt-10 z-0"></div>
            
            <div class="relative z-10">
                <h2 class="text-xl font-bold text-slate-800 mb-6 flex items-center gap-2">
                    <Gift class="w-6 h-6 text-amber-500" />
                    도토리 선물하기
                </h2>
                
                <div class="flex flex-col gap-5">
                    <!-- Study Selector -->
                    <div>
                        <label class="block text-sm font-bold text-slate-600 mb-2">대상 스터디</label>
                        <select 
                            v-model="giftForm.studyId" 
                            class="w-full bg-slate-50 border border-slate-200 rounded-xl px-4 py-3 font-medium text-slate-800 focus:outline-none focus:border-brand-500 focus:ring-4 focus:ring-brand-500/10 transition-all cursor-pointer"
                        >
                            <option :value="null" disabled>스터디를 선택하세요</option>
                            <option v-for="study in studies" :key="study.id" :value="study.id">
                                {{ study.name }} (멤버 {{ study.memberCount }}명)
                            </option>
                        </select>
                    </div>

                    <div class="flex gap-5">
                        <!-- Amount -->
                        <div class="flex-1">
                            <label class="block text-sm font-bold text-slate-600 mb-2">지급 수량</label>
                            <div class="relative">
                                <input 
                                    v-model.number="giftForm.amount" 
                                    type="number" 
                                    placeholder="100"
                                    class="w-full bg-slate-50 border border-slate-200 rounded-xl px-4 py-3 font-medium text-slate-800 focus:outline-none focus:border-brand-500 focus:ring-4 focus:ring-brand-500/10 transition-all"
                                />
                                <span class="absolute right-4 top-1/2 -translate-y-1/2 text-sm font-bold text-slate-400">개</span>
                            </div>
                        </div>
                        
                        <!-- Reason -->
                        <div class="flex-[2]">
                            <label class="block text-sm font-bold text-slate-600 mb-2">지급 사유</label>
                            <input 
                                v-model="giftForm.reason" 
                                type="text" 
                                placeholder="예: 주간 우수 스터디 선정"
                                class="w-full bg-slate-50 border border-slate-200 rounded-xl px-4 py-3 font-medium text-slate-800 focus:outline-none focus:border-brand-500 focus:ring-4 focus:ring-brand-500/10 transition-all"
                            />
                        </div>
                    </div>

                    <div class="flex justify-end mt-2">
                        <button 
                            @click="handleGiftAcorns"
                            :disabled="!isValidGiftForm || gifting"
                            class="px-8 py-3 bg-slate-900 hover:bg-brand-600 text-white font-bold rounded-xl shadow-lg shadow-slate-200 transition-all flex items-center gap-2 disabled:opacity-50 disabled:shadow-none"
                        >
                            <Loader2 v-if="gifting" class="w-5 h-5 animate-spin" />
                            <Send v-else class="w-5 h-5" />
                            <span>도토리 지급</span>
                        </button>
                    </div>
                </div>
            </div>
        </section>

        <!-- Other Admin Sections (Placeholder) -->
        <section class="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div class="bg-slate-50 rounded-3xl p-6 border border-slate-200 border-dashed flex flex-col items-center justify-center text-center py-12">
                <UserX class="w-10 h-10 text-slate-300 mb-3" />
                <h3 class="font-bold text-slate-500">신고 접수 관리</h3>
                <p class="text-xs text-slate-400 mt-1">준비 중인 기능입니다</p>
            </div>
             <div class="bg-slate-50 rounded-3xl p-6 border border-slate-200 border-dashed flex flex-col items-center justify-center text-center py-12">
                <Settings class="w-10 h-10 text-slate-300 mb-3" />
                <h3 class="font-bold text-slate-500">시스템 설정</h3>
                <p class="text-xs text-slate-400 mt-1">준비 중인 기능입니다</p>
            </div>
        </section>

      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { ShieldAlert, Gift, Send, Loader2, UserX, Settings } from 'lucide-vue-next';
import { studyApi } from '@/api/study';
import { adminApi } from '@/api/admin';

const studies = ref([]);
const gifting = ref(false);

const giftForm = ref({
    studyId: null,
    amount: '',
    reason: ''
});

const isValidGiftForm = computed(() => {
    return giftForm.value.studyId && giftForm.value.amount > 0 && giftForm.value.reason.trim();
});

const fetchStudies = async () => {
    try {
        const res = await studyApi.getStudies(); // Assuming this returns all public studies
        studies.value = res.data;
    } catch (e) {
        console.error("Failed to fetch studies", e);
    }
};

const handleGiftAcorns = async () => {
    if (!isValidGiftForm.value) return;
    
    if (!confirm(`${giftForm.value.amount}개의 도토리를 지급하시겠습니까?`)) return;

    gifting.value = true;
    try {
        await adminApi.giftAcorns({
            studyId: giftForm.value.studyId,
            amount: giftForm.value.amount,
            reason: giftForm.value.reason
        });
        
        alert("도토리가 성공적으로 지급되었습니다.");
        
        // Reset form
        giftForm.value = {
            studyId: null,
            amount: '',
            reason: ''
        };
    } catch (e) {
        console.error("Failed to gift acorns", e);
        alert("도토리 지급에 실패했습니다.");
    } finally {
        gifting.value = false;
    }
};

onMounted(() => {
    fetchStudies();
});
</script>
