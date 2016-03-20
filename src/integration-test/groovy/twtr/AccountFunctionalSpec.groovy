package twtr

import geb.spock.GebSpec
import grails.test.mixin.integration.Integration
import groovyx.net.http.HttpResponseException
import groovyx.net.http.RESTClient
import spock.lang.Stepwise
import spock.lang.Unroll

import java.text.SimpleDateFormat

@Integration
@Stepwise
@Unroll
class AccountFunctionalSpec extends GebSpec {

  def restClient

  def accounts = []

  def setup() {
    restClient = new RESTClient(baseUrl)
    (1..5).each { i ->
      def json = "{\"handle\": \"account${i}\", \"password\": \"TestPass${i}\", \"email\": \"account${i}@gmail.com\", \"realName\": \"account ${i} guy\"}"
      accounts.add(restClient.post(path: "/api/accounts", requestContentType: "application/json", body: json))
    }
  }

  def cleanup() {
    (1..5).each { i ->
      def id = accounts[i - 1].data.id
      restClient.delete(path: "/api/accounts/${id}")
    }
  }

  def "create an account"() {
    when: "using account created in setup"

    then:
    accounts[0].status == 201
    accounts[0].data.id != null

    when: 'retreiving all accounts returns the ones created in setup'
    def response = restClient.get(path: '/api/accounts')

    then:
    response.status == 200
    response.data.size() == accounts.size()
  }

  def 'verify an error is returned for invalid JSON account creation request: #description'() {
    when: 'saving by submitting an invalid JSON request'
    restClient.post(path: "/api/accounts", requestContentType: "application/json", body: json)

    then: 'a 422 is received via JSON response and account is not saved to db'
    HttpResponseException e = thrown()
    !e.cause
    e.message == "Unprocessable Entity" //this is the message for a 422
    e.statusCode == 422

    where:
    description                | json
    'Fail on missing handle'   | '{"password": "TestPass1", "email": "test@gmail.com", "realName": "coding guy"}'
    'Fail on missing email'    | '{"handle": "coding", "password": "TestPass1", "realName": "coding guy"}'
    'Fail on missing password' | '{"handle": "coding", "email": "test@gmail.com", "realName": "coding guy"}'
    'Fail on missing name'     | '{"handle": "coding", "password": "TestPass1", "email": "test@gmail.com"}'
  }

  def "account endpoint returns account by id"() {
    when: "requesting an account by id"
    def response = restClient.get(path: "/api/accounts/${accounts[0].data.id}")

    then: "response should include account for corresponding id"
    response.status == 200
    response.data.findAll { k,v -> !k.contains('follow') } == accounts[0].data
  }

  def "account endpoint returns account based on given handle"(){
    when: "requesting an account by handle"
    def response = restClient.get(path: "/api/accounts/handle=${accounts[0].data.handle}")

    then: "response should include account for corresponding handle"
    response.status == 200
    response.data.findAll { k,v -> !k.contains('follow') } == accounts[0].data
  }


  def "accounts can follow other accounts"() {
    when: "one account follows another account"
    def response = restClient.get(path: "/api/accounts/${accounts[0].data.id}/follow/${accounts[1].data.id}", requestContentType: "application/json")

    then: "account 1 is following account 2 and account 2 is being followed by account 1"
    response.data[0].id == accounts[1].data.id
  }

  def "account gets return follower and following data fields in JSON response"() {
    when: "getting an account by id"
    def response = restClient.get(path: "/api/accounts/${accounts[0].data.id}")

    then:
    response.data.followerCount
    response.data.followingCount
  }

  def "followers endpoint will return all the followers for an account"() {
    given: "account 2,3,4 and 5 follow account 1"
    (1..4).each {
      restClient.get(path: "/api/accounts/${accounts[it].data.id}/follow/${accounts[0].data.id}", requestContentType: "application/json")
    }

    when: "getting the followers for account 1"
    def followerResponse = restClient.get(path: "/api/accounts/${accounts[0].data.id}/followers")

    then: "the json representation of account 2,3,4 and 5 followers will be returned"
    followerResponse.data.size() == 4
    (1..4).each { i ->
      assert followerResponse.data.find { follower -> follower.id == accounts[i].data.id }
    }
  }

  def "followers endpoint will return all the followers for an account using specified limit and offset"() {
    given: "account 2,3,4 and 5 follow account 1"
    def responses = []

    when:
    (1..4).each { i->
      responses << restClient.get(path: "/api/accounts/${accounts[i].data.id}/follow/${accounts[0].data.id}", requestContentType: "application/json")
    }

    then:
    responses.each {
      assert it.status == 200
    }

    when: "getting the followers for account 1 with specified max and offset as part of the query"
    def followerResponse = restClient.get(path: "/api/accounts/${accounts[0].data.id}/followers", query: [max: 2, offset: 1])

    then: "the json representation of account 4 and 5 followers will be returned due to query parameter max = 2 and offset = 1"
    followerResponse.data.size() == 2
    followerResponse.data[0].id == accounts[3].data.id
    followerResponse.data[1].id == accounts[4].data.id
  }

  def "feed endpoint will return messages from the users that the account follows"() {
    given: "account 1 follows account 2 and account 3, both of whom have 2 messages"
    def response1 = restClient.get(path: "/api/accounts/${accounts[0].data.id}/follow/${accounts[1].data.id}", requestContentType: "application/json")
    def response2 = restClient.get(path: "/api/accounts/${accounts[0].data.id}/follow/${accounts[2].data.id}", requestContentType: "application/json")
    def message1Json = '{"content": "testMessage1", "account": ' + accounts[1].data.id + '}'
    def message2Json = '{"content": "testMessage2", "account": ' + accounts[1].data.id + '}'
    def message3Json = '{"content": "testMessage3", "account": ' + accounts[2].data.id + '}'
    def message4Json = '{"content": "testMessage4", "account": ' + accounts[2].data.id + '}'
    def createMessageResponse1 = restClient.post(path: "/api/accounts/${accounts[1].data.id}/messages", requestContentType: "application/json", body: message1Json)
    def createMessageResponse2 = restClient.post(path: "/api/accounts/${accounts[1].data.id}/messages", requestContentType: "application/json", body: message2Json)
    def createMessageResponse3 = restClient.post(path: "/api/accounts/${accounts[2].data.id}/messages", requestContentType: "application/json", body: message3Json)
    def createMessageResponse4 = restClient.post(path: "/api/accounts/${accounts[2].data.id}/messages", requestContentType: "application/json", body: message4Json)

    when: "calling the feed endpoint on account 1"
    def feedResponse = restClient.get(path: "/api/accounts/${accounts[0].data.id}/feed")

    then: "the response will include all messages from the 2 users ordered chronologically starting with the most recent messages"
    feedResponse.data.size() == 4
    feedResponse.data[0].id == createMessageResponse4.data.id
    feedResponse.data[1].id == createMessageResponse3.data.id
    feedResponse.data[2].id == createMessageResponse2.data.id
    feedResponse.data[3].id == createMessageResponse1.data.id
  }

  def "the feed endpoint honors the date parameter"() {
    given: "account 1 follows account 2 and account 3, both of whom have 2 messages"
    restClient.get(path: "/api/accounts/${accounts[0].data.id}/follow/${accounts[1].data.id}", requestContentType: "application/json")
    restClient.get(path: "/api/accounts/${accounts[0].data.id}/follow/${accounts[2].data.id}", requestContentType: "application/json")
    def message1Json = '{"content": "testMessage1", "account": ' + accounts[1].data.id + '}'
    def message2Json = '{"content": "testMessage2", "account": ' + accounts[1].data.id + '}'
    def message3Json = '{"content": "testMessage3", "account": ' + accounts[2].data.id + '}'
    def message4Json = '{"content": "testMessage4", "account": ' + accounts[2].data.id + '}'
    restClient.post(path: "/api/accounts/${accounts[1].data.id}/messages", requestContentType: "application/json", body: message1Json)
    restClient.post(path: "/api/accounts/${accounts[1].data.id}/messages", requestContentType: "application/json", body: message2Json)
    restClient.post(path: "/api/accounts/${accounts[2].data.id}/messages", requestContentType: "application/json", body: message3Json)
    restClient.post(path: "/api/accounts/${accounts[2].data.id}/messages", requestContentType: "application/json", body: message4Json)

    when: "calling the feed endpoint on account 1 with a date param in the future"
    def dateNow = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy").parse("Tue Aug 02 21:53:43 EST 2016")//current date and time
    def feedResponse = restClient.get(path: "/api/accounts/${accounts[0].data.id}/feed", query: [fromDate: dateNow])

    then: "the response will include no messages because they wont satisfy the date param"
    feedResponse.data.size() == 0
  }

  def "the feed endpoint honors the limit param"() {
    given: "account 1 follows account 2 and account 3, both of whom have 2 messages"
    restClient.get(path: "/api/accounts/${accounts[0].data.id}/follow/${accounts[1].data.id}", requestContentType: "application/json")
    restClient.get(path: "/api/accounts/${accounts[0].data.id}/follow/${accounts[2].data.id}", requestContentType: "application/json")
    def message1Json = '{"content": "testMessage1", "account": ' + accounts[1].data.id + '}'
    def message2Json = '{"content": "testMessage2", "account": ' + accounts[1].data.id + '}'
    def message3Json = '{"content": "testMessage3", "account": ' + accounts[2].data.id + '}'
    def message4Json = '{"content": "testMessage4", "account": ' + accounts[2].data.id + '}'
    def message5Json = '{"content": "testMessage5", "account": ' + accounts[3].data.id + '}'
    def responses = []

    when:
    responses << restClient.post(path: "/api/accounts/${accounts[1].data.id}/messages", requestContentType: "application/json", body: message1Json)
    responses << restClient.post(path: "/api/accounts/${accounts[1].data.id}/messages", requestContentType: "application/json", body: message2Json)
    responses << restClient.post(path: "/api/accounts/${accounts[2].data.id}/messages", requestContentType: "application/json", body: message3Json)
    responses << restClient.post(path: "/api/accounts/${accounts[2].data.id}/messages", requestContentType: "application/json", body: message4Json)
    responses << restClient.post(path: "/api/accounts/${accounts[3].data.id}/messages", requestContentType: "application/json", body: message5Json)

    then:
    responses.each {
      assert it.status == 201
    }

    when: "calling the feed endpoint on account 1 with a limit parameter of 3"
    def feedResponse = restClient.get(path: "/api/accounts/${accounts[0].data.id}/feed", query: [max: 3])

    then: "the response should include the most recent messages by followed accounts and the number of returned messages should be capped at 3(specified as part of the query)"
    feedResponse.data.size() == 3
    feedResponse.data*.content == ['testMessage4', 'testMessage3', 'testMessage2']
  }


}
