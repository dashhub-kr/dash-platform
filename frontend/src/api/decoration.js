import http from './http';

export const decorationApi = {
    // --- Admin Endpoints ---

    // Create a new decoration
    // POST /api/admin/decorations
    create(data) {
        return http.post('/admin/decorations', data);
    },

    // Get all decorations
    // GET /api/admin/decorations
    findAll() {
        return http.get('/admin/decorations');
    },

    // Delete a decoration
    // DELETE /api/admin/decorations/{id}
    delete(id) {
        return http.delete(`/admin/decorations/${id}`);
    },

    // Grant a decoration to a user
    // POST /api/admin/decorations/grant
    grant(userId, decorationId) {
        return http.post('/admin/decorations/grant', { userId, decorationId });
    },

    // --- User Endpoints ---

    // Get my decoration inventory
    // GET /api/decorations/mine
    getMyInventory() {
        return http.get('/decorations/mine');
    },

    // Equip a decoration
    // POST /api/decorations/equip/{id}
    equip(id) {
        return http.post(`/decorations/equip/${id}`);
    },

    // Unequip decoration
    // POST /api/decorations/unequip
    unequip() {
        return http.post('/decorations/unequip');
    }
};
