import http from './http';

export const algorithmApi = {
    // Create a new algorithm record
    // POST /api/algorithm
    create(data) {
        // data should be FormData if it involves file uploads, or JSON otherwise.
        // Controller consumes Multipart/form-data.
        return http.post('/algorithm', data, {
            headers: {
                'Content-Type': 'multipart/form-data',
            },
        });
    },

    // Get all algorithm records
    // GET /api/algorithm
    findAll() {
        return http.get('/algorithm');
    },

    // Get a specific algorithm record by ID
    // GET /api/algorithm/{id}
    findById(id) {
        return http.get(`/algorithm/${id}`);
    },

    // Update an algorithm record
    // PUT /api/algorithm/{id}
    update(id, data) {
        // Controller consumes Multipart/form-data.
        return http.put(`/algorithm/${id}`, data, {
            headers: {
                'Content-Type': 'multipart/form-data',
            },
        });
    },

    // Delete an algorithm record
    // DELETE /api/algorithm/{id}
    delete(id) {
        return http.delete(`/algorithm/${id}`);
    },

    // Get algorithm records by study ID
    // GET /api/algorithm/study/{studyId}
    findByStudyId(studyId) {
        return http.get(`/algorithm/study/${studyId}`);
    },

    // Get algorithm records by user ID
    // GET /api/algorithm/user/{userId}
    findByUserId(userId) {
        return http.get(`/algorithm/user/${userId}`);
    },
};
