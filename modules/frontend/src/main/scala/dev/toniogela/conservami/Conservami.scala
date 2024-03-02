package dev.toniogela.conservami

import cats.effect.IO
import scala.scalajs.js.annotation.*
import tyrian.*
import tyrian.Html.*
import org.scalajs.dom.window
import dev.toniogela.conservami.components.Header
import dev.toniogela.conservami.pages.Page
import cats.syntax.all.*

object State:
  type Msg = Page.Msg
  case class Model(page: Page)
end State

@JSExportTopLevel("Conservami")
object Conservami extends TyrianIOApp[State.Msg, State.Model]:

  def main(args: Array[String]): Unit = launch("myapp")

  override def init(flags: Map[String, String]): (State.Model, Cmd[IO, State.Msg]) = {
    val location   = window.location.pathname
    val page: Page = Page.get(location)
    (State.Model(page), page.initCommand)
  }

  override def view(model: State.Model): Html[State.Msg] = div(
    Header.view,
    model.page.view match
      case h: Html[Page.Msg]        => tyrian.Html
          .main(`class` := "page-container container")(div(`class` := "col")(h))
      case hs: List[Html[Page.Msg]] => tyrian.Html
          .main(`class` := "page-container container")(div(`class` := "col")(hs*))
  )

  override def router: Location => State.Msg =
    case l: Location.Internal => Page.NavigateToInternal(Page.get(l.locationDetails.pathName))
    case l: Location.External => Page.NavigateToExternal(l.locationDetails.pathName)

  override def subscriptions(model: State.Model): Sub[IO, State.Msg] = Sub.None

  override def update(state: State.Model): State.Msg => (State.Model, Cmd[IO, State.Msg]) =
    case Page.NavigateToInternal(page) =>
      if state.page.url === page.url then (state, Cmd.None)
      else (state.copy(page = page), page.initCommand)
    case Page.NavigateToExternal(url)  => (state, Nav.loadUrl(url))
    case Page.InternalRedirect(page)   =>
      if state.page.url === page.url then (state, Cmd.None)
      else (state.copy(page = page), Cmd.Batch(Nav.pushUrl(page.url), page.initCommand))
    case msg: Page.Msg                 =>
      val (newPage, cmd) = state.page.update(msg)
      (state.copy(page = newPage), cmd)

end Conservami
