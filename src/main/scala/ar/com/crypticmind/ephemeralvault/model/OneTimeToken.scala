package ar.com.crypticmind.ephemeralvault.model

import java.time.ZonedDateTime

case class OneTimeToken(key: String,
                        data: String,
                        token: String,
                        validUntil: ZonedDateTime,
                        createDate: ZonedDateTime,
                        useDate: Option[ZonedDateTime],
                        id: Long = 0)
