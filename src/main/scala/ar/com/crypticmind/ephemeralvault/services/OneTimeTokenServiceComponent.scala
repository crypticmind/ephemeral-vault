package ar.com.crypticmind.ephemeralvault.services

import java.time.ZonedDateTime
import java.util.UUID

import ar.com.crypticmind.ephemeralvault.model.OneTimeToken
import ar.com.crypticmind.ephemeralvault.model.api.OneTimeTokenAPI.{CreateRQ, CreateRS}
import ar.com.crypticmind.ephemeralvault.repositories.OneTimeTokenRepositoryComponent

trait OneTimeTokenServiceComponent {

  sealed trait TokenError
  case object TokenExpired extends TokenError
  case object TokenAlreadyUsed extends TokenError

  trait OneTimeTokenService {
    def create(key: String, rq: CreateRQ): CreateRS
    def get(key: String, token: String): Option[Either[TokenError, OneTimeToken]]
  }

  def oneTimeTokenService: OneTimeTokenService
}

trait DefaultOneTimeTokenService { self: OneTimeTokenServiceComponent with OneTimeTokenRepositoryComponent =>

  val oneTimeTokenService = new OneTimeTokenService {

    def create(key: String, rq: CreateRQ): CreateRS = {
      val token = UUID.randomUUID().toString
      val validUntil = (rq.validFor, rq.validUntil) match {
        case (Some(duration), None) => ZonedDateTime.now().plus(duration)
        case (None, Some(dateTime)) => dateTime
        case (Some(_), Some(_)) => throw new Exception("Must indicate only one of validFor or validUntil")
        case (None, None) => throw new Exception("Must indicate either validFor or validUntil")
      }
      val oneTimeToken = OneTimeToken(key, rq.data.toString(), token, validUntil, ZonedDateTime.now(), None)
      oneTimeTokenRepository.insert(oneTimeToken)
      CreateRS(token)
    }

    def get(key: String, token: String): Option[Either[TokenError, OneTimeToken]] = {
      val oneTimeTokenOpt = oneTimeTokenRepository.get(key, token)
      oneTimeTokenOpt
        .filter(_.useDate.isEmpty)
        .map(_.copy(useDate = Some(ZonedDateTime.now())))
        .foreach(oneTimeTokenRepository.update)
      oneTimeTokenOpt.map {
        case alreadyUsed if alreadyUsed.useDate.isDefined => Left(TokenAlreadyUsed)
        case expired if expired.validUntil.isBefore(ZonedDateTime.now()) => Left(TokenExpired)
        case oneTimeToken => Right(oneTimeToken)
      }
    }
  }
}
