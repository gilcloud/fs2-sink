package fs2.sink.http.salesforce

import _root_.io.circe.Encoder
import _root_.io.circe.syntax._
import cats.effect.kernel.Async
import fs2.sink.core.{InputRecord, KeyExtract, SinkAlgebra, Transformer}
import org.http4s.Method.{GET, PATCH}
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.client.{Client, UnexpectedStatus}
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.client.middleware.Logger
import org.http4s.headers.{Accept, Authorization, `Content-Type`}
import org.http4s.{AuthScheme, Credentials, MediaType, Request}

class HttpSalesforceApi[F[_], V, O: Encoder](client: Client[F], salesforceConfig: SalesforceConfig)(implicit
    aS: Async[F],
    override val keyExtractor: Option[KeyExtract[V, Option[String]]],
    override val payloadTransform: Transformer[V, O]
) extends SinkAlgebra[F, Option[String], V, O] {

  private val dsl = new Http4sClientDsl[F] {}

  import dsl._

  private val getReq: String => Request[F] = key =>
    GET(salesforceConfig.uri / key, Authorization(Credentials.Token(AuthScheme.Bearer, salesforceConfig.token)), Accept(MediaType.application.json))

  private val patchReq: String => V => Request[F] = key =>
    record =>
      PATCH(
        payloadTransform.transform(record).asJson.dropNullValues,
        salesforceConfig.uri / key,
        Authorization(Credentials.Token(AuthScheme.Bearer, salesforceConfig.token)),
        Accept(MediaType.application.json),
        `Content-Type`(MediaType.application.json)
      )

  override def updateRecord(record: InputRecord[Option[String], V]): F[String] = key(record).fold(aS.pure("error no key")) { k =>
    Logger(logHeaders = false, logBody = false)(client)
      .run(patchReq(k)(record.value))
      .use { re =>
        aS.pure(if (List(204, 404).contains(re.status.code)) "yay" else throw UnexpectedStatus(re.status, patchReq(k)(record.value).method, patchReq(k)(record.value).uri))
      }
  }

  override def getRecord(record: InputRecord[Option[String], V]): F[String] = key(record).fold(aS.pure("error no key")) { k =>
    Logger(logHeaders = true, logBody = false)(client).expect[String](getReq(k))
  }
}
