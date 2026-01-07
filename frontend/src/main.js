import { createApp } from "vue";
import App from "./App.vue";
import "./styles.css";
import "@/assets/css/effects.css";
import router from "./router";
import { MotionPlugin } from '@vueuse/motion'

import { createPinia } from "pinia";

const app = createApp(App);

app.use(createPinia());
app.use(MotionPlugin);
app.use(router);
app.mount("#app");
