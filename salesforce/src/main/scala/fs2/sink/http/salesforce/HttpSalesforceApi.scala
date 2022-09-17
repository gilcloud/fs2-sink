package fs2.sink.http.salesforce

import cats._
import cats.implicits._
import _root_.io.circe.Encoder
import _root_.io.circe.syntax._
import cats.effect.{Ref, Resource}
import cats.effect.kernel.Async
import fs2.sink.core.{InputRecord, KeyEx, KeyExtract, SinkAlgebra, Transformer}
import org.http4s.Method.{GET, PATCH}
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.client.{Client, UnexpectedStatus}
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.client.middleware.Logger
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.{Request, Status}

class HttpSalesforceApi[F[_], V, O: Encoder](client: Client[F], salesforceConfig: SalesforceConfig, tokeRef: Ref[F, Cache])(implicit
    aS: Async[F],
    override val keyExtractor: KeyEx[V],
    override val payloadTransform: Transformer[V, O]
) extends SinkAlgebra[F, Option[String], V, O] {

  private val salesforceClient = SalesforceAuthMiddleware(salesforceConfig, tokeRef)(client)

  private val dsl = new Http4sClientDsl[F] {}

  import dsl._

  private val getReq: String => Request[F] = key => GET(salesforceConfig.uri / key)

  private val patchReq: String => V => Request[F] = key => record => PATCH(payloadTransform.transform(record).asJson.dropNullValues, salesforceConfig.uri / key)

  override def updateRecord(record: InputRecord[Option[String], V]): F[String] = key(record).fold(aS.pure("error no key")) { k =>
    Logger(logHeaders = false, logBody = false)(salesforceClient)
      .run(patchReq(k)(record.value))
      .use { re =>
        aS.pure(
          if (List(Status.NoContent.code, Status.NotFound.code).contains(re.status.code)) "Success"
          else throw UnexpectedStatus(re.status, patchReq(k)(record.value).method, patchReq(k)(record.value).uri)
        )
      }
  }

  override def getRecord(record: InputRecord[Option[String], V]): F[String] = key(record).fold(aS.pure("error no key")) { k =>
    Logger(logHeaders = true, logBody = false)(salesforceClient).expect[String](getReq(k))
  }
}

object HttpSalesforceApi {

  def buildSinkResource[F[_], V: KeyEx, O: Encoder](salesforceConfig: SalesforceConfig)(implicit
      F: Async[F],
      transformer: Transformer[V, O]
  ): F[Resource[F, HttpSalesforceApi[F, V, O]]] = {
    Ref
      .of[F, Cache](Cache.defaultCache)
      .flatMap { tokenRef =>
        F.delay(EmberClientBuilder.default[F].build.flatMap(cli => Resource.liftK(F.delay(new HttpSalesforceApi[F, V, O](cli, salesforceConfig, tokenRef)))))
      }
  }
}
