package dev.toniogela.conservami

import cats.effect.*
import java.util.UUID

object Main extends IOApp.Simple {
  def run: IO[Unit] = IO.println(User(UUID.randomUUID(), "pippo", "pluto"))
}
