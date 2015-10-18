package ar.com.crypticmind.ephemeralvault.repositories

import java.time.ZonedDateTime
import java.util.concurrent.atomic.AtomicLong

import ar.com.crypticmind.ephemeralvault.misc.DatabaseComponent
import ar.com.crypticmind.ephemeralvault.model.OneTimeToken

trait OneTimeTokenRepositoryComponent {

  trait OneTimeTokenRepository {
    def insert(oneTimeToken: OneTimeToken): OneTimeToken
    def update(oneTimeToken: OneTimeToken): OneTimeToken
    def get(key: String, token: String): Option[OneTimeToken]
  }

  def oneTimeTokenRepository: OneTimeTokenRepository
}

trait InMemoryOneTimeTokenRepository { self: OneTimeTokenRepositoryComponent =>

  val oneTimeTokenRepository = new OneTimeTokenRepository {

    var tokens = Map.empty[Long, OneTimeToken]
    val nextId = new AtomicLong()

    def insert(oneTimeToken: OneTimeToken): OneTimeToken = {
      val withID = oneTimeToken.copy(id = nextId.incrementAndGet())
      tokens += withID.id -> withID
      withID
    }

    def update(oneTimeToken: OneTimeToken): OneTimeToken = {
      tokens += oneTimeToken.id -> oneTimeToken
      oneTimeToken
    }

    def get(key: String, token: String): Option[OneTimeToken] =
      tokens.find(e => e._2.key == key && e._2.token == token).map(_._2)

  }

}

trait DatabaseOneTimeTokenRepository { self: OneTimeTokenRepositoryComponent with DatabaseComponent =>

  val oneTimeTokenRepository = new OneTimeTokenRepository {

    import anorm._
    import anorm.SqlParser

    val parser = for {
      id <- SqlParser.long("ID")
      key <- SqlParser.str("TOKEN_KEY")
      data <- SqlParser.str("DATA")
      token <- SqlParser.str("TOKEN")
      validUntil <- SqlParser.get[ZonedDateTime]("VALID_UNTIL")
      createDate <- SqlParser.get[ZonedDateTime]("CREATE_DATE")
      useDate <- SqlParser.get[Option[ZonedDateTime]]("USE_DATE")
    } yield OneTimeToken(key, data, token, validUntil, createDate, useDate, id)

    def get(key: String, token: String): Option[OneTimeToken] =
      database.withConnection { implicit conn =>
        SQL("SELECT * FROM ONE_TIME_TOKEN WHERE TOKEN_KEY = {key} AND TOKEN = {token}")
          .on('key -> key, 'token -> token)
          .as(parser.singleOpt)
      }

    def insert(ott: OneTimeToken): OneTimeToken =
      database.withConnection { implicit conn =>
        val id: Option[Long] =
          SQL(
            """INSERT INTO ONE_TIME_TOKEN (TOKEN_KEY, TOKEN, DATA, CREATE_DATE, VALID_UNTIL, USE_DATE)
               VALUES ({key}, {token}, {data}, {createDate}, {validUntil}, {useDate})""")
            .on('key → ott.key, 'token → ott.token, 'data → ott.data, 'createDate → ott.createDate, 'validUntil -> ott.validUntil,'useDate -> ott.useDate)
            .executeInsert()
        ott.copy(id = id.get)
      }

    def update(ott: OneTimeToken): OneTimeToken = {
      require(ott.id > 0, "Transient entity")
      database.withConnection { implicit conn =>
        SQL(
          """UPDATE ONE_TIME_TOKEN
             SET TOKEN_KEY = {key}, TOKEN = {token}, DATA = {data}, CREATE_DATE = {createDate}, VALID_UNTIL = {validUntil}, USE_DATE = {useDate}
             WHERE ID = {id}""")
          .on('key → ott.key, 'token → ott.token, 'data → ott.data, 'createDate → ott.createDate, 'validUntil -> ott.validUntil, 'useDate -> ott.useDate, 'id -> ott.id)
          .executeUpdate()
        ott
      }
    }
  }
}
