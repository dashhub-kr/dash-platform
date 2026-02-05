<template>
  <div class="space-y-6" :class="{ 'h-full': isOnboarding }">
    <!-- 헤더 (온보딩 여부에 따라 표시) -->
    <div v-if="!isOnboarding" class="mb-8 flex items-center justify-between">
      <div>
        <div class="flex items-center gap-3 mb-2">
           <Compass class="w-7 h-7 text-brand-500" stroke-width="2.5" fill="currentColor" />
           <h1 class="text-xl font-black text-slate-800">스터디 찾기</h1>
        </div>
        <p class="text-slate-500 font-medium">관심 있는 스터디를 찾아보세요</p>
      </div>
      <button
          v-if="user?.studyType === 'PERSONAL'"
          @click="showCreateModal = true"
          class="flex items-center gap-2 px-5 py-2.5 bg-brand-600 hover:bg-brand-700 text-white rounded-xl font-bold transition-all shadow-md shadow-brand-200 hover:-translate-y-0.5 shrink-0"
        >
          <Plus :size="18" stroke-width="3" />
          스터디 만들기
      </button>
    </div>

    <!-- 로딩 -->
    <div v-if="loading" class="flex flex-col items-center justify-center py-20">
      <div class="w-12 h-12 border-4 border-brand-200 border-t-brand-600 rounded-full animate-spin mb-4"></div>
      <p class="text-slate-400 font-medium animate-pulse">스터디 목록을 불러오는 중...</p>
    </div>

    <div v-else :class="{ 'flex flex-col h-full overflow-hidden': isOnboarding }">
      <!-- 검색 창 (BoardList 스타일) - 온보딩에서만 표시 -->
      <div v-if="isOnboarding" class="animate-fade-in-up mb-8">
          <div class="flex gap-3">
            <div class="flex-1 relative">
              <Search :size="18" class="absolute left-4 top-1/2 -translate-y-1/2 text-slate-400" />
              <input 
                v-model="searchKeyword"
                @keyup.enter="searchStudy"
                type="text" 
                placeholder="관심 있는 스터디 주제나 이름을 검색해보세요" 
                class="w-full pl-12 pr-4 py-3 bg-white border border-slate-200 rounded-xl text-slate-700 placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-brand-400 focus:border-transparent shadow-sm font-medium"
              />
            </div>
            <button 
              @click="searchStudy" 
              class="px-6 py-3 bg-slate-100 hover:bg-slate-200 text-slate-700 rounded-xl font-bold transition-colors shadow-sm"
            >
              검색
            </button>
            <button 
              v-if="searchKeyword" 
              @click="resetSearch" 
              class="px-4 py-3 text-slate-500 hover:text-slate-700 transition-colors"
            >
              초기화
            </button>
          </div>
          
          <p v-if="searchError" class="text-red-500 text-sm mt-2 ml-2 flex items-center gap-1 font-medium animate-shake">
              <AlertCircle class="w-4 h-4" /> {{ searchError }}
          </p>
      </div>

      <div v-if="isOnboarding" class="flex justify-end -mt-4 mb-8 pr-2">
          <button @click="handleCreatePersonalStudy" class="text-xs text-slate-500 hover:text-brand-600 underline underline-offset-4 flex items-center gap-1 transition-colors">
              일단 혼자 시작할래요 <ArrowRight :size="12" />
          </button>
      </div>

      <!-- 검색창 제외 나머지 영역에 스크롤 적용 -->
      <div :class="{ 'flex-1 min-h-0 overflow-y-auto custom-scrollbar px-1 -mx-1 pb-2': isOnboarding }">
      <!-- 추천 스터디 섹션 -->
      <div v-if="recommendedStudies.length > 0" class="mb-12">
        <div class="bg-white rounded-3xl p-6 border border-slate-200 shadow-sm">
            <div class="mb-6">
              <h2 class="text-lg font-bold text-slate-800 flex items-center gap-2">
                 <Sparkles class="w-5 h-5 text-violet-500" fill="currentColor" />
                 맞춤 추천 스터디
              </h2>
            </div>
    
            <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div v-for="(study, idx) in recommendedStudies" :key="'rec-'+study.id" 
                   class="bg-white rounded-3xl p-6 border-2 border-violet-100 shadow-sm transition-all group relative overflow-visible flex flex-col h-full ring-4 ring-transparent hover:shadow-xl hover:-translate-y-1 hover:ring-violet-50"
                   :class="{ 'z-50': openMemberPopup === study.id }">
                
                <!-- 추천 배지 -->
                 <div class="absolute top-4 right-4 bg-violet-100 text-violet-600 text-[10px] font-bold px-2 py-1 rounded-full uppercase tracking-wider">
                    Recommended
                 </div>
    
                <!-- 상단 정보 -->
                <div class="relative z-30 mb-4 mt-2">
                  <div class="flex items-center gap-2 mb-3">
                      <!-- 아바타 영역 전체 클릭 가능 -->
                      <div @click="toggleMemberPopup(study.id, $event)" class="flex items-center -space-x-2 cursor-pointer relative">
                         <template v-for="member in (study.memberPreviews || []).slice(0, 11)" :key="member.userId">
                            <div class="relative group/avatar">
                               <NicknameRenderer :username="member.username" :avatar-url="member.avatarUrl" avatar-class="w-7 h-7 border-2 border-white shadow-sm hover:ring-2 hover:ring-violet-300" text-class="hidden" :show-avatar="true" />
                               <div class="absolute left-1/2 -translate-x-1/2 -top-8 bg-slate-800 text-white text-[10px] font-bold px-2 py-1 rounded-lg opacity-0 group-hover/avatar:opacity-100 transition-opacity whitespace-nowrap pointer-events-none z-50">{{ member.username }}</div>
                            </div>
                         </template>
                         <div v-if="study.memberCount > 11" class="w-7 h-7 rounded-full bg-slate-200 border-2 border-white flex items-center justify-center text-[10px] font-bold text-slate-600 hover:bg-slate-300 transition-colors">···</div>
                         
                         <!-- Member Popup -->
                         <div v-if="openMemberPopup === study.id" 
                              @click.stop 
                              class="absolute top-full left-0 mt-2 bg-white text-slate-800 rounded-2xl z-50 min-w-[200px] max-h-[300px] overflow-y-auto shadow-xl ring-1 ring-slate-900/5 p-3 animate-in fade-in zoom-in-95 duration-200 origin-top-left"
                              style="scrollbar-width: thin;">
                            <div class="text-slate-400 text-[10px] uppercase tracking-wider mb-2 pb-2 border-b border-slate-100 font-bold flex justify-between items-center bg-white sticky top-0">
                                <span>스터디원 ({{ study.memberCount }}명)</span>
                                <button @click.stop="closeMemberPopup" class="hover:bg-slate-100 p-1 rounded-full"><X :size="12" /></button>
                            </div>
                            <div v-for="member in (study.memberPreviews || [])" :key="member.userId" class="flex items-center gap-3 py-2 border-b border-slate-50 last:border-0 hover:bg-slate-50 rounded-lg px-2 transition-colors">
                               <NicknameRenderer 
                                    :username="member.username" 
                                    :avatar-url="member.avatarUrl" 
                                    :user-id="member.userId"
                                    :clickable="true"
                                    :enable-decoration="true"
                                    :decoration-class="member.decorationClass"
                                    avatar-class="w-8 h-8" 
                                    text-class="text-sm font-medium text-slate-700" 
                                    :show-avatar="true" 
                                />
                            </div>
                         </div>

                      </div>
                      <span class="text-xs font-medium text-slate-400 ml-1">{{ study.memberCount }}명</span>
                   </div>
    
                   <h3 class="text-xl font-bold text-slate-900 mb-2 truncate pr-8 group-hover:text-violet-600 transition-colors">
                     {{ study.name }}
                   </h3>
                   <p class="text-slate-500 text-sm line-clamp-2 h-10 leading-relaxed">
                     {{ study.description || '스터디 소개가 없습니다.' }}
                   </p>
                </div>
    
                <!-- 통계 정보 -->
                <div class="flex flex-wrap items-center py-3 mb-6 relative gap-2">
                   <!-- Tier -->
                   <div class="flex-1 min-w-[100px] flex items-center justify-center gap-2 px-3 py-2 rounded-xl cursor-default relative group/tier hover:bg-slate-50 transition-colors">
                      <img :src="`https://static.solved.ac/tier_small/${Math.round(study.averageTier || 0)}.svg`" class="w-6 h-6 object-contain" alt="Tier" />
                      <span class="text-sm md:text-lg font-black text-slate-800 whitespace-nowrap">{{ study.tierBadge || 'Unranked' }}</span>
                      <!-- Tooltip -->
                      <div class="absolute left-1/2 -translate-x-1/2 bottom-full mb-2 bg-slate-800 text-white text-xs font-medium px-4 py-3 rounded-2xl opacity-0 group-hover/tier:opacity-100 transition-all pointer-events-none z-[9999] shadow-xl min-w-[160px]">
                         <div class="text-slate-400 text-[10px] uppercase tracking-wider mb-1">평균 티어</div>
                         <div class="text-base font-bold">{{ study.tierBadge || 'Unranked' }}</div>
                         <div class="absolute left-1/2 -translate-x-1/2 top-full w-0 h-0 border-l-8 border-r-8 border-t-8 border-l-transparent border-r-transparent border-t-slate-800"></div>
                      </div>
                   </div>
                   <!-- Streak -->
                   <div class="flex-none flex items-center justify-center gap-2 px-3 py-2 rounded-xl cursor-default relative group/streak hover:bg-slate-50 transition-colors">
                      <Flame class="w-6 h-6 text-orange-500 fill-orange-500" stroke-width="2" />
                      <span class="text-lg font-black text-slate-800 whitespace-nowrap">{{ study.streak || 0 }}</span>
                      <!-- Tooltip -->
                      <div class="absolute left-1/2 -translate-x-1/2 bottom-full mb-2 bg-slate-800 text-white text-xs font-medium px-4 py-3 rounded-2xl opacity-0 group-hover/streak:opacity-100 transition-all pointer-events-none z-[9999] shadow-xl min-w-[160px]">
                         <div class="text-slate-400 text-[10px] uppercase tracking-wider mb-1">연속 스트릭</div>
                         <div class="text-base font-bold">{{ study.streak || 0 }}일 연속</div>
                         <div class="absolute left-1/2 -translate-x-1/2 top-full w-0 h-0 border-l-8 border-r-8 border-t-8 border-l-transparent border-r-transparent border-t-slate-800"></div>
                      </div>
                   </div>
                   <!-- Activity -->
                   <div class="flex-none flex items-center justify-center gap-2 px-3 py-2 rounded-xl cursor-default relative group/activity hover:bg-slate-50 transition-colors">
                      <Send class="w-5 h-5 text-sky-500 fill-sky-500" stroke-width="2" />
                      <span class="text-lg font-black text-slate-800 whitespace-nowrap">{{ (study.averageSubmissionRate || 0).toFixed(0) }}</span>
                      <!-- Tooltip -->
                      <div class="absolute left-1/2 -translate-x-1/2 bottom-full mb-2 bg-slate-800 text-white text-xs font-medium px-4 py-3 rounded-2xl opacity-0 group-hover/activity:opacity-100 transition-all pointer-events-none z-[9999] shadow-xl min-w-[160px]">
                         <div class="text-slate-400 text-[10px] uppercase tracking-wider mb-1">최근 7일간 1인당 평균 풀이</div>
                         <div class="text-base font-bold">{{ (study.averageSubmissionRate || 0).toFixed(0) }}문제</div>
                         <div class="absolute left-1/2 -translate-x-1/2 top-full w-0 h-0 border-l-8 border-r-8 border-t-8 border-l-transparent border-r-transparent border-t-slate-800"></div>
                      </div>
                   </div>
                </div>
    
                 <!-- 하단 버튼 -->
                 <div class="mt-auto relative z-10">
                    <div v-if="userData?.studyId === study.id" 
                         class="w-full py-3 bg-brand-50 text-brand-600 font-bold rounded-xl text-center border border-brand-100 flex items-center justify-center gap-2">
                         <span class="w-2 h-2 rounded-full bg-brand-500 animate-pulse"></span>
                         참여 중
                    </div>
                    <div v-else-if="pendingApp && pendingApp.studyId === study.id" class="flex gap-2">
                         <div class="flex-1 py-3 bg-amber-50 text-amber-600 font-bold rounded-xl text-center border border-amber-100 flex items-center justify-center gap-2">
                            <span class="w-2 h-2 rounded-full bg-amber-500 animate-pulse"></span>
                            가입 대기중
                         </div>
                         <button @click="handleCancelApplication(pendingApp.id)" class="px-4 py-3 bg-slate-100 text-slate-500 font-bold rounded-xl hover:bg-slate-200 transition-colors">
                            취소
                         </button>
                    </div>
                    <button v-else-if="pendingApp" disabled
                            class="w-full py-3 bg-slate-100 text-slate-400 font-bold rounded-xl cursor-not-allowed border border-slate-200">
                       신청 진행 중
                    </button>
                    <button v-else-if="user?.studyId && user?.studyType !== 'PERSONAL'" 
                            @click="showLeaveRequiredAlert"
                            class="w-full py-3 bg-violet-600 hover:bg-violet-700 text-white font-bold rounded-xl shadow-md hover:shadow-lg transition-all flex items-center justify-center gap-2">
                       가입 신청 <ArrowRight class="w-4 h-4 opacity-50" />
                    </button>
                    <button v-else 
                            @click="openApplyModal(study)"
                            class="w-full py-3 bg-violet-600 hover:bg-violet-700 text-white font-bold rounded-xl shadow-md hover:shadow-lg transition-all flex items-center justify-center gap-2 group-hover:scale-[1.02]">
                       가입 신청 <ArrowRight class="w-4 h-4 opacity-50 group-hover:opacity-100 transition-opacity" />
                    </button>
                 </div>
              </div>
            </div>
        </div>
      </div>
      
      <!-- 추천 스터디 없음 (Empty State) - Card Style -->
      <div v-else class="mb-12">
        <div class="bg-white rounded-3xl p-6 border border-slate-200 shadow-sm">
            <div class="mb-6">
              <h2 class="text-lg font-bold text-slate-800 flex items-center gap-2">
                 <Sparkles class="w-5 h-5 text-violet-500" fill="currentColor" />
                 맞춤 추천 스터디
              </h2>
            </div>
            
             <div class="text-center py-12">
                 <div class="w-14 h-14 bg-slate-50 rounded-full flex items-center justify-center mx-auto mb-4 border border-slate-100">
                    <Search class="w-6 h-6 text-slate-300" />
                 </div>
                 <p class="text-slate-600 font-bold mb-1 text-sm">내 티어(±1)와 비슷한 스터디가 존재하지 않습니다</p>
             </div>
        </div>
      </div>

      <!-- 스터디 리스트 -->
      <div v-if="studies.length > 0">
        <div class="bg-white rounded-3xl p-6 border border-slate-200 shadow-sm">
             <div class="flex items-center justify-between mb-6">
               <h2 class="text-lg font-bold text-slate-800 flex items-center gap-2">
                 <Search class="w-5 h-5 text-sky-500" stroke-width="2.5" />
                 활동 중인 스터디
                 <span class="text-sm font-normal text-slate-400 ml-1">총 {{ studies.length }}개</span>
               </h2>
             </div>
    
             <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
          <div v-for="(study, idx) in studies" :key="study.id" 
               class="bg-white rounded-3xl p-6 border border-slate-200 shadow-sm transition-all group relative overflow-visible flex flex-col h-full"
               :class="openMemberPopup === study.id ? 'z-50' : 'hover:shadow-xl hover:-translate-y-1'">
            
            <!-- 랭킹 배지 (순서) -->
             <div class="absolute top-4 right-4 text-4xl font-black text-slate-100 italic pointer-events-none group-hover:text-brand-50 transition-colors">
                {{ idx + 1 }}
             </div>

            <!-- 상단 정보 -->
            <div class="relative z-30 mb-4">
                              <div class="flex items-center gap-2 mb-3">
                  <!-- 아바타 영역 전체 클릭 가능 -->
                  <div @click="toggleMemberPopup(study.id, $event)" class="flex items-center -space-x-2 cursor-pointer relative">
                     <template v-for="member in (study.memberPreviews || []).slice(0, 5)" :key="member.userId">
                        <div class="relative group/avatar">
                           <NicknameRenderer :username="member.username" :avatar-url="member.avatarUrl" avatar-class="w-7 h-7 border-2 border-white shadow-sm hover:ring-2 hover:ring-brand-300" text-class="hidden" :show-avatar="true" />
                           <div class="absolute left-1/2 -translate-x-1/2 -top-8 bg-slate-800 text-white text-[10px] font-bold px-2 py-1 rounded-lg opacity-0 group-hover/avatar:opacity-100 transition-opacity whitespace-nowrap pointer-events-none z-50">{{ member.username }}</div>
                        </div>
                     </template>
                     <div v-if="study.memberCount > 5" class="w-7 h-7 rounded-full bg-slate-200 border-2 border-white flex items-center justify-center text-[10px] font-bold text-slate-600 hover:bg-slate-300 transition-colors">+{{ study.memberCount - 5 }}</div>
                     
                     <!-- Member Popup (Active) -->
                     <div v-if="openMemberPopup === study.id" 
                          @click.stop 
                          class="absolute top-full left-0 mt-2 bg-white text-slate-800 rounded-2xl z-50 min-w-[200px] max-h-[300px] overflow-y-auto shadow-xl ring-1 ring-slate-900/5 p-3 animate-in fade-in zoom-in-95 duration-200 origin-top-left"
                          style="scrollbar-width: thin;">
                        <div class="text-slate-400 text-[10px] uppercase tracking-wider mb-2 pb-2 border-b border-slate-100 font-bold flex justify-between items-center bg-white sticky top-0">
                            <span>스터디원 ({{ study.memberCount }}명)</span>
                            <button @click.stop="closeMemberPopup" class="hover:bg-slate-100 p-1 rounded-full"><X :size="12" /></button>
                        </div>
                        <div v-for="member in (study.memberPreviews || [])" :key="member.userId" class="flex items-center gap-3 py-2 border-b border-slate-50 last:border-0 hover:bg-slate-50 rounded-lg px-2 transition-colors">
                           <NicknameRenderer 
                                :username="member.username" 
                                :avatar-url="member.avatarUrl" 
                                :user-id="member.userId"
                                :clickable="true"
                                :enable-decoration="true"
                                :decoration-class="member.decorationClass"
                                avatar-class="w-8 h-8" 
                                text-class="text-sm font-medium text-slate-700" 
                                :show-avatar="true" 
                            />
                        </div>
                     </div>

                  </div>
                  <span class="text-xs font-medium text-slate-400 ml-1">{{ study.memberCount }}명</span>
               </div>

               
               <h3 class="text-xl font-bold text-slate-900 mb-2 truncate pr-8 group-hover:text-brand-600 transition-colors">
                 {{ study.name }}
               </h3>
               <p class="text-slate-500 text-sm line-clamp-2 h-10 leading-relaxed">
                 {{ study.description || '스터디 소개가 없습니다.' }}
               </p>
            </div>

            <!-- 통계 정보 (Horizontal Divided Layout) -->
            <div class="flex flex-wrap items-center py-3 mb-6 relative gap-2">
               
               <!-- Tier (Solved.ac Icon) -->
               <div class="flex-1 min-w-[100px] flex items-center justify-center gap-2 px-3 py-2 rounded-xl cursor-default relative group/tier hover:bg-slate-50 transition-colors">
                  <img :src="`https://static.solved.ac/tier_small/${Math.round(study.averageTier || 0)}.svg`" class="w-6 h-6 object-contain" alt="Tier" />
                  <span class="text-sm md:text-lg font-black text-slate-800 whitespace-nowrap">{{ study.tierBadge || 'Unranked' }}</span>
                  <!-- Tooltip -->
                  <div class="absolute left-1/2 -translate-x-1/2 bottom-full mb-2 bg-slate-800 text-white text-xs font-medium px-4 py-3 rounded-2xl opacity-0 group-hover/tier:opacity-100 transition-all pointer-events-none z-50 shadow-xl min-w-[160px]">
                     <div class="text-slate-400 text-[10px] uppercase tracking-wider mb-1">평균 티어</div>
                     <div class="text-base font-bold">{{ study.tierBadge || 'Unranked' }}</div>
                     <div class="absolute left-1/2 -translate-x-1/2 top-full w-0 h-0 border-l-8 border-r-8 border-t-8 border-l-transparent border-r-transparent border-t-slate-800"></div>
                  </div>
               </div>

               <!-- Streak -->
               <div class="flex-none flex items-center justify-center gap-2 px-3 py-2 rounded-xl cursor-default relative group/streak hover:bg-slate-50 transition-colors">
                  <Flame class="w-6 h-6 text-orange-500 fill-orange-500" stroke-width="2" />
                  <span class="text-lg font-black text-slate-800 whitespace-nowrap">{{ study.streak || 0 }}</span>
                  <!-- Tooltip -->
                  <div class="absolute left-1/2 -translate-x-1/2 bottom-full mb-2 bg-slate-800 text-white text-xs font-medium px-4 py-3 rounded-2xl opacity-0 group-hover/streak:opacity-100 transition-all pointer-events-none z-50 shadow-xl min-w-[160px]">
                     <div class="text-slate-400 text-[10px] uppercase tracking-wider mb-1">연속 스트릭</div>
                     <div class="text-base font-bold">{{ study.streak || 0 }}일 연속</div>
                     <div class="absolute left-1/2 -translate-x-1/2 top-full w-0 h-0 border-l-8 border-r-8 border-t-8 border-l-transparent border-r-transparent border-t-slate-800"></div>
                  </div>
               </div>

               <!-- Activity -->
               <div class="flex-none flex items-center justify-center gap-2 px-3 py-2 rounded-xl cursor-default relative group/activity hover:bg-slate-50 transition-colors">
                  <Send class="w-5 h-5 text-sky-500 fill-sky-500" stroke-width="2" />
                  <span class="text-lg font-black text-slate-800 whitespace-nowrap">{{ (study.averageSubmissionRate || 0).toFixed(0) }}</span>
                  <!-- Tooltip -->
                  <div class="absolute left-1/2 -translate-x-1/2 bottom-full mb-2 bg-slate-800 text-white text-xs font-medium px-4 py-3 rounded-2xl opacity-0 group-hover/activity:opacity-100 transition-all pointer-events-none z-50 shadow-xl min-w-[160px]">
                     <div class="text-slate-400 text-[10px] uppercase tracking-wider mb-1">최근 7일간 1인당 평균 풀이 수</div>
                     <div class="text-base font-bold">{{ (study.averageSubmissionRate || 0).toFixed(0) }}문제</div>
                     <div class="absolute left-1/2 -translate-x-1/2 top-full w-0 h-0 border-l-8 border-r-8 border-t-8 border-l-transparent border-r-transparent border-t-slate-800"></div>
                  </div>
               </div>
            </div>

             <!-- 하단 버튼 -->
             <div class="mt-auto relative z-10">
                <!-- 내 스터디 -->
                <div v-if="user?.studyId === study.id" 
                     class="w-full py-3 bg-brand-50 text-brand-600 font-bold rounded-xl text-center border border-brand-100 flex items-center justify-center gap-2">
                     <span class="w-2 h-2 rounded-full bg-brand-500 animate-pulse"></span>
                     참여 중
                </div>

                <!-- 가입 대기 중 -->
                <div v-else-if="pendingApp && pendingApp.studyId === study.id" class="flex gap-2">
                     <div class="flex-1 py-3 bg-amber-50 text-amber-600 font-bold rounded-xl text-center border border-amber-100 flex items-center justify-center gap-2">
                        <span class="w-2 h-2 rounded-full bg-amber-500 animate-pulse"></span>
                        가입 대기중
                     </div>
                     <button @click="handleCancelApplication(pendingApp.id)" class="px-4 py-3 bg-slate-100 text-slate-500 font-bold rounded-xl hover:bg-slate-200 transition-colors">
                        취소
                     </button>
                </div>

                <button v-else-if="pendingApp" disabled
                        class="w-full py-3 bg-slate-100 text-slate-400 font-bold rounded-xl cursor-not-allowed border border-slate-200">
                   신청 진행 중
                </button>
                <button v-else-if="user?.studyId && user?.studyType !== 'PERSONAL'" 
                        @click="showLeaveRequiredAlert"
                        class="w-full py-3 bg-brand-600 hover:bg-brand-700 text-white font-bold rounded-xl shadow-md hover:shadow-lg transition-all flex items-center justify-center gap-2">
                   가입 신청 <ArrowRight class="w-4 h-4 opacity-50" />
                </button>

                <!-- 신청하기 -->
                <button v-else 
                        @click="openApplyModal(study)"
                        class="w-full py-3 bg-slate-900 hover:bg-brand-600 text-white font-bold rounded-xl shadow-md hover:shadow-lg transition-all flex items-center justify-center gap-2 group-hover:scale-[1.02]">
                   가입 신청 <ArrowRight class="w-4 h-4 opacity-50 group-hover:opacity-100 transition-opacity" />
                </button>
             </div>
          </div>
        </div>
      </div>
      </div>

      <!-- 빈 상태 -->
      <div v-else class="text-center py-24 bg-white rounded-3xl border border-slate-200 shadow-sm">
        <div class="w-16 h-16 bg-slate-50 rounded-full flex items-center justify-center mx-auto mb-4">
           <Search class="w-8 h-8 text-slate-300" />
        </div>
        <p class="text-slate-500 font-medium text-lg mb-6">등록된 스터디가 없습니다</p>
        <button v-if="!isOnboarding" @click="$router.back()" class="px-6 py-2.5 bg-slate-100 hover:bg-slate-200 text-slate-600 font-bold rounded-xl transition-colors">
          뒤로가기
        </button>
      </div>
    </div> 
    <!-- 스크롤 래퍼 종료 -->

       <!-- 가입 신청 모달 (기존 위치 고정) -->
      <Teleport to="body">
        <div v-if="showModal" class="fixed inset-0 z-50 flex items-center justify-center p-4">
          <div class="absolute inset-0 bg-slate-900/40 backdrop-blur-sm transition-opacity" @click="closeModal"></div>
          <div class="bg-white rounded-3xl p-8 w-full max-w-lg relative z-10 shadow-2xl animate-fade-in-up transform transition-all scale-100">
            
            <div class="text-center mb-8">
               <div class="w-16 h-16 bg-brand-50 rounded-full flex items-center justify-center mx-auto mb-4 border-4 border-white shadow-lg">
                  <Send class="w-8 h-8 text-brand-600 -ml-1 translate-y-0.5" />
               </div>
               <h3 class="text-2xl font-bold text-slate-900 mb-2">가입 신청 보내기</h3>
               <p class="text-slate-500">
                 <span class="font-bold text-brand-600 bg-brand-50 px-2 py-0.5 rounded-md">{{ selectedStudy?.name }}</span> 
                 스터디장에게 메시지를 남겨주세요
               </p>
            </div>
            
            <div class="space-y-4 mb-8">
               <label class="block text-sm font-bold text-slate-700">자기소개 및 각오</label>
               <textarea 
                 v-model="applyMessage"
                 class="w-full h-40 bg-slate-50 border border-slate-200 rounded-2xl p-5 text-slate-800 placeholder:text-slate-400 focus:outline-none focus:border-brand-500 focus:ring-4 focus:ring-brand-500/10 resize-none font-medium transition-all"
                 placeholder="안녕하세요! 매일 1문제씩 꾸준히 풀고 싶어서 신청합니다."
               ></textarea>
            </div>
    
            <div class="flex gap-3">
              <button @click="closeModal" class="flex-1 py-3.5 bg-white border border-slate-200 text-slate-600 font-bold rounded-xl hover:bg-slate-50 hover:text-slate-800 transition-colors">
                 취소
              </button>
              <button @click="submitApplication" 
                      :disabled="applying || !applyMessage.trim()"
                      class="flex-1 py-3.5 bg-brand-600 text-white font-bold rounded-xl hover:bg-brand-700 transition-all shadow-lg shadow-brand-200 disabled:opacity-50 disabled:shadow-none flex items-center justify-center gap-2">
                 <div v-if="applying" class="w-5 h-5 border-2 border-white/30 border-t-white rounded-full animate-spin"></div>
                 <span v-else>신청하기</span>
              </button>
            </div>
          </div>
        </div>
      </Teleport>

      <!-- Create Study Modal -->
      <Teleport to="body">
        <div v-if="showCreateModal" class="fixed inset-0 z-[9999] flex items-center justify-center p-4">
          <div class="absolute inset-0 bg-slate-900/50 backdrop-blur-sm" @click="showCreateModal = false"></div>
          
          <div class="relative bg-white rounded-3xl w-full max-w-lg p-8 shadow-2xl animate-in fade-in zoom-in-95 duration-200">
            <h2 class="text-2xl font-black text-slate-800 mb-6 flex items-center gap-3">
              <div class="w-10 h-10 bg-brand-100 rounded-xl flex items-center justify-center">
                <Plus class="w-5 h-5 text-brand-600" stroke-width="3" />
              </div>
              새 스터디 만들기
            </h2>
            
            <div class="space-y-5">
              <!-- Name -->
              <div>
                <label class="block text-sm font-bold text-slate-600 mb-2">스터디 이름 *</label>
                <input 
                  v-model="newStudy.name"
                  type="text"
                  placeholder="예: 알고리즘 마스터즈"
                  class="w-full px-4 py-3 bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:border-brand-500 focus:ring-4 focus:ring-brand-500/10 transition-all font-medium"
                />
              </div>
              
              <!-- Description -->
              <div>
                <label class="block text-sm font-bold text-slate-600 mb-2">스터디 소개</label>
                <textarea 
                  v-model="newStudy.description"
                  rows="3"
                  placeholder="스터디를 소개해주세요"
                  class="w-full px-4 py-3 bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:border-brand-500 focus:ring-4 focus:ring-brand-500/10 transition-all font-medium resize-none"
                ></textarea>
              </div>
            </div>
            
            <div class="flex gap-3 mt-8">
              <button 
                @click="showCreateModal = false"
                class="flex-1 py-3.5 bg-white border border-slate-200 text-slate-600 font-bold rounded-xl hover:bg-slate-50 transition-colors"
              >
                취소
              </button>
              <button 
                @click="handleCreateStudy"
                :disabled="creatingStudy || !newStudy.name.trim()"
                class="flex-1 py-3.5 bg-brand-600 text-white font-bold rounded-xl hover:bg-brand-700 transition-all shadow-lg shadow-brand-200 disabled:opacity-50 disabled:shadow-none flex items-center justify-center gap-2"
              >
                <div v-if="creatingStudy" class="w-5 h-5 border-2 border-white/30 border-t-white rounded-full animate-spin"></div>
                <span v-else>스터디 만들기</span>
              </button>
            </div>
          </div>
        </div>
      </Teleport>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed, watch } from 'vue';
import { useRouter } from 'vue-router';
import axios from 'axios';
import { useAuth } from '@/composables/useAuth';
import { Trophy, Flame, Users, Search, Activity, ArrowRight, Send, Sparkles, Compass, AlertCircle, Plus, Eye, X } from 'lucide-vue-next'; // added X
import NicknameRenderer from '@/components/common/NicknameRenderer.vue';
import { studyApi } from '@/api/study';

const props = defineProps({
  isOnboarding: {
    type: Boolean,
    default: false
  },
  externalSearchKeyword: {
    type: String,
    default: ''
  }
});

const emit = defineEmits(['apply-success']);

const { user } = useAuth();
const router = useRouter();
const loading = ref(true);
const studies = ref([]);
const showModal = ref(false);
const selectedStudy = ref(null);
const applyMessage = ref('');

const applying = ref(false);

const searchKeyword = ref('');
const searchError = ref('');

// 외부 검색 키워드 동기화
watch(() => props.externalSearchKeyword, (newVal) => {
    if (newVal !== searchKeyword.value) {
        searchKeyword.value = newVal;
        searchStudy();
    }
});

const pendingApp = ref(null);

// Member popup state
const openMemberPopup = ref(null);
const closeMemberPopup = () => { openMemberPopup.value = null; };
const toggleMemberPopup = (studyId, event) => {
    event.stopPropagation();
    if (openMemberPopup.value === studyId) {
        openMemberPopup.value = null;
    } else {
        openMemberPopup.value = studyId;
    }
};

const isAdmin = computed(() => {
    return user.value?.role === 'ROLE_ADMIN' || user.value?.role === 'ADMIN'; 
});

const observeStudy = (studyId) => {
    router.push(`/admin/study/${studyId}/dashboard`);
};

// Create Study Modal State
const showCreateModal = ref(false);
const creatingStudy = ref(false);
const newStudy = ref({
  name: '',
  description: '',
  visibility: 'PUBLIC'
});

const checkMyApplication = async () => {
    try {
        const res = await studyApi.getMyApplication();
        if (res.data && res.data.status === 'PENDING') {
            pendingApp.value = res.data;
        } else {
            pendingApp.value = null;
        }
    } catch (e) {
        pendingApp.value = null;
    }
};

const handleCancelApplication = async (appId) => {
    if (!confirm('가입 신청을 취소하시겠습니까?')) return;
    try {
        await studyApi.cancelApplication(appId);
        pendingApp.value = null;
        alert('가입 신청이 취소되었습니다.');
        // If onboarded, user might want to apply to others right away.
    } catch (e) {
        console.error(e);
        alert('취소에 실패했습니다.');
    }
};

const fetchAllStudies = async () => {
    loading.value = true;
    searchError.value = '';
    try {
        const res = await axios.get('/api/studies');
        studies.value = res.data;
    } catch (e) {
        console.error('스터디 목록 로드 실패', e);
    } finally {
        loading.value = false;
    }
};

const searchStudy = async () => {
    if (!searchKeyword.value.trim()) {
        await fetchAllStudies();
        return;
    }
    
    loading.value = true;
    searchError.value = '';
    
    try {
        const res = await axios.get('/api/studies', { params: { keyword: searchKeyword.value } });
        studies.value = res.data;
        if (res.data.length === 0) {
            searchError.value = '검색 결과가 없습니다.';
        }
    } catch (e) {
        console.error('검색 실패', e);
        studies.value = [];
        searchError.value = '검색 중 오류가 발생했습니다.';
    } finally {
        loading.value = false;
    }
};

const resetSearch = async () => {
    searchKeyword.value = '';
    await fetchAllStudies();
};

const recommendedStudies = computed(() => {
  if (!user.value || !user.value.solvedacTier) return [];
  
  const userTier = user.value.solvedacTier;
  return studies.value.filter(study => {
    // 평균 티어가 없는 경우 제외
    if (!study.averageTier) return false;
    
    // 내 스터디 제외
    if (study.id === user.value.studyId) return false;

    // 내 티어 기준 ±1 범위 내
    const diff = Math.abs(study.averageTier - userTier);
    return diff <= 1;
  });
});

onMounted(() => {
  fetchAllStudies();
  if (user.value && (!user.value.studyId || user.value.studyType === 'PERSONAL')) {
      checkMyApplication();
  }
  // 외부 클릭 시 팝업 닫기
  document.addEventListener('click', closeMemberPopup);
});

onUnmounted(() => {
  document.removeEventListener('click', closeMemberPopup);
});

const openApplyModal = (study) => {
  selectedStudy.value = study;
  applyMessage.value = '';
  showModal.value = true;
};

const closeModal = () => {
  showModal.value = false;
  selectedStudy.value = null;
};

const showLeaveRequiredAlert = () => {
  alert('현재 가입된 스터디가 있습니다.\n\n다른 스터디에 가입하려면 먼저 기존 스터디를 탈퇴해야 합니다.\n\n[프로필 > 스터디 탈퇴] 에서 탈퇴 가능합니다.');
};

const submitApplication = async () => {
  if (!selectedStudy.value) return;
  
  applying.value = true;
  try {
    await axios.post(`/api/studies/${selectedStudy.value.id}/apply`, {
      message: applyMessage.value
    });
    
    // Refresh pending status immediately
    await checkMyApplication();

    closeModal();
    // 온보딩 중이면 alert 대신 success 이벤트 발생
    if (props.isOnboarding) {
        emit('apply-success');
    } else {
        alert('가입 신청이 전송되었습니다! 스터디장의 승인을 기다려주세요.');
    }
  } catch (e) {
    console.error('신청 실패', e);
    
    // 이미 신청한 경우 등 에러 처리
    if (e.response && e.response.status === 409) {
        alert('이미 가입 신청을 보냈거나, 가입된 상태입니다.');
    } else {
        alert('신청 중 오류가 발생했습니다.');
    }
  } finally {
    applying.value = false;
  }
};

const { refresh } = useAuth();

const handleCreateStudy = async () => {
  if (!newStudy.value.name.trim()) return;
  
  creatingStudy.value = true;
  try {
    await axios.post('/api/studies', {
      name: newStudy.value.name,
      description: newStudy.value.description,
      visibility: newStudy.value.visibility
    });
    
    showCreateModal.value = false;
    newStudy.value = { name: '', description: '', visibility: 'PUBLIC' };
    
    // Refresh user session to get new studyId
    await refresh();
    
    alert('스터디가 생성되었습니다! 🎉');
    
    // Redirect to missions page
    window.location.href = '/study/missions';
  } catch (e) {
    console.error('스터디 생성 실패', e);
    alert(e.response?.data?.message || '스터디 생성 중 오류가 발생했습니다.');
  } finally {
    creatingStudy.value = false;
  }
};

const handleCreatePersonalStudy = async () => {
  if (!confirm('나만의 연구실(개인 스터디)을 생성하시겠습니까?\n이후 다른 스터디에 가입할 수 있습니다.')) return;
  
  try {
    await studyApi.createPersonalStudy();
    await refresh();
    alert('나만의 연구실이 생성되었습니다!');
    // Redirect to study missions
    window.location.href = '/study/missions';
  } catch (e) {
    console.error('Failed to create personal study', e);
    alert('개인 스터디 생성에 실패했습니다.');
  }
};
</script>

<style scoped>
@import url('https://cdn.jsdelivr.net/gh/orioncactus/pretendard/dist/web/static/pretendard.css');

@keyframes bounce-slow {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-8px); }
}
.animate-bounce-slow {
  animation: bounce-slow 3s ease-in-out infinite;
}

/* 커스텀 스크롤바 스타일 (온보딩 전용) */
.custom-scrollbar::-webkit-scrollbar {
  width: 6px;
}
.custom-scrollbar::-webkit-scrollbar-track {
  background: transparent;
  margin: 1.5rem; 
}
.custom-scrollbar::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 3px;
}
.custom-scrollbar::-webkit-scrollbar-thumb:hover {
  background: #94a3b8;
}
</style>
