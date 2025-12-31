import { createRouter, createWebHistory } from "vue-router";
import LandingView from "../views/landing/LandingView.vue";

const OauthRedirect = () => import("../views/auth/OauthRedirect.vue");
const Playground = () => import("../components/playground/Playground.vue");

const Dashboard = () => import("../views/dashboard/DashboardView.vue");
const BoardList = () => import("../views/board/BoardList.vue");
const BoardForm = () => import("../views/board/BoardForm.vue");
const BoardDetail = () => import("../views/board/BoardDetail.vue");
const Profile = () => import("../views/user/ProfileView.vue");
const YouTubeSearch = () => import("../views/utils/YouTubeSearch.vue");
const StudySimCity = () => import("../views/study/StudySimCity.vue");

const routes = [
  { path: "/", name: "Landing", component: LandingView },
  { path: "/playground", name: "Playground", component: Playground },
  { path: "/oauth2/redirect", name: "OauthRedirect", component: OauthRedirect },
  { path: "/onboarding", name: "Onboarding", component: () => import("../views/onboarding/OnboardingView.vue") },
  { path: "/dashboard", name: "Dashboard", component: Dashboard },
  { path: "/boards", name: "BoardList", component: BoardList },
  { path: "/boards/write", name: "BoardWrite", component: BoardForm },
  { path: "/boards/:id", name: "BoardDetail", component: BoardDetail },
  { path: "/boards/edit/:id", name: "BoardEdit", component: BoardForm },
  { path: "/profile", name: "Profile", component: Profile },
  { path: "/youtube", name: "YouTubeSearch", component: YouTubeSearch },
  { path: "/training", redirect: "/training/roadmap" },
  { path: "/training/roadmap", name: "TrainingRoadmapView", component: () => import("../views/training/TrainingRoadmapView.vue") },
  { path: "/training/skilltree", name: "TrainingSkillTreeView", component: () => import("../views/training/TrainingSkillTreeView.vue") },
  { path: "/defense", name: "DefenseView", component: () => import("../views/defense/DefenseView.vue") },
  { path: "/mockexam", name: "MockExamView", component: () => import("../views/mockexam/MockExamView.vue") },
  { path: "/study/analysis", name: "StudyAnalysisView", component: () => import("../views/study/StudyAnalysisView.vue") },
  { path: "/study/missions", name: "StudyMissionView", component: () => import("../views/study/StudyMissionView.vue") },
  { path: "/study/ranking", name: "StudyListView", component: () => import("../views/study/StudyListView.vue") },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

export default router;
