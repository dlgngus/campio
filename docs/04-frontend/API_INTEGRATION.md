# Frontend API Integration

## API Client

Create `src/api/client.js`.

Base URL:

```js
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";
```

## Domain APIs

- `authApi.js`
- `opportunityApi.js`
- `savedApi.js`
- `communityApi.js`
- `mentorApi.js`

## Integration Order

1. Opportunities list
2. Opportunity detail
3. Save/unsave
4. Application records
5. Community posts
6. Mentors
7. Profile
