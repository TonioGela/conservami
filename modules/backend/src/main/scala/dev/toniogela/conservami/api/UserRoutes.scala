package dev.toniogela.conservami.api

import dev.toniogela.conservami.*
import dev.toniogela.conservami.persistence.*
import fs2.*
import cats.*
import cats.effect.*
import cats.syntax.all.*
import org.http4s.*
import org.http4s.dsl.*
import org.http4s.dsl.io.*
import org.http4s.circe.*
import org.http4s.headers.*
import cats.effect.std.Console
import dev.toniogela.conservami.{PersistenceUserAdd, UserAdd}
import dev.toniogela.conservami.pdf.PdfCreator
import skunk.SqlState
import cats.data.Kleisli
import cats.data.OptionT

class UserRoutes[F[_]: Async: Console](userRepo: UserRepo[F]) extends Http4sDsl[F] {

  val retrievalRoutes: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root                                       => userRepo.getAllUsers().ok
    case GET -> Root / "name" / name                       => userRepo.getUsersByName(name).ok
    case GET -> Root / "surname" / surname                 => userRepo.getUsersBySurname(surname).ok
    case GET -> Root / "name" / name / "surname" / surname => userRepo
        .getUsersByNameAndSurname(name, surname).ok

    case GET -> Root / UUIDVar(id)         => userRepo.getUserById(id).orNotFound
    case GET -> Root / UUIDVar(id) / "pdf" => userRepo.getPdf(id).flatMap {
        case Some(pdf) => Ok(
            body = Stream.iterable[F, Byte](pdf),
            headers = List[Header.ToRaw](
              `Content-Type`(MediaType("application", "pdf")),
              `Content-Length`(pdf.length.toLong)
            )
          )
        case None      => NotFound()
      }
  }

  val creationOrModificationRoutes: HttpRoutes[F] = HttpRoutes.of[F] {
    case req @ POST -> Root =>
      for
        user <- req.as[UserAdd]
        pdf  <- PdfCreator.create(user).compile.to(Array)
        persistenceUser = PersistenceUserAdd.from(user, pdf)
        resp <- (userRepo.addUser(persistenceUser) >> Created()).recoverWith {
          case SqlState.UniqueViolation(ex) => Conflict()
        }
      yield resp // TODO! ritorna userId

    case req @ PUT -> Root / UUIDVar(id) => req.as[UserAdd].flatMap { user =>
        userRepo.getUserById(id).flatMap {
          case None    => NotFound()
          case Some(_) =>
            for
              pdf <- PdfCreator.create(user).compile.to(Array)
              persistenceUser = PersistenceUserAdd.from(user, pdf)
              resp <- userRepo.alterUser(id, persistenceUser) >> Ok()
            yield resp
        }
      }

  }

  val routes: HttpRoutes[F] = retrievalRoutes <+> creationOrModificationRoutes

  private given EntityEncoder[F, UserView]       = jsonEncoderOf[F, UserView]
  private given EntityEncoder[F, List[UserView]] = jsonEncoderOf[F, List[UserView]]
  private given EntityDecoder[F, UserAdd]        = jsonOf[F, UserAdd]

  extension (users: F[List[User]])
    def ok: F[Response[F]] = users.nested.map(UserView.from).value.flatMap(Ok(_))

  extension (users: F[Option[User]])

    def orNotFound: F[Response[F]] = users.flatMap {
      case Some(user) => Ok(UserView.from(user))
      case None       => NotFound()
    }
}

object UserRoutes:

  def apply[F[_]: Async: Console](userRepo: UserRepo[F]): F[HttpRoutes[F]] = Applicative[F]
    .pure(new UserRoutes(userRepo).routes)
