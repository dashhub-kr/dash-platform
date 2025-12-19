import http from './http';
import axios from 'axios';

export const authApi = {
    // Get current OAuth session info
    getSession() {
        return http.get('/oauth/session');
    },
    // Logout - calls /logout directly (not /api/logout)
    logout() {
        return axios.post('/logout', null, { withCredentials: true });
    },
};
