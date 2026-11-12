# Student Registration API

A Spring Boot application for registering students and saving them in a database (H2 by default, PostgreSQL optional).

## What it does

- **Register** a student with first name, last name, middle initial, gender, date of birth, nationality and class.
- **Blocks duplicates**: the same first name, last name and date of birth can't be registered twice.
- **Exports** the student list (all, or one class) to Excel or CSV.
- **Works out the age** from the date of birth, so it is always up to date.
- **Gives each student an ID** automatically.
- **Records when the student was created** (`createdAt`) and **when the record was last updated** (`updatedAt`). Both are set by the system.
- **Saves everything in a database**, so the data is still there after the app restarts.
- **Shows the data** through the API, and you can also view the `students` table in the H2 web console.

## Tech used

| Part | Choice |
|---|---|
| Language | Java 17 or newer |
| Framework | Spring Boot 4.1.2 |
| Build tool | Maven |
| Database | H2 (file-based, default) or PostgreSQL |
| Database access | Spring Data JPA (Hibernate) |

## How the code is organised

```
src/main/java/com/example/StudentRegistration/
├── StudentRegistrationApplication.java   Starts the app
├── export/
│   └── TableExport.java        Builds the Excel (.xlsx) and CSV files
├── country/
│   ├── Countries.java          The list of countries for the nationality dropdown
│   └── CountryController.java  GET /api/countries
├── student/
│   ├── Student.java              The database table (entity)
│   ├── StudentRequest.java       The data a client sends, with validation rules
│   ├── StudentRepository.java    Talks to the database
│   ├── StudentService.java       Business logic (register, update, find)
│   └── StudentController.java    The URLs (API endpoints)
└── common/
    ├── NotFoundException.java     Raised when a student ID doesn't exist
    ├── FieldValidationException.java  Raised when a rule fails (e.g. student too young)
    ├── DuplicateStudentException.java Raised when the student is already registered (409)
    └── GlobalExceptionHandler.java  Turns errors into clear JSON messages
src/main/resources/application.properties   Database connection settings
src/main/resources/static/index.html         The web page (frontend)
mvnw.cmd             Runs Maven without installing it
```

A request travels **Controller → Service → Repository → Database**, and the answer comes back the same way.

## The student record

| Field | Type | Set by | Rule |
|---|---|---|---|
| id | number | System | Unique, auto-generated |
| firstName | text | User | Required |
| lastName | text | User | Required |
| middleInitial | one letter | User | Optional, single letter, stored in capitals |
| gender | text | User | Required: Male or Female |
| dateOfBirth | date | User | Required, YYYY-MM-DD; student must be 10–25 years old |
| age | number | System | Worked out from dateOfBirth, not stored |
| nationality | text | User | Required, must be a country from `/api/countries` |
| grade | text | User | Required: Form 1, Form 2 or Form 3 |
| createdAt | date-time | System | Set once when registered |
| updatedAt | date-time | System | Refreshed on every update |

## Running it on your computer

### 1. What you need
- **Java 17 or newer** (JDK)
- Maven is **not** required: the included Maven wrapper (`mvnw.cmd`) downloads it automatically

### 2. The database
Nothing to set up. The app uses **H2**, a database that runs inside the app and saves its data to
`data/students.mv.db` in the project folder. The file is created on first start, and the data stays
there after the app stops.

### 3. Start the app
In VS Code: open `StudentRegistrationApplication.java` and click **Run** above `main`.

Or in a terminal (PowerShell, inside the project folder):
```
.\mvnw spring-boot:run
```
The Maven wrapper downloads Maven automatically the first time, so you don't need to install it.

The app is ready when you see `Started StudentRegistrationApplication`. It runs at **http://localhost:8080**.
The `students` table is created automatically on first start.

## Using the API

| Action | Method | URL |
|---|---|---|
| Register a student | POST | `/api/students` |
| List all students | GET | `/api/students` |
| Get one student | GET | `/api/students/{id}` |
| Update a student | PUT | `/api/students/{id}` |
| Delete a student | DELETE | `/api/students/{id}` |
| List countries | GET | `/api/countries` |
| Export to Excel | GET | `/api/students/export?format=xlsx` (add `&grade=Form%201` for one class) |
| Export to CSV | GET | `/api/students/export?format=csv` |

### Register a student
`POST http://localhost:8080/api/students` with this JSON body (in Postman: Body → raw → JSON):
```json
{
  "firstName": "Kofi",
  "lastName": "Mensah",
  "middleInitial": "A",
  "gender": "Male",
  "dateOfBirth": "2011-03-24",
  "nationality": "Ghana",
  "grade": "Form 2"
}
```
Response (`201 Created`):
```json
{
  "id": 1,
  "firstName": "Kofi",
  "lastName": "Mensah",
  "middleInitial": "A",
  "gender": "Male",
  "dateOfBirth": "2011-03-24",
  "nationality": "Ghana",
  "grade": "Form 2",
  "age": 15,
  "createdAt": "2026-11-12T09:15:02.481",
  "updatedAt": "2026-11-12T09:15:02.481"
}
```

### Update a student
`PUT http://localhost:8080/api/students/1` with the full record:
```json
{
  "firstName": "Kofi",
  "lastName": "Mensah",
  "middleInitial": "A",
  "gender": "Male",
  "dateOfBirth": "2011-03-24",
  "nationality": "Ghana",
  "grade": "Form 3"
}
```
`createdAt` stays the same and `updatedAt` changes to the time of the update.

### Error examples
Missing or invalid data → `400 Bad Request`:
```json
{
  "timestamp": "2026-11-12T09:20:11.002",
  "status": 400,
  "error": "Validation failed",
  "fields": {
    "firstName": "First name is required",
    "middleInitial": "Middle initial must be a single letter"
  }
}
```
Unknown ID → `404 Not Found`:
```json
{
  "timestamp": "2026-11-12T09:21:40.117",
  "status": 404,
  "error": "Student with id 99 was not found"
}
```

### Same requests with curl
```bash
curl -X POST http://localhost:8080/api/students \
  -H "Content-Type: application/json" \
  -d '{"firstName":"Kofi","lastName":"Mensah","middleInitial":"A","gender":"Male","dateOfBirth":"2011-03-24","nationality":"Ghana","grade":"Form 2"}'

curl http://localhost:8080/api/students
```

## Seeing the data in the database

While the app is running, open **http://localhost:8080/h2-console** and enter:
- JDBC URL: `jdbc:h2:file:./data/students`
- User Name: `sa`
- Password: *(leave empty)*

Click **Connect**, then run:
```sql
SELECT * FROM students;
```

## Switching to PostgreSQL (optional)

No code changes are needed. Install PostgreSQL, create a database called `studentdb`, then set
these environment variables before starting the app (PowerShell):
```
$env:DB_URL="jdbc:postgresql://localhost:5432/studentdb"
$env:DB_USERNAME="postgres"
$env:DB_PASSWORD="postgres"
.\mvnw spring-boot:run
```

## Deploying online

The app reads its database settings from environment variables, so nothing needs changing in the code:

| Variable | Example |
|---|---|
| `DB_URL` | `jdbc:postgresql://<host>:5432/<database>` |
| `DB_USERNAME` | provided by the host |
| `DB_PASSWORD` | provided by the host |

On Render or Railway: push this project to GitHub, create a PostgreSQL database on the platform, create a web service from the repo, and set the three variables above.
