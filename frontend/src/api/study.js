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
    },
    // Get study details
    get(studyId) {
        return http.get(`/studies/${studyId}`);
    },
    // Get acorn logs
    getAcornLogs(studyId) {
        return http.get(`/studies/${studyId}/acorns`);
    },
    // Get weekly missions
    getMissions(studyId) {
        return http.get(`/studies/${studyId}/missions`);
    },
    // Application Management
    getApplications(studyId) {
        return http.get(`/studies/${studyId}/applications`);
    },
    approveApplication(applicationId) {
        return http.post(`/studies/applications/${applicationId}/approve`);
    },
    leaveStudy() {
        return http.post(`/studies/leave`);
    },
    // User Application
    getMyApplication() {
        return http.get(`/studies/applications/me`);
    },
    cancelApplication(applicationId) {
        return http.delete(`/studies/applications/${applicationId}`);
    }
};
