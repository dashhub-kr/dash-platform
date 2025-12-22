import http from './http';

export const aiApi = {
  // 코드 리뷰
  analyzeCode: (request) => http.post('/ai/review', request),
  getAnalysisResult: (recordId) => http.get(`/ai/review/${recordId}`),



  // AI 튜터 대화
  tutorChat: (request) => http.post('/ai/tutor/chat', request),

  // 학습 경로 생성
  getLearningPath: (userId) => http.get(`/ai/learning-path/${userId}`),

  // 코딩 스타일 분석 (MBTI)
  getCodingStyle: (userId) => http.get(`/ai/coding-style/${userId}`),

  // 반례 생성
  generateCounterExample: (request) => http.post('/ai/debug/counter-example', request),

  // 코드 시뮬레이터 (가상 컴파일러)
  simulate: (request) => http.post('/ai/simulator/run', request),

  // Solved.ac 통계
  getTagStats: (userId, limit = 10) => http.get(`/users/${userId}/solvedac/stats/tags?limit=${limit}`),
  getClassStats: (userId) => http.get(`/users/${userId}/solvedac/stats/classes`),
};
