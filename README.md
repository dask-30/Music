# Sheet Music Library & Playback

A web application for uploading, storing, visualizing, and playing sheet music.

The project consists of:

- Spring Boot REST API backend
- JavaScript frontend
- OpenSheetMusicDisplay (OSMD) for rendering MusicXML scores
- Tone.js for audio playback using piano samples

---

## Features

### Score Management

- Upload MusicXML / MXL scores
- Store scores in a database
- Retrieve scores by ID
- Browse user score library
- Delete scores

### Sheet Music Rendering

- Render MusicXML files directly in the browser
- SVG-based score display
- Responsive score scaling
- Cursor support

### Playback

- Piano sound playback using Tone.js
- Automatic note extraction from rendered score
- Multi-note/chord playback
- Pause / Stop playback
- Tempo-controlled playback

---

## Architecture

Frontend
│
├── OpenSheetMusicDisplay
│       │
│       └── Renders MusicXML
│
├── Tone.js
│       │
│       └── Plays extracted notes
│
└── REST API Client
        │
        ▼

Spring Boot REST API
        │
        ▼

Database

---

## Technologies

### Backend

- Java
- Spring Boot
- Spring Web
- Spring Data JPA
- Maven
- MySQL / PostgreSQL

### Frontend

- HTML
- CSS
- JavaScript

### Libraries

- OpenSheetMusicDisplay (OSMD)
- Tone.js
- JSZip

---

## REST API

### Get User Scores

GET /music/users/{userId}/scores

Returns all scores belonging to a user.

Example Response:

```json
[
  {
    "id": 1,
    "title": "Für Elise",
    "author": "Ludwig van Beethoven"
  }
]
```

### Get Score

GET /music/scores/{scoreId}

Returns score metadata and encoded score data.

Example Response:

```json
{
  "id": 1,
  "title": "Für Elise",
  "author": "Ludwig van Beethoven",
  "data": "base64..."
}
```

### Upload Score

POST /music/users/{userId}/scores

Request:

```json
{
  "data": "base64EncodedMxl"
}
```

Response:

```json
{
  "id": 1,
  "title": "Für Elise",
  "author": "Ludwig van Beethoven"
  "data": "base64encodedMxl"
}
```

### Delete Score

DELETE /music/users/{userId}/scores/{scoreId}

Response:

```http
204 No Content
```

---

## Playback Flow

Load Score
↓
Render with OSMD
↓
Traverse Cursor
↓
Build Note Events
↓
Tone.Part Schedule
↓
Tone Sampler Playback

---


Server starts on:

```text
http://localhost:8080
```

---


---

## Future Improvements

- Tempo detection from MusicXML
- Cursor synchronization during playback
- Dynamic markings (p, mf, f)
- MIDI export
- User authentication
- Playlist support
- Multiple instrument playback
- Score search and filtering

---

## Author

Szőcs-Braic Darius
