import { createRouter, createWebHistory } from "vue-router";
import Landing from "../components/Landing.vue";

const OauthRedirect = () => import("../views/OauthRedirect.vue");
const Playground = () => import("../components/Playground.vue");

const Dashboard = () => import("../views/Dashboard.vue");
const BoardList = () => import("../views/BoardList.vue");
const BoardForm = () => import("../views/BoardForm.vue");
const BoardDetail = () => import("../views/BoardDetail.vue");
const Profile = () => import("../views/Profile.vue");
const YouTubeSearch = () => import("../views/YouTubeSearch.vue");
const StudySimCity = () => import("../views/StudySimCity.vue");

const routes = [
  { path: "/", name: "Landing", component: Landing },
  { path: "/playground", name: "Playground", component: Playground },
  { path: "/oauth2/redirect", name: "OauthRedirect", component: OauthRedirect },
  { path: "/onboarding/welcome", name: "OnboardingWelcome", component: () => import("../views/onboarding/OnboardingWelcome.vue") },
  { path: "/onboarding/analysis", name: "OnboardingAnalysis", component: () => import("../views/onboarding/OnboardingAnalysis.vue") },
  { path: "/onboarding/study", name: "OnboardingStudy", component: () => import("../views/onboarding/OnboardingStudy.vue") },
  { path: "/onboarding/repo", name: "OnboardingRepository", component: () => import("../views/onboarding/OnboardingRepository.vue") },
  { path: "/dashboard", name: "Dashboard", component: Dashboard },
  { path: "/boards", name: "BoardList", component: BoardList },
  { path: "/boards/write", name: "BoardWrite", component: BoardForm },
  { path: "/boards/:id", name: "BoardDetail", component: BoardDetail },
  { path: "/boards/edit/:id", name: "BoardEdit", component: BoardForm },
  { path: "/profile", name: "Profile", component: Profile },
  { path: "/youtube", name: "YouTubeSearch", component: YouTubeSearch },
  { path: "/simcity", name: "StudySimCity", component: StudySimCity },
  { path: "/map", name: "WorldMap", component: () => import("../components/WorldMap.vue") },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

export default router;
