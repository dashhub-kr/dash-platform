import { ref } from 'vue';

// Global state for FloatingMessagePanel
const isOpen = ref(false);
const activeChat = ref(null); // { partnerId, partnerName, partnerAvatar, partnerDecoration }

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

    return {
        isOpen,
        activeChat,
        openChat,
        closeChat,
        toggle
    };
}
