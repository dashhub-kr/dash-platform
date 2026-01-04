import http from './http';

const api = http;

export async function getNotifications() {
    const { data } = await api.get('/notifications');
    return data;
}

export async function markAsRead(id) {
    await api.post(`/notifications/${id}/read`);
}

export async function markAllAsRead() {
    await api.post('/notifications/read-all');
}
