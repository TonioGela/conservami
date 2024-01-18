package dev.toniogela.conservami

import cats.effect.*
import cats.syntax.all.*
import dev.toniogela.conservami.User.PhoneNumber

object Main extends IOApp.Simple:

  def run: IO[Unit] = IO
    .fromEither(PhoneNumber.from("+39/3333341324").leftMap(s => new Exception(s))) >>= IO.println
