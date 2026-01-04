import http from '@/api/http';

export const socialApi = {
    // Friends
    getFriends: () => http.get('/social/friends'),
    getReceivedRequests: () => http.get('/social/friends/requests'),
    sendFriendRequest: (receiverId) => http.post('/social/friends/request', { receiverId }),
    acceptFriendRequest: (requestId) => http.post(`/social/friends/accept/${requestId}`),
    rejectFriendRequest: (requestId) => http.post(`/social/friends/reject/${requestId}`),
    deleteFriend: (friendId) => http.delete(`/social/friends/${friendId}`),
    
    // Search
    searchUsers: (query) => http.get('/social/users/search', { params: { query } }),
    
    // Messages
    getConversation: (partnerId) => http.get(`/social/messages/${partnerId}`),
    sendMessage: (receiverId, content) => http.post('/social/messages', { receiverId, content }),
};
