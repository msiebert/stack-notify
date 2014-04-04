package stack_notify.controllers

import stack_notify.models.{User, UserModel}
import play.api._
import play.api.data._
import play.api.data.Forms._
import play.api.libs.json.Json
import play.api.mvc._

object UserController extends StackNotifyController {

	private case class NewUser(
		name: String,
		googleId: String
	)

	private case class GoogleIdAccessTokenPairing(
		accessToken: String
	)

	private val newUserForm = Form(
		mapping(
			"name" -> text,
			"googleId" -> text
		)(NewUser.apply)(NewUser.unapply)
	)

	private val accessTokenForm = Form(
		mapping(
			"accessToken" -> text
		)(GoogleIdAccessTokenPairing.apply)(GoogleIdAccessTokenPairing.unapply)
	)

	/**
	 * Create a new user in the system
	 */
	def create() = Action { implicit request =>
		newUserForm.bindFromRequest().fold(
			formWithErrors => {
				failure("Invalid parameters")
			},
			data => {
				val user = new User(data.name, data.googleId)
				UserModel.create(user)
				success("message" -> "Created new user.")
			}
		)
	}

	/**
	 * Get a user from the database
	 * @param googleId the google id of the user to look up
	 */
	def get(googleId: String) = Action { implicit request =>
		val user = UserModel.findByGoogleId(googleId)
		if (user.isDefined) {
			success(
				"user" -> Json.obj(
					"id" -> user.get.id.toString,
					"name" -> user.get.name,
					"googleId" -> user.get.googleId,
					"accessToken" -> user.get.accessToken
				)
			)
		}
		else {
			failure("User not found")
		}
	}

	/**
	 * Update a user's access token
	 * @param googleId the Google id of the user to update
	 */
	def updateAccessToken(googleId: String) = Action { implicit request =>
		accessTokenForm.bindFromRequest().fold(
			formWithErrors => {
				failure("Invalid parameters")
			},
			data => {
				val user = UserModel.findByGoogleId(googleId)

				if (user.isDefined) {
					UserModel.update(user.get.copy(accessToken = Some(data.accessToken)))
					success("message" -> "Updated access token.")
				}
				else {
					failure("User not found.")
				}
			}
		)
	}

}