package twtr


import geb.spock.GebSpec
import grails.test.mixin.integration.Integration
import groovyx.net.http.HttpResponseException
import spock.lang.Stepwise
import groovyx.net.http.RESTClient
import spock.lang.Unroll


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
        assert restClient.delete(path: "/api/accounts/${account1Resp.data.id}").status == 204
        def delete2 = restClient.delete(path: "/api/accounts/${account2Resp.data.id}")
        def delete3 = restClient.delete(path: "/api/accounts/${account3Resp.data.id}")

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
        given: "an account with two followers"

        when: "calling the followers endpoint for that account"

        then: "the json representation of those followers will be returned"
    }

}
