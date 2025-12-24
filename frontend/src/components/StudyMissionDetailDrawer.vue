<template>
  <div v-if="isOpen" class="fixed inset-0 z-50 flex justify-end">
    <!-- Backdrop -->
    <div class="absolute inset-0 bg-black/30 backdrop-blur-sm transition-opacity" @click="closeDrawer"></div>

    <!-- Drawer Panel -->
    <div class="relative w-full max-w-2xl h-full bg-white shadow-2xl flex flex-col animate-in slide-in-from-right duration-300"
         :class="{ 'opacity-90': localMission.status === 'COMPLETED' }">
      
      <!-- Header -->
      <div class="px-6 py-5 border-b border-slate-100 flex items-center justify-between"
           :class="localMission.status === 'COMPLETED' ? 'bg-slate-100' : 'bg-slate-50/50'">
        <div>
           <div class="flex items-center gap-2 mb-1">
             <span class="px-2 py-0.5 rounded textxs font-bold bg-emerald-100 text-emerald-700">
                Week {{ mission.week }}
             </span>
             <span v-if="localMission.sourceType === 'AI_RECOMMENDED'" class="px-2 py-0.5 rounded text-[10px] font-bold bg-purple-100 text-purple-700">
                AI 추천
             </span>
             <span v-if="localMission.status === 'COMPLETED'" class="px-2 py-0.5 rounded text-[10px] font-bold bg-slate-100 text-slate-500 border border-slate-200">
                종료됨
             </span>
           </div>
           
           <!-- Title Editing -->
           <div v-if="isEditing" class="flex items-center gap-2">
              <input v-model="editForm.title" type="text" 
                     class="px-2 py-1 border rounded text-lg font-bold w-full focus:ring-2 focus:ring-emerald-500" />
           </div>
           <h2 v-else class="text-xl font-bold text-slate-800 flex items-center gap-2">
             {{ localMission.title }}
             <button v-if="isLeader" @click="startEditing" class="text-slate-400 hover:text-emerald-600 transition-colors">
               <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                 <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15.232 5.232l3.536 3.536m-2.036-5.036a2.5 2.5 0 113.536 3.536L6.5 21.036H3v-3.572L16.732 3.732z" />
               </svg>
             </button>
           </h2>
        </div>
        
        <button @click="closeDrawer" class="p-2 hover:bg-slate-100 rounded-full text-slate-400 transition-colors">
          <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
          </svg>
        </button>
      </div>

      <!-- Scrollable Content -->
      <div class="flex-1 overflow-y-auto p-6 space-y-8">
        
        <!-- Deadline Section -->
        <div class="bg-slate-50 rounded-xl p-4 border border-slate-100 flex items-center justify-between">
           <div class="flex items-center gap-3">
              <div class="w-10 h-10 rounded-full bg-brand-100 flex items-center justify-center text-brand-600">
                 <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                 </svg>
              </div>
              <div>
                 <p class="text-xs text-slate-500 font-bold uppercase">마감일</p>
                 <div v-if="isEditing">
                    <input v-model="editForm.deadline" type="date" class="border rounded px-2 py-1 text-sm font-semibold" />
                 </div>
                 <p v-else class="text-slate-800 font-bold">{{ formatDate(localMission.deadline) }} ({{ getDDay(localMission.deadline) }})</p>
              </div>
           </div>
           
           <div v-if="isEditing" class="flex gap-2">
              <button @click="cancelEditing" class="text-xs px-3 py-1.5 rounded-lg border bg-white font-bold text-slate-500">취소</button>
              <button @click="saveMissionInfo" class="text-xs px-3 py-1.5 rounded-lg bg-emerald-500 text-white font-bold hover:bg-emerald-600">저장</button>
           </div>
        </div>

        <!-- 1. Mission Matrix (현황판) -->
        <div>
           <h3 class="text-base font-bold text-slate-800 mb-4 flex items-center gap-2">
              📊 미션 현황판 <span class="text-xs font-normal text-slate-500 bg-slate-100 px-2 py-0.5 rounded-full">Matrix View</span>
           </h3>
           
           <div class="overflow-x-auto border border-slate-200 rounded-xl shadow-sm">
              <table class="w-full text-sm text-left">
                 <thead class="text-xs text-slate-500 uppercase bg-slate-50 border-b border-slate-200">
                    <tr>
                       <th class="px-4 py-3 font-bold w-32 sticky left-0 bg-slate-50 z-10">팀원</th>
                       <!-- Problem Headers -->
                       <th v-for="pid in localMission.problemIds" :key="pid" class="px-4 py-3 text-center min-w-[80px]">
                          <a :href="`https://www.acmicpc.net/problem/${pid}`" target="_blank" class="hover:text-emerald-600 hover:underline">
                            {{ pid }}
                          </a>
                       </th>
                       <th class="px-4 py-3 text-center w-20">진행률</th>
                    </tr>
                 </thead>
                  <tbody class="divide-y divide-slate-100">
                     <tr v-for="member in sortedMemberProgressList" :key="member.userId" 
                         class="hover:bg-slate-50/50"
                         :class="{ 'bg-emerald-50/70': isCurrentUser(member.userId) }">
                       <td class="px-4 py-3 font-medium flex items-center gap-2 sticky left-0 z-10"
                           :class="isCurrentUser(member.userId) ? 'bg-emerald-50/70 text-emerald-800' : 'bg-white text-slate-800'">
                          <img :src="member.avatarUrl" 
                               class="w-6 h-6 rounded-full" 
                               :class="isCurrentUser(member.userId) ? 'ring-2 ring-emerald-400' : ''" />
                          <span class="truncate max-w-[100px]">{{ member.username }}</span>
                       </td>
                       
                       <!-- Problem Cells -->
                        <td v-for="pid in localMission.problemIds" :key="pid" 
                            class="px-4 py-3 text-center group relative">
                           <div v-if="isSolved(member, pid)" class="flex justify-center">
                              <span class="w-6 h-6 rounded-full bg-emerald-100 text-emerald-600 flex items-center justify-center">
                                 <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" viewBox="0 0 20 20" fill="currentColor">
                                    <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd" />
                                 </svg>
                              </span>
                           </div>
                           <div v-else class="flex justify-center items-center min-h-[24px]">
                              <!-- 1. SOS State (Flag) -->
                              <span v-if="isSos(member, pid)" class="cursor-help" title="SOS 요청 중!">
                                 <span class="text-xl animate-pulse">🚩</span>
                              </span>
                              
                              <!-- 2. Empty Dot -->
                              <span v-else class="w-2 h-2 rounded-full bg-slate-200"></span>
                           </div>
                           
                           <!-- 3. SOS Button (호버 시 셀 중앙에 표시) -->
                           <button v-if="isCurrentUser(member.userId) && !isSolved(member, pid) && localMission.status !== 'COMPLETED'" 
                                   @click.stop="toggleSos(pid)"
                                   class="absolute inset-0 m-auto w-7 h-7 rounded-full items-center justify-center shadow-md transition-all z-20 hidden group-hover:flex"
                                   :class="isSos(member, pid) ? 'bg-slate-500 text-white hover:bg-slate-600' : 'bg-white text-rose-500 border border-rose-100 hover:bg-rose-50'"
                                   :title="isSos(member, pid) ? 'SOS 취소' : 'SOS: 도움이 필요해요!'">
                              {{ isSos(member, pid) ? '✕' : '🏳️' }}
                           </button>
                        </td>
                       
                       <td class="px-4 py-3 text-center font-bold"
                            :class="isCurrentUser(member.userId) ? 'text-emerald-700' : 'text-slate-600'">
                           {{ Math.round((member.completedCount / localMission.totalProblems) * 100) }}%
                        </td>
                    </tr>
                 </tbody>
              </table>
           </div>
        </div>

        <!-- 2. Problem Management (Leader Only) -->
        <div v-if="isLeader">
           <h3 class="text-base font-bold text-slate-800 mb-4 flex items-center gap-2">
              🛠️ 문제 관리
           </h3>
           
           <div class="space-y-3">
              <div v-for="pid in localMission.problemIds" :key="pid" 
                   class="flex items-center justify-between p-4 bg-white border border-slate-200 rounded-xl hover:border-slate-300 transition-colors">
                 <div class="flex items-center gap-4">
                    <span class="text-lg font-bold text-slate-700">#{{ pid }}</span>
                    <a :href="`https://www.acmicpc.net/problem/${pid}`" target="_blank" class="text-xs text-brand-500 hover:underline flex items-center gap-1">
                       문제 보기
                       <svg xmlns="http://www.w3.org/2000/svg" class="h-3 w-3" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 6H6a2 2 0 00-2 2v10a2 2 0 002 2h10a2 2 0 002-2v-4M14 4h6m0 0v6m0-6L10 14" />
                       </svg>
                    </a>
                 </div>
                 
                 <button v-if="localMission.status !== 'COMPLETED'" @click="confirmDeleteProblem(pid)" 
                         class="px-3 py-1.5 text-xs font-bold text-rose-500 bg-rose-50 hover:bg-rose-100 rounded-lg transition-colors">
                    삭제
                 </button>
              </div>
              
              <button v-if="localMission.status !== 'COMPLETED'" @click="openAddProblemModal" class="w-full py-3 border-2 border-dashed border-slate-200 rounded-xl text-slate-400 font-bold hover:border-brand-300 hover:text-brand-500 hover:bg-brand-50/50 transition-all flex items-center justify-center gap-2">
                 <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
                 </svg>
                 문제 추가하기
              </button>
           </div>
        </div>

      </div>
      
      <!-- Footer -->
      <div v-if="isLeader" class="p-6 border-t border-slate-100 bg-slate-50 flex justify-between items-center">
         <div class="flex items-center gap-4">
            <span class="text-xs text-slate-400">마지막 업데이트: {{ new Date().toLocaleTimeString() }}</span>
         </div>
         
         <button v-if="isLeader && localMission.status !== 'COMPLETED'" @click="forceCompleteMission" 
                 class="px-4 py-2 bg-slate-800 text-white rounded-lg text-sm font-bold hover:bg-slate-700 transition-colors shadow-lg">
             미션 종료하기
         </button>
         <button v-else-if="localMission.status === 'COMPLETED'" disabled
                 class="px-4 py-2 bg-slate-100 text-slate-400 rounded-lg text-sm font-bold cursor-not-allowed">
             종료된 미션
         </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue';
import axios from 'axios';

const props = defineProps({
  isOpen: Boolean,
  mission: Object,
  studyId: Number,
  isLeader: Boolean,
  currentUserId: Number
});

const emit = defineEmits(['close', 'refresh', 'open-add-modal']);

const localMission = ref({});
const isEditing = ref(false);
const editForm = ref({ title: '', deadline: '' });

// Watchers
watch(() => props.mission, (newVal) => {
   if (newVal) {
      localMission.value = JSON.parse(JSON.stringify(newVal)); // Deep copy
   }
}, { immediate: true, deep: true });

// Computed: 본인을 맨 위에 정렬
const sortedMemberProgressList = computed(() => {
   if (!localMission.value.memberProgressList) return [];
   return [...localMission.value.memberProgressList].sort((a, b) => {
      if (a.userId === props.currentUserId) return -1;
      if (b.userId === props.currentUserId) return 1;
      return 0;
   });
});

// Methods
const closeDrawer = () => {
    isEditing.value = false;
    emit('close');
};

const formatDate = (dateStr) => {
   if (!dateStr) return '-';
   return new Date(dateStr).toLocaleDateString('ko-KR');
};

const getDDay = (dateStr) => {
   if (!dateStr) return '';
   const deadline = new Date(dateStr);
   deadline.setHours(23, 59, 59, 999);
   const now = new Date();
   const diff = deadline - now;
   const days = Math.floor(diff / (1000 * 60 * 60 * 24));
   
   if (days < 0) return '마감됨';
   if (days === 0) return 'D-Day';
   return `D-${days}`;
};

const isCurrentUser = (userId) => {
   return props.currentUserId === userId;
};

const isSolved = (member, problemId) => {
   return member.solvedProblemIds && member.solvedProblemIds.includes(problemId);
};

const isSos = (member, problemId) => {
   return member.sosProblemIds && member.sosProblemIds.includes(problemId);
};

const toggleSos = async (problemId) => {
   try {
      await axios.post(`/api/studies/${props.studyId}/missions/${props.mission.id}/problems/${problemId}/sos`);
      
      // 즉시 UI 반영을 위한 로컬 상태 업데이트
      const myProgress = localMission.value.memberProgressList.find(m => m.userId === props.currentUserId);
      if (myProgress) {
         if (!myProgress.sosProblemIds) myProgress.sosProblemIds = [];
         
         const idx = myProgress.sosProblemIds.indexOf(problemId);
         if (idx > -1) {
             myProgress.sosProblemIds.splice(idx, 1); // Remove
         } else {
             myProgress.sosProblemIds.push(problemId); // Add
         }
      }
      
      emit('refresh'); // 서버 동기화
   } catch (e) {
      alert('SOS 요청 처리에 실패했습니다. ' + (e.response?.data?.message || ''));
   }
};

const startEditing = () => {
   editForm.value = {
      title: localMission.value.title,
      deadline: localMission.value.deadline
   };
   isEditing.value = true;
};

const cancelEditing = () => {
   isEditing.value = false;
};

const saveMissionInfo = async () => {
   try {
      await axios.patch(`/api/studies/${props.studyId}/missions/${props.mission.id}`, {
         title: editForm.value.title,
         deadline: editForm.value.deadline
      });
      
      // Local update & Emit refresh
      localMission.value.title = editForm.value.title;
      localMission.value.deadline = editForm.value.deadline;
      isEditing.value = false;
      emit('refresh');
   } catch (e) {
      alert('미션 수정에 실패했습니다.');
   }
};

const confirmDeleteProblem = async (problemId) => {
   if (!confirm(`정말 문제 #${problemId}를 이 미션에서 삭제하시겠습니까?`)) return;
   
   try {
      await axios.delete(`/api/studies/${props.studyId}/missions/${props.mission.id}/problems/${problemId}`);
      
      // Local update
      localMission.value.problemIds = localMission.value.problemIds.filter(pid => pid !== problemId);
      emit('refresh');
   } catch (e) {
      alert('문제 삭제에 실패했습니다.');
   }
};

const openAddProblemModal = () => {
   // 부모 컴포넌트에 알림 -> StudyMissionCreateModal을 ADD 모드로 열기
   emit('open-add-modal', props.mission.id);
};

const forceCompleteMission = async () => {
   if (!confirm('미션을 강제로 종료하시겠습니까?\n종료 후에는 문제를 추가하거나 SOS를 요청할 수 없습니다.')) return;
   
   try {
      await axios.patch(`/api/studies/${props.studyId}/missions/${props.mission.id}/status`, {
         status: 'COMPLETED'
      });
      
      localMission.value.status = 'COMPLETED';
      emit('refresh');
   } catch (e) {
      alert('미션 종료 처리에 실패했습니다.');
   }
};
</script>

<style scoped>
/* Scrollbar Styling */
::-webkit-scrollbar {
  width: 6px;
}
::-webkit-scrollbar-track {
  background: transparent; 
}
::-webkit-scrollbar-thumb {
  background-color: #e2e8f0; 
  border-radius: 20px;
}
::-webkit-scrollbar-thumb:hover {
  background-color: #cbd5e1; 
}
</style>
