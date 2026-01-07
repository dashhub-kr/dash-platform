<template>
    <div class="fixed inset-0 z-[9999] flex items-center justify-center p-4">
        <div class="absolute inset-0 bg-slate-900/40 backdrop-blur-sm" @click="$emit('close')"></div>
        
        <div class="relative bg-white rounded-3xl w-full max-w-lg shadow-2xl flex flex-col h-[600px] overflow-hidden animate-in fade-in zoom-in-95 duration-200">
            <!-- Header -->
            <div class="px-6 py-4 bg-slate-50 border-b border-slate-100 flex items-center justify-between shrink-0">
                <div class="flex items-center gap-3">
                    <img 
                        :src="partnerAvatar || '/images/profiles/default-profile.png'" 
                        class="w-10 h-10 rounded-full border border-brand-100 object-cover bg-white"
                        :alt="partnerName"
                    />
                    <div>
                        <NicknameRenderer 
                            :nickname="partnerName" 
                            :decorationClass="partnerDecoration"
                            class="text-base"
                        />
                        <p class="text-xs text-brand-500 font-bold">Online</p>
                    </div>
                </div>
                <button @click="$emit('close')" class="p-2 text-slate-400 hover:text-slate-600 bg-white rounded-xl shadow-sm hover:shadow-md transition-all">
                    <X :size="20"/>
                </button>
            </div>
            
            <!-- Message List -->
            <div class="flex-1 overflow-y-auto p-4 space-y-4 bg-slate-50" ref="messagesContainer">
                <div v-if="loading" class="flex justify-center py-10"><Loader2 class="animate-spin text-brand-500"/></div>
                <template v-else>
                    <div 
                        v-for="msg in messages" 
                        :key="msg.id" 
                        class="flex flex-col"
                        :class="msg.isMine ? 'items-end' : 'items-start'"
                    >
                         <div 
                            class="max-w-[70%] px-4 py-2.5 rounded-2xl text-sm font-medium shadow-sm leading-relaxed whitespace-pre-wrap"
                            :class="msg.isMine ? 'bg-brand-500 text-white rounded-tr-sm' : 'bg-white text-slate-700 border border-slate-200 rounded-tl-sm'"
                         >
                            {{ msg.content }}
                         </div>
                         <span class="text-[10px] text-slate-400 mt-1 px-1">{{ formatTime(msg.createdAt) }}</span>
                    </div>
                </template>
            </div>
            
            <!-- Input Area -->
            <div class="p-4 bg-white border-t border-slate-100 shrink-0">
                <form @submit.prevent="sendMessage" class="flex items-center gap-2">
                    <input 
                        v-model="newMessage" 
                        type="text" 
                        placeholder="메시지를 입력하세요..." 
                        class="flex-1 px-4 py-3 bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:border-brand-500 focus:ring-2 focus:ring-brand-500/20 transition-all text-sm font-medium"
                        :disabled="sending"
                    />
                    <button 
                        type="submit" 
                        class="p-3 bg-brand-500 text-white rounded-xl shadow-lg shadow-brand-500/30 hover:bg-brand-600 disabled:opacity-50 transition-all flex items-center justify-center"
                        :disabled="!newMessage.trim() || sending"
                    >
                        <Send :size="20" :class="sending ? 'opacity-0' : ''"/>
                        <Loader2 v-if="sending" class="animate-spin absolute" :size="20"/>
                    </button>
                </form>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, onMounted, nextTick, onBeforeUnmount } from 'vue';
import { socialApi } from '@/api/social';
import { X, Send, Loader2 } from 'lucide-vue-next';
import NicknameRenderer from '@/components/common/NicknameRenderer.vue';

const props = defineProps({
    partnerId: Number,
    partnerName: String,
    partnerId: Number,
    partnerName: String,
    partnerAvatar: String,
    partnerDecoration: String
});

const emit = defineEmits(['close']);

const messages = ref([]);
const loading = ref(true);
const newMessage = ref('');
const sending = ref(false);
const messagesContainer = ref(null);

const scrollToBottom = () => {
    nextTick(() => {
        if (messagesContainer.value) {
            messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight;
        }
    });
};

const fetchMessages = async () => {
    try {
        const res = await socialApi.getConversation(props.partnerId);
        messages.value = res.data;
        if (loading.value) scrollToBottom();
    } catch(e) {
        console.error(e);
    } finally {
        loading.value = false;
    }
};

const sendMessage = async () => {
    if (!newMessage.value.trim() || sending.value) return;
    
    const content = newMessage.value;
    newMessage.value = ''; // 낙관적 업데이트 (Optimistic clear)
    sending.value = true;
    
    try {
        await socialApi.sendMessage(props.partnerId, content);
        await fetchMessages(); // 새 메시지 확인 (혹은 시뮬레이션된 응답 등)
        scrollToBottom();
    } catch(e) {
        console.error(e);
        newMessage.value = content; // 실패 시 복구
        alert('전송 실패');
    } finally {
        sending.value = false;
    }
};

const formatTime = (iso) => {
    const d = new Date(iso);
    return d.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
};

let pollInterval;

onMounted(() => {
    fetchMessages();
    pollInterval = setInterval(fetchMessages, 3000); // 3초마다 새 메시지 폴링
});

onBeforeUnmount(() => {
    clearInterval(pollInterval);
});

</script>
