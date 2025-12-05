import http from './http';

export const authApi = {
    // Get current OAuth session info
    getSession() {
        return http.get('/oauth/session');
    },
    // Logout
    logout() {
        return http.post('/logout');
    },
};
