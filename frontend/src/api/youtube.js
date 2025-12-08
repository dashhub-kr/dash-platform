import http from './http';

export const youtubeApi = {
    // Search videos
    // GET /api/youtube/search?keyword={keyword}
    search(keyword) {
        return http.get('/youtube/search', {
            params: { keyword }
        });
    }
};
