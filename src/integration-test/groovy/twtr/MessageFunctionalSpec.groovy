package twtr

import geb.spock.GebSpec
import grails.test.mixin.integration.Integration
import groovyx.net.http.RESTClient
import spock.lang.Stepwise
import spock.lang.Unroll

@Integration
@Unroll
class MessageFunctionalSpec extends GebSpec {

    RESTClient restClient

    def setup() {
        restClient = new RESTClient(baseUrl)
    }

    def "create a message"(){
        given: "an account that has been saved and a message in json form"
        def accountJSON = '{"handle": "decoding", "password": "TestPass1", "email": "uniqueemail@gmail.com", "realName": "coding guy"}'
        def accountResponse = restClient.post(path: "/api/accounts", requestContentType: "application/json", body: accountJSON)
        def messageJson = '{"content": "twitter message content", "account": ' + accountResponse.data.id.toString() + '}'
        assert accountResponse.status == 201


        when: "creating the message via rest endpoint"
        def response = restClient.post(path: "/api/accounts/${accountResponse.data.id.toString()}/messages", requestContentType: "application/json", body: messageJson)

        then: "a 200 should be received and the account should have a message"
        response.status == 201
        response.data.id == 1
    }
}
