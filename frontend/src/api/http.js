import axios from 'axios';

const http = axios.create({
    baseURL: '/api',
    headers: {
        'Content-Type': 'application/json',
    },
    withCredentials: true,
});

// Response interceptor for error handling
http.interceptors.response.use(
    (response) => {
        return response;
    },
    (error) => {
        // Return the error object for the caller to handle
        // You can add global error handling logic here (e.g., toast notifications)
        return Promise.reject(error);
    }
);

export default http;
