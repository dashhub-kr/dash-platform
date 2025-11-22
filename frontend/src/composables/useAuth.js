import { ref, onMounted } from "vue";

const user = ref(null);
const authChecked = ref(false);

export function useAuth() {
  const refresh = async () => {
    try {
      const res = await fetch(`/api/users/me`, {
        credentials: "include",
      });
      if (res.ok) user.value = await res.json();
      else user.value = null;
    } catch (e) {
      user.value = null;
    } finally {
      authChecked.value = true;
    }
  };

  onMounted(() => {
    if (!authChecked.value) refresh();
  });

  return { user, authChecked, refresh };
}
