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

        <!-- Other Admin Sections -->
        <section class="grid grid-cols-1 gap-6">
            
            <!-- Study Management -->
            <div class="bg-white rounded-3xl p-8 border border-slate-200 shadow-sm">
                <h2 class="text-xl font-bold text-slate-800 mb-6 flex items-center gap-2">
                    <Compass class="w-6 h-6 text-brand-500" />
                    스터디 관리/관전
                </h2>
                <div class="flex gap-4 mb-6">
                    <input 
                        v-model="studySearchKeyword" 
                        @keyup.enter="searchStudies"
                        type="text" 
                        placeholder="스터디 이름으로 검색..." 
                        class="flex-1 bg-slate-50 border border-slate-200 rounded-xl px-4 py-3 font-medium text-slate-800 focus:outline-none focus:border-brand-500 focus:ring-4 focus:ring-brand-500/10 transition-all"
                    />
                    <button 
                        @click="searchStudies"
                        class="px-6 bg-slate-800 hover:bg-slate-900 text-white font-bold rounded-xl transition-colors"
                    >
                        검색
                    </button>
                </div>

                <!-- Study Search Results -->
                <div v-if="studySearchResults.length > 0" class="space-y-3">
                    <div v-for="study in studySearchResults" :key="study.id" class="flex items-center justify-between p-4 bg-slate-50 rounded-xl border border-slate-100">
                        <div class="flex items-center gap-3">
                            <div class="w-10 h-10 rounded-xl bg-brand-50 flex items-center justify-center text-brand-600">
                                <Users class="w-5 h-5" />
                            </div>
                            <div>
                                <div class="font-bold text-slate-800">{{ study.name }}</div>
                                <div class="text-xs text-slate-400">멤버 {{ study.memberCount }}명 • {{ study.description || '소개 없음' }}</div>
                            </div>
                        </div>
                        <div class="flex items-center gap-2">
                             <button 
                                @click="goToObservation(study.id)"
                                class="px-3 py-1.5 text-xs font-bold text-slate-700 border border-slate-300 hover:bg-slate-200 rounded-lg transition-colors flex items-center gap-1"
                            >
                                <Eye :size="14" />
                                관전
                            </button>
                        </div>
                    </div>
                </div>
                <div v-else-if="studySearched" class="text-center py-10 text-slate-400 bg-slate-50 rounded-xl border border-dashed border-slate-200">
                    검색 결과가 없습니다.
                </div>
            </div>

            <!-- User Management -->
            <div class="bg-white rounded-3xl p-8 border border-slate-200 shadow-sm">
                <h2 class="text-xl font-bold text-slate-800 mb-6 flex items-center gap-2">
                    <UserX class="w-6 h-6 text-rose-500" />
                    회원 관리 (차단)
                </h2>
                <div class="flex gap-4 mb-6">
                    <input 
                        v-model="searchKeyword" 
                        @keyup.enter="searchUsers"
                        type="text" 
                        placeholder="이름 또는 이메일로 검색..." 
                        class="flex-1 bg-slate-50 border border-slate-200 rounded-xl px-4 py-3 font-medium text-slate-800 focus:outline-none focus:border-brand-500 focus:ring-4 focus:ring-brand-500/10 transition-all"
                    />
                    <button 
                        @click="searchUsers"
                        class="px-6 bg-slate-800 hover:bg-slate-900 text-white font-bold rounded-xl transition-colors"
                    >
                        검색
                    </button>
                </div>

                <!-- Search Results -->
                <div v-if="searchResults.length > 0" class="space-y-3">
                    <div v-for="user in searchResults" :key="user.id" class="flex items-center justify-between p-4 bg-slate-50 rounded-xl border border-slate-100">
                        <div class="flex items-center gap-3">
                            <img :src="user.avatarUrl || '/images/default-profile.png'" class="w-10 h-10 rounded-full object-cover bg-white border border-slate-200" />
                            <div>
                                <div class="font-bold text-slate-800">{{ user.username }}</div>
                                <div class="text-xs text-slate-400">{{ user.email }}</div>
                            </div>
                        </div>
                        <div class="flex items-center gap-2">
                            <span v-if="user.status === 'BLOCKED'" class="text-xs font-bold text-rose-500 bg-rose-50 px-2 py-1 rounded">차단됨</span>
                            <button 
                                v-else
                                @click="handleBlockUser(user)"
                                class="px-3 py-1.5 text-xs font-bold text-rose-500 border border-rose-200 hover:bg-rose-50 rounded-lg transition-colors flex items-center gap-1"
                            >
                                <ShieldAlert :size="14" />
                                차단
                            </button>
                        </div>
                    </div>
                </div>
                <div v-else-if="searched" class="text-center py-10 text-slate-400 bg-slate-50 rounded-xl border border-dashed border-slate-200">
                    검색 결과가 없습니다.
                </div>
            </div>
        </section>

        <!-- Placeholders -->
        <section class="grid grid-cols-1 md:grid-cols-2 gap-6">
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
import { useRouter } from 'vue-router';
import { ShieldAlert, Gift, Send, Loader2, UserX, Settings, Compass, Users, Eye } from 'lucide-vue-next';
import { studyApi } from '@/api/study';
import { adminApi } from '@/api/admin';
import { userApi } from '@/api/user';

const router = useRouter();
const studies = ref([]);
const gifting = ref(false);

// Study Search State
const studySearchKeyword = ref('');
const studySearchResults = ref([]);
const studySearched = ref(false);

const searchStudies = async () => {
    if (!studySearchKeyword.value.trim()) return;
    try {
        const res = await studyApi.getStudies({ keyword: studySearchKeyword.value });
        studySearchResults.value = res.data;
        studySearched.value = true;
    } catch (e) {
        console.error("Study search failed", e);
        alert("스터디 검색에 실패했습니다.");
    }
};

const goToObservation = (studyId) => {
    router.push(`/admin/study/${studyId}/dashboard`);
};

// User Search State
const searchKeyword = ref('');
const searchResults = ref([]);
const searched = ref(false);

const searchUsers = async () => {
    if (!searchKeyword.value.trim()) return;
    try {
        const res = await userApi.list({ keyword: searchKeyword.value });
        searchResults.value = res.data;
        searched.value = true;
    } catch (e) {
        console.error("User search failed", e);
        alert("사용자 검색에 실패했습니다.");
    }
};

const handleBlockUser = async (user) => {
    if (!confirm(`'${user.username}' 회원을 정말로 차단하시겠습니까?`)) return;
    try {
        await adminApi.blockUser(user.id);
        alert("회원이 차단되었습니다.");
        // Refresh search results
        searchUsers();
    } catch (e) {
        console.error("Block failed", e);
        alert("차단에 실패했습니다.");
    }
};

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
