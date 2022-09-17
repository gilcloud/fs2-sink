package fs2.sink.http.salesforce

import org.http4s.AuthScheme
import org.http4s.Credentials.Token

import java.time.Instant

case class Cache(token: Token, issuedAt: Instant)

object Cache {

  def defaultCache: Cache = Cache(Token(AuthScheme.Bearer, ""), Instant.EPOCH)
}
