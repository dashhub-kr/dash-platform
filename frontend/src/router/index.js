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
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

// Global Navigation Guard
router.beforeEach(async (to, from, next) => {
  const { user, authChecked, refresh } = useAuth();

  // 1. Ensure authentication state is loaded
  if (!authChecked.value) {
    await refresh();
  }

  const isAuthenticated = !!user.value;
  const isPublicRoute = to.meta.public;

  // 2. Unauthenticated User Handling
  if (!isAuthenticated) {
    if (to.meta.requiresAuth) {
      // Accessing protected route -> Redirect to Landing
      return next('/');
    }
    // Accessing public route -> Allow
    return next();
  }

  // 3. Authenticated User Handling
  const isPlatformConnected = user.value &&
    (user.value.solvedacId || user.value.solvedacHandle) &&
    user.value.repositoryName;

  if (!isPlatformConnected) {
    // Case 3-1: Incomplete Profile (No Git/Solved.ac)
    // Block access to private routes except Onboarding
    if (to.path !== '/onboarding' && to.meta.requiresAuth) {
      return next('/onboarding');
    }
    return next();
  }

  // Case 3-2: Platform Connected
  // Prevent re-entry to Onboarding
  if (to.path === '/onboarding') {
    return next(user.value.studyId ? '/dashboard' : '/training/roadmap');
  }

  // Admin Check
  if (to.meta.requiresAdmin && user.value.role !== 'ROLE_ADMIN') {
      alert("관리자 권한이 필요합니다.");
      return next('/');
  }

  // Check Study Membership for specific routes
  const studyRoutes = ['/dashboard', '/study/missions', '/study/analysis'];
  const isStudyRoute = studyRoutes.includes(to.path);

  if (isStudyRoute && !user.value.studyId && !to.meta.requiresAdmin) {
    if (user.value.hasPendingApplication) {
        // Redirect to Roadmap but can show a toast or banner there, or just let them wait
        // Maybe redirect to /training/roadmap?pending=true
        return next('/training/roadmap?pending=true');
    }
    // If trying to access study routes without a study, redirect to Roadmap
    return next('/training/roadmap');
  }

  // Redirect Landing to appropriate home
  if (to.path === '/') {
    return next(user.value.studyId ? '/dashboard' : '/training/roadmap');
  }

  next();
});

export default router;
