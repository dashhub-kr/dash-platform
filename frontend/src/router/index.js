import { createRouter, createWebHistory } from "vue-router";
import Landing from "../components/Landing.vue";

const OauthRedirect = () => import("../views/OauthRedirect.vue");
const Playground = () => import("../components/Playground.vue");

const Dashboard = () => import("../views/Dashboard.vue");
const BoardList = () => import("../views/BoardList.vue");
const BoardForm = () => import("../views/BoardForm.vue");
const BoardDetail = () => import("../views/BoardDetail.vue");
const Profile = () => import("../views/Profile.vue");

const routes = [
  { path: "/", name: "Landing", component: Landing },
  { path: "/playground", name: "Playground", component: Playground },
  { path: "/oauth2/redirect", name: "OauthRedirect", component: OauthRedirect },
  { path: "/dashboard", name: "Dashboard", component: Dashboard },
  { path: "/boards", name: "BoardList", component: BoardList },
  { path: "/boards/write", name: "BoardWrite", component: BoardForm },
  { path: "/boards/:id", name: "BoardDetail", component: BoardDetail },
  { path: "/boards/edit/:id", name: "BoardEdit", component: BoardForm },
  { path: "/profile", name: "Profile", component: Profile },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

export default router;
