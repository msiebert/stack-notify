package stack_notify.controllers

import play.api._
import play.api.data._
import play.api.data.Forms._
import play.api.libs.json.Json
import play.api.mvc._
import stack_notify.models.{Tag, TagModel, UserModel}

//https://api.stackexchange.com/2.2/users/2317008/tags?page=1&pagesize=10&fromdate=1365033600&order=desc&sort=popular&site=stackoverflow

object TagController extends StackNotifyController {

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
		
		if (user.isDefined) {
			TagModel.deleteTagsForUser(user.get.id)
			val tags = List("scala", "openid")
			val tagIds = TagModel.tagIds(tags)
			TagModel.createTagsForUser(user.get.id, tagIds)

			success("message" -> "Tags updated.")
		}
		else {
			failure("User not found.")
		}
	}

}
