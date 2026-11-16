SMART HIGH SCHOOL LOST AND FOUND  (Java + Maven + SQLite)
Roles: ADMIN, FINDER, OWNER

SETUP (IntelliJ)
1. Open the SmartLostFound folder as a project (File > Open > select pom.xml > Open as Project).
2. Wait for Maven to download the dependencies (sqlite-jdbc). If needed:
   right-click pom.xml > Maven > Reload project.
3. Run com.smartlostfound.Main.
4. Run Configuration > Working directory must be the project root
   (the folder containing pom.xml and smart_lost_found.db).

DEFAULT ADMIN: username admin / password admin123

DATABASE: smart_lost_found.db is in the project root with the users table and the
default admin already created.
