FROM eclipse-temurin:17-jdk

WORKDIR /app

COPY src ./src
COPY web ./web

RUN mkdir -p lib
ADD https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/3.46.1.3/sqlite-jdbc-3.46.1.3.jar /app/lib/sqlite-jdbc.jar
ADD https://repo1.maven.org/maven2/org/postgresql/postgresql/42.7.4/postgresql-42.7.4.jar /app/lib/postgresql-jdbc.jar

RUN mkdir -p out && javac -d out $(find src/main/java -name "*.java")

EXPOSE 8080

CMD ["sh", "-c", "java -cp \"out:lib/sqlite-jdbc.jar:lib/postgresql-jdbc.jar\" com.student.Main"]
