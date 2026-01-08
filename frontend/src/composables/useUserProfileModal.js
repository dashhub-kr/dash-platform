import { ref } from 'vue';

const isOpen = ref(false);
const targetUserInfo = ref(null);

export function useUserProfileModal() {

    const open = (userInfo) => {
        targetUserInfo.value = userInfo;
        isOpen.value = true;
    };

    const close = () => {
        isOpen.value = false;
        targetUserInfo.value = null;
    };

    return {
        isOpen,
        targetUserInfo,
        open,
        close
    };
}
