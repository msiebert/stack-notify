package stack_notify.controllers

import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.ws.WS
import play.api.Play.current
import play.api.mvc._


object OAuthController extends Controller {


	val Host = "localhost:9000"
	// val Host = 184.73.152.240

	val StackExchange = "https://stackexchange.com/oauth"
	val ClientSecret = "OByZgeorlcL4k7NfIfpVAA(("
	val Key = "uzuwlVXnOAAwH*PM0goEPw(("
	val ClientId = "2836"
	val CallbackUrl = Host + "/oauth/callback"

	val PostRequest = "https://stackexchange.com/oauth/access_token"

	def getOAuthUrl = {
		StackExchange + "?client_id=" + ClientId + "&redirect_uri=" + CallbackUrl
	}
	
	def getPostParams(code: String): Map[String, Seq[String]] = {
		Map(
			"client_id" -> Seq(ClientId),
			"client_secret" -> Seq(ClientSecret),
			"code" -> Seq(code),
			"redirect_uri" -> Seq(CallbackUrl)
		)
	}

	def callback = Action { implicit request => 
		Async {
			val code = request.getQueryString("code").get

			WS.url(PostRequest).post(getPostParams(code)).map { response =>
				Ok(response.body)
			}
		}
	}
}