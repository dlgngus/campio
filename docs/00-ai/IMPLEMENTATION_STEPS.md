# Implementation Steps

## Step 1: Verify repository

Expected root structure:

```text
campio/
  README.md
  PROJECT.md
  docs/
  campio-frontend/
  campio-backend/
```

If frontend/backend folders do not exist, create them.

## Step 2: Build frontend shell

Create React app with Vite.

Required routes:

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

## Step 3: Add design tokens

Create:

```text
src/styles/tokens.css
src/styles/globals.css
```

All colors, radius, spacing, typography, and shadows must come from tokens.

## Step 4: Add components

Create reusable components:

```text
Button
Card
Input
Badge
FilterChip
SectionHeader
EmptyState
LoadingSkeleton
OpportunityCard
FeaturedOpportunityCard
PostCard
MentorCard
Navbar
MobileTabBar
```

## Step 5: Connect API data

Do not create frontend mock data files. Public opportunity lists must come from the backend API, and backend opportunity rows must come from ingestion import, API adapters, or RSS adapters.

## Step 6: Implement pages

Priority:

1. Home
2. Explore
3. Opportunity Detail
4. Saved
5. Landing
6. Onboarding
7. Community
8. Mentors
9. Profile
10. Admin

## Step 7: Backend

Create Spring Boot app.

Domains:
- user
- opportunity
- saved
- application
- community
- mentor

## Step 8: Connect API

Create frontend API files:

```text
src/api/client.js
src/api/opportunityApi.js
src/api/savedApi.js
src/api/communityApi.js
src/api/mentorApi.js
src/api/authApi.js
```
