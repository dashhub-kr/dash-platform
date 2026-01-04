import http from './http';

export const dashboardApi = {
    // 알고리즘 기록 조회
    getRecords: (studyId) => http.get('/dashboard/records', { params: { studyId } }),

    // 히트맵 데이터 조회
    getHeatmap: (studyId) => http.get('/dashboard/heatmap', { params: { studyId } }),
};
