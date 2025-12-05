import http from './http';

export const userApi = {
    // Get current user profile (synced with session)
    getMyProfile() {
        return http.get('/users/me');
    },
};
