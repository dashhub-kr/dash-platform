import http from './http';

export const aiApi = {
  // 코드 리뷰
  analyzeCode: (request) => http.post('/ai/review', request),
  getAnalysisResult: (recordId) => http.get(`/ai/review/${recordId}`),

  // 힌트 생성
  getHint: (request) => http.post('/ai/hint', request),

  // 학습 경로 생성
  getLearningPath: (userId) => http.get(`/ai/learning-path/${userId}`),

  // 코딩 스타일 분석 (MBTI)
  getCodingStyle: (userId) => http.get(`/ai/coding-style/${userId}`),

  // 튜터 채팅
  chat: (request) => http.post('/ai/tutor/chat', request),

  // Solved.ac 통계
  getTagStats: (userId, limit = 10) => http.get(`/users/${userId}/solvedac/stats/tags?limit=${limit}`),
  getClassStats: (userId) => http.get(`/users/${userId}/solvedac/stats/classes`),
};
