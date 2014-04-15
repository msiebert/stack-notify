package stack_notify.controllers

import java.io._
import java.net._
import play.api._
import play.api.data._
import play.api.data.Forms._
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json.Json
import play.api.libs.json._
import play.api.libs.ws.WS
import play.api.mvc._
import stack_notify.models.{Tag, TagModel, UserModel}

///2.2/me/tags?order=desc&sort=popular&site=stackoverflow
object TagController extends StackNotifyController {

	private val key = "uzuwlVXnOAAwH*PM0goEPw(("
	private def updateTagUrl = "https://api.stackexchange.com/2.2/me/tags"
	private def updateTagParams(accessToken: String) = Map(
		"order" -> "desc",
		"sort" ->	"popular",
		"site" -> "stackoverflow",
		"key" -> key,
		"accessToken" -> accessToken
	).toSeq

	/**
	 * Get all the tags that belong to a User
	 * @param googleId the Google id of the user to search for
	 */
	def tagsForUser(googleId: String) = Action { implicit request =>
		val user = UserModel.findByGoogleId(googleId)

		if (user.isDefined) {
			val tags = TagModel.tagsForUser(user.get.id).map { tag =>
				tag.tag
			}.toList
			success(
				"tags" -> tags
			)
		}
		else {
			failure("User not found.")
		}
	}

	/**
	 * Update all of a User's tags
	 * @param googleId the Google id of the user to update
	 */
	def updateTags(googleId: String) = Action { implicit request =>
		val user = UserModel.findByGoogleId(googleId)
		
		if (user.isDefined && user.get.accessToken.isDefined) {
			TagModel.deleteTagsForUser(user.get.id)
			val tags = List("scala", "openid")
			//get the tags from SO
			val result = get(updateTagUrl + "?" + updateTagParams(user.get.accessToken.get).map { case (name, value) =>
				name + "=" + value
			}.mkString("&"))
			println(result)
			Async {
				WS.url(updateTagUrl).withQueryString(
					updateTagParams(user.get.accessToken.get):_*
				).get().map { response =>
					response.status match {
						case 200 => {
							val tags = (response.json \ "items").as[JsArray].value.map { tag =>
								(tag \ "name").as[String]
							}.toList

							val tagIds = TagModel.tagIds(tags)
							TagModel.createTagsForUser(user.get.id, tagIds)

							success("message" -> "Tags updated.")
						}
						case _ => {
							failure("Error accessing SO. Error code: " + response.status)
						}
					}
				}
			}
		}
		else if (user.isDefined && !user.get.accessToken.isDefined) {
			failure("User has no access token.")
		}
		else {
			failure("User not found.")
		}
	}

	/**
	 * Get an HTML response from a remote source
	 * @param url
	 */
	def get(urlToRead: String): String = {
		var result = ""
		val url = new URL(urlToRead)
		val conn = url.openConnection().asInstanceOf[HttpURLConnection]
		conn.setRequestMethod("GET")
		val rd = new BufferedReader(new InputStreamReader(conn.getInputStream()))
		var line = rd.readLine()
		while (line != null) {
			 result += line
			 line = rd.readLine()
		}
		rd.close()
		result
	}

}
