package dev.toniogela.conservami.core

import tyrian.*
import tyrian.http.*
import cats.syntax.all.*
import buildinfo.BuildInfo
import io.circe.Encoder
import io.circe.syntax.*
import cats.effect.kernel.Async
import dev.toniogela.conservami.pages.CreateUserPage
import dev.toniogela.conservami.UserAdd
import dev.toniogela.conservami.UserView
import dev.toniogela.conservami.pages.UserListPage
import io.circe.parser.*
import dev.toniogela.conservami.pages.IndividualUserPage
import dev.toniogela.conservami.pages.Page
import dev.toniogela.conservami.pages.Page.InternalRedirect
import java.util.UUID

trait Endpoint[M, A: Encoder] private (
    val partialUrl: String,
    val method: Method,
    val onSuccess: Response => M,
    val onFailure: HttpError => M
) {

  def apply[F[_]: Async](payload: A): Cmd[F, M] = Http.send[F, A, M](
    Request(method, s"${BuildInfo.host}/api$partialUrl", Body.json(payload.asJson.noSpaces)),
    Decoder[M](onSuccess, onFailure)
  )
}

object Endpoint {

  private def apply[M, A: Encoder](
      partialUrl: String,
      method: Method,
      onSuccess: Response => M,
      onFailure: HttpError => M
  ) = new Endpoint[M, A](partialUrl, method, onSuccess, onFailure) {}

  val createUser: Endpoint[CreateUserPage.Msg, UserAdd] = Endpoint[CreateUserPage.Msg, UserAdd](
    "/user",
    Method.Post,
    r =>
      r.status.code match {
        case 201 => CreateUserPage.UserCreated
        case 409 => CreateUserPage.UserCreationError("Utente già esistente")
        case x   => CreateUserPage.UserCreationError(s"Errore $x")
      },
    e => CreateUserPage.UserCreationError(e.toString)
  )

  def alterUser(id: UUID): Endpoint[CreateUserPage.Msg, UserAdd] =
    Endpoint[CreateUserPage.Msg, UserAdd](
      s"/user/${id.toString}",
      Method.Put,
      r =>
        r.status.code match {
          case 200 => CreateUserPage.UserCreated
          case x   => CreateUserPage.UserCreationError(s"Errore $x")
        },
      e => CreateUserPage.UserCreationError(e.toString)
    )

  val getUsers: Endpoint[UserListPage.Msg, Unit] = Endpoint[UserListPage.Msg, Unit](
    "/user",
    Method.Get,
    r =>
      if r.status.code === 200 then
        decode[List[UserView]](r.body).fold(
          e =>
            UserListPage.UsersRetriveFailure(
              s"Risposta del server non deserializzabile: ${r.body} \n Errore: ${e.toString}"
            ),
          UserListPage.UsersRetriveSuccess(_)
        )
      else UserListPage.UsersRetriveFailure(s"Errore: $r.status.code"),
    e => UserListPage.UsersRetriveFailure(e.toString)
  )

  def getUser(id: String): Endpoint[IndividualUserPage.Msg, Unit] =
    Endpoint[IndividualUserPage.Msg, Unit](
      s"/user/$id",
      Method.Get,
      r =>
        if r.status.code === 200 then
          decode[UserView](r.body).fold(
            e =>
              IndividualUserPage.UserRetriveFailure(
                s"Risposta del server non deserializzabile: ${r.body} \n Errore: ${e.toString}"
              ),
            IndividualUserPage.UserRetriveSuccess(_)
          )
        else IndividualUserPage.UserRetriveFailure(s"Errore: $r.status.code"),
      e => IndividualUserPage.UserRetriveFailure(e.toString)
    )

  def deleteUser(id: UUID): Endpoint[Page.Msg, Unit] = Endpoint[Page.Msg, Unit](
    s"/user/${id.toString}",
    Method.Delete,
    r =>
      if r.status.code === 202 then InternalRedirect(UserListPage())
      else IndividualUserPage.UserDeletionError(s"Ricevuto status code: $r.status.code"),
    e => IndividualUserPage.UserDeletionError(e.toString)
  )

  def searchUsers(queryS: String): Endpoint[UserListPage.Msg, Unit] =
    Endpoint[UserListPage.Msg, Unit](
      s"/user/search/$queryS",
      Method.Get,
      r =>
        if r.status.code === 200 then
          decode[List[UserView]](r.body).fold(
            e =>
              UserListPage.UsersRetriveFailure(
                s"Risposta del server non deserializzabile: ${r.body} \n Errore: ${e.toString}"
              ),
            UserListPage.UsersRetriveSuccess(_)
          )
        else UserListPage.UsersRetriveFailure(s"Errore: $r.status.code"),
      e => UserListPage.UsersRetriveFailure(e.toString)
    )

}
