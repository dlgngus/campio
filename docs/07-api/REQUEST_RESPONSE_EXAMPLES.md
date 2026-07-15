# Request and Response Examples

## GET /api/opportunities

Response:

```json
[
  {
    "id": 1,
    "title": "Toss Frontend Internship",
    "organization": "Toss",
    "category": "INTERNSHIP",
    "deadline": "2026-08-30",
    "location": "Seoul",
    "online": false,
    "tags": ["frontend", "react", "internship"],
    "saved": false
  }
]
```

## POST /api/opportunities/{id}/save

Response:

```json
{
  "saved": true
}
```

## POST /api/opportunities/{id}/apply-record

Request:

```json
{
  "status": "PREPARING",
  "memo": "Need to update portfolio"
}
```

Response:

```json
{
  "id": 10,
  "status": "PREPARING"
}
```
