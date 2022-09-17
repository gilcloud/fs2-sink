package fs2.sink.http.salesforce

import org.http4s.Uri

case class SalesforceConfig(uri: Uri, clientKey: String, clientSecret: String, username: String, password: String, passwordToken: String) {

  private[salesforce] def oauthString: String =
    s"""grant_type=password&client_id=$clientKey&client_secret=$clientSecret&username=$username&password=$password$passwordToken""".stripMargin
}
