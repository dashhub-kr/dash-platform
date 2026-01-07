<template>
  <div class="bg-white rounded-2xl p-6 border border-slate-200 shadow-sm animate-in slide-in-from-right duration-300">
    <!-- 1. 미션 현황판 -->
    <div class="mb-8">
       <div class="flex items-center justify-between mb-4">
         <h4 class="font-bold text-slate-700 text-sm flex items-center gap-2">
            📊 미션 현황판
         </h4>
       </div>
       
       <div class="overflow-x-auto relative">
          <table class="w-full text-xs text-left">
             <thead class="text-xs text-slate-500 uppercase">
                <tr>
                   <th class="px-2 py-2 font-bold w-12 sticky left-0 bg-white z-10 text-center">팀원</th>
                   <th v-for="pid in mission.problemIds" :key="pid" class="px-2 py-2 text-center min-w-[40px]">
                      <a :href="`https://www.acmicpc.net/problem/${pid}`" target="_blank" class="hover:text-emerald-600 hover:underline font-bold">
                        {{ pid }}
                      </a>
                   </th>
                   <th class="px-2 py-2 text-center w-12 font-bold text-[10px]">진행률</th>
                </tr>
             </thead>
              <tbody class="divide-y divide-slate-50">
                 <tr v-for="member in sortedMemberProgressList" :key="member.userId" 
                     class="hover:bg-slate-50/50"
                     :class="{ 'bg-emerald-50/30': isCurrentUser(member.userId) }">
                   
                   <!-- 팀원 이름 (아바타만 표시) -->
                   <td class="px-2 py-2 font-medium flex justify-center sticky left-0 z-10 group relative"
                       :class="isCurrentUser(member.userId) ? 'bg-emerald-50/30' : 'bg-white'">
                      <div class="relative">
                        <NicknameRenderer 
                            :username="member.username"
                            :avatar-url="member.avatarUrl"
                            avatar-class="w-8 h-8 font-bold border border-slate-100"
                            text-class="hidden"
                            :icon-size="16"
                        />
                        
                        <div v-if="member.completedCount === member.totalProblems" class="absolute -top-1 -right-1 text-rose-500 drop-shadow-sm">
                            <span class="text-[10px]">👑</span>
                        </div>

                        <!-- 커스텀 툴팁 (우측) -->
                        <div class="absolute left-full top-1/2 -translate-y-1/2 ml-3 px-2 py-1 bg-slate-800 text-white text-[10px] font-medium rounded shadow-xl z-50 whitespace-nowrap opacity-0 group-hover:opacity-100 transition-opacity pointer-events-none">
                            <NicknameRenderer 
                                :username="member.username"
                                :show-avatar="false"
                                text-class="text-white"
                            />
                            <div class="absolute top-1/2 -translate-y-1/2 -left-1 w-2 h-2 bg-slate-800 rotate-45"></div>
                        </div>
                      </div>
                   </td>
                   
                   <!-- 문제 셀 -->
                    <td v-for="pid in mission.problemIds" :key="pid" 
                        class="px-1 py-2 text-center group relative h-10"> <!-- 정렬을 위한 고정 높이 -->
                       
                       <div class="flex items-center justify-center w-full h-full">
                           <!-- 해결됨 -->
                           <div v-if="isSolved(member, pid)" class="flex justify-center">
                              <span class="w-5 h-5 rounded-full bg-emerald-100 text-emerald-600 flex items-center justify-center">
                                 <CheckIcon class="w-3 h-3" stroke-width="3" />
                              </span>
                           </div>
                           
                           <!-- 미해결 -->
                           <div v-else class="flex justify-center items-center">
                               <!-- SOS 상태 -->
                               <div v-if="isSos(member, pid)" class="cursor-help animate-pulse" title="SOS 요청 중!">
                                  <FlagIcon class="w-4 h-4 text-rose-500 fill-rose-100" />
                               </div>
                               
                               <!-- 빈 점 -->
                               <span v-else class="w-1.5 h-1.5 rounded-full bg-slate-200"></span>
                           </div>
                       </div>
                       
                       <!-- SOS 버튼 오버레이 -->
                       <button v-if="isCurrentUser(member.userId) && !isSolved(member, pid) && mission.status !== 'COMPLETED'" 
                               @click.stop="toggleSos(pid)"
                               class="absolute inset-0 m-auto w-6 h-6 rounded-full items-center justify-center shadow-sm z-20 hidden group-hover:flex transition-all"
                               :class="isSos(member, pid) ? 'bg-slate-700 text-white' : 'bg-white text-rose-500 border border-rose-100'"
                               :title="isSos(member, pid) ? 'SOS 취소' : 'SOS 요청'">
                          <XIcon v-if="isSos(member, pid)" class="w-3 h-3" />
                          <FlagIcon v-else class="w-3 h-3" />
                       </button>
                    </td>
                   
                   <!-- 진행률 -->
                   <td class="px-2 py-2 text-center font-bold text-[10px]"
                        :class="isCurrentUser(member.userId) ? 'text-emerald-600' : 'text-slate-400'">
                       {{ Math.round((member.completedCount / Math.max(mission.totalProblems, 1)) * 100) }}%
                    </td>
                </tr>
             </tbody>
          </table>
       </div>
    </div>

    <!-- 2. 문제 목록 및 관리 -->
    <div v-if="isLeader">
       <h4 class="font-bold text-slate-700 text-sm mb-4 flex items-center gap-2">
          🛠️ 문제 관리
       </h4>
       
       <div class="space-y-2">
          <div v-for="pid in mission.problemIds" :key="pid" 
               class="flex items-center justify-between p-3 bg-slate-50 rounded-xl border border-slate-100 hover:border-slate-200 transition-colors group">
             <div class="flex items-center gap-3">
                <span class="text-sm font-bold text-slate-700">#{{ pid }}</span>
                <a :href="`https://www.acmicpc.net/problem/${pid}`" target="_blank" class="text-[10px] text-slate-400 hover:text-brand-500 hover:underline flex items-center gap-1">
                   백준
                   <ExternalLinkIcon class="w-3 h-3" />
                </a>
             </div>
             
             <button v-if="isLeader && mission.status !== 'COMPLETED'" @click="confirmDeleteProblem(pid)" 
                     class="opacity-0 group-hover:opacity-100 px-2 py-1 text-[10px] font-bold text-rose-500 bg-white hover:bg-rose-50 rounded border border-rose-100 transition-all">
                삭제
             </button>
          </div>
          
          <div v-if="isAddingProblem" class="flex items-center gap-2 p-1 bg-white rounded-xl border border-brand-200 shadow-sm animate-in fade-in zoom-in-95 duration-200">
              <span class="pl-2 text-xs font-bold text-slate-500">#</span>
              <input 
                  ref="inputRef"
                  v-model="newProblemId"
                  @keyup.enter="submitAddProblem"
                  type="text" 
                  placeholder="문제 번호" 
                  class="flex-1 w-full text-sm font-bold text-slate-700 outline-none placeholder:text-slate-300 placeholder:font-normal bg-transparent py-1.5"
              />
              <button @click="submitAddProblem" class="p-1.5 bg-brand-500 text-white rounded-lg hover:bg-brand-600 transition-colors">
                  <CheckIcon class="w-4 h-4" stroke-width="3" />
              </button>
              <button @click="cancelAdding" class="p-1.5 text-slate-400 hover:text-slate-600 hover:bg-slate-100 rounded-lg transition-colors">
                  <XIcon class="w-4 h-4" />
              </button>
          </div>

          <button v-else-if="isLeader && mission.status !== 'COMPLETED'" @click="startAdding" 
                  class="w-full py-2.5 border border-dashed border-slate-300 rounded-xl text-slate-400 text-xs font-bold hover:border-brand-300 hover:text-brand-500 hover:bg-brand-50/50 transition-all flex items-center justify-center gap-1">
             <PlusIcon class="w-4 h-4" />
             문제 추가
          </button>
       </div>
    </div>
  </div>
</template>

<script setup>
import { computed, ref, nextTick } from 'vue';
import { 
    X as XIcon, 
    Check as CheckIcon, 
    Flag as FlagIcon, 
    ExternalLink as ExternalLinkIcon,
    Plus as PlusIcon
} from 'lucide-vue-next';
import NicknameRenderer from '@/components/common/NicknameRenderer.vue';
import axios from 'axios';

const props = defineProps({
  mission: {
    type: Object,
    required: true
  },
  currentUserId: Number,
  studyId: Number,
  isLeader: Boolean
});

const emit = defineEmits(['close', 'refresh', 'open-add-modal']);

const isAddingProblem = ref(false);
const newProblemId = ref('');
const inputRef = ref(null);

const sortedMemberProgressList = computed(() => {
   if (!props.mission.memberProgressList) return [];
   return [...props.mission.memberProgressList].sort((a, b) => {
      if (a.userId === props.currentUserId) return -1;
      if (b.userId === props.currentUserId) return 1;
      return 0;
   });
});

const isCurrentUser = (userId) => props.currentUserId === userId;

const isSolved = (member, problemId) => {
   return member.solvedProblemIds && member.solvedProblemIds.includes(problemId);
};

const isSos = (member, problemId) => {
   return member.sosProblemIds && member.sosProblemIds.includes(problemId);
};

const toggleSos = async (problemId) => {
   try {
      await axios.post(`/api/studies/${props.studyId}/missions/${props.mission.id}/problems/${problemId}/sos`);
      emit('refresh'); // Refresh parent data
   } catch (e) {
      alert('SOS 요청 처리에 실패했습니다.');
   }
};

const confirmDeleteProblem = async (problemId) => {
   if (!confirm(`정말 문제 #${problemId}를 이 미션에서 삭제하시겠습니까?`)) return;
   
   try {
      await axios.delete(`/api/studies/${props.studyId}/missions/${props.mission.id}/problems/${problemId}`);
      emit('refresh');
   } catch (e) {
      alert('문제 삭제에 실패했습니다.');
   }
};

const startAdding = async () => {
    isAddingProblem.value = true;
    newProblemId.value = '';
    await nextTick();
    if (inputRef.value) inputRef.value.focus();
};

const cancelAdding = () => {
    isAddingProblem.value = false;
    newProblemId.value = '';
};

const submitAddProblem = async () => {
    if (!newProblemId.value) return;
    
    // 간단한 유효성 검사 (숫자 확인)
    const pid = parseInt(newProblemId.value);
    if (isNaN(pid)) {
        alert('유효한 문제 번호를 입력해주세요.');
        return;
    }

    try {
        await axios.put(`/api/studies/${props.studyId}/missions/${props.mission.id}/problems`, {
            problemIds: [pid]
        });
        emit('refresh');
        isAddingProblem.value = false;
        newProblemId.value = '';
    } catch (e) {
        console.error('문제 추가 실패', e);
        alert('문제 추가 중 오류가 발생했습니다.');
    }
};
</script>
