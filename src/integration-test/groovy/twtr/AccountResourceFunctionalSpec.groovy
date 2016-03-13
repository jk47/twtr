package twtr


import geb.spock.GebSpec
import grails.test.mixin.integration.Integration
import groovyx.net.http.HttpResponseException
import spock.lang.Stepwise
import groovyx.net.http.RESTClient
import spock.lang.Unroll

import java.text.SimpleDateFormat


@Integration
@Stepwise
class AccountFunctionalSpec extends GebSpec {

    def restClient
    def account1Resp
    def account2Resp
    def account3Resp

    def setup() {
        restClient = new RESTClient(baseUrl)
        def account1JSON = '{"handle": "account1", "password": "TestPass1", "email": "account1@gmail.com", "realName": "account 1 guy"}'
        def account2JSON = '{"handle": "account2", "password": "TestPass1", "email": "account2@gmail.com", "realName": "account 2 guy"}'
        def account3JSON = '{"handle": "account3", "password": "TestPass1", "email": "account3@gmail.com", "realName": "account 3 guy"}'
        account1Resp = restClient.post(path: "/api/accounts", requestContentType: "application/json", body: account1JSON)
        account2Resp = restClient.post(path: "/api/accounts", requestContentType: "application/json", body: account2JSON)
        account3Resp = restClient.post(path: "/api/accounts", requestContentType: "application/json", body: account3JSON)
    }

    def cleanup() {
        restClient.delete(path: "/api/accounts/${account1Resp.data.id}")
        restClient.delete(path: "/api/accounts/${account2Resp.data.id}")
        restClient.delete(path: "/api/accounts/${account3Resp.data.id}")
    }

    def "create an account"(){
        when: "using account created in setup"

        then:
        account1Resp.status == 201
        account1Resp.data.id != null
    }

    @Unroll('#description')
    def "verify an error is returned for invalid JSON account creation request"() {
        when: "saving by submitting an invalid JSON request"
        def resp = restClient.post(path: "/api/accounts", requestContentType: "application/json", body: json)

        then: "a 422 is received via JSON response and account is not saved to db"
        HttpResponseException e = thrown()
        e.cause == null
        e.message == "Unprocessable Entity"//this is the message for a 422
        e.statusCode == 422

        where:
        description                | json
        'Fail on missing handle'   | '{"password": "TestPass1", "email": "test@gmail.com", "realName": "coding guy"}'
        'Fail on missing email'    | '{"handle": "coding", "password": "TestPass1", "realName": "coding guy"}'
        'Fail on missing password' | '{"handle": "coding", "email": "test@gmail.com", "realName": "coding guy"}'
        'Fail on missing name'     | '{"handle": "coding", "password": "TestPass1", "email": "test@gmail.com"}'
    }

    def "account endpoint returns account based on given ID"(){
        when: "requesting an account by id"
        def response = restClient.get(path: "/api/accounts/${account1Resp.data.id}")

        then: "response should include account for corresponding id"
        response.status == 200
        response.data.handle == account1Resp.data.handle
    }

    def "account endpoint returns account based on given handle"(){
        when: "requesting an account by handle"
        def response = restClient.get(path: "/api/accounts/handle=${account1Resp.data.handle}")

        then: "response should include account for corresponding handle"
        response.status == 200
        response.data.handle == account1Resp.data.handle
    }

    def "accounts can follow other accounts"(){
        when: "one account follows another account"
        def response = restClient.get(path: "/api/accounts/${account1Resp.data.id}/follow/${account2Resp.data.id}", requestContentType: "application/json")

        then: "account 1 is following account 2 and account 2 is being followed by account 1"
        response.data[0].id == account2Resp.data.id
    }

    def "account gets return follower and following data fields in JSON response"(){
        when: "getting an account by id"
        def response = restClient.get(path: "/api/accounts/${account1Resp.data.id}")

        then:
        response.data.followerCount
        response.data.followingCount
    }

    def"followers endpoint will return all the followers for an account"() {
        given: "account 2 and 3 follow account 1"
        def response1 = restClient.get(path: "/api/accounts/${account2Resp.data.id}/follow/${account1Resp.data.id}", requestContentType: "application/json")
        def response2 = restClient.get(path: "/api/accounts/${account3Resp.data.id}/follow/${account1Resp.data.id}", requestContentType: "application/json")

        when: "getting the followers for account 1"
        def followerResponse = restClient.get(path: "/api/accounts/${account1Resp.data.id}/followers")

        then: "the json representation of account 2 and 3 followers will be returned"
        followerResponse.data[0].id == account2Resp.data.id
        followerResponse.data[1].id == account3Resp.data.id
    }

    def "feed endpoint will return messages from the users that the account follows"(){
        given: "account 1 follows account 2 and account 3, both of whom have 2 messages"
        def response1 = restClient.get(path: "/api/accounts/${account1Resp.data.id}/follow/${account2Resp.data.id}", requestContentType: "application/json")
        def response2 = restClient.get(path: "/api/accounts/${account1Resp.data.id}/follow/${account3Resp.data.id}", requestContentType: "application/json")
        def message1Json = '{"content": "testMessage1", "account": ' + account2Resp.data.id + '}'
        def message2Json = '{"content": "testMessage2", "account": ' + account2Resp.data.id + '}'
        def message3Json = '{"content": "testMessage3", "account": ' + account3Resp.data.id + '}'
        def message4Json = '{"content": "testMessage4", "account": ' + account3Resp.data.id + '}'
        def createMessageResponse1 = restClient.post(path: "/api/accounts/${account2Resp.data.id}/messages", requestContentType: "application/json", body: message1Json)
        def createMessageResponse2 = restClient.post(path: "/api/accounts/${account2Resp.data.id}/messages", requestContentType: "application/json", body: message2Json)
        def createMessageResponse3 = restClient.post(path: "/api/accounts/${account3Resp.data.id}/messages", requestContentType: "application/json", body: message3Json)
        def createMessageResponse4 = restClient.post(path: "/api/accounts/${account3Resp.data.id}/messages", requestContentType: "application/json", body: message4Json)

        when: "calling the feed endpoint on account 1"
        def feedResponse = restClient.get(path: "/api/accounts/${account1Resp.data.id}/feed")

        then: "the response will include all messages from the 2 users"
        feedResponse.data.size() == 4
    }

    def "the feed endpoint honors the date parameter"(){
        given: "account 1 follows account 2 and account 3, both of whom have 2 messages"
        def response1 = restClient.get(path: "/api/accounts/${account1Resp.data.id}/follow/${account2Resp.data.id}", requestContentType: "application/json")
        def response2 = restClient.get(path: "/api/accounts/${account1Resp.data.id}/follow/${account3Resp.data.id}", requestContentType: "application/json")
        def message1Json = '{"content": "testMessage1", "account": ' + account2Resp.data.id + '}'
        def message2Json = '{"content": "testMessage2", "account": ' + account2Resp.data.id + '}'
        def message3Json = '{"content": "testMessage3", "account": ' + account3Resp.data.id + '}'
        def message4Json = '{"content": "testMessage4", "account": ' + account3Resp.data.id + '}'
        def createMessageResponse1 = restClient.post(path: "/api/accounts/${account2Resp.data.id}/messages", requestContentType: "application/json", body: message1Json)
        def createMessageResponse2 = restClient.post(path: "/api/accounts/${account2Resp.data.id}/messages", requestContentType: "application/json", body: message2Json)
        def createMessageResponse3 = restClient.post(path: "/api/accounts/${account3Resp.data.id}/messages", requestContentType: "application/json", body: message3Json)
        def createMessageResponse4 = restClient.post(path: "/api/accounts/${account3Resp.data.id}/messages", requestContentType: "application/json", body: message4Json)

        when: "calling the feed endpoint on account 1 with a date param in the future"
        def dateNow = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy").parse("Tue Aug 02 21:53:43 EST 2016")//current date and time
        def feedResponse = restClient.get(path: "/api/accounts/${account1Resp.data.id}/feed", query: [fromDate: dateNow])

        then: "the response will include no messages because they wont satisfy the date param"
        feedResponse.data.size() == 0
    }

    def "the feed endpoint honors the limit param"(){
        given: "account 1 follows account 2 and account 3, both of whom have 2 messages"
        def response1 = restClient.get(path: "/api/accounts/${account1Resp.data.id}/follow/${account2Resp.data.id}", requestContentType: "application/json")
        def response2 = restClient.get(path: "/api/accounts/${account1Resp.data.id}/follow/${account3Resp.data.id}", requestContentType: "application/json")
        def message1Json = '{"content": "testMessage1", "account": ' + account2Resp.data.id + '}'
        def message2Json = '{"content": "testMessage2", "account": ' + account2Resp.data.id + '}'
        def message3Json = '{"content": "testMessage3", "account": ' + account3Resp.data.id + '}'
        def message4Json = '{"content": "testMessage4", "account": ' + account3Resp.data.id + '}'
        def createMessageResponse1 = restClient.post(path: "/api/accounts/${account2Resp.data.id}/messages", requestContentType: "application/json", body: message1Json)
        def createMessageResponse2 = restClient.post(path: "/api/accounts/${account2Resp.data.id}/messages", requestContentType: "application/json", body: message2Json)
        def createMessageResponse3 = restClient.post(path: "/api/accounts/${account3Resp.data.id}/messages", requestContentType: "application/json", body: message3Json)
        def createMessageResponse4 = restClient.post(path: "/api/accounts/${account3Resp.data.id}/messages", requestContentType: "application/json", body: message4Json)

        when: "calling the feed endpoint on account 1 with a limit parameter of 3"
        def feedResponse = restClient.get(path: "/api/accounts/${account1Resp.data.id}/feed", query: [max: 3])

        then: "the response should include the most recent messages by followed accounts and the number of returned messages should be capped at 3(specified as part of the query)"
        feedResponse.data.size() == 3
    }


}
