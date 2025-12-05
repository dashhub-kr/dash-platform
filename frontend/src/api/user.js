import http from './http';

export const userApi = {
    // Create a new user
    // POST /api/users
    create(data) {
        return http.post('/users', data);
    },

    // Get all users
    // GET /api/users
    list() {
        return http.get('/users');
    },

    // Get current user profile (synced with session)
    // GET /api/users/me
    getMyProfile() {
        return http.get('/users/me');
    },

    // Get a specific user by ID
    // GET /api/users/{id}
    getById(id) {
        return http.get(`/users/${id}`);
    },

    // Update a user
    // PUT /api/users/{id}
    update(id, data) {
        return http.put(`/users/${id}`, data);
    },

    // Delete a user
    // DELETE /api/users/{id}
    delete(id) {
        return http.delete(`/users/${id}`);
    },
};
