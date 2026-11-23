# Task & Habits – backend

REST API for an app that manages tasks and habits. Spring Boot 3.4, Java 21, PostgreSQL, JWT.
Frontend: [task-habits-frontend](https://github.com/peTakova/task-habits-frontend).

## Features

- **Accounts** – registration, login, logout, change name and email, change password, delete account
- **Tasks** – create, edit, complete, delete, due date (`dueDate`), pagination and sorting
- **Habits** – daily / weekly / monthly, mark as done, edit, delete, pagination
- **Reminders** – a scheduler runs every 10 minutes and sets `reminderMessage` on tasks due within 24 hours
- **Statistics** – completed / pending tasks and habits by frequency
- **Security** – JWT in an HttpOnly `authToken` cookie, BCrypt-hashed passwords, users only see their own data
- **Errors** – consistent `{ "error": "…" }` response body (400 / 401 / 404 / 409)

## API

| Method | Path | Description |
|---|---|---|
| POST | `/users/register` · `/users/login` · `/users/logout` | register, log in, log out (public) |
| GET / PUT / DELETE | `/users/me` | current user – detail, update, delete |
| PUT | `/users/me/password` | change password |
| GET | `/users/stats` · `/users/completed-tasks` · `/users/weekly-habits` | statistics |
| GET / POST | `/task` | list / create tasks |
| GET / PUT / DELETE | `/task/{id}` | task detail / update / delete |
| PATCH | `/task/{id}/complete` | mark task as completed |
| GET | `/task/page?page=0&size=10&sort=id,asc` | paginated tasks |
| GET / POST | `/habit` | list / create habits |
| GET / PUT / DELETE | `/habit/{id}` | habit detail / update / delete |
| PATCH | `/habit/{id}/complete` | mark habit as done |
| GET | `/habit/page?page=0&size=10&sort=id,asc` | paginated habits |

Everything except register, login and logout requires an authenticated user.

## Running

Requirements: Java 21 and a running PostgreSQL with a `task&habits` database
(connection settings in `src/main/resources/application.properties`). Hibernate creates the tables automatically.

```powershell
.\gradlew.bat bootRun
```

The API runs on `http://localhost:7010`.

> If the project folder name contains `&`, `gradlew.bat` does not work. Use:
> `java -classpath "gradle\wrapper\gradle-wrapper.jar" org.gradle.wrapper.GradleWrapperMain bootRun`

## Tests (Postman)

The collection in [`postman/`](postman/) contains 39 requests and 73 assertions.
In Postman: **Import** → the collection → **Run**. The collection creates its own test account and cleans everything up at the end.

From the command line with [Newman](https://github.com/postmanlabs/newman):

```powershell
npx newman run "postman/Task&Habits-nove-API.postman_collection.json"
```
