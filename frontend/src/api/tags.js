import http from './http';

export const tagApi = {
    // 사용자 스킬 트리 조회
    getSkillTree: (userId) => http.get(`/tags/user/${userId}/skill-tree`),
};
