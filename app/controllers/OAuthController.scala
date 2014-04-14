package stack_notify.controllers

import stack_notify.models.{User, UserModel}
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.ws.WS
import play.api.Play.current
import play.api.mvc._


object OAuthController extends StackNotifyController {


	//val Host = "localhost:9000"
	val Host = "184.73.152.240"

	val StackExchange = "https://stackexchange.com/oauth"
	val ClientSecret = "OByZgeorlcL4k7NfIfpVAA(("
	val Key = "uzuwlVXnOAAwH*PM0goEPw(("
	val ClientId = "2836"
	val CallbackUrl = "http://" + Host + "/oauth/"

	val PostRequest = "https://stackexchange.com/oauth/access_token"

	def getOAuthUrl = {
		StackExchange + "?client_id=" + ClientId + "&redirect_uri=" + CallbackUrl
	}
	
	def getPostParams(code: String, googleId: String): Map[String, Seq[String]] = {
		Map(
			"client_id" -> Seq(ClientId),
			"client_secret" -> Seq(ClientSecret),
			"code" -> Seq(code),
			"redirect_uri" -> Seq(CallbackUrl + googleId + "/callback")
		)
	}

	def callback = Action { implicit request => 
		Async {
			
			val code = request.getQueryString("code").get
			val idRegex = ".*/oauth/(.+)/.*".r
			val googleId = request.path match {
				case idRegex(group) => group
			}
			WS.url(PostRequest).post(getPostParams(code, googleId)).map { response =>
				val regex = "access_token=(.+)&.*".r
				val access = response.body match {
					case regex(group) => group
				}
				val user = UserModel.findByGoogleId(googleId)
				if (user.isDefined) {
					UserModel.update(user.get.copy(accessToken = Some(access)))
					Ok("Successfully connected. It is safe to close this window.")
				}
				else {
					failure("Unable to find user")
				}
			}
		}
	}
}
