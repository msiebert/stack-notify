package controllers

import play.api.mvc._
import java.net.URL
import java.net.HttpURLConnection
import java.io._


object Application extends Controller {

  val StackExchange = "https://stackexchange.com/oauth"
  val ClientSecret = "OByZgeorlcL4k7NfIfpVAA(("
  val Key = "uzuwlVXnOAAwH*PM0goEPw(("
  val ClientId = 2836
  val CallbackUrl = "localhost:8080/callback"

  val PostRequest = "https://stackexchange.com/oauth/access_token"

  def getOAuthUrl = {
    StackExchange + "?client_id=" + ClientId + "&redirect_uri=" + CallbackUrl
  }
  def getPostParams(code: String) = {
    "client_id=" + ClientId + "&client_secret=" + ClientSecret + "&code=" + code + "&redirect_uri=" + CallbackUrl
  }

  def callback = Action { implicit request => 
    val code = request.getQueryString("code").get

    val urlParams = getPostParams(code)
    val url = new URL(PostRequest)
    val connection = url.openConnection().asInstanceOf[HttpURLConnection]

    connection.setDoOutput(true)
    connection.setDoInput(true)
    connection.setRequestMethod("POST")
    connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
    connection.setRequestProperty("charset", "utf-8")
    connection.setRequestProperty("Content-Length", "" + Integer.toString(urlParams.getBytes().length))
    connection.setUseCaches (false)

    val wr = new DataOutputStream(connection.getOutputStream ())
    wr.writeBytes(urlParams)
    wr.flush()
    wr.close()
    connection.disconnect()

    Ok(views.html.Application.questions())
  }

  def index = Action { implicit request =>
    Ok("it works")
  }

}