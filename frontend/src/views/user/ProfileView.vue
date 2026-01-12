<script setup>
import { ref, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import { userApi } from '@/api/user';
import { studyApi } from '@/api/study';
import { shopApi } from '@/api/shop';
import { useAuth } from '@/composables/useAuth';
import { onboardingApi } from '@/api/onboarding';
import { Settings, LogOut, Github, Award, Users, Crown, RotateCcw, Loader2, HelpCircle, Zap as ZapIcon, Copy, Trash2, UserCheck, MoreVertical, AlertTriangle, ChevronDown, Palette, Check, Sparkles, X } from 'lucide-vue-next';
import BaseIconBadge from '@/components/common/BaseIconBadge.vue';
import TroubleshootingModal from '@/components/common/TroubleshootingModal.vue';

const router = useRouter();
const { refresh, user } = useAuth();

// 스터디 메뉴 상태
const showStudyMenu = ref(false);

// 위임 모달 상태
const showDelegateModal = ref(false);
const memberList = ref([]);
const loadingMembers = ref(false);
const selectedMemberId = ref(null);

const openDelegateModal = async () => {
    showDelegateModal.value = true;
    loadingMembers.value = true;
    selectedMemberId.value = null; // reset selection
    try {
        const res = await studyApi.getMembers(userData.value.studyId);
        // Exclude self
        memberList.value = res.data.filter(m => m.id !== userData.value.id);
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
    if (!confirm("정말로 스터디장 권한을 위임하시겠습니까?\n위임 후에는 일반 멤버로 전환됩니다.")) return;
    try {
        await studyApi.delegateLeader(userData.value.studyId, selectedMemberId.value);
        alert("스터디장이 변경되었습니다.");
        window.location.reload();
    } catch(e) {
        console.error(e);
        alert("위임에 실패했습니다.");
    }
};

// 삭제 모달 상태
const showDeleteConfirmModal = ref(false);
const deleteInput = ref('');

const openDeleteModal = () => {
    if (userData.value.studyType === 'PERSONAL') {
        alert("임시 스터디(Personal Study)는 직접 삭제할 수 없습니다.\n새로운 스터디를 생성하거나 다른 스터디에 가입하면 자동으로 정리됩니다.");
        return;
    }
    deleteInput.value = '';
    showDeleteConfirmModal.value = true;
};

const handleConfirmDelete = async () => {
    if (deleteInput.value !== studyName.value) return;
    
    try {
        await studyApi.deleteStudy(userData.value.studyId);
        alert("스터디가 해체되었습니다.");
        window.location.reload();
    } catch (e) {
        console.error(e);
        alert(e.response?.data?.message || "해체에 실패했습니다.");
    }
};

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
    if (tier >= 1 && tier <= 5) return `Bronze ${toRoman(6 - tier)}`;
    if (tier >= 6 && tier <= 10) return `Silver ${toRoman(11 - tier)}`;
    if (tier >= 11 && tier <= 15) return `Gold ${toRoman(16 - tier)}`;
    if (tier >= 16 && tier <= 20) return `Platinum ${toRoman(21 - tier)}`;
    if (tier >= 21 && tier <= 25) return `Diamond ${toRoman(26 - tier)}`;
    if (tier >= 26 && tier <= 30) return `Ruby ${toRoman(31 - tier)}`;
    if (tier >= 31) return 'Master';
    return 'Unrated';
};

const toRoman = (n) => {
    const map = { 1: 'I', 2: 'II', 3: 'III', 4: 'IV', 5: 'V' };
    return map[n] || '';
};

const formatJoinedDate = (dateString) => {
    if (!dateString) return '';
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', { year: 'numeric', month: 'short' });
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
    isStudyLeader: false,
    studyType: null,
    equippedDecorationClass: null
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
        
        // 보유 장식 효과 로드 (장착된 효과 이름 표시용)
        try {
            const decoRes = await shopApi.getMyDecorations();
            ownedDecorations.value = decoRes.data;
        } catch (err) {
            console.error("Failed to load decorations", err);
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
        // 백엔드 에러 메시지 표시
        let msg = e.response?.data?.message || "Solved.ac 연동 실패. 아이디를 확인해주세요.";
        // 자바 예외 클래스 명 제거 (모든 Exception Class 포괄)
        msg = msg.replace(/^[\w.]+Exception:\s*/, '');
        alert(msg);
    } finally {
        syncingSolvedac.value = false;
    }
};

const copyVerificationCode = () => {
    const code = `DashHub:${userData.value.username}`;
    navigator.clipboard.writeText(code).then(() => {
        alert("인증 코드가 복사되었습니다!\nSolved.ac 로그인 > '프로필 편집'을 통해 붙여넣어주세요.");
    });
};

const handleRedetect = async () => {
    syncingRepo.value = true;
    
    // 1. 요청 발송 (익스텐션 데이터 요청)
    window.dispatchEvent(new CustomEvent('DashHub-dash-request'));
    
    // 2. 잠시 대기 (브릿지 연결 대기)
    await new Promise(resolve => setTimeout(resolve, 500));
    
    // 3. DOM 확인
    const dataEl = document.getElementById('DashHub-dash-data');
    const hook = dataEl?.getAttribute('data-hook');
    const repo = dataEl?.getAttribute('data-repo');
    
    // 감지된 저장소 (Hook 우선)
    const detectedName = hook || repo;
    
    if (!detectedName) {
        alert("익스텐션 설정을 감지할 수 없습니다.\n\n다음 항목을 확인해주세요:\n1. DashHub 익스텐션이 설치되어 있는지\n2. 익스텐션에서 GitHub 로그인이 완료되었는지\n3. 저장소가 연결되어 있는지");
        syncingRepo.value = false;
        return;
    }

    // 4. 서버로 전송 (Submit Update)
    try {
        const res = await onboardingApi.submitRepository(detectedName);
        const data = res.data;
        userData.value.repositoryName = data.repositoryName;
        userData.value.webhookConfigured = data.webhookConfigured;
        
        if (data.webhookConfigured) {
             alert(`저장소가 '${detectedName}'(으)로 업데이트되었습니다!`);
        } else {
             alert(`저장소가 '${detectedName}'(으)로 설정되었으나 웹훅 확인에 실패했습니다.`);
        }
    } catch (e) {
        console.error(e);
        alert("저장소 업데이트 실패. 잠시 후 다시 시도해주세요.");
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

const handleLeaveStudy = async () => {
    if (!confirm("정말로 스터디를 탈퇴하시겠습니까?")) return;
    try {
        await studyApi.leaveStudy();
        alert("스터디에서 탈퇴했습니다.");
        window.location.reload();
    } catch (e) {
        console.error(e);
        alert(e.response?.data?.message || "탈퇴에 실패했습니다.");
    }
};

const handleDeleteStudy = async () => {
    if (userData.value.studyType === 'PERSONAL') {
        alert("임시 스터디(Personal Study)는 직접 삭제할 수 없습니다.\n새로운 스터디를 생성하거나 다른 스터디에 가입하면 자동으로 정리됩니다.");
        return;
    }

    if (!confirm("정말로 스터디를 해체하시겠습니까?\n\n이 작업은 되돌릴 수 없으며, 모든 미션과 기록이 삭제됩니다.")) return;
    // 이중 확인 (안전을 위해)
    if (!confirm("모든 스터디원이 탈퇴 처리되며, 스터디 정보가 영구적으로 삭제됩니다. 계속하시겠습니까?")) return;
    
    try {
        await studyApi.deleteStudy(userData.value.studyId);
        alert("스터디가 해체되었습니다.");
        window.location.reload();
    } catch (e) {
        console.error(e);
        alert(e.response?.data?.message || "해체에 실패했습니다.");
    }
};


// 칭호(장식) 관리
const showDecorationModal = ref(false);
const showDecorationMenu = ref(false); // 카드 내 더보기 메뉴
const ownedDecorations = ref([]);
const loadingDecorations = ref(false);

const openDecorationModal = async () => {
    showDecorationModal.value = true;
    showDecorationMenu.value = false;
    loadingDecorations.value = true;
    try {
        const res = await shopApi.getMyDecorations();
        ownedDecorations.value = res.data;
    } catch (e) {
        console.error("Failed to load decorations", e);
        // alert("보유한 아이템을 불러오지 못했습니다.");
    } finally {
        loadingDecorations.value = false;
    }
};

const handleEquip = async (decorationId, className) => {
    try {
        await shopApi.equipDecoration(decorationId);
        userData.value.equippedDecorationClass = className;
        showDecorationModal.value = false;
        // alert("효과가 적용되었습니다!");
    } catch (e) {
        console.error(e);
        alert("착용에 실패했습니다.");
    }
};

const handleUnequip = async () => {
    try {
        await shopApi.unequipDecoration();
        userData.value.equippedDecorationClass = null;
        showDecorationModal.value = false;
        // alert("효과가 해제되었습니다.");
    } catch (e) {
        console.error(e);
        alert("해제에 실패했습니다.");
    }
};

const equippedDecorationName = computed(() => {
    if (!userData.value.equippedDecorationClass) return 'No Effect';
    
    // ownedDecorations에서 장착된 효과 찾기
    const equipped = ownedDecorations.value.find(
        item => item.decoration.cssClass === userData.value.equippedDecorationClass
    );
    
    return equipped ? equipped.decoration.name : 'Effect';
});

const showDangerZone = ref(false);
const showFaq = ref(false);
</script>

<template>
  <div class="min-h-screen bg-white text-slate-700 font-[Pretendard]">
    <main class="container mx-auto px-6 py-10 max-w-5xl">
      
      <div class="grid grid-cols-1 lg:grid-cols-12 gap-8 items-start">
        
        <!-- 좌측 컬럼: 프로필 요약 및 통계 -->
        <div class="lg:col-span-4 space-y-6">
            <!-- 프로필 카드 -->
            <div class="bg-white rounded-3xl p-6 text-center shadow-sm">
                <div class="relative inline-block mx-auto mb-4">
                    <img :src="userProfileImage" class="w-32 h-32 rounded-3xl object-cover bg-slate-50 mx-auto shadow-sm" />
                </div>
                <h1 class="text-2xl font-black text-slate-800 tracking-tight mb-4 relative z-10">{{ userData.username }}</h1>
                <!-- 요청에 따라 이메일 숨김 -->
                <!-- <p class="text-slate-400 font-bold text-sm mb-4">{{ userData.email }}</p> -->
                
                <div class="text-xs text-slate-300 font-bold uppercase tracking-wider">
                    Joined {{ formatJoinedDate(userData.createdAt) }}
                </div>
            </div>

            <!-- 실제 통계 정보 -->
            <div class="space-y-4">
                <!-- 티어 카드 -->
                <div class="bg-white rounded-3xl p-5 flex items-center gap-5 transition-colors shadow-sm">
                    <BaseIconBadge 
                        :icon="Award" 
                        :color="getTierColorName(userData.solvedacTier)" 
                        size="lg"
                    />
                    <div>
                        <div class="text-xl font-black text-slate-800 tracking-tight">
                            {{ getTierName(userData.solvedacTier) || 'Unrated' }}
                        </div>
                        <div class="text-xs font-bold text-slate-400 uppercase">Solved.ac Tier</div>
                    </div>
                </div>

                <!-- 스터디 카드 -->
                <div class="bg-white rounded-3xl p-5 flex items-center justify-between transition-colors relative shadow-sm">
                    <div class="flex items-center gap-5">
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
                                    <BaseIconBadge :icon="Crown" color="orange" size="sm" />
                                </div>
                            </div>
                            <div class="text-xs font-bold text-slate-400 uppercase mt-0.5">
                                Affiliated Study
                            </div>
                        </div>
                    </div>

                    <!-- 액션 메뉴 (드롭다운) -->
                    <div class="relative" v-if="userData.studyId">
                        <button 
                            @click="showStudyMenu = !showStudyMenu"
                            class="p-2 text-slate-300 hover:text-slate-600 hover:bg-slate-100 rounded-xl transition-all"
                        >
                            <MoreVertical :size="20" />
                        </button>

                        <!-- 백드롭 -->
                        <div v-if="showStudyMenu" @click="showStudyMenu = false" class="fixed inset-0 z-10 cursor-default bg-transparent"></div>

                        <!-- 메뉴 -->
                        <div v-if="showStudyMenu" class="absolute right-0 top-full mt-2 w-48 bg-white rounded-xl shadow-xl border border-slate-100 p-1.5 z-20 animate-in fade-in zoom-in-95 duration-200 origin-top-right flex flex-col gap-1">
                            
                            <!-- 스터디장 전용 -->
                            <template v-if="userData.isStudyLeader">
                                <button 
                                    @click="openDelegateModal(); showStudyMenu = false"
                                    class="w-full text-left px-3 py-2.5 text-sm font-bold text-slate-600 hover:bg-slate-50 hover:text-slate-800 rounded-lg transition-colors flex items-center gap-2"
                                >
                                    <UserCheck :size="16" />
                                    스터디장 위임
                                </button>
                                <button 
                                    @click="openDeleteModal(); showStudyMenu = false"
                                    class="w-full text-left px-3 py-2.5 text-sm font-bold text-rose-500 hover:bg-rose-50 rounded-lg transition-colors flex items-center gap-2"
                                >
                                    <Trash2 :size="16" />
                                    스터디 해체
                                </button>
                            </template>

                            <!-- 스터디원 전용 -->
                            <template v-else>
                                <button 
                                    @click="handleLeaveStudy(); showStudyMenu = false"
                                    class="w-full text-left px-3 py-2.5 text-sm font-bold text-rose-500 hover:bg-rose-50 rounded-lg transition-colors flex items-center gap-2"
                                >
                                    <LogOut :size="16" />
                                    스터디 탈퇴
                                </button>
                            </template>

                        </div>
                    </div>
                    </div>

                <!-- 칭호(장식) 카드 -->
                <div class="bg-white rounded-3xl p-5 flex items-center justify-between transition-colors relative shadow-sm">
                    <div class="flex items-center gap-5">
                         <BaseIconBadge 
                            :icon="Palette" 
                            color="violet" 
                            size="lg"
                        />
                        <div class="z-10">
                            <div class="text-xl font-black text-slate-800 tracking-tight flex items-center gap-2">
                                <span :class="userData.equippedDecorationClass" class="transition-all">
                                    {{ equippedDecorationName }}
                                </span>
                            </div>
                            <div class="text-xs font-bold text-slate-400 uppercase mt-0.5">
                                Nickname Effect
                            </div>
                        </div>
                    </div>

                    <!-- 액션 메뉴 (드롭다운) -->
                    <div class="relative">
                        <button 
                            @click="showDecorationMenu = !showDecorationMenu"
                            class="p-2 text-slate-300 hover:text-slate-600 hover:bg-slate-100 rounded-xl transition-all"
                        >
                            <MoreVertical :size="20" />
                        </button>

                        <!-- 백드롭 -->
                        <div v-if="showDecorationMenu" @click="showDecorationMenu = false" class="fixed inset-0 z-10 cursor-default bg-transparent"></div>

                        <!-- 메뉴 -->
                        <div v-if="showDecorationMenu" class="absolute right-0 top-full mt-2 w-48 bg-white rounded-xl shadow-xl border border-slate-100 p-1.5 z-20 animate-in fade-in zoom-in-95 duration-200 origin-top-right flex flex-col gap-1">
                            <button 
                                @click="openDecorationModal"
                                class="w-full text-left px-3 py-2.5 text-sm font-bold text-slate-600 hover:bg-slate-50 hover:text-slate-800 rounded-lg transition-colors flex items-center gap-2"
                            >
                                <Sparkles :size="16" />
                                효과 변경하기
                            </button>
                             <button 
                                v-if="userData.equippedDecorationClass"
                                @click="handleUnequip(); showDecorationMenu = false"
                                class="w-full text-left px-3 py-2.5 text-sm font-bold text-rose-500 hover:bg-rose-50 rounded-lg transition-colors flex items-center gap-2"
                            >
                                <X :size="16" />
                                효과 해제
                            </button>
                        </div>
                    </div>
                </div>
            </div>
            
            <!-- 위험 구역 (모바일 전용) -->
        </div>

        <!-- RIGHT COLUMN: Settings Forms -->
        <!-- ... (skipped for brevity) ... -->
        
        <!-- (At the end of template before styles) -->
        <!-- Delegation Modal -->
        <Teleport to="body">
            <div v-if="showDelegateModal" class="fixed inset-0 z-[9999] flex items-center justify-center p-4">
                <div class="absolute inset-0 bg-slate-900/40 backdrop-blur-sm" @click="showDelegateModal = false"></div>
                <!-- ... Content ... -->
                <div class="relative bg-white rounded-3xl w-full max-w-md p-6 shadow-2xl animate-in fade-in zoom-in-95 duration-200">
                    <h3 class="text-xl font-bold text-slate-800 mb-2">스터디장 위임</h3>
                    <p class="text-sm text-slate-500 mb-6">새로운 스터디장을 선택해주세요.</p>
                    
                    <div v-if="loadingMembers" class="py-8 flex justify-center">
                        <Loader2 class="animate-spin text-brand-500" :size="32"/>
                    </div>
                    
                    <div v-else-if="memberList.length === 0" class="py-10 text-center text-slate-400 font-bold bg-slate-50 rounded-2xl">
                        위임할 수 있는 다른 멤버가 없습니다.<br>
                        <span class="text-xs font-normal mt-1 block">혼자라면 스터디 삭제만 가능합니다.</span>
                    </div>

                    <div v-else class="space-y-2 max-h-[300px] overflow-y-auto mb-6 custom-scrollbar">
                        <label 
                            v-for="member in memberList" 
                            :key="member.id"
                            class="flex items-center gap-3 p-3 rounded-xl border-2 cursor-pointer transition-all"
                            :class="selectedMemberId === member.id ? 'border-brand-500 bg-brand-50' : 'border-slate-100 hover:border-slate-300'"
                        >
                            <input type="radio" :value="member.id" v-model="selectedMemberId" class="hidden">
                            <img :src="member.avatarUrl || '/images/profiles/default-profile.png'" class="w-10 h-10 rounded-full bg-slate-200 object-cover" />
                            <div class="flex-1">
                                <div class="font-bold text-slate-700">{{ member.username }}</div>
                                <div class="text-xs text-slate-400">Tier: {{ member.solvedacTier }}</div>
                            </div>
                            <div v-if="selectedMemberId === member.id" class="text-brand-600">
                                <Crown :size="20" fill="currentColor" />
                            </div>
                        </label>
                    </div>

                    <div class="flex gap-3">
                        <button 
                            @click="showDelegateModal = false"
                            class="flex-1 py-3.5 rounded-xl font-bold text-slate-500 hover:bg-slate-100 transition-colors"
                        >
                            취소
                        </button>
                        <button 
                            @click="handleDelegate"
                            :disabled="!selectedMemberId"
                            class="flex-1 py-3.5 rounded-xl font-bold text-white bg-brand-500 hover:bg-brand-600 transition-colors disabled:opacity-50 disabled:cursor-not-allowed shadow-lg shadow-brand-500/30"
                        >
                            위임하기
                        </button>
                    </div>
                </div>
            </div>

            <!-- Delete Confirmation Modal -->
            <div v-if="showDeleteConfirmModal" class="fixed inset-0 z-[9999] flex items-center justify-center p-4">
                <div class="absolute inset-0 bg-slate-900/60 backdrop-blur-sm" @click="showDeleteConfirmModal = false"></div>
                
                <div class="relative bg-white rounded-3xl w-full max-w-md p-6 shadow-2xl animate-in fade-in zoom-in-95 duration-200 border-2 border-slate-100">
                    <div class="mb-4">
                        <h3 class="text-xl font-black text-slate-800 mb-2">스터디 삭제</h3>
                        <p class="text-sm text-slate-500 leading-relaxed">
                            정말 <span class="text-slate-800 font-bold">'{{ studyName }}'</span> 스터디를 삭제하시겠습니까?<br>
                            이 작업은 되돌릴 수 없으며, 모든 미션과 기록이 영구적으로 삭제됩니다.
                        </p>
                    </div>
                    
                    <div class="mb-6">
                        <label class="block text-xs font-bold text-slate-400 mb-2 uppercase">스터디 이름을 입력하세요</label>
                        <input 
                            v-model="deleteInput"
                            type="text"
                            class="w-full bg-slate-50 border-2 border-slate-200 rounded-xl px-4 py-3 font-bold text-slate-700 focus:outline-none focus:border-rose-500 focus:bg-white transition-all placeholder:text-slate-300"
                            :placeholder="studyName"
                        />
                    </div>

                    <div class="flex gap-3">
                        <button 
                            @click="showDeleteConfirmModal = false"
                            class="flex-1 py-3.5 rounded-xl font-bold text-slate-500 hover:bg-slate-100 transition-colors"
                        >
                            취소
                        </button>
                        <button 
                            @click="handleConfirmDelete"
                            :disabled="deleteInput !== studyName"
                            class="flex-1 py-3.5 rounded-xl font-bold text-white bg-rose-500 hover:bg-rose-600 transition-colors disabled:opacity-50 disabled:cursor-not-allowed shadow-lg shadow-rose-500/30"
                        >
                            스터디 삭제
                        </button>
                    </div>
                </div>
            </div>


            <!-- Decoration Select Modal -->
            <div v-if="showDecorationModal" class="fixed inset-0 z-[9999] flex items-center justify-center p-4">
                <div class="absolute inset-0 bg-slate-900/40 backdrop-blur-sm" @click="showDecorationModal = false"></div>
                
                <div class="relative bg-white rounded-3xl w-full max-w-lg p-6 shadow-2xl animate-in fade-in zoom-in-95 duration-200 flex flex-col max-h-[85vh]">
                    <div class="flex items-center justify-between mb-6 shrink-0">
                        <h3 class="text-xl font-black text-slate-800 flex items-center gap-3">
                            <BaseIconBadge :icon="Palette" color="violet" size="lg" />
                            보유 중인 효과
                        </h3>
                        <button @click="showDecorationModal = false" class="p-2 text-slate-400 hover:bg-slate-100 rounded-full transition-colors">
                            <X :size="20" />
                        </button>
                    </div>

                    <div v-if="loadingDecorations" class="flex-1 flex items-center justify-center py-20">
                         <Loader2 class="animate-spin text-brand-500" :size="32"/>
                    </div>
                    
                    <div v-else-if="ownedDecorations.length === 0" class="flex-1 py-12 text-center text-slate-400 font-bold bg-slate-50 rounded-2xl flex flex-col items-center justify-center gap-2">
                        <span class="bg-slate-200 p-3 rounded-full"><Sparkles class="w-6 h-6 text-slate-400" /></span>
                        보유한 효과가 없습니다.<br>
                        <router-link to="/shop" class="text-brand-500 hover:underline text-sm mt-1">상점에서 구매하기</router-link>
                    </div>

                    <div v-else class="flex-1 overflow-y-auto custom-scrollbar p-1 -m-1">
                        <div class="grid grid-cols-1 sm:grid-cols-2 gap-3">
                            <button 
                                v-for="item in ownedDecorations" 
                                :key="item.id"
                                @click="handleEquip(item.decoration.id, item.decoration.cssClass)"
                                class="group relative p-4 rounded-2xl border-2 transition-all text-left flex flex-col gap-3 hover:-translate-y-0.5 hover:shadow-md"
                                :class="userData.equippedDecorationClass === item.decoration.cssClass 
                                    ? 'border-violet-500 bg-violet-50 ring-2 ring-violet-200 ring-offset-2' 
                                    : 'border-slate-100 bg-white hover:border-violet-200'"
                            >
                                <!-- Preview -->
                                <div class="h-12 flex items-center justify-center bg-slate-50 rounded-xl border border-slate-100 group-hover:bg-white transition-colors overflow-hidden">
                                    <span class="font-black text-lg truncate px-2" :class="item.decoration.cssClass">{{ item.decoration.name }}</span>
                                </div>
                                
                                <div class="flex items-center justify-between">
                                    <div class="truncate pr-2">
                                        <div class="font-bold text-slate-700 truncate text-sm">{{ item.decoration.name }}</div>
                                        <div class="text-[10px] text-slate-400 truncate">{{ item.decoration.description }}</div>
                                    </div>
                                    <div v-if="userData.equippedDecorationClass === item.decoration.cssClass" class="shrink-0 w-6 h-6 bg-violet-500 text-white rounded-full flex items-center justify-center shadow-sm">
                                        <Check :size="14" stroke-width="3" />
                                    </div>
                                </div>
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        </Teleport>


        <!-- 우측 컬럼: 설정 폼 -->
        <div class="lg:col-span-8 space-y-8">
            
            <!-- 프로필 수정 -->
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
                            readonly
                            class="w-full bg-transparent border-none p-0 font-bold text-slate-700 focus:outline-none cursor-default"
                        />
                    </div>
                    <!-- Email Field Removed -->

                </div>
            </section>

             <!-- 저장소 연동 -->
            <section class="bg-white rounded-3xl p-6 md:p-8 shadow-sm">
                <div class="flex items-center justify-between mb-6">
                    <h2 class="text-xl font-bold text-slate-800 flex items-center gap-2">
                        <ZapIcon :size="20" class="text-slate-400"/>
                        계정 연동
                    </h2>
                    <button 
                        @click="showFaq = true"
                        class="text-xs font-bold text-slate-400 hover:text-slate-600 flex items-center gap-1.5 px-3 py-1.5 bg-slate-50 hover:bg-slate-100 rounded-lg transition-colors"
                    >
                        <HelpCircle class="w-3.5 h-3.5" />
                        연동 문제 해결
                    </button>
                </div>
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
                        <!-- 인증 가이드 -->
                        <div class="mt-3 p-4 bg-slate-50 rounded-xl border border-slate-200">
                            <h4 class="text-xs font-bold text-slate-500 mb-2 flex items-center gap-1.5">
                                <span class="w-1.5 h-1.5 rounded-full bg-slate-400"></span>
                                본인 인증이 필요합니다
                            </h4>
                            <p class="text-xs text-slate-400 leading-relaxed mb-3">
                                타인 도용 방지를 위해, 
                                <a :href="`https://solved.ac/profile/${userData.solvedacHandle}`" target="_blank" class="text-emerald-600 hover:underline font-bold">Solved.ac</a>
                                로그인 후 '프로필 편집' 버튼을 눌러 아래 코드를 입력해주세요.
                            </p>
                            <div 
                                @click="copyVerificationCode"
                                class="flex items-center justify-between bg-white border border-slate-200 rounded-lg p-2.5 cursor-pointer hover:border-emerald-500 hover:text-emerald-600 transition-all group"
                            >
                                <code class="font-mono text-sm font-bold text-slate-700 group-hover:text-emerald-600">DashHub:{{ userData.username }}</code>
                                <Copy class="w-4 h-4 text-slate-400 group-hover:text-emerald-500" />
                            </div>
                        </div>
                    </div>

                     <!-- Github -->
                     <div>
                         <label class="block text-xs font-bold text-slate-400 mb-2 uppercase flex items-center gap-2">
                            <Github :size="14" />
                            Github Repository
                         </label>
                        <div class="flex gap-3">
                            <div 
                                class="flex-1 bg-slate-100 border-2 border-slate-200 rounded-xl px-4 py-3 font-bold text-slate-500 flex items-center"
                            >
                                <span v-if="userData.repositoryName">{{ userData.repositoryName }}</span>
                                <span v-else class="text-slate-400 font-normal">연결된 저장소 없음</span>
                            </div>
                            <button 
                                @click="handleRedetect"
                                :disabled="syncingRepo"
                                class="px-4 bg-slate-800 hover:bg-slate-700 text-white rounded-xl font-bold transition-colors disabled:opacity-50 shrink-0 flex items-center gap-2"
                            >
                                <Loader2 v-if="syncingRepo" class="animate-spin w-4 h-4" />
                                <RotateCcw v-else class="w-4 h-4" />
                                <span class="hidden md:inline">{{ syncingRepo ? '감지 중...' : '재탐지' }}</span>
                                <span class="md:hidden">재탐지</span>
                            </button>
                        </div>

                         <div v-if="userData.webhookConfigured" class="mt-2 text-xs font-bold text-emerald-600 flex items-center gap-1.5 px-1">
                            <span class="relative flex h-2 w-2">
                              <span class="animate-ping absolute inline-flex h-full w-full rounded-full bg-emerald-400 opacity-75"></span>
                              <span class="relative inline-flex rounded-full h-2 w-2 bg-emerald-500"></span>
                            </span>
                            웹훅이 정상적으로 연결되었습니다
                        </div>
                        <div v-else class="mt-2 text-xs text-slate-400 px-1">
                            * 저장소 변경은 대시허브 익스텐션에서 설정 후 '재탐지'를 눌러주세요.
                        </div>
                    </div>
                </div>
            </section>
            
            <!-- Danger Zone (Mobile only) - Moved to bottom -->
            <!-- Danger Zone (Collapsible) -->
            <section class="border rounded-3xl overflow-hidden transition-all duration-300" 
                     :class="showDangerZone ? 'border-rose-200 bg-rose-50/30' : 'border-slate-100 bg-white hover:border-slate-200'">
                
                <button 
                    @click="showDangerZone = !showDangerZone"
                    class="w-full flex items-center justify-between p-6 md:p-8 text-left focus:outline-none"
                    :class="{ 'pb-2': showDangerZone }"
                >
                    <div class="flex items-center gap-2">
                         <div class="p-2 rounded-xl transition-colors" :class="showDangerZone ? 'bg-rose-100 text-rose-600' : 'bg-slate-50 text-slate-400'">
                            <AlertTriangle :size="20" />
                         </div>
                         <div>
                             <h2 class="text-lg font-bold transition-colors" :class="showDangerZone ? 'text-rose-600' : 'text-slate-600'">Danger Zone</h2>
                             <p v-if="!showDangerZone" class="text-xs text-slate-400 font-medium">회원 탈퇴 등 되돌릴 수 없는 작업</p>
                         </div>
                    </div>
                    <ChevronDown :size="20" class="text-slate-400 transition-transform duration-300" :class="{ 'rotate-180': showDangerZone }" />
                </button>

                <div v-show="showDangerZone" class="px-6 md:px-8 pb-8 pt-2 animate-in slide-in-from-top-2 fade-in duration-200">
                    <div class="bg-white rounded-2xl p-5 flex flex-col md:flex-row items-center justify-between gap-4 border border-rose-100 shadow-sm">
                        <div>
                            <h3 class="font-bold text-slate-800 mb-1 flex items-center gap-2">
                                회원 탈퇴
                                <span class="text-[10px] px-1.5 py-0.5 rounded bg-rose-100 text-rose-600 font-bold uppercase tracking-wider">Irreversible</span>
                            </h3>
                            <p class="text-xs text-slate-500 font-medium leading-relaxed">
                                계정의 모든 데이터(미션, 풀이 기록 등)가 영구적으로 삭제됩니다.<br>
                                삭제된 계정은 복구할 수 없습니다.
                            </p>
                        </div>
                        <button 
                            @click="handleDelete"
                            class="w-full md:w-auto px-5 py-3 bg-rose-50 border border-rose-200 text-rose-600 font-bold rounded-xl hover:bg-rose-600 hover:text-white transition-all shadow-sm shrink-0 whitespace-nowrap text-sm"
                        >
                            탈퇴하기
                        </button>
                    </div>
                </div>
            </section>
        </div>
      </div>

    <!-- 전역 모달 -->
    <TroubleshootingModal :isOpen="showFaq" @close="showFaq = false" />
    </main>
  </div>
</template>

<style scoped>
@import url('https://cdn.jsdelivr.net/gh/orioncactus/pretendard/dist/web/static/pretendard.css');
</style>
