/** @type {import('tailwindcss').Config} */
module.exports = {
  content: ["./src/**/*.{html,ts}"],
  theme: {
    extend: {
      colors: {
        brand: {
          50: "#eef8ff",
          100: "#d9efff",
          500: "#0077b6",
          700: "#005f8f"
        },
        success: "#2f9e44",
        warning: "#f59f00",
        danger: "#d9480f"
      }
    }
  },
  plugins: []
};
