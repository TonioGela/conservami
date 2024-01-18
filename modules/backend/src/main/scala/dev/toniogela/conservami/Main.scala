package dev.toniogela.conservami

import fly4s.*
import cats.effect.*
import cats.syntax.all.*
import com.monovore.decline.effect.CommandIOApp
import com.monovore.decline.Opts
import dev.toniogela.conservami.config.Configuration
import dev.toniogela.conservami.config.PgCredentials
import dev.toniogela.conservami.persistence.DatabaseUserRepo

import skunk.Session
import natchez.Trace.Implicits.noop
import dev.toniogela.conservami.api.UserRoutes
import org.typelevel.log4cats.LoggerFactory
import org.typelevel.log4cats.slf4j.Slf4jFactory
import org.http4s.ember.server.EmberServerBuilder
import com.comcast.ip4s._
import fs2.io.net.SocketOption
import org.http4s.server.Router
import cats.effect.std.Console
import cats.data.Kleisli
import dev.toniogela.conservami.api.StaticRoutes

// VALUTARE DOOBIE
// provare con ab quanto regge

object Main extends CommandIOApp("conservami", "app di conservami", true, "0.1.0"):

  def migrateDb: PgCredentials => Resource[IO, Unit] = (pgCreds: PgCredentials) =>
    Fly4s.make[IO](
      url = pgCreds.jdbcUrl,
      user = pgCreds.user.some,
      password = pgCreds.password.map(_.toCharArray()),
      config = Configuration.fly4s
    ).evalMap(_.migrate).void

  def createSessions: PgCredentials => Resource[IO, Resource[IO, Session[IO]]] =
    (pgCreds: PgCredentials) =>
      Session.pooled[IO](
        host = pgCreds.host,
        port = pgCreds.port,
        user = pgCreds.user,
        database = pgCreds.database,
        password = pgCreds.password,
        max = 5,
        socketOptions = SocketOption.keepAlive(true) :: Session.DefaultSocketOptions
      )

  override def main: Opts[IO[ExitCode]] = Configuration.pgOpts.map { pgCreds =>
    (migrateDb(pgCreds) >> createSessions(pgCreds)).use { sessions =>
      for
        repo <- DatabaseUserRepo[IO](sessions)
        staticRoutes = StaticRoutes[IO]
        userRoutes <- UserRoutes[IO](repo).map(r => Router("user" -> r))
        apiRoutes               = Router("api" -> userRoutes)
        routes                  = apiRoutes <+> staticRoutes
        given LoggerFactory[IO] = Slf4jFactory.create[IO]
        app = routes.orNotFound.onError(t => Kleisli.liftF(Console[IO].printStackTrace(t)))
        server <- EmberServerBuilder.default[IO].withHttp2.withHost(ipv4"0.0.0.0")
          .withPort(port"8080").withHttpApp(app).build.useForever
      yield ExitCode.Success
    }
  }
