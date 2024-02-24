package dev.toniogela.conservami.persistence

import dev.toniogela.conservami.User
import dev.toniogela.conservami.*
import java.util.UUID
import skunk.*
import skunk.implicits.*
import skunk.codec.all.*
import cats.Monad
import cats.syntax.all.*
import cats.effect.Concurrent
import cats.Applicative
import cats.effect.kernel.Resource
import cats.ApplicativeThrow
import cats.effect.std.Console

trait UserRepo[F[_]] {
  def search(string: String): F[List[User]]
  def addUser(user: PersistenceUserAdd): F[Unit]
  def alterUser(id: UUID, user: PersistenceUserAdd): F[Unit]
  def getAllUsers(): F[List[User]]
  def getUserById(id: UUID): F[Option[User]]
  def getUsersByName(name: String): F[List[User]]
  def getUsersBySurname(surname: String): F[List[User]]
  def getUsersByNameAndSurname(name: String, surname: String): F[List[User]]
  def getPdf(id: UUID): F[Option[Array[Byte]]]
}

class DatabaseUserRepo[F[_]: Monad: Concurrent: Console](sessions: Resource[F, Session[F]])
    extends UserRepo[F]:

  import DatabaseUserRepo.*

  override def search(queryS: String): F[List[User]] = {
    val likeId          = sql"WHERE lower(cast(id as text)) like $varchar"
    val likeName        = sql" OR lower(name) like $varchar"
    val likeSurname     = sql" OR lower(surname) like $varchar"
    val likeFiscalCode  = sql" OR lower(fiscalCode) like $varchar"
    val likeEmail       = sql" OR lower(email) like $varchar"
    val likePhoneNumber = sql" OR lower(phoneNumber) like $varchar"

    val combinedWhere: Fragment[String] =
      (likeId *: likeName *: likeSurname *: likeFiscalCode *: likeEmail *: likePhoneNumber)
        .contramap((s: String) => (s, s, s, s, s, s))

    val query: Query[String, User] = sql"SELECT * FROM conservami.users $combinedWhere"
      .query(userCodec)

    retry(2)(sessions.use(_.execute(query)(s"%${queryS.toLowerCase}%")))
  }

  override def addUser(userAdd: PersistenceUserAdd): F[Unit] = {
    val query: Command[PersistenceUserAdd] =
      sql"INSERT INTO conservami.users VALUES (gen_random_uuid(), $userAddCodec)".command

    // TODO should we automatically enforce (lower(name), lower(surname)) uniqueness?

    retry(2)(sessions.flatMap(_.prepareR(query)).use(_.execute(userAdd).void))
  }

  override def alterUser(id: UUID, userAdd: PersistenceUserAdd): F[Unit] = {
    val tupledQuery = sql"""UPDATE conservami.users
            SET
              name = ${varchar(100)},
              surname = ${varchar(100)},
              birthPlace = ${varchar(100)},
              birthDate = $date,
              fiscalCode = $fiscalCode,
              residence = ${varchar(100)},
              phoneNumber = $phoneNumber,
              email = $email,
              profession = ${varchar(100).opt},
              memberSince = $date,
              membershipCardNumber = ${varchar(100)},
              donation = ${int8.imap(_.toInt)(_.toLong)},
              pdfDocument = $bytea
            WHERE id = $uuid""".command

    val query: Command[User] = tupledQuery.contramap { case u: User =>
      val generic = Tuple.fromProductTyped(u)
      generic.tail :* generic.head
    }

    retry(2)(sessions.flatMap(_.prepareR(query)).use(_.execute(userAdd.toUser(id)).void))
  }

  override def getAllUsers(): F[List[User]] = {
    val query: Query[Void, User] = sql"SELECT * FROM conservami.users".query(userCodec)

    retry(2)(sessions.use(_.execute(query)))
  }

  override def getUsersByName(name: String): F[List[User]] = {
    val query: Query[String, User] =
      sql"SELECT * FROM conservami.users where lower(name) = lower($varchar)".query(userCodec)

    retry(2)(sessions.use(_.prepare(query).flatMap(ps => ps.stream(name, 32).compile.toList)))
  }

  override def getUsersBySurname(surname: String): F[List[User]] = {
    val query: Query[String, User] =
      sql"SELECT * FROM conservami.users where lower(surname) = lower($varchar)".query(userCodec)
    retry(2)(sessions.use(_.prepare(query).flatMap(ps => ps.stream(surname, 32).compile.toList)))
  }

  override def getUsersByNameAndSurname(name: String, surname: String): F[List[User]] = {
    val query: Query[(String, String), User] =
      sql"SELECT * FROM conservami.users where lower(name) = lower($varchar) and lower(surname) = lower($varchar)"
        .query(userCodec)
    retry(2)(
      sessions.use(_.prepare(query).flatMap(ps => ps.stream((name, surname), 32).compile.toList))
    )
  }

  override def getUserById(id: UUID): F[Option[User]] = {
    val query: Query[UUID, User] = sql"SELECT * FROM conservami.users where id = $uuid"
      .query(userCodec)
    retry(2)(sessions.use(_.prepare(query).flatMap(_.option(id))))
  }

  override def getPdf(id: UUID): F[Option[Array[Byte]]] = {
    val query: Query[UUID, Array[Byte]] =
      sql"SELECT pdfDocument FROM conservami.users where id = $uuid".query(bytea)
    retry(2)(sessions.use(_.prepare(query).flatMap(_.option(id))))
  }

  private def retry[F[_]: ApplicativeThrow, T](n: Int)(ft: F[T]): F[T] =
    if n === 1 then ft else ft.recoverWith(_ => retry(n - 1)(ft))

end DatabaseUserRepo

object DatabaseUserRepo:

  val fiscalCode: Codec[ItalianFiscalCode] = bpchar(16).eimap(ItalianFiscalCode.from)(_.value)
  val phoneNumber: Codec[PhoneNumber]      = varchar(100).eimap(PhoneNumber.from)(_.value)
  val email: Codec[Email]                  = varchar(100).eimap(Email.from)(_.value)

  val userCodec: Codec[User] =
    (uuid *: varchar(100) *: varchar(100) *: varchar(100) *: date *: fiscalCode *: varchar(100) *:
      phoneNumber *: email *: varchar(100).opt *: date *: varchar(100) *:
      int8.imap(_.toInt)(_.toLong) *: bytea).to[User]

  val userAddCodec: Codec[PersistenceUserAdd] =
    (varchar(100) *: varchar(100) *: varchar(100) *: date *: fiscalCode *: varchar(100) *:
      phoneNumber *: email *: varchar(100).opt *: date *: varchar(100) *:
      int8.imap(_.toInt)(_.toLong) *: bytea).to[PersistenceUserAdd]

  def apply[F[_]: Monad: Concurrent: Console](
      sessions: Resource[F, Session[F]]
  ): F[DatabaseUserRepo[F]] = Applicative[F].pure(new DatabaseUserRepo[F](sessions))
