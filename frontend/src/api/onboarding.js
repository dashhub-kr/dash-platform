import http from './http';

export const onboardingApi = {
    // Submit GitHub repository for onboarding
    submitRepository(repositoryName) {
        return http.post('/onboarding/repository', { repositoryName });
    },
    // Search user's GitHub repositories
    searchRepositories(query) {
        return http.get(`/onboarding/repository/search?q=${encodeURIComponent(query || '')}`);
    },
    // Verify Solved.ac handle (returns user info if valid)
    verifySolvedac(handle) {
        return http.get(`/onboarding/solvedac/verify?handle=${encodeURIComponent(handle)}`);
    },
    // Register Solved.ac handle
    registerSolvedac(handle) {
        return http.post('/onboarding/solvedac', { handle });
    },
};
