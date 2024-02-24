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
    case Search(query)              => (this, Endpoint.searchUsers(query)(()))
    case UsersRetriveSuccess(users) => (this.copy(users = users), Cmd.None)
    case UsersRetriveFailure(error) => (this.copy(error = error.some), Cmd.None)
  }

  override def view: Html[Page.Msg] | List[Html[Page.Msg]] = error.fold(List(
    div(`class` := "form-floating mb-3 mx-auto", style := "max-width:90%")(
      input(
        `type`      := "text",
        `class`     := "form-control rounded-corners",
        id          := "searchbox",
        placeholder := "Ricerca socio",
        onInput(Search(_))
      ),
      label(`for` := "searchbox")("Ricerca socio")
    ),
    table(`class` := "table table-hover align-middle")(tableHeader, tbody(users.map(renderUser)))
  ))(e => div(h1("Utenti"), div(e)))

  val tableHeader = thead(tr(th("Nominativo"), th("Contatti"), th("Tessera")))

  def renderUser(u: UserView) = tr(onClick(InternalRedirect(IndividualUserPage(u.id.toString))))(
    th(scope := "row")(s"${u.name} ${u.surname}"),
    td(
      div(href := s"mailto:${u.email.value}")(u.email.value),
      div(href := s"tel:${u.phoneNumber.value}")(u.phoneNumber.value)
    ),
    td(
      div(s"Numero: ${u.membershipCardNumber}"),
      div(
        s"Scadenza: ${u.memberSince.plusYears(1).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}"
      )
    )
  )

}

object UserListPage:
  trait Msg                                             extends Page.Msg
  case class Search(query: String)                      extends Msg
  case class UsersRetriveSuccess(users: List[UserView]) extends Msg
  case class UsersRetriveFailure(error: String)         extends Msg
