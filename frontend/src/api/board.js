import http from './http';

export const boardApi = {
    // Create a new board post
    // POST /api/boards
    create(data) {
        return http.post('/boards', data);
    },

    // Get all board posts
    // GET /api/boards
    findAll() {
        return http.get('/boards');
    },

    // Get a specific board post by ID
    // GET /api/boards/{id}
    findById(id) {
        return http.get(`/boards/${id}`);
    },

    // Update a board post
    // PUT /api/boards/{id}
    update(id, data) {
        return http.put(`/boards/${id}`, data);
    },

    // Delete a board post
    // DELETE /api/boards/{id}
    delete(id) {
        return http.delete(`/boards/${id}`);
    },
};
