package stack_notify.models

import anorm._
import anorm.SqlParser._
import AnormImplicits._
import java.util.UUID
import play.api.db.DB
import play.api.Play.current

case class User (
	id: UUID,
	name: String,
	googleId: String,
	channelId: Option[String] = None,
	accessToken: Option[String] = None
) {

	/**
	 * Create a new User record with a randomly generated id
	 * @param name of the new User
	 * @param googleId the Google id of the new User
	 */
	def this(name: String, googleId: String) = this(UUID.randomUUID(),
		name, googleId)
}

object UserModel {

	protected val userParser = {
		get[UUID]("id") ~
		get[String]("name") ~
		get[String]("google_id") ~
		get[Option[String]]("channel_id") ~
		get[Option[String]]("access_token") map {
			case id ~ name ~ google_id ~ channel_id ~ access_token =>
				new User(id, name, google_id, channel_id, access_token)
		}
	}
	
	/**
	 * Create a new User in the database
	 * @param user the new User
	 */
	def create(user: User): Unit = {
		DB.withConnection("default") { connection =>
			SQL("""
				INSERT INTO `users` (`id`, `name`, `google_id`)
				VALUES ({id}, {name}, {google_id})
			""").on(
				"id" -> user.id,
				"name" -> user.name,
				"google_id" -> user.googleId
			).executeUpdate()(connection)
		}
	}

	/**
	 * Get a User from the database
	 * @param google_id the googleId of the User to get
	 * @return the User if found
	 */
	def findByGoogleId(google_id: String): Option[User] = {
		DB.withConnection("default") { connection =>
			SQL("""
				SELECT *
				FROM `users`
				WHERE `google_id` = {google_id}
				LIMIT 1
			""").on(
				"google_id" -> google_id
			).as(userParser.singleOpt)(connection)
		}
	}

	/**
	 * Update a user in the database
	 * @param user the user's information to update
	 */
	def update(user: User): Unit = {
		DB.withConnection("default") { connection =>
			SQL("""
				UPDATE `users`
				SET `name`={name}, `google_id`={google_id}, `channel_id`={channel_id}, `access_token`={access_token}
				WHERE `id`={id}
			""").on(
				"name" -> user.name,
				"google_id" -> user.googleId,
				"channel_id" -> user.channelId,
				"access_token" -> user.accessToken,
				"id" -> user.id
			).executeUpdate()(connection)
		}
	}

}