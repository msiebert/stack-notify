import binascii
import calendar
import datetime
import json
import MySQLdb
import requests
from uuid import UUID

GOOGLE_CLIENT_ID = '970444553690-o1rjhr7vqcj8ns8o9pkhdc8bvok6gooh.apps.googleusercontent.com'
GOOGLE_SECRET = 'NW-ojO2ewOvndWxamSEhLcfw'
GOOGLE_REFRESH_TOKEN = '1/wFxq47iHvo7eHjtFZMLdGnXlaJjtzM7KVGdXzhlhYrQ'
GOOGLE_API_KEY = 'AIzaSyBwIvZ6UA-0Q5pLKmM6MWi4O2_txjvBhvw'

class Tag:
	'''
	A class that holds information about a tag

		field: string id the id of the tag
		field: string tag the value of the tag
	'''
	def __init__(self, tag_id, tag):
		self.id = tag_id
		self.tag = tag

class User:
	'''
	A class that holds information about an User

		field: string name the name of the user
		field: string google_id the user's google_id
		field: string channel_id the user's id in the Chrome extension
		field: string access_token the user's Stack Overflow access token
	'''
	def __init__(self, name, google_id, channel_id, access_token):
		self.name = name
		self.google_id = google_id
		self.channel_id = channel_id
		self.access_token = access_token

class Question:
	'''
	A class that holds information about a Question

		field: string title the title of the question
		field: string link the link to the question on SO
	'''
	def __init__(self, title, link):
		self.title = title
		self.link = link

class Poller:
	'''
	A class that polls Stack Overflow's API to check for new questions and notify users
	'''

	def __init__(self):
		self.db = MySQLdb.connect(host='localhost', user='dev', passwd='dev', db='stack_notify')
		self.cursor = self.db.cursor()

	def poll(self):
		'''
		Poll Stack Overflow's API to check for new questions concerning topics the users have subscribed to
		'''
		tags = self.get_tags()
		for tag in tags:
		 	self.process_tag(tag)
	#	self.process_tag(tags[1])

	def get_tags(self):
		'''
		Get all the tags in the database

			return: Tag[] the tags in the database
		'''
		self.cursor.execute('''
				SELECT `id`, `tag`
				FROM `tags`
			''',
			()
		)

		rows = self.cursor.fetchall()
		tags = []
		for row in rows:
			tags.append(Tag(binascii.b2a_hex(row[0]), row[1]))

		return tags

	def process_tag(self, tag):
		'''
		Check if there are new questions about a tag in stack overflow and then notify users
			subscribed to the tag

			param: Tag tag the tag to process
		'''
		print tag.tag
		#get the new questions for the tag
		#/2.2/questions?order=desc&min=1396828800&sort=activity&tagged=scala&site=stackoverflow
		time = datetime.datetime.now() - datetime.timedelta(minutes=10)
		time = calendar.timegm(time.timetuple())
		params = {
			'order': 'desc',
			'sort': 'activity',
			'site': 'stackoverflow',
			'key': 'uzuwlVXnOAAwH*PM0goEPw((',
			'pagesize': 10,
			'tagged': tag.tag,
			'fromdate': time
		}
		r = requests.get('https://api.stackexchange.com/2.2/questions', params=params)
		
		questions = []
		for item in r.json()['items']:
			questions.append(Question(item['title'], item['link']))

		#get access token for Google Cloud Messaging
		access_token = self.get_access_token()

		#send the questions to the chrome app
		users = self.get_users_for_tag(tag.id)
		for user in users:
			for question in questions:
				params = {
					'channelId': user.channel_id,
					'subchannelId': '0',
					'payload': question.title + "," + question.link# {
						#'title': question.title, 
						#'link': question.link
				#	}
				}

				headers = {'Authorization' : 'Bearer ' + access_token,
						'Content-Type': 'application/json'}
				print user.name
				#print headers
				print json.dumps(params)
				print "\n"
				
				something = requests.post('https://www.googleapis.com/gcm_for_chrome/v1/messages', data=json.dumps(params), headers=headers)
				print something.text

	def get_access_token(self):
		time_format = '%d %b %Y %H:%M:%S'
		f = open('access-token.txt', 'r+')
		lines = f.read().split('\n')
		access_token = lines[0]
		time = datetime.datetime.strptime(lines[1], time_format)
		expires = int(lines[2])

		#if we've run out of time, get a new acccess token
		if datetime.datetime.now() - time > datetime.timedelta(seconds=expires):
			params = {
				'client_id': GOOGLE_CLIENT_ID,
				'client_secret': GOOGLE_SECRET,
				'refresh_token': GOOGLE_REFRESH_TOKEN,
				'grant_type': 'refresh_token'
			}
			print params

			response = requests.post('https://accounts.google.com/o/oauth2/token', data=params).json()
			print response
			access_token = response['access_token']
			expires = response['expires_in']

			f.seek(0)
			f.truncate()
			f.write(access_token + '\n')
			f.write(datetime.datetime.now().strftime(time_format) + '\n')
			f.write(str(expires))

		f.close()
		return access_token

	def get_users_for_tag(self, tag_id):
		'''
		Get all the users subscribed to a tag

			param: tag_id the id of the tag to search for
			return: User[] the users subscribed to the tag
		'''
		tag_id = UUID(tag_id)

		self.cursor.execute('''
				SELECT `user_id`
				FROM `users_tags`
				WHERE `tag_id`=%s
			''',
			(tag_id.bytes)
		)

		rows = self.cursor.fetchall()
		user_ids = []
		for row in rows:
			user_ids.append(binascii.b2a_hex(row[0]))

		#now get data on each of those users
		users = []
		for user_id in user_ids:
			user_id = UUID(user_id)

			self.cursor.execute('''
					SELECT `name`, `google_id`, `channel_id`, `access_token`
					FROM `users`
					WHERE `id`=%s
				''',
				(user_id.bytes)
			)

			row = self.cursor.fetchone()
			if row is not None:
				users.append(User(row[0], row[1], row[2], row[3]))

		return users


# Actually run the poller
poller = Poller()
poller.poll()
