import { createRouter, createWebHistory } from "vue-router";
import Landing from "../components/Landing.vue";

const OauthRedirect = () => import("../views/OauthRedirect.vue");
const Playground = () => import("../components/Playground.vue");

const routes = [
  { path: "/", name: "Landing", component: Landing },
  { path: "/playground", name: "Playground", component: Playground },
  { path: "/oauth2/redirect", name: "OauthRedirect", component: OauthRedirect },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

export default router;
