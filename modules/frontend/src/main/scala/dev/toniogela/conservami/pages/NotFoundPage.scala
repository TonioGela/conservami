package dev.toniogela.conservami.pages

import tyrian.*
import tyrian.Html.*
import cats.effect.IO
import dev.toniogela.conservami.pages.Page.Msg

case class NotFoundPage(dummyUrl: String) extends Page {

  val url: String = dummyUrl // TODO! This thing is subject to XSS

  override def initCommand: Cmd[IO, Page.Msg] = Cmd.None

  override def update(msg: Msg): (Page, Cmd[IO, Page.Msg]) = (this, Cmd.None)

  override def view: Html[Page.Msg] = div("Not Found Page - TODO")
}
