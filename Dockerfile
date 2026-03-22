FROM eclipse-temurin:17-jdk

WORKDIR /app

COPY src ./src
COPY web ./web

RUN mkdir -p out && javac -d out $(find src/main/java -name "*.java")

EXPOSE 8080

CMD ["sh", "-c", "java -cp out com.student.Main"]
