import http from './http';

export const adminApi = {
    // Gift acorns to a study
    // POST /api/admin/acorns/gift
    giftAcorns(data) {
        return http.post('/admin/acorns/gift', data);
    },

    // Gift logs (items) to a user
    // POST /api/admin/logs/gift
    giftLogs(data) {
        return http.post('/admin/logs/gift', data);
    },

    // Block a user
    // POST /api/admin/users/{userId}/block
    blockUser(userId) {
        return http.post(`/admin/users/${userId}/block`);
    }
};
