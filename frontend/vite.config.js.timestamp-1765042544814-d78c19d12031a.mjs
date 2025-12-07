import { defineConfig } from "file:///C:/dash/frontend/node_modules/vite/dist/node/index.js";
import vue from "file:///C:/dash/frontend/node_modules/@vitejs/plugin-vue/dist/index.mjs";
var vite_config_default = defineConfig({
  plugins: [vue()],
  server: {
    proxy: {
      "/api": "http://localhost:8080",
      "/oauth2/authorization": "http://localhost:8080",
      "/login": "http://localhost:8080"
    }
  }
});
export {
  vite_config_default as default
};
