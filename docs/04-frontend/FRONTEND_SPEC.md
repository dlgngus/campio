# Frontend Spec

## Stack

- React
- Vite
- React Router
- CSS variables
- Axios or fetch

## Required Routes

```text
/
 /login
 /signup
 /onboarding
 /home
 /explore
 /opportunities/:id
 /saved
 /community
 /mentors
 /profile
 /admin/opportunities
```

## Required Behavior

- Load application data from backend APIs.
- Do not add frontend mock data files for production screens.
- Use reusable components.
- Add loading/empty/error states.
- Mobile responsive.

## Global Layout

Authenticated pages use:

- top nav on desktop
- mobile tab bar on mobile
- max-width content container
- dark canvas background
