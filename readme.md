# Place import guides here

## Information About the Project

Go to documentation.md to view

## Get start of the frontend
Make sure to have node v18.17.0 or above.
1. cd frontend
2. npm i
3. npm run dev

## Get start of the backend
make sure to have java version 21 or above
1. if you don't have maven

   1. MacOs: run brew install maven
   2. Window: install maven and add it to path

3. inside the server directory, run `mvn clean spring-boot:run`
4. goto localhost:8080 (and use the endpoints accordingly)

## If the above doesn't work / How to run both at the same time:

1. Open a terminal or command prompt in the **backend** directory of your project.
   Use Maven to build the project:

   ```
   mvn clean install
   ```

2. Once the build is successful, you can run the Spring Boot application:

   ```
   mvn spring-boot:run
   ```

3. Open another terminal or command prompt in the **frontend** directory of your project

   ```
   npm run dev
   ```

4. If there is dependency missing, do the following first and then run it again (npm run dev):
   ```
   npm i
   ```
