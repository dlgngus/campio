# Entity Spec

## User

Fields:
- id
- email
- password
- name
- school
- major
- grade
- role
- verified
- createdAt
- updatedAt

## Opportunity

Fields:
- id
- title
- organization
- category
- description
- requirements
- benefits
- deadline
- startDate
- endDate
- location
- isOnline
- applyUrl
- thumbnailUrl
- tags
- status
- createdAt
- updatedAt

## SavedOpportunity

Fields:
- id
- userId
- opportunityId
- createdAt

## ApplicationRecord

Fields:
- id
- userId
- opportunityId
- status
- memo
- createdAt
- updatedAt

## CommunityPost

Fields:
- id
- userId
- opportunityId
- type
- title
- content
- createdAt
- updatedAt

## Comment

Fields:
- id
- postId
- userId
- content
- createdAt
- updatedAt

## MentorProfile

Fields:
- id
- userId
- company
- position
- experience
- helpTopics
- available
- createdAt
- updatedAt
