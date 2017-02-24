package twtr

import geb.spock.GebSpec
import grails.test.mixin.integration.Integration
import groovyx.net.http.HttpResponseException
import groovyx.net.http.RESTClient
import spock.lang.*
import grails.converters.JSON
import java.text.SimpleDateFormat

@Integration
@Stepwise
@Unroll
class AccountFunctionalSpec extends GebSpec {

    @Shared
    def token
    def restClient

    def accounts = []

    def setup() {
        restClient = new RESTClient(baseUrl)
    }

    def createAccounts(){
        (1..5).each { i ->
            def json = "{\"handle\": \"account${i}\", \"password\": \"TestPass${i}\", \"email\": \"account${i}@gmail.com\", \"realName\": \"account ${i} guy\"}"
            accounts.add(restClient.post(path: "/api/accounts", requestContentType: "application/json", body: json, headers: ['X-Auth-Token': token]))
        }
    }

    def deleteAccounts(){
        (1..5).each { i ->
            def id = accounts[i - 1].data.id
            restClient.delete(path: "/api/accounts/${id}", headers: ['X-Auth-Token': token])
        }
    }


    def 'calling accounts endpoint without token is forbidden'() {
        when:
        restClient.get(path: '/api/accounts')

        then:
        HttpResponseException problem = thrown(HttpResponseException)
        problem.statusCode == 403
        problem.message.contains('Forbidden')
    }

    def 'valid username and password generates a token'(){
        setup:
        def auth = ([username: 'admin', password: 'Password1'] as JSON) as String

        when:
        def response = restClient.post(path: '/api/login', body: auth, requestContentType: 'application/json')

        then:
        response.status == 200
        response.data.username == 'admin'
        response.data.roles == ['ROLE_READ']
        //noinspection GroovyDoubleNegation
        !!(token = response.data.access_token)
    }

    def "create an account"() {
        when: "using account created in setup"
        createAccounts()

        then:
        accounts[0].status == 201
        accounts[0].data.id != null

        when: 'retreiving all accounts returns the ones created in setup'
        def response = restClient.get(path: '/api/accounts', headers: ['X-Auth-Token': token])

        then:
        response.status == 200
        response.data.size() == accounts.size() + 3// for the bootstrap accounts
        deleteAccounts()
    }

    def 'verify an error is returned for invalid JSON account creation request: #description'() {
        when: 'saving by submitting an invalid JSON request'
        restClient.post(path: "/api/accounts", requestContentType: "application/json", body: json, headers: ['X-Auth-Token': token])

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
        given: "created accounts"
        createAccounts()

        when: "requesting an account by id"
        def response = restClient.get(path: "/api/accounts/${accounts[0].data.id}", headers: ['X-Auth-Token': token])

        then: "response should include account for corresponding id"
        response.status == 200
        response.data.findAll { k, v -> !k.contains('follow') } == accounts[0].data
        deleteAccounts()
    }

    def "account endpoint returns account based on given handle"() {
        given: "created accounts"
        createAccounts()

        when: "requesting an account by handle"
        def response = restClient.get(path: "/api/accounts/handle=${accounts[0].data.handle}", headers: ['X-Auth-Token': token])

        then: "response should include account for corresponding handle"
        response.status == 200
        response.data.findAll { k, v -> !k.contains('follow') } == accounts[0].data
        deleteAccounts()
    }


    def "accounts can follow other accounts"() {
        given: "created accounts"
        createAccounts()

        when: "one account follows another account"
        def response = restClient.get(path: "/api/accounts/${accounts[0].data.id}/follow/${accounts[1].data.id}", requestContentType: "application/json", headers: ['X-Auth-Token': token])

        then: "account 1 is following account 2 and account 2 is being followed by account 1"
        response.data[0].id == accounts[1].data.id
        deleteAccounts()
    }

    def "account gets return follower and following data fields in JSON response"() {
        given: "created accounts"
        createAccounts()

        when: "getting an account by id"
        def response = restClient.get(path: "/api/accounts/${accounts[0].data.id}", headers: ['X-Auth-Token': token])

        then:
        response.data.followerCount
        response.data.followingCount
        deleteAccounts()
    }

    def "followers endpoint will return all the followers for an account"() {
        given: "account 2,3,4 and 5 follow account 1"
        createAccounts()
        (1..4).each {
            restClient.get(path: "/api/accounts/${accounts[it].data.id}/follow/${accounts[0].data.id}", requestContentType: "application/json", headers: ['X-Auth-Token': token])
        }

        when: "getting the followers for account 1"
        def followerResponse = restClient.get(path: "/api/accounts/${accounts[0].data.id}/followers", headers: ['X-Auth-Token': token])

        then: "the json representation of account 2,3,4 and 5 followers will be returned"
        followerResponse.data.size() == 4
        (1..4).each { i ->
            assert followerResponse.data.find { follower -> follower.id == accounts[i].data.id }
        }
        deleteAccounts()
    }

    def "followers endpoint will return all the followers for an account using specified limit and offset"() {
        given: "account 2,3,4 and 5 follow account 1"
        createAccounts()
        def responses = []

        when:
        (1..4).each { i ->
            responses << restClient.get(path: "/api/accounts/${accounts[i].data.id}/follow/${accounts[0].data.id}", requestContentType: "application/json", headers: ['X-Auth-Token': token])
        }

        then:
        responses.each {
            assert it.status == 200
        }

        when: "getting the followers for account 1 with specified max and offset as part of the query"
        def followerResponse = restClient.get(path: "/api/accounts/${accounts[0].data.id}/followers", query: [max: 2, offset: 1], headers: ['X-Auth-Token': token])

        then: "the json representation of account 4 and 5 followers will be returned due to query parameter max = 2 and offset = 1"
        followerResponse.data.size() == 2
        followerResponse.data[0].id == accounts[3].data.id
        followerResponse.data[1].id == accounts[4].data.id
        deleteAccounts()
    }

    def "feed endpoint will return messages from the users that the account follows"() {
        given: "account 1 follows account 2 and account 3, both of whom have 2 messages"
        createAccounts()
        def response1 = restClient.get(path: "/api/accounts/${accounts[0].data.id}/follow/${accounts[1].data.id}", requestContentType: "application/json", headers: ['X-Auth-Token': token])
        def response2 = restClient.get(path: "/api/accounts/${accounts[0].data.id}/follow/${accounts[2].data.id}", requestContentType: "application/json", headers: ['X-Auth-Token': token])
        def message1Json = '{"content": "testMessage1", "account": ' + accounts[1].data.id + '}'
        def message2Json = '{"content": "testMessage2", "account": ' + accounts[1].data.id + '}'
        def message3Json = '{"content": "testMessage3", "account": ' + accounts[2].data.id + '}'
        def message4Json = '{"content": "testMessage4", "account": ' + accounts[2].data.id + '}'
        def createMessageResponse1 = restClient.post(path: "/api/accounts/${accounts[1].data.id}/messages", requestContentType: "application/json", body: message1Json, headers: ['X-Auth-Token': token])
        def createMessageResponse2 = restClient.post(path: "/api/accounts/${accounts[1].data.id}/messages", requestContentType: "application/json", body: message2Json, headers: ['X-Auth-Token': token])
        def createMessageResponse3 = restClient.post(path: "/api/accounts/${accounts[2].data.id}/messages", requestContentType: "application/json", body: message3Json, headers: ['X-Auth-Token': token])
        def createMessageResponse4 = restClient.post(path: "/api/accounts/${accounts[2].data.id}/messages", requestContentType: "application/json", body: message4Json, headers: ['X-Auth-Token': token])

        when: "calling the feed endpoint on account 1"
        def feedResponse = restClient.get(path: "/api/accounts/${accounts[0].data.id}/feed", headers: ['X-Auth-Token': token])

        then: "the response will include all messages from the 2 users ordered chronologically starting with the most recent messages"
        feedResponse.data.size() == 4
        feedResponse.data[0].id == createMessageResponse4.data.id
        feedResponse.data[1].id == createMessageResponse3.data.id
        feedResponse.data[2].id == createMessageResponse2.data.id
        feedResponse.data[3].id == createMessageResponse1.data.id
        deleteAccounts()
    }


    def "the feed endpoint honors the limit param"() {
        given: "account 1 follows account 2 and account 3, both of whom have 2 messages"
        createAccounts()
        restClient.get(path: "/api/accounts/${accounts[0].data.id}/follow/${accounts[1].data.id}", requestContentType: "application/json", headers: ['X-Auth-Token': token])
        restClient.get(path: "/api/accounts/${accounts[0].data.id}/follow/${accounts[2].data.id}", requestContentType: "application/json", headers: ['X-Auth-Token': token])
        def message1Json = '{"content": "testMessage1", "account": ' + accounts[1].data.id + '}'
        def message2Json = '{"content": "testMessage2", "account": ' + accounts[1].data.id + '}'
        def message3Json = '{"content": "testMessage3", "account": ' + accounts[2].data.id + '}'
        def message4Json = '{"content": "testMessage4", "account": ' + accounts[2].data.id + '}'
        def message5Json = '{"content": "testMessage5", "account": ' + accounts[3].data.id + '}'
        def responses = []

        when:
        responses << restClient.post(path: "/api/accounts/${accounts[1].data.id}/messages", requestContentType: "application/json", body: message1Json, headers: ['X-Auth-Token': token])
        responses << restClient.post(path: "/api/accounts/${accounts[1].data.id}/messages", requestContentType: "application/json", body: message2Json, headers: ['X-Auth-Token': token])
        responses << restClient.post(path: "/api/accounts/${accounts[2].data.id}/messages", requestContentType: "application/json", body: message3Json, headers: ['X-Auth-Token': token])
        responses << restClient.post(path: "/api/accounts/${accounts[2].data.id}/messages", requestContentType: "application/json", body: message4Json, headers: ['X-Auth-Token': token])
        responses << restClient.post(path: "/api/accounts/${accounts[3].data.id}/messages", requestContentType: "application/json", body: message5Json, headers: ['X-Auth-Token': token])

        then:
        responses.each {
            assert it.status == 201
        }

        when: "calling the feed endpoint on account 1 with a limit parameter of 3"
        def feedResponse = restClient.get(path: "/api/accounts/${accounts[0].data.id}/feed", query: [max: 3], headers: ['X-Auth-Token': token])

        then: "the response should include the most recent messages by followed accounts and the number of returned messages should be capped at 3(specified as part of the query)"
        feedResponse.data.size() == 3
        feedResponse.data*.content == ['testMessage4', 'testMessage3', 'testMessage2']
        deleteAccounts()
    }


}
