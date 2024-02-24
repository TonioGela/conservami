package dev.toniogela.conservami.pages

import tyrian.*
import cats.effect.IO
import cats.kernel.Eq
import cats.data.EitherNel

abstract class Page {
  type Field = [T] =>> EitherNel[String, T]
  val url: String
  def initCommand: Cmd[IO, Page.Msg]
  def update(msg: Page.Msg): (Page, Cmd[IO, Page.Msg])
  def view: Html[Page.Msg] | List[Html[Page.Msg]]

  def andNone: (Page, Cmd[IO, Page.Msg]) = (this, Cmd.None)
}

object Page:
  given Eq[Page] = Eq.fromUniversalEquals

  trait Msg
  case class NavigateToInternal(page: Page)  extends Msg
  case class NavigateToExternal(url: String) extends Msg
  case class InternalRedirect(page: Page)    extends Msg

  case object NoOp extends Msg

  object Urls:
    val empty  = ""
    val home   = "/"
    val user   = "/user"
    val create = "/create"

  import Urls.*

  def get(location: String): Page = location match
    case `empty` | `home` => UserListPage()
    case `create`         => CreateUserPage()
    // case s"/user/$id/edit" => EditUserPage(id) // TODO! "Questo percorso cozza con le API!"
    case s"/user/$id"     => IndividualUserPage(id)
    case x                => NotFoundPage(x)
