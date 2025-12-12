import http from './http';

export const dashboardApi = {
    getRecords: () => http.get('/dashboard/records'),
};
