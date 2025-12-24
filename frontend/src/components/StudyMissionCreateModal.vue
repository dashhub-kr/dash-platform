<template>
  <div v-if="isOpen" class="fixed inset-0 bg-black/50 backdrop-blur-sm flex items-center justify-center p-4 z-50">
    <div class="bg-white rounded-3xl w-full max-w-2xl shadow-2xl overflow-hidden animate-in fade-in zoom-in duration-200">
      
      <!-- 헤더 -->
      <div class="bg-gradient-to-r from-emerald-600 to-teal-600 p-8 text-white relative overflow-hidden">
        <div class="absolute top-0 right-0 w-32 h-32 bg-white/10 rounded-full blur-2xl translate-x-10 -translate-y-10"></div>
        <h2 class="text-2xl font-bold mb-2 relative z-10">미션 등록</h2>
        <p class="text-emerald-100 text-sm mt-1 relative z-10">스터디원들을 위한 주차별 과제를 등록하세요</p>
      </div>
      
      <!-- 안내 메시지 및 모드 표시 (탭 제거) -->
      <div v-if="creationMode === 'ADD'" class="bg-brand-50 px-8 py-3 border-b border-brand-100 flex items-center justify-between">
         <span class="text-brand-800 font-bold text-sm flex items-center gap-2">
            💡 진행 중인 미션에 문제를 추가합니다
            <span v-if="initialProblemIds" class="px-2 py-0.5 bg-brand-200 text-brand-800 text-xs rounded-full">자동 선택됨</span>
         </span>
      </div>
      <div v-else class="bg-emerald-50 px-8 py-3 border-b border-emerald-100 flex items-center justify-between">
         <span class="text-emerald-800 font-bold text-sm">✨ 새로운 주차 미션을 생성합니다</span>
      </div>
      
      <div class="p-6 space-y-5">
        <!-- 모드별 UI -->
        <template v-if="creationMode === 'NEW'">
            <!-- 주차 및 제목 Row -->
            <div class="grid grid-cols-4 gap-4">
              <div>
                <label class="block text-xs font-bold text-slate-500 mb-2 uppercase tracking-wider">주차</label>
                <input v-model.number="newMission.week" type="number" min="1"
                       class="w-full px-4 py-3 bg-slate-50 border-2 border-slate-200 rounded-xl focus:ring-2 focus:ring-emerald-500 focus:border-emerald-500 text-center font-bold text-lg transition-all"
                       placeholder="1" />
              </div>
              <div class="col-span-3">
                <label class="block text-xs font-bold text-slate-500 mb-2 uppercase tracking-wider">미션 제목</label>
                <input v-model="newMission.title" type="text"
                       class="w-full px-4 py-3 bg-slate-50 border-2 border-slate-200 rounded-xl focus:ring-2 focus:ring-emerald-500 focus:border-emerald-500 transition-all"
                       placeholder="예: DP 기초 다지기" />
              </div>
            </div>
        </template>

        <template v-if="creationMode === 'ADD'">
            <div>
                <label class="block text-xs font-bold text-slate-500 mb-2 uppercase tracking-wider">추가할 미션 선택</label>
                <select v-model="selectedMissionId" 
                        :disabled="!!initialProblemIds"
                        class="w-full px-4 py-3 bg-slate-50 border-2 border-slate-200 rounded-xl focus:ring-2 focus:ring-brand-500 focus:border-brand-500 font-bold text-slate-700 transition-all appearance-none cursor-pointer disabled:bg-slate-100 disabled:text-slate-400 disabled:cursor-not-allowed">
                    <option :value="null" disabled>미션을 선택하세요</option>
                    <option v-for="mission in activeMissions" :key="mission.id" :value="mission.id">
                        [#{{ mission.week }}] {{ mission.title }} (마감: {{ formatDate(mission.deadline) }})
                    </option>
                </select>
                <p v-if="activeMissions.length === 0" class="text-xs text-rose-500 mt-2 font-medium">
                    ⚠️ 현재 진행 중인 미션이 없습니다. 새 미션을 만들어보세요!
                </p>
            </div>
        </template>
        
        <!-- 문제 번호 (동적 추가 방식) -->
        <div>
          <label class="block text-xs font-bold text-slate-500 mb-2 uppercase tracking-wider">📝 추가할 문제 번호</label>
          
          <div class="space-y-3">
             <div v-for="(input, index) in problemInputs" :key="index" class="flex gap-2 animate-in fade-in slide-in-from-left-2 duration-200">
                <div class="relative flex-1">
                   <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                      <span class="text-slate-400 font-bold">#</span>
                   </div>
                   <input v-model="input.value" type="text"
                          :readonly="!!initialProblemIds"
                          class="w-full pl-8 pr-4 py-3 bg-slate-50 border-2 border-slate-200 rounded-xl focus:ring-2 transition-all read-only:bg-slate-100 read-only:text-slate-500"
                          :class="creationMode === 'NEW' ? 'focus:ring-emerald-500 focus:border-emerald-500' : 'focus:ring-brand-500 focus:border-brand-500'"
                          placeholder="문제 번호 입력 (예: 1000)"
                          @keydown.enter.prevent="addProblemInput" />
                </div>
                
                <button v-if="!initialProblemIds && problemInputs.length > 1" 
                        @click="removeProblemInput(index)"
                        class="px-3 text-slate-400 hover:text-rose-500 hover:bg-rose-50 rounded-lg transition-colors"
                        title="삭제">
                   <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                   </svg>
                </button>
             </div>
          </div>

          <button v-if="!initialProblemIds" 
                  @click="addProblemInput"
                  class="mt-3 w-full py-3 border-2 border-dashed border-slate-200 rounded-xl text-slate-500 font-bold hover:border-brand-300 hover:text-brand-500 hover:bg-brand-50/50 transition-all flex items-center justify-center gap-2">
             <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
             </svg>
             문제 추가하기
          </button>
        </div>
        
        <!-- 마감일 (NEW 모드일 때만) -->
        <div v-if="creationMode === 'NEW'">
          <label class="block text-xs font-bold text-slate-500 mb-2 uppercase tracking-wider">⏰ 마감일</label>
          <input v-model="newMission.deadline" type="date"
                 class="w-full px-4 py-3 bg-slate-50 border-2 border-slate-200 rounded-xl focus:ring-2 focus:ring-emerald-500 focus:border-emerald-500 transition-all" />
        </div>
      </div>
      
      <!-- Modal 푸터 -->
      <div class="flex gap-3 p-6 bg-slate-50 border-t border-slate-100">
        <button @click="closeModal"
                class="flex-1 py-3 border-2 border-slate-200 text-slate-600 rounded-xl font-bold hover:bg-slate-100 transition-all">
          취소
        </button>
        <button @click="handleCreateOrUpdate"
                class="flex-1 py-3 text-white rounded-xl font-bold shadow-lg transition-all"
                :disabled="isSubmitting"
                :class="creationMode === 'NEW' ? 'bg-gradient-to-r from-emerald-500 to-teal-500 hover:from-emerald-600 hover:to-teal-600 shadow-emerald-500/25' : 'bg-gradient-to-r from-brand-500 to-violet-500 hover:from-brand-600 hover:to-violet-600 shadow-brand-500/25'">
          {{ isSubmitting ? '처리 중...' : (creationMode === 'NEW' ? '✨ 미션 생성' : '➕ 문제 추가하기') }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue';
import axios from 'axios';

const props = defineProps({
  isOpen: Boolean,
  studyId: Number,
  initialProblemIds: String,
  initialTitle: String,
  preSelectedMissionId: Number,
  missions: {
    type: Array,
    default: () => []
  }
});

const emit = defineEmits(['close', 'refresh']);

// 상태
const creationMode = ref('NEW'); // 'NEW' | 'ADD'
const selectedMissionId = ref(null);
const isSubmitting = ref(false);

const newMission = ref({
  week: 1,
  title: '',
  deadline: ''
});
// const problemIdsInput = ref(''); // Legacy
const problemInputs = ref([{ value: '' }]); // Dynamic inputs

// Computed
const activeMissions = computed(() => {
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    return props.missions.filter(m => {
        const deadline = new Date(m.deadline);
        deadline.setHours(23, 59, 59, 999);
        return deadline >= today;
    }).sort((a, b) => b.week - a.week); // 최신순
});

// Watchers
watch(() => props.isOpen, (val) => {
  if (val) {
    initializeForm();
  }
});

// Methods
const formatDate = (dateStr) => {
  if (!dateStr) return '-';
  // 시간대 영향 없이 YYYY-MM-DD 값 그대로 표시
  const date = new Date(dateStr);
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${year}. ${month}. ${day}.`;
};

const initializeForm = () => {
  // 1. props로 전달된 초기값 설정
  if (props.initialProblemIds) {
    // 쉼표로 구분된 문자열을 배열로 변환
    const ids = props.initialProblemIds.split(',').map(s => s.trim()).filter(s => s);
    problemInputs.value = ids.map(id => ({ value: id }));
  } else {
    problemInputs.value = [{ value: '' }];
  }
  
  if (props.initialTitle) {
    newMission.value.title = props.initialTitle;
  } else {
    newMission.value.title = '';
  }

  // 2. 주차 및 마감일 자동 계산
  const maxWeek = props.missions.reduce((max, m) => Math.max(max, m.week || 0), 0);
  newMission.value.week = maxWeek + 1;
  
  // 마감일: 오늘 + 7일 (시간대 영향 최소화)
  const nextWeek = new Date();
  nextWeek.setDate(nextWeek.getDate() + 7);
  
  // YYYY-MM-DD 형식으로 변환 (로컬 시간 기준)
  const offset = nextWeek.getTimezoneOffset() * 60000;
  const localISOTime = new Date(nextWeek.getTime() - offset).toISOString().slice(0, 10);
  
  newMission.value.deadline = localISOTime;
  
  // 3. 모드 자동 설정
  // 진행 중인 미션이 있다면 기본적으로 '기존 미션에 추가' 모드로 설정
  if (activeMissions.value.length > 0) {
    if (props.preSelectedMissionId) {
       selectedMissionId.value = props.preSelectedMissionId;
    } else {
       selectedMissionId.value = activeMissions.value[0].id; // 가장 최신(마감일 안 지난) 미션
    }
    creationMode.value = 'ADD';
  } else {
    creationMode.value = 'NEW';
  }
};

const closeModal = () => {
  emit('close');
};

const handleCreateOrUpdate = async () => {
  try {
    isSubmitting.value = true;
    
    const problemIds = problemInputs.value
      .map(input => parseInt(input.value))
      .filter(n => !isNaN(n));
      
    if (problemIds.length === 0) {
        alert('추가할 문제 번호를 입력해주세요.');
        return;
    }
    
    if (creationMode.value === 'NEW') {
        await axios.post(`/api/studies/${props.studyId}/missions`, {
          week: newMission.value.week,
          title: newMission.value.title,
          problemIds,
          deadline: newMission.value.deadline
        });
    } else {
        if (!selectedMissionId.value) {
            alert('추가할 미션을 선택해주세요.');
            return;
        }
        await axios.put(`/api/studies/${props.studyId}/missions/${selectedMissionId.value}/problems`, {
            problemIds
        });
    }
    
    // 성공 시 초기화 및 닫기
    newMission.value = { week: 1, title: '', deadline: '' };
    problemInputs.value = [{ value: '' }];
    creationMode.value = 'NEW';
    
    emit('refresh');
    closeModal();
    
  } catch (e) {
    console.error('작업 실패', e);
    alert('미션 저장 중 오류가 발생했습니다.');
  } finally {
    isSubmitting.value = false;
  }
};


const addProblemInput = () => {
    problemInputs.value.push({ value: '' });
};

const removeProblemInput = (index) => {
    if (problemInputs.value.length > 1) {
        problemInputs.value.splice(index, 1);
    }
};
</script>
