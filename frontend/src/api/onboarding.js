import http from './http';

export const onboardingApi = {
    // Submit GitHub repository for onboarding
    submitRepository(repositoryName) {
        return http.post('/onboarding/repository', { repositoryName });
    },
};
