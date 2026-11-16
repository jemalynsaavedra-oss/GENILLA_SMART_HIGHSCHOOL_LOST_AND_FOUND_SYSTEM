# Smart High School Lost and Found System

Java 17 console application using SQLite and Maven. This version extends the supplied project while retaining the `ADMIN`, `FINDER`, and `OWNER` roles and the existing package layout.

## Run it

1. Install JDK 17+ and Maven 3.8+.
2. Open a terminal in this folder.
3. Run `mvn clean compile exec:java`.
4. The SQLite database `smart_lost_found.db` is created/updated in the working directory automatically.

A Windows launcher (`run.bat`) is included. It runs the same Maven command.

## Initial administrator

- Username: `admin`
- Password: `admin123`
- The initializer creates this account only when no ADMIN role account exists. Change this password before using the project outside a classroom/demo environment.

## Role functionality

### ADMIN
- View all users and pending registrations.
- Approve pending accounts.
- Update a user's role and status (ADMIN/FINDER/OWNER; APPROVED/PENDING/REJECTED).
- View all audit logs and filter them by user ID.
- Clear historical logs only after typing `CLEAR ALL`; the clearing operation itself is then written as a new audit event.
- View all found-item records and delete a found-item record.

### FINDER
- Create a found-item report.
- Read/list their own reports.
- Update their own records while status is `FOUND`.
- Delete their own records while status is `FOUND`.

### OWNER
- Search available found items by name, description, or location.
- Claim an available item.
- View items claimed by their account.
- Confirm physical receipt; the record becomes `RETURNED`.

Each role menu verifies the signed-in user's role before displaying its actions. Inputs are validated before database writes. Item update/delete operations are owner-scoped to the Finder who created them; an Owner can only confirm receipt for an item claimed by that Owner.

## Activity audit trail

The `activity_logs` table is initialized automatically. The application records account registration, successful login/logout, account approval, role/status changes, found-item creation/update/deletion, claims, receipt confirmation, and admin deletion/clearing actions. Logs include actor user ID, action type, description, and local timestamp. Audit queries are parameterized and ordered newest-first.

## Database tables

- `users`: account, role, and approval status.
- `found_items`: found item details, Finder, claiming Owner, and lifecycle status.
- `activity_logs`: append-oriented audit history.

## Important notes / assumptions

- The screenshot explicitly specifies the audit table and log DAO methods. It gives examples of domain-specific event names but does not specify the full lost-item workflow. The Finder/Owner workflow above is a practical implementation based on the supplied application name and your request: Finder reports found items; Owner searches, claims, and confirms receipt.
- The existing starter project stored passwords as plain text, so this version preserves that storage format for compatibility with your existing SQLite users table. This is suitable only for a local class/demo project. For production, replace it with salted password hashing and add account recovery/secret management.
- SQLite JDBC is provided by Maven at runtime; it is not bundled as a binary JAR in this source ZIP. Maven downloads the dependency on first run.
- A pre-initialized SQLite database is included. The application also runs the initializer on every startup and safely creates missing tables/indexes.
