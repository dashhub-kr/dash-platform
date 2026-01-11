import { createRouter, createWebHistory } from "vue-router";
import LandingView from "../views/landing/LandingView.vue";
import { useAuth } from "@/composables/useAuth";

const OauthRedirect = () => import("../views/auth/OauthRedirect.vue");
const Playground = () => import("../components/playground/Playground.vue");

const Dashboard = () => import("../views/dashboard/DashboardView.vue");
const BoardList = () => import("../views/board/BoardList.vue");
const BoardForm = () => import("../views/board/BoardForm.vue");
const BoardDetail = () => import("../views/board/BoardDetail.vue");
const Profile = () => import("../views/user/ProfileView.vue");
const YouTubeSearch = () => import("../views/utils/YouTubeSearch.vue");

const routes = [
  {
    path: "/",
    name: "Landing",
    component: LandingView,
    meta: { public: true }
  },
  {
    path: "/playground",
    name: "Playground",
    component: Playground,
    meta: { requiresAuth: true }
  },
  {
    path: "/oauth2/redirect",
    name: "OauthRedirect",
    component: OauthRedirect,
    meta: { public: true }
  },
  {
    path: "/onboarding",
    name: "Onboarding",
    component: () => import("../views/onboarding/OnboardingView.vue"),
    meta: { requiresAuth: true }
  },
  {
    path: "/dashboard",
    name: "Dashboard",
    component: Dashboard,
    meta: { requiresAuth: true }
  },
  {
    path: "/boards",
    name: "BoardList",
    component: BoardList,
    meta: { requiresAuth: true }
  },
  {
    path: "/boards/write",
    name: "BoardWrite",
    component: BoardForm,
    meta: { requiresAuth: true }
  },
  {
    path: "/boards/:id",
    name: "BoardDetail",
    component: BoardDetail,
    meta: { requiresAuth: true }
  },
  {
    path: "/boards/edit/:id",
    name: "BoardEdit",
    component: BoardForm,
    meta: { requiresAuth: true }
  },
  {
    path: "/profile",
    name: "Profile",
    component: Profile,
    meta: { requiresAuth: true }
  },
  {
    path: "/youtube",
    name: "YouTubeSearch",
    component: YouTubeSearch,
    meta: { requiresAuth: true }
  },
  {
    path: "/training",
    redirect: "/training/roadmap",
    meta: { requiresAuth: true }
  },
  {
    path: "/training/roadmap",
    name: "TrainingRoadmapView",
    component: () => import("../views/training/TrainingRoadmapView.vue"),
    meta: { requiresAuth: true }
  },
  {
    path: "/training/skilltree",
    name: "TrainingSkillTreeView",
    component: () => import("../views/training/TrainingSkillTreeView.vue"),
    meta: { requiresAuth: true }
  },
  {
    path: "/defense",
    name: "DefenseView",
    component: () => import("../views/defense/DefenseView.vue"),
    meta: { requiresAuth: true }
  },
  {
    path: "/mockexam",
    name: "MockExamView",
    component: () => import("../views/mockexam/MockExamView.vue"),
    meta: { requiresAuth: true }
  },
  {
    path: "/study/analysis",
    name: "StudyAnalysisView",
    component: () => import("../views/study/StudyAnalysisView.vue"),
    meta: { requiresAuth: true }
  },
  {
    path: "/study/missions",
    name: "StudyMissionView",
    component: () => import("../views/study/StudyMissionView.vue"),
    meta: { requiresAuth: true }
  },
  {
    path: "/study/ranking",
    name: "StudyListView",
    component: () => import("../views/study/StudyListView.vue"),
    meta: { requiresAuth: true }
  },
  {
    path: "/admin/study/:id/dashboard",
    name: "AdminStudyDashboard",
    component: Dashboard,
    props: (route) => ({ studyId: Number(route.params.id) }),
    meta: { requiresAuth: true, requiresAdmin: true }
  },
  {
    path: "/admin/study/:id/missions",
    name: "AdminStudyMissions",
    component: () => import("../views/study/StudyMissionView.vue"),
    props: (route) => ({ studyId: Number(route.params.id) }),
    meta: { requiresAuth: true, requiresAdmin: true }
  },
  {
    path: "/social",
    name: "SocialView",
    component: () => import("../views/social/SocialView.vue"),
    meta: { requiresAuth: true }
  },
  {
    path: "/admin",
    name: "AdminView",
    component: () => import("../views/admin/AdminView.vue"),
    meta: { requiresAuth: true, requiresAdmin: true }
  },

  {
    path: "/profile/decorations",
    name: "MyDecorations",
    component: () => import("../views/user/MyDecorations.vue"),
    meta: { requiresAuth: true }
  },
  {
    path: "/shop",
    name: "ShopView",
    component: () => import("../views/shop/ShopView.vue"),
    meta: { requiresAuth: true }
  },
  // Battle
  {
    path: "/battle/:id",
    name: "BattleLobby",
    component: () => import("../views/battle/BattleLobbyView.vue"),
    meta: { requiresAuth: true }
  },
  {
    path: "/battle/:id/play",
    name: "BattlePlay",
    component: () => import("../views/battle/BattlePlayView.vue"),
    meta: { requiresAuth: true }
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

// 글로벌 네비게이션 가드 (Global Navigation Guard)
router.beforeEach(async (to, from, next) => {
  const { user, authChecked, refresh } = useAuth();

  // 1. 인증 상태 로드 확인
  if (!authChecked.value) {
    await refresh();
  }

  const isAuthenticated = !!user.value;
  const isPublicRoute = to.meta.public;

  // 2. 미인증 사용자 처리
  if (!isAuthenticated) {
    if (to.meta.requiresAuth) {
      // 보호된 라우트 접근 -> Landing으로 리다이렉트
      return next('/');
    }
    // 공개 라우트 접근 -> 허용
    return next();
  }

  // 3. 인증된 사용자 처리
  const isPlatformConnected = user.value &&
    (user.value.solvedacId || user.value.solvedacHandle) &&
    user.value.repositoryName;

  if (!isPlatformConnected) {
    // Case 3-1: 프로필 미완성 (Git/Solved.ac 미연동)
    // Onboarding을 제외한 비공개 라우트 접근 차단
    if (to.path !== '/onboarding' && to.meta.requiresAuth) {
      return next('/onboarding');
    }
    return next();
  }

  // Case 3-2: 플랫폼 연동 완료
  // Onboarding 재진입 방지
  if (to.path === '/onboarding') {
    return next(user.value.studyId ? '/dashboard' : '/training/roadmap');
  }

  // 관리자 권한 확인
  if (to.meta.requiresAdmin && user.value.role !== 'ROLE_ADMIN') {
    alert("관리자 권한이 필요합니다.");
    return next('/');
  }

  // 특정 라우트에 대한 스터디 멤버십 확인
  const studyRoutes = ['/dashboard', '/study/missions', '/study/analysis'];
  const isStudyRoute = studyRoutes.includes(to.path);

  if (isStudyRoute && !user.value.studyId && !to.meta.requiresAdmin) {
    if (user.value.hasPendingApplication) {
      // 로드맵으로 리다이렉트하지만 토스트나 배너를 표시할 수 있음
      // 예: /training/roadmap?pending=true 로 리다이렉트
      return next('/training/roadmap?pending=true');
    }
    // 스터디 없이 스터디 라우트 접근 시 로드맵으로 리다이렉트
    return next('/training/roadmap');
  }

  // Landing을 적절한 홈으로 리다이렉트
  if (to.path === '/') {
    return next(user.value.studyId ? '/dashboard' : '/training/roadmap');
  }

  next();
});

export default router;
