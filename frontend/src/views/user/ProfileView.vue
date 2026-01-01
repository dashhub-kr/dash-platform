<script setup>
import { ref, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import { userApi } from '@/api/user';
import { studyApi } from '@/api/study';
import { useAuth } from '@/composables/useAuth';
import { onboardingApi } from '@/api/onboarding';
import { Settings, LogOut, Github, Award, Users, Crown } from 'lucide-vue-next';
import BaseIconBadge from '@/components/common/BaseIconBadge.vue';

const router = useRouter();
const { refresh, user } = useAuth();

// ... (userData definition)

// BaseIconBadge용 티어 색상 매퍼
const getTierColorName = (tier) => {
    if (tier >= 1 && tier <= 5) return 'orange'; // 브론즈
    if (tier >= 6 && tier <= 10) return 'slate';  // 실버
    if (tier >= 11 && tier <= 15) return 'yellow'; // 골드
    if (tier >= 16 && tier <= 20) return 'emerald'; // 플래티넘
    if (tier >= 21 && tier <= 25) return 'brand'; // 다이아몬드
    if (tier >= 26 && tier <= 30) return 'rose'; // 루비
    return 'slate'; // 언랭크
};

const getTierName = (tier) => {
    if (tier >= 1 && tier <= 5) return `Bronze ${6 - tier}`;
    if (tier >= 6 && tier <= 10) return `Silver ${11 - tier}`;
    if (tier >= 11 && tier <= 15) return `Gold ${16 - tier}`;
    if (tier >= 16 && tier <= 20) return `Platinum ${21 - tier}`;
    if (tier >= 21 && tier <= 25) return `Diamond ${26 - tier}`;
    if (tier >= 26 && tier <= 30) return `Ruby ${31 - tier}`;
    return 'Unrated';
};

const userData = ref({
    id: null,
    username: '',
    email: '',
    avatarUrl: '', 
    solvedacHandle: '',
    solvedacTier: 0,
    repositoryName: '',
    webhookConfigured: false,
    studyId: null,
    isStudyLeader: false
});

const studyName = ref(null);
const loadingStudy = ref(false);

const updating = ref(false);
const syncingSolvedac = ref(false);
const syncingRepo = ref(false);

const userProfileImage = computed(() => {
    if (userData.value.avatarUrl) return userData.value.avatarUrl;
    return '/images/profiles/default-profile.png';
});


onMounted(async () => {
    try {
        const res = await userApi.getMyProfile();
        userData.value = res.data;
        
        // 스터디 정보가 있다면 가져오기
        if (userData.value.studyId) {
            loadingStudy.value = true;
            try {
                const studyRes = await studyApi.get(userData.value.studyId);
                studyName.value = studyRes.data.name;
            } catch (err) {
                console.error("Failed to load study info", err);
                studyName.value = "Unknown Study";
            } finally {
                loadingStudy.value = false;
            }
        }
    } catch (e) {
        console.error("Failed to load profile", e);
    }
});

const handleUpdate = async () => {
    updating.value = true;
    try {
        await userApi.update(userData.value.id, { 
            username: userData.value.username,
            // email 제외
        });
        alert("정보가 수정되었습니다.");
        window.location.reload(); 
    } catch (e) {
        console.error("Update failed", e);
        alert("수정에 실패했습니다.");
    } finally {
        updating.value = false;
    }
};

const updateSolvedac = async () => {
    if (!userData.value.solvedacHandle) return;
    syncingSolvedac.value = true;
    try {
        await onboardingApi.registerSolvedacRaw(userData.value.solvedacHandle);
        alert("Solved.ac 핸들이 업데이트 되었습니다.\n데이터 분석에는 시간이 걸릴 수 있습니다.");
        window.location.reload();
    } catch (e) {
        console.error(e);
        alert("Solved.ac 연동 실패. 아이디를 확인해주세요.");
    } finally {
        syncingSolvedac.value = false;
    }
};

const updateRepository = async () => {
    if (!userData.value.repositoryName) return;
    syncingRepo.value = true;
    try {
        const res = await onboardingApi.submitRepository(userData.value.repositoryName);
        const data = res.data;
        userData.value.repositoryName = data.repositoryName;
        userData.value.webhookConfigured = data.webhookConfigured;
        
        if (data.webhookConfigured) {
             alert("저장소가 연결되고 웹훅이 설정되었습니다!");
        } else {
             alert("저장소는 연결되었으나 웹훅 설정에 실패했습니다.\n잠시 후 다시 시도해주세요.");
        }
    } catch (e) {
        console.error(e);
        alert("저장소 연결 실패. 이름을 확인해주세요.");
    } finally {
        syncingRepo.value = false;
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

<template>
  <div class="min-h-screen bg-white text-slate-700 font-[Pretendard]">
    <main class="container mx-auto px-6 py-10 max-w-5xl">
      
      <div class="grid grid-cols-1 lg:grid-cols-12 gap-8 items-start">
        
        <!-- LEFT COLUMN: Profile Summary & Stats -->
        <div class="lg:col-span-4 space-y-6">
            <!-- Profile Card -->
            <div class="bg-white rounded-3xl p-6 text-center shadow-sm">
                <div class="relative inline-block mx-auto mb-4">
                    <img :src="userProfileImage" class="w-32 h-32 rounded-3xl object-cover bg-slate-50 mx-auto shadow-sm" />
                </div>
                <h1 class="text-2xl font-black text-slate-800 tracking-tight mb-4 relative z-10">{{ userData.username }}</h1>
                <!-- 요청에 따라 이메일 숨김 -->
                <!-- <p class="text-slate-400 font-bold text-sm mb-4">{{ userData.email }}</p> -->
                
                <div class="text-xs text-slate-300 font-bold uppercase tracking-wider">Joined Dec 2024</div>
            </div>

            <!-- Real Stats -->
            <div class="space-y-4">
                <!-- Tier Card -->
                <div class="bg-white rounded-3xl p-5 flex items-center gap-5 transition-colors shadow-sm">
                    <BaseIconBadge 
                        :icon="Award" 
                        :color="getTierColorName(userData.solvedacTier)" 
                        size="lg"
                    />
                    <div>
                        <div class="text-xl font-black text-slate-800 tracking-tight">
                            {{ getTierName(userData.solvedacTier).split(' ')[0] || 'Unrated' }}
                        </div>
                        <div class="text-xs font-bold text-slate-400 uppercase">Solved.ac Tier</div>
                    </div>
                </div>

                <!-- Study Card -->
                <!-- Study Card -->
                 <div class="bg-white rounded-3xl p-5 flex items-center gap-5 transition-colors relative overflow-hidden shadow-sm">
                    <BaseIconBadge 
                        :icon="Users" 
                        color="brand" 
                        size="lg"
                    />
                    <div class="z-10">
                        <div class="text-xl font-black text-slate-800 tracking-tight flex items-center gap-2">
                             <span v-if="loadingStudy" class="text-sm text-slate-400 animate-pulse">로딩중...</span>
                             <span v-else-if="studyName">{{ studyName }}</span>
                             <span v-else class="text-slate-400 text-base">스터디 없음</span>
                             
                             <div v-if="userData.isStudyLeader" class="inline-flex">
                                <BaseIconBadge :icon="Crown" text="LEADER" color="fox" size="sm" />
                             </div>
                        </div>
                        <div class="text-xs font-bold text-slate-400 uppercase mt-0.5">
                            Affiliated Study
                        </div>
                    </div>
                </div>
            </div>
            
            <!-- Danger Zone (Mobile only) -->
             <button 
                @click="handleDelete"
                class="lg:hidden w-full py-4 rounded-2xl bg-rose-50 text-rose-500 font-bold hover:bg-rose-100 transition-colors flex items-center justify-center gap-2"
            >
                <LogOut :size="18" />
                회원 탈퇴
            </button>
        </div>

        <!-- RIGHT COLUMN: Settings Forms -->
        <div class="lg:col-span-8 space-y-8">
            
            <!-- Edit Profile -->
            <section class="bg-white rounded-3xl p-6 md:p-8 shadow-sm">
                <div class="flex items-center justify-between mb-6">
                    <h2 class="text-xl font-bold text-slate-800 flex items-center gap-2">
                        <Settings :size="20" class="text-slate-400"/>
                        프로필 설정
                    </h2>
                </div>
                
                <div class="space-y-4">
                    <div>
                        <label class="block text-xs font-bold text-slate-400 mb-2 uppercase">이름</label>
                        <input 
                            v-model="userData.username" 
                            class="w-full bg-slate-50 border-2 border-slate-200 rounded-xl px-4 py-3 font-bold text-slate-700 focus:outline-none focus:border-brand-500 focus:bg-white transition-all"
                        />
                    </div>
                    <!-- Email Field Removed -->
                    <div class="pt-2">
                        <button 
                            @click="handleUpdate"
                            :disabled="updating"
                            class="w-full bg-brand-500 hover:bg-brand-600 text-white font-bold py-4 rounded-xl transition-all shine-effect disabled:opacity-50"
                        >
                            {{ updating ? '저장 중...' : '변경사항 저장' }}
                        </button>
                    </div>
                </div>
            </section>

             <!-- Repository Sync -->
            <section class="bg-white rounded-3xl p-6 md:p-8 shadow-sm">
                 <h2 class="text-xl font-bold text-slate-800 mb-6 flex items-center gap-2">
                    <Zap :size="20" class="text-slate-400"/>
                    계정 연동
                </h2>
                <div class="space-y-6">
                    <!-- Solved.ac -->
                    <div>
                         <label class="block text-xs font-bold text-slate-400 mb-2 uppercase flex items-center gap-2">
                            <span class="w-4 h-4 rounded-sm bg-[#17C55B] flex items-center justify-center text-white text-[8px] font-bold">S</span>
                            Solved.ac
                         </label>
                        <div class="flex gap-3">
                            <input 
                                v-model="userData.solvedacHandle"
                                class="flex-1 bg-slate-50 border-2 border-slate-200 rounded-xl px-4 py-3 font-bold text-slate-700 focus:outline-none focus:border-[#17C55B] focus:bg-white transition-all"
                                placeholder="백준 핸들 입력"
                            />
                            <button 
                                @click="updateSolvedac"
                                :disabled="syncingSolvedac"
                                class="px-6 bg-[#17C55B] hover:bg-[#15a04a] text-white rounded-xl font-bold transition-colors disabled:opacity-50 shrink-0"
                            >
                                {{ syncingSolvedac ? '...' : '연동' }}
                            </button>
                        </div>
                    </div>

                    <!-- Github -->
                     <div>
                         <label class="block text-xs font-bold text-slate-400 mb-2 uppercase flex items-center gap-2">
                            <Github :size="14" />
                            Github Repository
                         </label>
                        <div class="flex gap-3">
                            <input 
                                v-model="userData.repositoryName"
                                class="flex-1 bg-slate-50 border-2 border-slate-200 rounded-xl px-4 py-3 font-bold text-slate-700 focus:outline-none focus:border-slate-800 focus:bg-white transition-all"
                                placeholder="owner/repo"
                            />
                            <button 
                                @click="updateRepository"
                                :disabled="syncingRepo"
                                class="px-6 bg-slate-800 hover:bg-slate-700 text-white rounded-xl font-bold transition-colors disabled:opacity-50 shrink-0"
                            >
                                {{ syncingRepo ? '...' : '연결' }}
                            </button>
                        </div>
                         <div v-if="userData.webhookConfigured" class="mt-2 text-xs font-bold text-emerald-600 flex items-center gap-1.5 px-1">
                            <span class="relative flex h-2 w-2">
                              <span class="animate-ping absolute inline-flex h-full w-full rounded-full bg-emerald-400 opacity-75"></span>
                              <span class="relative inline-flex rounded-full h-2 w-2 bg-emerald-500"></span>
                            </span>
                            웹훅이 정상적으로 연결되었습니다
                        </div>
                    </div>
                </div>
            </section>
            
            <div class="flex justify-end lg:block hidden">
                 <button 
                    @click="handleDelete"
                    class="text-rose-400 hover:text-rose-600 font-bold text-sm flex items-center gap-2 px-4 py-2 hover:bg-rose-50 rounded-lg transition-colors"
                >
                    <LogOut :size="16" />
                    계정 탈퇴하기
                </button>
            </div>
        </div>
      </div>

    </main>
  </div>
</template>

<style scoped>
@import url('https://cdn.jsdelivr.net/gh/orioncactus/pretendard/dist/web/static/pretendard.css');
</style>
