import { ref, onMounted } from "vue";
import { userApi } from "../api/user";

const user = ref(null);
const authChecked = ref(false);

export function useAuth() {
  const refresh = async () => {
    try {
      const res = await userApi.getMyProfile();
      user.value = res.data;
    } catch (e) {
      user.value = null;
    } finally {
      authChecked.value = true;
    }
  };

  if (!authChecked.value && !user.value) {
    refresh();
  }

  const logout = async () => {
    try {
      await import("../api/auth").then(m => m.authApi.logout());
    } catch (e) {
      console.error("Logout failed", e);
    } finally {
      user.value = null;
      authChecked.value = false;
      window.location.href = "/";
    }
  };

  return { user, authChecked, refresh, logout };
}

