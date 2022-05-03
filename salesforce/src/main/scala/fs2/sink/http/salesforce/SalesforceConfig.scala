package fs2.sink.http.salesforce

import org.http4s.Uri

case class SalesforceConfig(token: String, uri: Uri)
