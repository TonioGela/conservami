package dev.toniogela.conservami.api

import cats.syntax.all.*
import org.http4s.dsl.Http4sDsl
import org.http4s.HttpRoutes
import org.http4s.*
import cats.effect.kernel.Sync

class StaticRoutes[F[_]: Sync] extends Http4sDsl[F]:

  def staticFromResource(file: String, maybeReq: Option[Request[F]]): F[Response[F]] = StaticFile
    .fromResource[F](file, maybeReq).getOrElseF(NotFound())

  def routes: HttpRoutes[F] = HttpRoutes.of[F] {
    case req @ GET -> "assets" /: filename => staticFromResource(s"assets/$filename", req.some)
    case GET -> Root / "health"            => Ok()
    case req @ GET -> Root                 => staticFromResource("index.html", req.some)
    case req if req.method === GET         => staticFromResource("index.html", req.some)
  }

end StaticRoutes

object StaticRoutes:
  def apply[F[_]: Sync]: HttpRoutes[F] = new StaticRoutes[F].routes
