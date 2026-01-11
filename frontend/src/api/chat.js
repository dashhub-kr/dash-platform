import http from './http';

export const chatApi = {
    // 채팅방 목록
    getRooms: () => http.get('/chat/rooms'),

    // 채팅방 상세
    getRoom: (roomId) => http.get(`/chat/rooms/${roomId}`),

    // 그룹 채팅방 생성
    createRoom: (name, memberIds) => http.post('/chat/rooms', { name, memberIds }),

    // 메시지 조회
    getMessages: (roomId, page = 0, size = 50) =>
        http.get(`/chat/rooms/${roomId}/messages`, { params: { page, size } }),

    // 메시지 발송
    sendMessage: (roomId, content) =>
        http.post(`/chat/rooms/${roomId}/messages`, { content }),

    // 멤버 초대
    addMember: (roomId, userId) =>
        http.post(`/chat/rooms/${roomId}/members`, { userId }),

    // 채팅방 나가기
    leaveRoom: (roomId) =>
        http.delete(`/chat/rooms/${roomId}/members/me`),

    // 읽음 처리
    markAsRead: (roomId) =>
        http.post(`/chat/rooms/${roomId}/read`),

    // 채팅방 삭제
    deleteRoom: (roomId) =>
        http.delete(`/chat/rooms/${roomId}`)
};
