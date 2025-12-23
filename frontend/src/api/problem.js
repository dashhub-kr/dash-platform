import axios from './http';

export const problemApi = {
    // 문제 추천 (태그 + 티어 기반)
    getRecommendations: (tag, tier) => axios.get('/problems/recommend', { params: { tag, tier } }),

    // 문제 상세 정보 (필요시)
    getProblem: (problemNumber) => axios.get(`/problems/${problemNumber}`),
};
