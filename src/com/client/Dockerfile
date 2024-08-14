FROM hseeberger/scala-sbt:8u222_1.3.5_2.13.1
WORKDIR /app
COPY . .
RUN sbt clean compile
CMD ["sbt", "run"]
