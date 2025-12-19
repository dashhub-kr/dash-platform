import http from './http';

export const studyApi = {
    // Get list of all studies
    getStudies() {
        return http.get('/studies');
    },
    // Create a new study
    createStudy(name) {
        return http.post('/studies', { name });
    },
    // Join an existing study
    joinStudy(studyId) {
        return http.post(`/studies/${studyId}/join`);
    },
    // Get study statistics
    getStats(studyId) {
        return http.get(`/studies/${studyId}/stats`);
    }
};
