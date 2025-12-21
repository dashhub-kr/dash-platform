import http from './http';

export const algorithmApi = {
    // Create a new algorithm record
    // POST /api/algorithm-records
    create(data) {
        // data should be FormData if it involves file uploads, or JSON otherwise.
        // Controller consumes Multipart/form-data.
        return http.post('/algorithm-records', data, {
            headers: {
                'Content-Type': 'multipart/form-data',
            },
        });
    },

    // Get all algorithm records
    // GET /api/algorithm-records
    findAll() {
        return http.get('/algorithm-records');
    },

    // Get a specific algorithm record by ID
    // GET /api/algorithm-records/{id}
    findById(id) {
        return http.get(`/algorithm-records/${id}`);
    },

    // Update an algorithm record
    // PUT /api/algorithm-records/{id}
    update(id, data) {
        // Controller consumes Multipart/form-data.
        return http.put(`/algorithm-records/${id}`, data, {
            headers: {
                'Content-Type': 'multipart/form-data',
            },
        });
    },

    // Delete an algorithm record
    // DELETE /api/algorithm-records/{id}
    delete(id) {
        return http.delete(`/algorithm-records/${id}`);
    },

    // Get algorithm records by study ID
    // GET /api/algorithm-records/study/{studyId}
    findByStudyId(studyId) {
        return http.get(`/algorithm-records/study/${studyId}`);
    },

    // Get algorithm records by user ID
    // GET /api/algorithm-records/user/{userId}
    findByUserId(userId) {
        return http.get(`/algorithm-records/user/${userId}`);
    },
};
