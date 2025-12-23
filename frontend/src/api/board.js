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

    // Get a board by Algorithm Record ID
    // GET /api/boards/record/{recordId}
    findByRecordId(recordId) {
        return http.get(`/boards/record/${recordId}`);
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

    // Like a board post
    // POST /api/boards/{id}/like
    like(id) {
        return http.post(`/boards/${id}/like`);
    },

    // Unlike a board post
    // DELETE /api/boards/{id}/like
    unlike(id) {
        return http.delete(`/boards/${id}/like`);
    },
};

// Comment API
export const commentApi = {
    // Get comments for a board post
    // GET /api/boards/{boardId}/comments
    findByBoardId(boardId, lineNumber = null) {
        const params = lineNumber ? { lineNumber } : {};
        return http.get(`/boards/${boardId}/comments`, { params });
    },

    // Create a comment
    // POST /api/boards/{boardId}/comments
    create(boardId, data) {
        return http.post(`/boards/${boardId}/comments`, data);
    },

    // Update a comment
    // PUT /api/boards/{boardId}/comments/{commentId}
    update(boardId, commentId, data) {
        return http.put(`/boards/${boardId}/comments/${commentId}`, data);
    },

    // Delete a comment
    // DELETE /api/boards/{boardId}/comments/{commentId}
    delete(boardId, commentId) {
        return http.delete(`/boards/${boardId}/comments/${commentId}`);
    },

    // Like a comment
    // POST /api/comments/{commentId}/like
    like(commentId) {
        return http.post(`/comments/${commentId}/like`);
    },

    // Unlike a comment
    // DELETE /api/comments/{commentId}/like
    unlike(commentId) {
        return http.delete(`/comments/${commentId}/like`);
    },
};
