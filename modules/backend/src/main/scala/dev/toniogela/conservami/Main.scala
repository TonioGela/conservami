package dev.toniogela.conservami

import cats.effect.*
import java.util.UUID
import io.circe.syntax.*
import java.time.*

object Main extends IOApp.Simple:

  def run: IO[Unit] = IO.println(
    User(
      UUID.randomUUID(),
      "tonio",
      "gela",
      "Brescia",
      ZonedDateTime.now(ZoneId.of("Europe/Rome")).toOffsetDateTime(),
      "GLMNTN89S03B157Y",
      "Milano",
      "3333341324",
      "toniogela@sticazzi.com",
      "developer",
      ZonedDateTime.now(ZoneId.of("Europe/Rome")).toOffsetDateTime()
        .atZoneSameInstant(ZoneId.of("Europe/Rome")),
      "001-A",
      "foo".getBytes()
    ).asJson
  )
