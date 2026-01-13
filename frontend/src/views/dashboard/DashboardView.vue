<template>
  <!-- 분할 뷰를 위한 Flex 컨테이너 -->
  <div class="flex h-screen overflow-hidden bg-white font-sans">
    
    <!-- 컨텍스트 카드 모드: 드로어가 열려 있을 때 - 선택된 문제만 표시 -->
    <div 
      v-if="showDrawer && currentDrawerRecord"
      class="w-full md:w-[40%] bg-gradient-to-br from-slate-50 to-slate-100 border-r border-slate-200 flex flex-col"
    >
      <div class="flex-1 p-6 flex flex-col">
        <!-- 컨텍스트 헤더 -->
        <div class="mb-6">
          <div class="text-xs font-bold text-slate-400 uppercase tracking-wider mb-2">분석 중인 문제</div>
          <div class="flex items-center gap-2 mb-3">
            <span class="px-2 py-1 bg-brand-100 text-brand-700 text-xs font-bold rounded-lg">#{{ currentDrawerRecord.problemId }}</span>
            <span class="px-2 py-1 bg-slate-200 text-slate-600 text-xs font-bold rounded-lg">{{ currentDrawerRecord.language }}</span>
            <span v-if="currentDrawerRecord.result === 'FAIL'" class="px-2 py-1 bg-red-100 text-red-600 text-xs font-bold rounded-lg">FAILED</span>
            <span v-else class="px-2 py-1 bg-emerald-100 text-emerald-600 text-xs font-bold rounded-lg">PASSED</span>
          </div>
          <h2 class="text-xl font-black text-slate-800 leading-tight">{{ currentDrawerRecord.problemTitle }}</h2>
          <p class="text-sm text-slate-500 mt-1">{{ formatDate(currentDrawerRecord.createdAt) }}</p>
        </div>
        
        <!-- 성능 지표 그리드 -->
        <div class="grid grid-cols-2 gap-3 mb-6">
          <div class="bg-white rounded-xl p-4 border border-slate-100 shadow-sm text-center">
            <div class="text-xs font-bold text-slate-400 uppercase mb-1">실행 시간</div>
            <div class="text-lg font-black text-slate-800">{{ currentDrawerRecord.executionTime || '-' }}<span class="text-sm font-normal text-slate-500">ms</span></div>
          </div>
          <div class="bg-white rounded-xl p-4 border border-slate-100 shadow-sm text-center">
            <div class="text-xs font-bold text-slate-400 uppercase mb-1">메모리</div>
            <div class="text-lg font-black text-slate-800">{{ currentDrawerRecord.memory || '-' }}<span class="text-sm font-normal text-slate-500">MB</span></div>
          </div>
          <div class="bg-white rounded-xl p-4 border border-slate-100 shadow-sm text-center">
            <div class="text-xs font-bold text-slate-400 uppercase mb-1">시간 복잡도</div>
            <div class="text-lg font-black text-brand-600">{{ currentDrawerRecord.timeComplexity || '-' }}</div>
          </div>
          <div class="bg-white rounded-xl p-4 border border-slate-100 shadow-sm text-center">
            <div class="text-xs font-bold text-slate-400 uppercase mb-1">점수</div>
            <div class="text-lg font-black text-slate-800">{{ currentDrawerRecord.score || '-' }}</div>
          </div>
        </div>
        
        <!-- 코드 미리보기 -->
        <div class="flex-1 bg-white rounded-xl overflow-hidden flex flex-col border border-slate-200">
          <div class="px-4 py-2 bg-slate-50 text-slate-500 text-xs font-mono flex justify-between items-center border-b border-slate-200">
            <span>{{ currentDrawerRecord.language }}.{{ getFileExtension(currentDrawerRecord.language) }}</span>
            <span class="text-slate-400">제출 코드</span>
          </div>
          <div class="flex-1 overflow-y-auto p-4">
            <pre class="text-sm font-mono leading-relaxed"><code class="text-slate-700" v-html="highlightCode(currentDrawerRecord.code?.substring(0, 800) || '// 코드 없음', currentDrawerRecord.language)"></code></pre>
            <div v-if="currentDrawerRecord.code?.length > 800" class="text-center mt-4">
              <span class="text-slate-400 text-xs">... 코드 더 보기는 AI 분석에서</span>
            </div>
          </div>
        </div>
        
        <!-- 닫기 버튼 -->
        <button 
          @click="closeDrawer" 
          class="mt-4 w-full py-3 rounded-xl bg-slate-200 hover:bg-slate-300 text-slate-700 font-bold transition-colors flex items-center justify-center gap-2"
        >
          <X class="w-4 h-4" />
          분석 닫기
        </button>
      </div>
    </div>
    
    <!-- 일반 모드: 드로어가 닫혀 있을 때 전체 대시보드 -->
    <div 
      v-else
      class="w-full overflow-y-auto [scrollbar-gutter:stable]"
      @click="collapseExpandedCard"
    >
      <div class="min-h-screen bg-white text-slate-800">
        <!-- 네비게이션/헤더 영역 -->
        
        <!-- 관전 모드 배너 -->
        <div v-if="isObserving" class="bg-slate-900 text-white px-4 py-3 flex items-center justify-between sticky top-0 z-50 shadow-md">
            <div class="flex items-center gap-2 font-bold">
                <div class="w-2 h-2 rounded-full bg-red-500 animate-pulse"></div>
                <span>관리자 관전 모드</span>
                <span class="text-slate-400 text-sm font-normal mx-2">|</span>
                <span class="text-brand-300">{{ studyData?.name || 'Loading...' }}</span>
            </div>
            <button @click="exitObservation" class="text-xs bg-white/10 hover:bg-white/20 px-3 py-1.5 rounded-lg transition-colors font-bold flex items-center gap-1">
                <X :size="14" /> 나가기
            </button>
        </div>

        <div class="flex-1 flex justify-center p-4 md:p-8">
            <div class="flex gap-8 max-w-screen-xl w-full">
            
            <!-- Resize Overlay: Blocks interactions during resize -->
            <div 
                v-if="isResizing" 
                class="fixed inset-0 z-[9999] cursor-col-resize"
                @click.stop
            ></div>

            <!-- 2. 메인 타임라인 컬럼 -->
            <main class="flex-1 min-w-0 pb-16">
                <!-- 깔끔한 지표가 있는 헤더 섹션 -->
                <div class="animate-fade-in-down">
                    <!-- 승인 대기 배너 -->
                    <div v-if="isPendingApproval" class="bg-amber-50 border border-amber-200 rounded-2xl p-4 flex items-start gap-3 shadow-sm animate-pulse mb-6">
                        <div class="bg-amber-100 p-2 rounded-lg text-amber-600">
                            <Clock class="w-5 h-5" stroke-width="2.5" />
                        </div>
                        <div>
                            <h3 class="font-bold text-amber-800 text-sm mb-1">
                                <span class="font-black">[{{ pendingStudyName }}]</span> 스터디 승인 대기 중입니다
                            </h3>
                            <p class="text-xs text-amber-700 leading-relaxed font-medium">
                                현재 개인 연구실(Personal Study)을 이용 중이며, 스터디장의 승인을 기다리고 있습니다.<br/>
                                승인이 완료되면 해당 스터디의 대시보드로 자동 전환됩니다.
                            </p>
                        </div>
                    </div>

                    <!-- 주간 미션 섹션 -->
                    <div class="mb-6">
                        <div v-if="targetMission" 
                            class="rounded-3xl p-6 shadow-none relative transition-all duration-500 bg-white border border-slate-200"
                            :class="getMissionThemeClass(targetMission)">
                            
                            <div v-if="isMissionAllClear(targetMission)" class="relative z-10 flex flex-col items-center text-center gap-4 py-4">
                                <div class="w-16 h-16 bg-gradient-to-br from-emerald-400 to-teal-500 rounded-full flex items-center justify-center text-white shadow-lg shadow-emerald-200 animate-bounce">
                                    <Trophy :size="32" stroke-width="3" />
                                </div>
                                <div>
                                    <h2 class="text-2xl font-black text-slate-800 mb-1">이번 주 미션 ALL CLEAR!</h2>
                                    <p class="text-sm text-slate-500 font-medium">모든 팀원이 미션을 완수했습니다.<br/>스터디장님, 다음 미션을 등록해주세요!</p>
                                </div>
                                <div class="flex flex-wrap gap-2 justify-center mt-2 w-full">
                                     <a 
                                        v-for="problemId in targetMission.problemIds" 
                                        :key="problemId"
                                        :href="getProblemLink(problemId)" 
                                        target="_blank"
                                        class="flex items-center gap-2 px-4 py-2 rounded-xl text-sm font-bold bg-emerald-50 text-emerald-600 border border-emerald-100 hover:bg-emerald-100 transition-colors"
                                    >
                                        {{ problemId }}번
                                        <Check :size="14" stroke-width="3" />
                                    </a>
                                </div>
                            </div>

                            <div v-else class="relative z-10 flex flex-col items-start gap-4">
                                <div>
                                    <div class="flex items-center gap-2 mb-2">
                                        <span class="px-2 py-0.5 bg-slate-100 rounded-lg text-xs font-bold uppercase tracking-wider text-slate-400">
                                            #{{ targetMission.week }}
                                        </span>
                                        <span class="flex items-center gap-1 text-xs font-bold text-slate-500">
                                            <Calendar :size="12" />
                                            ~ {{ formatMissionDate(targetMission.deadline) }}
                                        </span>
                                        <span v-if="isMissionUrgent(targetMission) && !isMissionCompleted(targetMission)" class="px-2 py-0.5 bg-rose-100 text-rose-600 rounded text-xs font-bold animate-pulse">
                                            마감 임박
                                        </span>
                                    </div>
                                    <div class="flex items-center gap-2">
                                        <h2 class="text-3xl font-black text-slate-800 mb-1 tracking-tight">{{ targetMission.title }}</h2>
                                        <span v-if="isMissionCompleted(targetMission)" class="bg-emerald-100 text-emerald-700 px-2 py-0.5 rounded-lg text-xs font-bold flex items-center gap-1">
                                            <Check :size="12" /> 해결 완료!
                                        </span>
                                    </div>
                                </div>
                                
                                <div class="flex flex-wrap gap-2 w-full">
                                    <a 
                                        v-for="problemId in targetMission.problemIds" 
                                        :key="problemId"
                                        :href="getProblemLink(problemId)" 
                                        target="_blank"
                                        class="flex items-center gap-2 px-6 py-3.5 rounded-2xl font-black text-sm transition-all shadow-none group flex-1 justify-center border-2"
                                        :class="isProblemSolved(problemId) 
                                            ? 'bg-emerald-500 border-emerald-500 text-white hover:bg-emerald-600 hover:border-emerald-600' 
                                            : 'bg-white border-brand-200 text-brand-600 hover:bg-brand-50 hover:border-brand-300 hover:scale-[1.02]'"
                                    >
                                        <span class="flex items-center gap-2">
                                            {{ problemId }}번
                                            <Check v-if="isProblemSolved(problemId)" :size="16" stroke-width="3" />
                                        </span>
                                        <ExternalLink v-if="!isProblemSolved(problemId)" :size="14" class="opacity-50 group-hover:opacity-100" />
                                    </a>
                                </div>
                            </div>

                            <!-- 멤버 진행 상황 섹션 -->
                            <div v-if="targetMission.memberProgressList?.length > 0" class="mt-6 pt-4 border-t border-slate-100 flex flex-wrap items-center gap-4">
                                <div v-for="member in sortMembers(targetMission.memberProgressList)" :key="member.userId"> 
                                    <!-- 아바타 및 이름 (NicknameRenderer 사용) -->
                                    <div class="flex flex-col items-center gap-1 group relative cursor-help">
                                        <!-- 이름 툴팁 -->
                                        <div class="absolute bottom-full mb-2 px-2 py-1 bg-black/80 text-white text-xs rounded opacity-0 group-hover:opacity-100 transition-opacity whitespace-nowrap pointer-events-none z-20">
                                            {{ member.username === '탈퇴한 회원' ? '탈퇴한 회원' : member.username }} {{ isMe(member.userId) ? '(나)' : '' }}
                                            <div class="absolute bottom-0 left-1/2 -translate-x-1/2 translate-y-1 w-2 h-2 bg-black/80 rotate-45"></div>
                                        </div>

                                        <!-- 렌더러 -->
                                        <NicknameRenderer 
                                            :username="member.username"
                                            :avatar-url="getMemberProfileImage(member)"
                                            :class="[
                                                isMe(member.userId) 
                                                    ? '' 
                                                    : 'opacity-80 grayscale-[0.0]'
                                            ]"
                                            :avatar-class="`w-10 h-10 border-2 rounded-full ${
                                                isMe(member.userId)
                                                    ? 'border-emerald-400 ring-2 ring-emerald-400/30' + (member.allCompleted ? ' shadow-[0_0_12px_rgba(52,211,153,0.6)]' : '')
                                                    : member.allCompleted
                                                    ? 'border-orange-400 shadow-[0_0_12px_rgba(251,146,60,0.5)]'
                                                    : 'border-slate-200'
                                            }`"
                                        />
                                        
                                        <!-- 상태 아이콘 -->
                                        <div class="flex items-center gap-0.5 mt-0.5">
                                            <Flame :size="13" 
                                                class="transition-all"
                                                :class="member.allCompleted ? 'fill-orange-400 text-orange-400 animate-pulse' : 'text-slate-300/40'" />
                                            <span v-if="!member.allCompleted" class="text-[11px] font-bold text-slate-400">
                                                {{ member.completedCount }}
                                            </span>
                                        </div>
                                    </div>
                            </div>
                        </div>
                    </div>
                        
                        <div v-if="!targetMission" class="bg-white rounded-3xl p-6 shadow-sm flex items-center justify-center gap-3 text-slate-400 border border-slate-200">
                            <MapIcon :size="20" />
                            <span class="font-medium">진행 중인 미션이 없어요!</span>
                        </div>
                    </div>
                </div>

                <!-- 타임라인 섹션 -->
                <div>
                    <!-- 타임라인 헤더 -->
                    <div class="mb-4 flex items-center justify-between">
                        <h2 class="text-xl font-bold text-slate-800 flex items-center gap-2">
                            <Activity :size="20" class="text-brand-500 fill-brand-500"/>
                            타임라인
                        </h2>
                        <!-- 필터 탭 -->
                        <div class="flex p-1 bg-slate-200/50 rounded-xl font-bold">
                            <button 
                                v-for="filter in ['ALL', 'MISSION', 'MOCK_EXAM', 'DEFENSE', 'GENERAL']" 
                                :key="filter"
                                @click="selectedFilter = filter"
                                class="px-3 py-1.5 rounded-lg text-xs font-bold transition-all"
                                :class="selectedFilter === filter ? 'bg-white text-slate-800 shadow-sm' : 'text-slate-400 hover:text-slate-600'"
                            >
                                {{ filter === 'ALL' ? '전체' : filter === 'MOCK_EXAM' ? '모의고사' : filter === 'MISSION' ? '과제' : filter === 'DEFENSE' ? '디펜스' : '일반' }}
                            </button>
                        </div>
                    </div>

                    <!-- 날짜 네비게이터 + 추가 필터 -->
                    <div class="bg-white rounded-2xl p-4 border border-slate-200 mb-4 shadow-sm">
                        <!-- 날짜 탐색 -->
                        <div class="flex items-center justify-between mb-3">
                            <button 
                                @click="goToPrevDate"
                                class="p-2 bg-slate-100 hover:bg-slate-200 rounded-xl transition-all active:scale-95"
                            >
                                <ChevronLeft :size="20" class="text-slate-600" />
                            </button>
                            
                            <div class="flex items-center gap-3">
                                <span class="text-lg font-black text-slate-800 tracking-tight">{{ formatSelectedDate }}</span>
                                <button 
                                    v-if="!isToday"
                                    @click="goToToday"
                                    class="px-3 py-1 bg-brand-50 hover:bg-brand-100 text-brand-600 text-xs font-bold rounded-lg transition-all flex items-center gap-1"
                                >
                                    오늘로
                                    <ChevronRight :size="12" />
                                </button>
                            </div>
                            
                            <button 
                                @click="goToNextDate"
                                :disabled="isNextDisabled"
                                class="p-2 bg-slate-100 hover:bg-slate-200 rounded-xl transition-all active:scale-95"
                                :class="{ 'opacity-40 cursor-not-allowed hover:bg-slate-100': isNextDisabled }"
                            >
                                <ChevronRight :size="20" class="text-slate-600" />
                            </button>
                        </div>
                        
                        <!-- 추가 필터 토글 -->
                        <div class="flex items-center gap-4 pt-3 border-t border-slate-100">
                            <label class="flex items-center gap-2 cursor-pointer group">
                                <input 
                                    type="checkbox" 
                                    v-model="showSuccessOnly"
                                    class="w-4 h-4 rounded border-slate-300 text-brand-500 focus:ring-brand-500/30"
                                />
                                <span class="text-sm font-medium text-slate-600 group-hover:text-slate-800 transition-colors">성공만 보기</span>
                            </label>
                            
                            <label class="flex items-center gap-2 cursor-pointer group">
                                <input 
                                    type="checkbox" 
                                    v-model="groupByProblem"
                                    class="w-4 h-4 rounded border-slate-300 text-brand-500 focus:ring-brand-500/30"
                                />
                                <span class="text-sm font-medium text-slate-600 group-hover:text-slate-800 transition-colors">문제별 그룹</span>
                            </label>
                            
                            <div class="ml-auto text-xs font-bold text-slate-400">
                                {{ filteredRecords.length }}건
                            </div>
                        </div>
                    </div>

                    <!-- 빈 상태 -->
                    <div v-if="filteredRecords.length === 0 && !loading" class="flex flex-col items-center justify-center py-20 bg-white rounded-3xl border-2 border-dashed border-slate-200">
                        <div class="w-16 h-16 bg-slate-50 rounded-full flex items-center justify-center mb-4">
                            <Code2 :size="32" class="text-slate-300" />
                        </div>
                        <h3 class="text-lg font-bold text-slate-800 mb-1">이 날의 기록이 없습니다</h3>
                        <p class="text-sm text-slate-400 mb-0 font-medium">
                            {{ isToday ? '오늘 첫 번째 문제를 풀어보세요!' : '다른 날짜를 선택해보세요.' }}
                        </p>
                    </div>

                    <!-- 문제별 그룹 뷰 -->
                    <div v-else-if="groupByProblem && groupedRecords" class="space-y-3 pb-20">
                        <div 
                            v-for="group in groupedRecords" 
                            :key="group.problemNumber"
                            class="bg-white rounded-2xl border border-slate-200 overflow-hidden shadow-sm"
                        >
                            <!-- 그룹 헤더 -->
                            <div 
                                @click="toggleGroup(group.problemNumber)"
                                class="p-4 flex items-center justify-between cursor-pointer hover:bg-slate-50 transition-colors"
                            >
                                <div class="flex items-center gap-3">
                                    <span class="px-2.5 py-1 bg-brand-100 text-brand-700 text-xs font-bold rounded-lg">
                                        #{{ group.problemNumber }}
                                    </span>
                                    <span class="font-bold text-slate-800 truncate max-w-[200px]">{{ group.title }}</span>
                                    <span 
                                        class="px-2 py-0.5 text-xs font-bold rounded-full"
                                        :class="group.hasSuccess ? 'bg-emerald-100 text-emerald-600' : 'bg-red-100 text-red-600'"
                                    >
                                        {{ group.hasSuccess ? '성공' : '실패' }}
                                    </span>
                                </div>
                                <div class="flex items-center gap-2">
                                    <span class="text-xs font-bold text-slate-400">{{ group.records.length }}회 제출</span>
                                    <ChevronRight 
                                        :size="16" 
                                        class="text-slate-400 transition-transform duration-200"
                                        :class="{ 'rotate-90': isGroupExpanded(group.problemNumber) }"
                                    />
                                </div>
                            </div>
                            
                            <!-- 그룹 내 기록들 -->
                            <div 
                                v-if="isGroupExpanded(group.problemNumber)"
                                class="border-t border-slate-100 bg-slate-50/50 p-3 space-y-2"
                            >
                                <DashboardRecordCard 
                                    v-for="record in group.records"
                                    :key="record.id"
                                    :ref="el => { if (expandedRecordId === record.id) activeCardRef = el }"
                                    :record="record"
                                    :is-expanded="expandedRecordId === record.id"
                                    @toggle-expand="handleToggleExpand"
                                />
                            </div>
                        </div>
                    </div>

                    <!-- 일반 리스트 뷰 -->
                    <div v-else class="space-y-4 pb-20">
                        <template v-for="record in filteredRecords" :key="record.id">
                            <DashboardRecordCard 
                                :ref="el => { if (expandedRecordId === record.id) activeCardRef = el }"
                                :record="record"
                                :is-expanded="expandedRecordId === record.id"
                                @toggle-expand="handleToggleExpand"
                            />
                        </template>
                    </div>
                </div>
            </main>

            <!-- Resizer Handle -->
            <div
                class="hidden lg:block w-1 cursor-col-resize hover:bg-brand-400 active:bg-brand-600 transition-colors z-50 select-none relative group"
                @mousedown="startResize"
            >
                <div class="absolute inset-y-0 -left-1 -right-1 group-hover:bg-brand-400/10"></div>
            </div>

            <!-- 3. 우측 사이드바 컬럼 (통계 + 활동 + 분석 패널) -->
            <aside 
                class="hidden lg:flex shrink-0 flex-col gap-6 sticky top-8 h-[calc(100vh-4rem)]"
                :style="{ width: sidebarWidth + 'px' }"
            >
                
                <!-- 1. 통계 열 (UserQuickStats) -->
                <UserQuickStats 
                    v-if="studyData"
                    :items="[
                        { 
                            icon: IconAcorn, 
                            value: (studyData.acornCount || 0).toLocaleString(), 
                            tooltip: 'Acorns',
                            iconClass: 'text-fox',
                            fill: 'currentColor',
                            onClick: goToPlayground
                        },
                        { 
                            icon: Flame, 
                            value: currentStreak.toLocaleString(), 
                            tooltip: 'Streak',
                            iconClass: currentStreak > 0 ? 'text-rose-500 animate-pulse' : 'text-slate-300',
                            fill: currentStreak > 0 ? 'currentColor' : 'none'
                        },
                        { 
                            icon: Send, 
                            value: records.length.toLocaleString(), 
                            tooltip: 'Submissions',
                            iconClass: 'text-sky-400',
                            sizeClass: 'w-5 h-5',
                            fill: 'currentColor'
                        }
                    ]"
                />

                <!-- 2. 활동 로그 (분석 중일 때는 숨김) -->
                <div v-if="!loading && !activeAnalysisRecord" class="bg-white rounded-2xl p-6 border border-slate-200 shadow-sm animate-fade-in-down">
                    <h3 class="font-black text-slate-700 text-sm mb-4">스터디 활동 로그</h3>
                    
                    <!-- 히트맵 시각화 -->
                    <div class="relative">
                       <!-- 스크롤 래퍼 -->
                       <div ref="heatmapScrollRef" class="overflow-x-auto pb-2 custom-scrollbar no-scrollbar" style="direction: rtl;">
                           <div class="flex gap-[3px] justify-end min-w-max" style="direction: ltr;">
                               <div v-for="(week, wIdx) in heatmapWeeks" :key="wIdx" class="flex flex-col gap-[3px]">
                                   <div v-for="(day, dIdx) in week" :key="dIdx"
                                        class="w-2.5 h-2.5 rounded-[2px] transition-all relative cursor-pointer hover:ring-2 hover:ring-brand-400 hover:ring-offset-1"
                                        :class="day.colorClass"
                                        @mouseenter="showHeatmapTooltip($event, day)"
                                        @mouseleave="hideHeatmapTooltip">
                                   </div>
                               </div>
                           </div>
                       </div>
                       
                       <!-- 커스텀 툴팁 -->
                       <Teleport to="body">
                           <Transition name="fade">
                               <div 
                                   v-if="heatmapTooltip.visible"
                                   class="fixed z-[9999] bg-slate-900/95 backdrop-blur-sm text-white text-xs rounded-xl p-3 shadow-2xl pointer-events-none min-w-[180px] max-w-[240px]"
                                   :style="heatmapTooltipStyle"
                               >
                                   <!-- 날짜 헤더 -->
                                   <div class="flex items-center justify-between mb-2 pb-2 border-b border-slate-700">
                                       <span class="font-bold text-sm">{{ heatmapTooltip.date }}</span>
                                       <span class="bg-brand-500/20 text-brand-300 px-2 py-0.5 rounded-full text-[10px] font-bold">
                                           {{ heatmapTooltip.count }}건
                                       </span>
                                   </div>
                                   
                                   <!-- 기여자 목록 -->
                                   <div v-if="heatmapTooltip.contributors?.length > 0" class="space-y-2">
                                       <div v-for="c in heatmapTooltip.contributors" :key="c.userId || c.username" 
                                            class="flex items-center gap-2 bg-slate-800/50 rounded-lg p-1.5 pr-3">
                                           <!-- 렌더러 -->
                                           <NicknameRenderer 
                                               :username="c.username"
                                               :avatar-url="c.avatarUrl || getDefaultProfileImage(c.userId)"
                                               avatar-class="w-6 h-6 border border-slate-600 bg-slate-700"
                                               text-class="font-medium text-white truncate"
                                               container-class="flex-1 min-w-0"
                                               :icon-size="16"
                                           />
                                           <div class="text-brand-400 font-bold text-sm">{{ c.count }}건</div>
                                       </div>
                                   </div>
                                   <div v-else class="text-slate-400 text-center py-2">
                                       <span class="text-lg">😴</span>
                                       <div class="mt-1">활동 없음</div>
                                   </div>
                               </div>
                           </Transition>
                       </Teleport>
                    </div>
                </div>

                <!-- 3. 분석 사이드바 (컨텍스트) -->
                <div class="flex-1 min-h-0 overflow-hidden rounded-2xl border border-slate-200 shadow-sm bg-white" @click.stop>
                     <AnalysisSidebar :record="activeAnalysisRecord" @scroll-to-line="handleScrollToLine" @acorn-used="handleAcornUsed" />
                </div>



            </aside>

            </div>
        </div>
    </div>
  </div>

    <!-- 인라인 AI 드로어 (열렸을 때 60% 너비) -->
    <div 
      v-if="showDrawer"
      class="w-full md:w-[60%] border-l border-slate-200 transition-all duration-300 ease-in-out bg-white"
    >
      <AiDrawer 
        :is-visible="true"
        :type="drawerType"
        :title="drawerTitle"
        :loading="drawerLoading"
        :data="drawerData"
        :code="currentRecordCode"
        :record-id="currentDrawerRecord?.id"
        :user-id="user?.id"
        :solve-status="currentDrawerRecord?.result !== 'FAIL' ? 'solved' : 'wrong'"
        :wrong-reason="currentDrawerRecord?.result"
        :problem-number="currentDrawerRecord?.problemNumber"
        :problem-title="currentDrawerRecord?.title"
        @close="closeDrawer"
        @acorn-used="handleAcornUsed"
      />
    </div>

  </div>
  <!-- 분할 뷰 Flex 컨테이너 종료 -->
</template>

<script setup>
import { ref, onMounted, computed, watch, nextTick } from 'vue';
import { useRouter } from 'vue-router';
import { dashboardApi } from '@/api/dashboard';
import { studyApi } from '@/api/study';
import { useAuth } from '@/composables/useAuth';
import DashboardRecordCard from './DashboardRecordCard.vue';
import AnalysisSidebar from './AnalysisSidebar.vue';
import http from '@/api/http';
import { aiApi } from '@/api/ai';
import AlgorithmRadarChart from '@/components/charts/AlgorithmRadarChart.vue';
import AiDrawer from '@/components/ai/AiDrawer.vue';
import IconAcorn from '@/components/icons/IconAcorn.vue';
import { 
  Bot, 
  Lightbulb, 
  Bug, 
  Cpu, 
  Zap, 
  Database, 
  Binary, 
  LayoutGrid, 
  Network, 
  MessageSquare, 
  X,
  Code2,
  Youtube,
  Nut,
  Trophy,
  Activity,
  Copy,
  Check,
  ExternalLink,
  Calendar,
  Map as MapIcon,
  TrendingUp,
  School,
  Flame,
  Users,
  BarChart2,
  Hexagon,
  Send,
  UserX,
  Clock,
  ChevronLeft,
  ChevronRight,
  Filter
} from 'lucide-vue-next';
import hljs from 'highlight.js';
import 'highlight.js/styles/github.css';
import { marked } from 'marked';
import NicknameRenderer from '@/components/common/NicknameRenderer.vue';
import UserQuickStats from '@/components/common/UserQuickStats.vue';

const getMemberProfileImage = (member) => {
    if (member.avatarUrl) return member.avatarUrl;
    const id = member.userId || 0;
    const index = id % profileImages.length;
    return profileImages[index];
};

const props = defineProps({
    studyId: {
        type: Number,
        default: null
    }
});

// Sidebar Resizing
const sidebarWidth = ref(380);
const isResizing = ref(false);

const startResize = () => {
    isResizing.value = true;
    document.addEventListener('mousemove', handleResize);
    document.addEventListener('mouseup', stopResize);
    document.body.style.cursor = 'col-resize';
    document.body.style.userSelect = 'none';
};

const handleResize = (e) => {
    if (!isResizing.value) return;
    const containerWidth = document.body.clientWidth;
    const newWidth = containerWidth - e.clientX - 32; // 32px padding/margin adjustment
    
    // Constraints: Min 300px, Max 600px (reduced from 800px)
    if (newWidth >= 300 && newWidth <= 600) {
        sidebarWidth.value = newWidth;
    }
};

const stopResize = () => {
    // Delay resetting isResizing to prevent click events from firing immediately after release
    setTimeout(() => {
        isResizing.value = false;
    }, 100);
    
    document.removeEventListener('mousemove', handleResize);
    document.removeEventListener('mouseup', stopResize);
    document.body.style.cursor = '';
    document.body.style.userSelect = '';
};

const handleToggleExpand = (recordId) => {
    // Prevent toggling if a resize operation just finished
    if (isResizing.value) return; 

    if (expandedRecordId.value === recordId) {
        expandedRecordId.value = null;
        activeAnalysisRecord.value = null;
    } else {
        expandedRecordId.value = recordId;
        const record = records.value.find(r => r.id === recordId) || 
                       groupedRecords.value.flatMap(g => g.records).find(r => r.id === recordId);
        activeAnalysisRecord.value = record;
    }
};

const { user, refresh } = useAuth();
const router = useRouter();
const records = ref([]);
const studyData = ref(null);
const loading = ref(true);
const heatmapWeeks = ref([]);
const heatmapScrollRef = ref(null);

const isObserving = computed(() => !!props.studyId);
const currentStudyId = computed(() => props.studyId || user.value?.studyId);

const isPendingApproval = computed(() => !!user.value?.pendingStudyName);
const pendingStudyName = computed(() => user.value?.pendingStudyName);

const exitObservation = () => {
    router.push('/study/ranking');
};

const goToPlayground = () => {
    router.push({
        path: '/playground',
        query: { studyId: currentStudyId.value }
    });
};

// 히트맵 툴팁 상태
const heatmapTooltip = ref({
    visible: false,
    x: 0,
    y: 0,
    date: '',
    count: 0,
    contributors: []
});

const showHeatmapTooltip = (event, day) => {
    const rect = event.target.getBoundingClientRect();
    heatmapTooltip.value = {
        visible: true,
        x: rect.left + rect.width / 2,
        y: rect.top - 8,
        date: day.dateFormatted,
        count: day.count || 0,
        contributors: day.contributors || []
    };
};

const hideHeatmapTooltip = () => {
    heatmapTooltip.value.visible = false;
};

// 빈 공간 클릭 시 확장된 카드 접기
const collapseExpandedCard = (event) => {
    // Resizing 중이거나 방금 종료된 경우 무시
    if (isResizing.value) return;

    // Don't collapse if clicking inside a card (handled by stopPropagation on cards)
    if (expandedRecordId.value !== null) {
        expandedRecordId.value = null;
        activeAnalysisRecord.value = null;
        activeCardRef.value = null;
    }
};

// 툴팁 위치 계산 (오버플로우 방지)
const heatmapTooltipStyle = computed(() => {
    const tooltip = heatmapTooltip.value;
    const tooltipWidth = 200;
    const tooltipHeight = 150;
    const padding = 16;
    
    let left = tooltip.x;
    let top = tooltip.y - tooltipHeight - 8;
    
    // 가로 범위 유지
    if (left - tooltipWidth / 2 < padding) {
        left = tooltipWidth / 2 + padding;
    } else if (left + tooltipWidth / 2 > window.innerWidth - padding) {
        left = window.innerWidth - tooltipWidth / 2 - padding;
    }
    
    // 툴팁이 화면 위로 넘치면 아래에 표시
    if (top < padding) {
        top = tooltip.y + 20;
    }
    
    return {
        left: left + 'px',
        top: top + 'px',
        transform: 'translateX(-50%)'
    };
});

// 사용자 ID 기반 기본 프로필 이미지
const getDefaultProfileImage = (userId) => {
    const id = userId || 0;
    const index = id % profileImages.length;
    return profileImages[index];
};

const selectedFilter = ref('ALL');

// 날짜 탐색 관련 state
const selectedDate = ref(new Date());
const showSuccessOnly = ref(false);
const groupByProblem = ref(false);
const expandedGroups = ref(new Set());

// 날짜 네비게이션 함수
const goToPrevDate = () => {
    const newDate = new Date(selectedDate.value);
    newDate.setDate(newDate.getDate() - 1);
    selectedDate.value = newDate;
    expandedGroups.value.clear();
};

const goToNextDate = () => {
    if (!isNextDisabled.value) {
        const newDate = new Date(selectedDate.value);
        newDate.setDate(newDate.getDate() + 1);
        selectedDate.value = newDate;
        expandedGroups.value.clear();
    }
};

const goToToday = () => {
    selectedDate.value = new Date();
    expandedGroups.value.clear();
};

const isNextDisabled = computed(() => {
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    const selected = new Date(selectedDate.value);
    selected.setHours(0, 0, 0, 0);
    return selected >= today;
});

const isToday = computed(() => {
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    const selected = new Date(selectedDate.value);
    selected.setHours(0, 0, 0, 0);
    return selected.getTime() === today.getTime();
});

const formatSelectedDate = computed(() => {
    const d = selectedDate.value;
    const days = ['일', '월', '화', '수', '목', '금', '토'];
    return `${d.getFullYear()}. ${String(d.getMonth() + 1).padStart(2, '0')}. ${String(d.getDate()).padStart(2, '0')} (${days[d.getDay()]})`;
});

// 날짜 + 카테고리 + 성공여부 복합 필터링
const filteredRecords = computed(() => {
    let result = records.value;
    
    // 1. 날짜 필터링
    const selectedDateStr = selectedDate.value.toISOString().split('T')[0];
    result = result.filter(r => {
        const recordDate = new Date(r.committedAt || r.createdAt).toISOString().split('T')[0];
        return recordDate === selectedDateStr;
    });
    
    // 2. 카테고리 필터링
    if (selectedFilter.value !== 'ALL') {
        result = result.filter(r => r.tag === selectedFilter.value);
    }
    
    // 3. 성공만 보기
    if (showSuccessOnly.value) {
        result = result.filter(r => r.result !== 'FAIL');
    }
    
    return result;
});

// 문제별 그룹화
const groupedRecords = computed(() => {
    if (!groupByProblem.value) return null;
    
    const groups = new Map();
    for (const record of filteredRecords.value) {
        const key = record.problemNumber;
        if (!groups.has(key)) {
            groups.set(key, {
                problemNumber: record.problemNumber,
                title: record.title,
                records: [],
                hasSuccess: false,
                latestResult: null
            });
        }
        const group = groups.get(key);
        group.records.push(record);
        if (record.result !== 'FAIL') {
            group.hasSuccess = true;
        }
        // 가장 최신 결과
        if (!group.latestResult) {
            group.latestResult = record.result;
        }
    }
    // 시간순 정렬 (최신순)
    return Array.from(groups.values()).map(g => ({
        ...g,
        records: g.records.sort((a, b) => new Date(b.committedAt || b.createdAt) - new Date(a.committedAt || a.createdAt))
    }));
});

const toggleGroup = (problemNumber) => {
    if (expandedGroups.value.has(problemNumber)) {
        expandedGroups.value.delete(problemNumber);
    } else {
        expandedGroups.value.add(problemNumber);
    }
    expandedGroups.value = new Set(expandedGroups.value);
};

const isGroupExpanded = (problemNumber) => expandedGroups.value.has(problemNumber);

// 필터 변경 시 확장 초기화
watch(selectedFilter, () => {
    expandedRecordId.value = null;
});

const expandedRecordId = ref(null);
const activeAnalysisRecord = ref(null);
const activeCardRef = ref(null);



const handleScrollToLine = ({ start, end }) => {
    console.log('Dashboard: handleScrollToLine', start, end, 'activeCardRef:', !!activeCardRef.value);
    if (activeCardRef.value && activeCardRef.value.scrollToLine) {
        activeCardRef.value.scrollToLine(start, end);
    }
};

const acornLogs = ref([]);
const missions = ref([]);

// 마감되지 않은 + 모든 팀원이 완료하지 않은 미션만 표시
const activeMissions = computed(() => {
    if (!missions.value || missions.value.length === 0) return [];
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    
    return missions.value
        .filter(m => {
            const deadline = new Date(m.deadline);
            deadline.setHours(23, 59, 59, 999);
            const notExpired = deadline >= today;
            
            // 모든 팀원이 완료했는지 확인 (memberProgressList가 있는 경우)
            // const allMembersCompleted = m.memberProgressList?.length > 0 && 
            //     m.memberProgressList.every(member => member.allCompleted);
            
            // 완료되어도 마감일이 지나지 않았으면 계속 표시 (유저 피드백 반영)
            return notExpired;
        })
        .sort((a, b) => a.week - b.week);  // 낮은 주차부터 표시
});

// 화면에 표시할 단일 미션 (가장 우선순위 높은 것)
const targetMission = computed(() => {
    return activeMissions.value.length > 0 ? activeMissions.value[0] : null;
});

const tagStats = ref([]);
const topTagName = computed(() => tagStats.value.length > 0 ? tagStats.value[0].tagKey : '');
const totalSolvedCount = computed(() => tagStats.value.reduce((acc, curr) => acc + (curr.solved || 0), 0));

// 특정 문제가 나에 의해 해결되었는지 확인 (computed용 raw 버전)
const isProblemSolvedRaw = (problemId) => {
    return records.value.some(r => 
        r.problemNumber == problemId && 
        r.userId === user.value?.id &&
        (r.result === 'SUCCESS' || r.result === 'PASSED' || (r.runtimeMs !== undefined && r.runtimeMs !== -1))
    );
};

// 특정 문제가 나에 의해 해결되었는지 확인 (팀원 미포함)
const isProblemSolved = (problemId) => {
    return isProblemSolvedRaw(problemId);
};

// 특정 미션이 완료되었는지 확인
const isMissionCompleted = (mission) => {
    if (!mission) return false;
    return mission.problemIds.every(pid => isProblemSolved(pid));
};

// 특정 미션이 마감 임박인지 확인 (3일 이내)
const isMissionUrgent = (mission) => {
    if (!mission || isMissionCompleted(mission)) return false;
    const now = new Date();
    const deadline = new Date(mission.deadline);
    const diffTime = deadline - now;
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24)); 
    return diffDays <= 3 && diffDays >= 0;
};

// 미션별 테마 클래스
const getMissionThemeClass = (mission) => {
    if (isMissionAllClear(mission)) return 'bg-emerald-50/50 border-emerald-100';
    return 'bg-white';
};

const isMissionAllClear = (mission) => {
    if (!mission || !mission.memberProgressList) return false;
    return mission.memberProgressList.length > 0 && mission.memberProgressList.every(m => m.allCompleted);
};

// 멤버 리스트 정렬: 본인 우선, 그 외 이름순
const sortMembers = (members) => {
    if (!members) return [];
    return [...members].sort((a, b) => {
        if (a.userId === user.value?.id) return -1;
        if (b.userId === user.value?.id) return 1;
        return a.username.localeCompare(b.username);
    });
};

// 본인인지 확인
const isMe = (userId) => {
    return user.value?.id === userId;
};

const processHeatmap = (data) => {
    console.log('processHeatmap called with:', data?.length); // Debug
    try {
        const activityMap = new Map();
        (data || []).forEach(item => {
             if(item?.date) activityMap.set(item.date, item);
        });

        const weeks = [];
        const today = new Date();
        const end = new Date(today);
        const start = new Date(end);
        start.setDate(start.getDate() - (52 * 7) + 1);

        let current = new Date(start);
        let currentWeek = [];

        for (let i = 0; i < 52 * 7; i++) {
             const dateStr = current.toISOString().split('T')[0];
             const activity = activityMap.get(dateStr);
             
             // --- 참여 로직 시작 ---
             const totalMembers = studyData.value?.memberCount || 1;
             const contributors = Array.isArray(activity?.contributors) ? activity.contributors : [];
             const activeCount = contributors.length;
             const totalSolvedCount = activity?.count || 0;
             
             let participationRate = 0;
             if (totalMembers > 0) {
                 participationRate = activeCount / totalMembers;
             }
             
             let colorClass = 'bg-slate-100';
             if (participationRate > 0) colorClass = 'bg-brand-200';
             if (participationRate >= 0.25) colorClass = 'bg-brand-400';
             if (participationRate >= 0.50) colorClass = 'bg-brand-500';
             if (participationRate >= 0.75) colorClass = 'bg-brand-600';
             if (participationRate >= 1.0) colorClass = 'bg-brand-800';
             // --- 참여 로직 끝 ---

             currentWeek.push({
                 date: dateStr,
                 dateFormatted: current.toLocaleDateString('ko-KR', { month: 'short', day: 'numeric' }),
                 count: totalSolvedCount,
                 contributors: contributors,
                 colorClass: colorClass
             });

             if (currentWeek.length === 7) {
                 weeks.push(currentWeek);
                 currentWeek = [];
             }
             current.setDate(current.getDate() + 1);
        }
        if (currentWeek.length > 0) weeks.push(currentWeek);
        console.log('Heatmap Generated:', weeks.length, 'weeks'); // Debug
        heatmapWeeks.value = weeks;
    } catch(err) {
        console.error("Heatmap Gen Error", err);
    }
};

const currentWeekCount = computed(() => {
    if (!heatmapWeeks.value.length) return 0;
    const lastWeek = heatmapWeeks.value[heatmapWeeks.value.length - 1];
    return lastWeek.reduce((acc, day) => acc + (day.count || 0), 0);
});

const heatmapResData = ref([]);

watch(() => studyData.value, () => {
    // Re-process heatmap when study data (member count) loads
    if (heatmapResData.value) {
        processHeatmap(heatmapResData.value);
    }
});


// Computed: 현재 스트릭 (백엔드 캐시 데이터 사용)
const currentStreak = computed(() => {
    if (!studyData.value) return 0;
    
    // 백엔드에서 streakUpdatedAt과 streak 값을 받아옴
    const streak = studyData.value.streak || 0;
    const updatedAt = studyData.value.streakUpdatedAt;
    
    // streak 유효성 판단: updatedAt이 어제 이전이면 체인 끊김
    if (!updatedAt) return 0;
    
    const yesterday = new Date();
    yesterday.setDate(yesterday.getDate() - 1);
    yesterday.setHours(0, 0, 0, 0);
    
    const updatedDate = new Date(updatedAt);
    updatedDate.setHours(0, 0, 0, 0);
    
    if (updatedDate < yesterday) {
        return 0;  // 체인 끊김
    }
    
    return streak;
});

const showModal = ref(false);
const copiedInput = ref(false);

const copyToClipboard = async (text) => {
    try {
        await navigator.clipboard.writeText(text);
        copiedInput.value = true;
        setTimeout(() => {
            copiedInput.value = false;
        }, 2000);
    } catch (err) {
        console.error('Failed to copy text: ', err);
    }
};
const modalType = ref('');
const modalTitle = ref('');
const modalData = ref(null);
const modalLoading = ref(false);
const activeTab = ref('insight');

// 드로어 상태
const showDrawer = ref(false);
const drawerType = ref('');
const drawerTitle = ref('');
const drawerData = ref(null);
const drawerLoading = ref(false);
const currentDrawerRecord = ref(null);  // 컨텍스트 카드에서 분석 중인 기록

// 코드 뷰를 위한 아코디언 상태
// const expandedRecordId = ref(null); // Removed

const tooltipData = ref(null);
const tooltipPos = ref({ x: 0, y: 0 });

const showTooltip = (event, day) => {
    const rect = event.target.getBoundingClientRect();
    tooltipPos.value = {
        x: rect.left + rect.width / 2,
        y: rect.top
    };
    tooltipData.value = day;
};

const hideTooltip = () => {
    tooltipData.value = null;
};



const fetchStudyData = async () => {
    if (!currentStudyId.value) return;
    try {
        const studyRes = await studyApi.get(currentStudyId.value);
        studyData.value = studyRes.data;

        const logsRes = await studyApi.getAcornLogs(currentStudyId.value);
        acornLogs.value = logsRes.data || [];

        const missionsRes = await studyApi.getMissions(currentStudyId.value);
        missions.value = missionsRes.data || [];
        
        // 히트맵 데이터 처리
        // Admin observation: use currentStudyId
        const heatmapRes = await dashboardApi.getHeatmap(currentStudyId.value);
        heatmapResData.value = heatmapRes.data || [];
        processHeatmap(heatmapResData.value);
    } catch(e) {
        console.error('Study Load Error:', e);
    }
};

const handleAcornUsed = () => {
    console.log('Acorn used, refreshing study data...');
    fetchStudyData();
};

onMounted(async () => {
    try {
        await refresh();

        // 1. 사용자 기록 가져오기
        try {
            const recordsRes = await dashboardApi.getRecords(currentStudyId.value);
            records.value = recordsRes.data;
        } catch(e) {
            console.error('Records Load Error:', e);
        }
      
      // 2. 태그 통계 가져오기
      try {
          const statsRes = await aiApi.getTagStats(user.value?.id || 1, 6);
          tagStats.value = statsRes.data || [];
      } catch(e) {
          console.error('TagStats Load Error:', e);
      }
      
      // 3. 스터디 데이터 가져오기 (해당되는 경우)
      if (currentStudyId.value) {
          await fetchStudyData();
      }

      // 4. Fetch Heatmap (If not already fetched by fetchStudyData)
      if (!currentStudyId.value) {
          try {
            const heatmapRes = await dashboardApi.getHeatmap();
            heatmapResData.value = heatmapRes.data || [];
            processHeatmap(heatmapResData.value);
            nextTick(() => {
              if (heatmapScrollRef.value) {
                heatmapScrollRef.value.scrollLeft = heatmapScrollRef.value.scrollWidth;
              }
            });
          } catch(e) {
            console.error('Heatmap Load Error:', e);
            processHeatmap([]);
          }
      }
  } catch (globalError) {
      console.error("Critical Dashboard Error:", globalError);
  } finally {
      loading.value = false;
  }
});

const getProblemLink = (problemId) => `https://www.acmicpc.net/problem/${problemId}`;





const formatDate = (dateString) => {
    if (!dateString) return '';
    const date = new Date(dateString);
    return date.toLocaleDateString('ko-KR', { 
        year: 'numeric', 
        month: 'long', 
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    });
};

const formatMissionDate = (dateString) => {
    if (!dateString) return '';
    const date = new Date(dateString);
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}. ${month}. ${day}.`;
};

// Markdown Renderer

// Markdown Renderer
const renderMarkdown = (text) => {
    if (!text) return '';
    return marked.parse(text);
};

const highlightCodeContent = (code, lang = 'java') => {
    if (!code) return '';
    try {
        return hljs.highlight(code, { language: lang }).value;
    } catch (e) {
        try {
             return hljs.highlightAuto(code).value;
        } catch (e2) {
             return code;
        }
    }
};

const copyCode = (code) => {
    navigator.clipboard.writeText(code);
    alert('코드가 복사되었습니다.');
};

const currentRecordCode = ref('');

const parsePatterns = (jsonString) => {
    if (!jsonString) return [];
    try {
        const parsed = JSON.parse(jsonString);
        return Array.isArray(parsed) ? parsed : [];
    } catch (e) {
        console.error("Failed to parse patterns", e);
        return [];
    }
};

const highlightCode = (code, language) => {
    try {
        return hljs.highlight(code, { language }).value;
    } catch (err) {
        return hljs.highlightAuto(code).value;
    }
};

// Helper to get file extension from language
const getFileExtension = (language) => {
    const extensions = {
        'java': 'java',
        'python': 'py',
        'cpp': 'cpp',
        'c++': 'cpp',
        'c': 'c',
        'javascript': 'js',
        'typescript': 'ts'
    };
    return extensions[language?.toLowerCase()] || 'txt';
};
</script>

<style scoped>
@import url('https://cdn.jsdelivr.net/gh/orioncactus/pretendard/dist/web/static/pretendard.css');
.no-scrollbar::-webkit-scrollbar {
    display: none;
}
.no-scrollbar {
    -ms-overflow-style: none;
    scrollbar-width: none;
}
</style>
