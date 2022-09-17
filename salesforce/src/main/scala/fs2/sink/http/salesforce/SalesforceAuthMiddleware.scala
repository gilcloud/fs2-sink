package fs2.sink.http.salesforce

import cats.syntax.all._
import cats.effect.{Async, Ref, Resource}
import org.http4s.implicits.http4sLiteralsSyntax
import _root_.io.circe.generic.auto._
import org.http4s.{AuthScheme, Credentials, MediaType}
import org.http4s.Credentials.Token
import org.http4s.Method.POST
import org.http4s.client.Client
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.client.middleware.ResponseLogger
import org.http4s.circe.CirceEntityCodec.circeEntityDecoder
import org.http4s.headers.{Accept, Authorization, `Content-Type`}

import java.time.Duration
import java.time.Instant
import scala.math.Ordered.orderingToOrdered

object SalesforceAuthMiddleware {

  def passOrRefreshToken[F[_]: Async](grantString: String, cacheRef: Ref[F, Cache], client: Client[F]): F[Token] = {
    val dsl = new Http4sClientDsl[F] {}
    import dsl._
    val authReq =
      POST(
        grantString,
        uri"https://login.salesforce.com/services/oauth2/token",
        `Content-Type`(MediaType.application.`x-www-form-urlencoded`),
        Accept(MediaType.application.json)
      )
    for {
      tr <- ResponseLogger(logHeaders = false, logBody = false)(client).expect[TokenResponse](authReq)
      newCache = Cache(Credentials.Token(AuthScheme.Bearer, tr.access_token), Instant.ofEpochSecond(tr.issued_at.toLong))
      _ <- cacheRef.set(newCache)
    } yield newCache.token

  }

  def apply[F[_]](salesforceConfig: SalesforceConfig, tokeRef: Ref[F, Cache])(client: Client[F])(implicit async: Async[F]): Client[F] =
    Client { req =>
      Resource
        .eval {
          for {
            cache <- tokeRef.get
            token <-
              if (Duration.between(cache.issuedAt, Instant.now()) > Duration.ofHours(2)) passOrRefreshToken(salesforceConfig.oauthString, tokeRef, client)
              else async.pure(cache.token)
          } yield req.withHeaders(Authorization(token), Accept(MediaType.application.json), `Content-Type`(MediaType.application.json))
        }
        .flatMap(modReq => client.run(modReq))
    }

}

case class TokenResponse(access_token: String, issued_at: String)
