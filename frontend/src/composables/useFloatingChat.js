import { ref } from 'vue';

// Global state for FloatingMessagePanel
const isOpen = ref(false);
const activeChat = ref(null); // { partnerId, partnerName, partnerAvatar, partnerDecoration }
const refreshTrigger = ref(0); // 채팅 목록 새로고침 트리거

export function useFloatingChat() {

    // Open the panel and optionally start a specific chat
    const openChat = (partner = null) => {
        isOpen.value = true;
        if (partner) {
            activeChat.value = partner;
        }
    };

    const closeChat = () => {
        isOpen.value = false;
        // activeChat.value = null; // Do not clear activeChat so we can return to it? No, maybe better to clear or keep.
        // Let's keep existing logic: usually distinct from minimize.
    };

    const toggle = () => {
        isOpen.value = !isOpen.value;
    };

    // DM 알림 수신 시 채팅 목록 새로고침 트리거
    const triggerRefresh = () => {
        refreshTrigger.value++;
    };

    return {
        isOpen,
        activeChat,
        refreshTrigger,
        openChat,
        closeChat,
        toggle,
        triggerRefresh
    };
}
