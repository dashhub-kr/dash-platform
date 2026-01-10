import http from "@/api/http";

export const shopApi = {
    // Get list of shop items (decorations)
    getItems() {
        return http.get('/shop/items');
    },

    // Buy an item
    buyItem(itemId) {
        return http.post(`/shop/buy/${itemId}`);
    },

    // Gift an item (Admin only)
    giftItem(targetUserId, itemId) {
        return http.post('/shop/gift', { targetUserId, itemId });
    },

    // Get my owned decorations
    getMyDecorations() {
        return http.get('/decorations/mine');
    },

    // Equip a decoration
    equipDecoration(decorationId) {
        return http.post(`/decorations/equip/${decorationId}`);
    },

    // Unequip decoration
    unequipDecoration() {
        return http.post('/decorations/unequip');
    }
};
