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
        // 다시 열었을 때 이어서 볼 수 있도록 activeChat은 유지
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
