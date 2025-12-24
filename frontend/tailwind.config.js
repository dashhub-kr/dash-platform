export default {
  content: ["./index.html", "./src/**/*.{vue,js,ts,jsx,tsx}"],
  theme: {
    extend: {
      colors: {
        brand: {
          50: '#eff6ff',
          100: '#dbedff',
          200: '#bfdfff',
          300: '#93caff',
          400: '#60a5fa',
          500: '#3396F4', // Main (User Preference)
          600: '#2563eb',
          700: '#1d4ed8',
          800: '#1e40af',
          900: '#1e3a8a',
          950: '#172554',
        },
        leaf: '#58CC02',    // Success Green
        beetle: '#2DD4BF',  // Neutral Positive Teal
        bee: '#FFC800',     // Warning Yellow
        fox: '#FF9600',     // High Alert Orange
        rose: '#FF4B4B',    // Error Red
      },
      fontFamily: {
        sans: ['Pretendard', 'sans-serif'],
      }
    },
  },
  plugins: [
    require('@tailwindcss/typography'),
  ],
};
