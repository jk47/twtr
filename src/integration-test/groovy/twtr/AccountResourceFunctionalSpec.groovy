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

    RESTClient restClient

    def setup() {
        restClient = new RESTClient(baseUrl)
    }

    def "create an account"(){
        given: "an account in JSON form"
        def accountJSON = '{"handle": "coding", "password": "TestPass1", "email": "test@gmail.com", "realName": "coding guy"}'

        when: "trying to post the account via rest"
        def resp = restClient.post(path: "/api/accounts", requestContentType: "application/json", body: accountJSON)

        then:
        resp.status == 201
        resp.data.id != null
        //Account.get(resp.data.id).realName == "coding guy"
    }

    @Unroll('#description')
    def "verify an error is returned for invalid JSON account creation request"() {
        given: "an account with invalid JSON"
        //def accountsBefore = Account.count()

        when: "saving by submitting a JSON request"
        def resp = restClient.post(path: "/api/accounts", requestContentType: "application/json", body: json)

        then: "a 422 is received via JSON response and account is not saved to db"
        HttpResponseException e = thrown()
        e.cause == null
        e.message == "Unprocessable Entity"//this is the message for a 422
        e.statusCode == 422
        //accountsBefore == Account.count()

        where:
        description                | json
        'Fail on missing handle'   | '{"password": "TestPass1", "email": "test@gmail.com", "realName": "coding guy"}'
        'Fail on missing email'    | '{"handle": "coding", "password": "TestPass1", "realName": "coding guy"}'
        'Fail on missing password' | '{"handle": "coding", "email": "test@gmail.com", "realName": "coding guy"}'
        'Fail on missing name'     | '{"handle": "coding", "password": "TestPass1", "email": "test@gmail.com"}'
    }

    def "account endpoint returns account based on given ID"(){
        when: "requesting an account by id"
        def response = restClient.get(path: "/api/accounts/1")

        then: "response should include account for corresponding id"
        response.status == 200
        response.data.handle == "coding"
    }

    def "account endpoint returns account based on given handle"(){
        when: "requesting an account by handle"
        def response = restClient.get(path: '/api/accounts/handle=coding')

        then: "response should include account for corresponding handle"
        response.status == 200
        response.data.handle == "coding"
    }

    def "accounts can follow other accounts"(){
        given: "two accounts"
        def account1JSON = '{"handle": "follower", "password": "TestPass1", "email": "follower@gmail.com", "realName": "coding guy"}'
        def resp1 = restClient.post(path: "/api/accounts", requestContentType: "application/json", body: account1JSON)
        def account2JSON = '{"handle": "following", "password": "TestPass1", "email": "following@gmail.com", "realName": "coding guy"}'
        def resp2 = restClient.post(path: "/api/accounts", requestContentType: "application/json", body: account2JSON)

        when: "one account follows another account"
        def response = restClient.get(path: "/api/accounts/${resp1.data.id}/follow/${resp2.data.id}", requestContentType: "application/json")

        then: "account 1 is following account 2 and account 2 is being followed by account 1"
        response.data[0].id == resp2.data.id


    }

}
