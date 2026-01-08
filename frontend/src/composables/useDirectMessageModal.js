import { ref } from 'vue';

const isOpen = ref(false);
const partnerInfo = ref(null);

export function useDirectMessageModal() {

    const open = (partner) => {
        partnerInfo.value = partner;
        isOpen.value = true;
    };

    const close = () => {
        isOpen.value = false;
        partnerInfo.value = null;
    };

    return {
        isOpen,
        partnerInfo,
        open,
        close
    };
}
