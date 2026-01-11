import http from '@/api/http';

export const battleApi = {
    // 배틀 생성
    createBattle: (type, detailType, problemIds, inviteeIds) =>
        http.post('/battle', { type, detailType, problemIds, inviteeIds }),

    // 배틀 조회
    getBattle: (battleId) =>
        http.get(`/battle/${battleId}`),

    // 내 배틀 목록
    getMyBattles: () =>
        http.get('/battle/my'),

    // 대기 중인 배틀 초대
    getPendingBattles: () =>
        http.get('/battle/pending'),

    // 배틀 초대 수락
    acceptBattle: (battleId) =>
        http.post(`/battle/${battleId}/accept`),

    // 배틀 초대 거절
    declineBattle: (battleId) =>
        http.post(`/battle/${battleId}/decline`),

    // 배틀 시작
    startBattle: (battleId) =>
        http.post(`/battle/${battleId}/start`),

    // 배틀 상태 조회 (폴링용)
    getBattleStatus: (battleId) =>
        http.get(`/battle/${battleId}/status`),

    // 결과 제출
    submitResult: (battleId, score, problemsSolved, totalTimeSeconds) =>
        http.post(`/battle/${battleId}/submit`, { score, problemsSolved, totalTimeSeconds }),
};

