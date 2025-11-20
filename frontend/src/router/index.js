import { createRouter, createWebHistory } from 'vue-router'
import Landing from '../components/Landing.vue'

const OauthRedirect = () => import('../views/OauthRedirect.vue')

const routes = [
  { path: '/', name: 'Landing', component: Landing },
  { path: '/oauth2/redirect', name: 'OauthRedirect', component: OauthRedirect },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

export default router
