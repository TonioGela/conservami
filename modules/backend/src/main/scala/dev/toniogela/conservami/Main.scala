package dev.toniogela.conservami

import cats.effect.*

object Main extends IOApp.Simple {
  def run: IO[Unit] = IO.println("Hello World")
}
