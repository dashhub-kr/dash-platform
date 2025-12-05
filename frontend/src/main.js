import { createApp } from "vue";
import App from "./App.vue";
import "./styles.css";
import router from "./router";

import { createPinia } from "pinia";

const app = createApp(App);

app.use(createPinia());
app.use(router);
app.mount("#app");
