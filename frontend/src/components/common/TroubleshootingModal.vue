<template>
  <Transition name="fade">
    <div v-if="isOpen" class="fixed inset-0 z-50 flex items-center justify-center p-4">
      <!-- Backdrop -->
      <div class="absolute inset-0 bg-slate-900/40 backdrop-blur-sm" @click="close"></div>

      <!-- Modal -->
      <div class="bg-white rounded-3xl w-full max-w-lg shadow-2xl relative z-10 overflow-hidden flex flex-col max-h-[80vh]">
        
        <!-- Header -->
        <div class="p-6 border-b border-slate-100 flex items-center justify-between bg-slate-50">
          <h3 class="text-xl font-bold text-slate-800 flex items-center gap-2">
            <HelpCircle class="w-6 h-6 text-brand-500" />
            자주 묻는 질문 (FAQ)
          </h3>
          <button @click="close" class="p-2 hover:bg-slate-200 rounded-full transition-colors text-slate-500">
            <X class="w-5 h-5" />
          </button>
        </div>

        <!-- Content -->
        <div class="overflow-y-auto p-6 space-y-4">
            
            <!-- Q1: Dashboard Not Updating -->
            <div class="border border-slate-200 rounded-2xl overflow-hidden">
                <button 
                    @click="toggle(1)"
                    class="w-full flex items-center justify-between p-4 bg-white hover:bg-slate-50 transition-colors text-left"
                >
                    <span class="font-bold text-slate-800 text-sm">Q. 대시보드 데이터가 갱신되지 않아요.</span>
                    <ChevronDown :class="{'rotate-180': openId === 1}" class="w-5 h-5 text-slate-400 transition-transform" />
                </button>
                <div v-if="openId === 1" class="p-5 bg-slate-50 border-t border-slate-100 text-sm text-slate-600 leading-relaxed space-y-3">
                    <p>대시허브는 <strong>크롬 익스텐션</strong>을 통해 작동합니다. 익스텐션이 삭제되었거나 연결이 끊어졌을 수 있습니다.</p>
                    
                    <div class="bg-white p-4 rounded-xl border border-slate-200 shadow-sm space-y-2">
                        <h4 class="font-bold text-slate-800 flex items-center gap-2">
                            <AlertTriangle class="w-4 h-4 text-amber-500" />
                            익스텐션을 실수로 삭제하셨나요?
                        </h4>
                        <ol class="list-decimal pl-5 space-y-1 text-slate-600 marker:font-bold marker:text-slate-400">
                            <li><strong>크롬 스토어</strong>에서 DashHub를 다시 설치해주세요.</li>
                            <li>익스텐션 팝업에서 <strong>GitHub 인증</strong>을 완료하세요.</li>
                            <li>
                                <strong>저장소 설정</strong> 단계에서:
                                <ul class="list-disc pl-4 mt-1 text-slate-500">
                                    <li><span class="text-slate-700 font-bold">기존 리포지토리 연결</span>을 선택하고,</li>
                                    <li>현재 서비스에 등록된 저장소(예: <code>my-algo</code>)를 입력해주세요.</li>
                                </ul>
                            </li>
                            <li>모든 설정 후, 마이페이지의 <strong>"재탐지"</strong> 버튼을 눌러 동기화하세요.</li>
                        </ol>
                    </div>
                    <p class="text-xs text-slate-400">
                        * 만약 새로운 저장소를 만들고 싶다면, 익스텐션에서 '새 저장소 생성' 후 재탐지하시면 자동으로 변경됩니다.
                    </p>
                </div>
            </div>

            <!-- Q2: Change Repo -->
            <div class="border border-slate-200 rounded-2xl overflow-hidden">
                <button 
                    @click="toggle(2)"
                    class="w-full flex items-center justify-between p-4 bg-white hover:bg-slate-50 transition-colors text-left"
                >
                    <span class="font-bold text-slate-800 text-sm">Q. 연동된 저장소를 변경하고 싶어요.</span>
                    <ChevronDown :class="{'rotate-180': openId === 2}" class="w-5 h-5 text-slate-400 transition-transform" />
                </button>
                <div v-if="openId === 2" class="p-5 bg-slate-50 border-t border-slate-100 text-sm text-slate-600 leading-relaxed">
                    <p>보안을 위해 웹사이트에서는 저장소를 직접 변경할 수 없습니다.</p>
                    <ol class="list-decimal pl-5 mt-2 space-y-1 marker:font-bold marker:text-slate-400">
                        <li>브라우저 우측 상단의 <strong>DashHub 익스텐션 아이콘</strong>을 클릭하세요.</li>
                        <li>팝업에서 '연결 해제' 후 다시 연결 과정을 진행하세요.</li>
                        <li>설정이 완료되면, 마이페이지에서 <strong>"재탐지"</strong> 버튼을 눌러주세요.</li>
                    </ol>
                </div>
            </div>

            <!-- Q3: Other Issues -->
            <div class="border border-slate-200 rounded-2xl overflow-hidden">
                <button 
                    @click="toggle(3)"
                    class="w-full flex items-center justify-between p-4 bg-white hover:bg-slate-50 transition-colors text-left"
                >
                    <span class="font-bold text-slate-800 text-sm">Q. 그 외 다른 문제가 있나요?</span>
                    <ChevronDown :class="{'rotate-180': openId === 3}" class="w-5 h-5 text-slate-400 transition-transform" />
                </button>
                <div v-if="openId === 3" class="p-5 bg-slate-50 border-t border-slate-100 text-sm text-slate-600 leading-relaxed space-y-3">
                    <p>
                        DashHub는 <strong>오픈소스 프로젝트</strong>입니다. 
                        문제가 해결되지 않거나 버그를 발견하셨다면 GitHub 이슈를 남겨주세요.
                    </p>
                    <div class="flex flex-col gap-2 mt-2">
                        <a href="https://github.com/utact/dash/issues" target="_blank" class="flex items-center gap-2 p-3 bg-white border border-slate-200 rounded-xl hover:border-slate-300 hover:shadow-sm transition-all group">
                            <div class="w-8 h-8 rounded-full bg-slate-100 flex items-center justify-center text-slate-600 group-hover:bg-slate-800 group-hover:text-white transition-colors">
                                <AlertTriangle class="w-4 h-4" />
                            </div>
                            <div class="flex-1">
                                <div class="font-bold text-slate-800">이슈 제보하기</div>
                                <div class="text-xs text-slate-400">버그 신고 및 기능 제안</div>
                            </div>
                        </a>
                        
                        <div class="p-4 bg-emerald-50 border border-emerald-100 rounded-xl mt-2">
                            <h4 class="font-bold text-emerald-800 text-sm mb-1">🚀 개발자 준비생 여러분을 환영합니다!</h4>
                            <p class="text-xs text-emerald-700 leading-relaxed mb-3">
                                DashHub와 함께 성장하고 싶으신가요?<br>
                                여러분의 소중한 기여(PR)를 언제나 기다리고 있습니다.
                            </p>
                            <a href="https://github.com/utact/dash" target="_blank" class="inline-flex text-xs font-bold text-white bg-emerald-600 px-3 py-1.5 rounded-lg hover:bg-emerald-500 transition-colors">
                                GitHub 저장소 구경가기
                            </a>
                        </div>
                    </div>
                </div>
            </div>

        </div>

        <!-- Footer -->
        <div class="p-4 bg-slate-50 border-t border-slate-100 text-center">
            <button @click="close" class="px-6 py-2.5 bg-slate-900 text-white font-bold rounded-xl text-sm hover:bg-slate-800 transition-colors">
                닫기
            </button>
        </div>
      </div>
    </div>
  </Transition>
</template>

<script setup>
import { ref } from 'vue';
import { HelpCircle, X, ChevronDown, AlertTriangle } from 'lucide-vue-next';

const props = defineProps({
  isOpen: Boolean
});

const emit = defineEmits(['close']);

const openId = ref(1); // Default open first item

const toggle = (id) => {
    openId.value = openId.value === id ? null : id;
};

const close = () => {
    emit('close');
};
</script>

<style scoped>
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
