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
import dev.toniogela.conservami.pages.CreateUserPage.Msg
import dev.toniogela.conservami.UserView
import dev.toniogela.conservami.pages.UserListPage
import io.circe.parser.*
import dev.toniogela.conservami.pages.IndividualUserPage

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

  val createUser: Endpoint[Msg, UserAdd] = Endpoint[CreateUserPage.Msg, UserAdd](
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

}
