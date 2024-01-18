package dev.toniogela.conservami.pages

import tyrian.*
import tyrian.Html.*
import cats.effect.IO
import cats.syntax.all.*
import dev.toniogela.conservami.pages.Page.Msg
import dev.toniogela.conservami.UserView
import dev.toniogela.conservami.core.Endpoint
import dev.toniogela.conservami.pages.UserListPage.*
import java.time.format.DateTimeFormatter
import dev.toniogela.conservami.pages.Page.InternalRedirect

final case class UserListPage(users: List[UserView] = Nil, error: Option[String] = None)
    extends Page {
  val url: String = "/"

  override def initCommand: Cmd[IO, Page.Msg] = Endpoint.getUsers(())

  override def update(msg: Msg): (Page, Cmd[IO, Page.Msg]) = msg match {
    case UsersRetriveSuccess(users) => (this.copy(users = users), Cmd.None)
    case UsersRetriveFailure(error) => (this.copy(error = error.some), Cmd.None)
  }

  override def view: Html[Page.Msg] = error.fold(div(
    table(`class` := "table table-hover")(tableHeader, tbody(users.map(renderUser)))
  ))(e => div(h1("Utenti"), div(e)))

  val tableHeader = thead(tr(
    th("Nome"),
    th("Cognome"),
    th("Mail"),
    th("Telefono"),
    th("# Tessera"),
    th("Data Iscrizione")
  ))

  def renderUser(u: UserView) = tr(onClick(InternalRedirect(IndividualUserPage(u.id.toString))))(
    td(u.name),
    td(u.surname),
    td(u.email.value),
    td(u.phoneNumber.value),
    td(u.membershipCardNumber),
    td(u.memberSince.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
  )

}

object UserListPage:
  trait Msg                                             extends Page.Msg
  case class UsersRetriveSuccess(users: List[UserView]) extends Msg
  case class UsersRetriveFailure(error: String)         extends Msg
