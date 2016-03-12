package twtr

import geb.spock.GebSpec
import grails.test.mixin.integration.Integration
import groovyx.net.http.HttpResponseException
import groovyx.net.http.RESTClient
import spock.lang.Stepwise
import spock.lang.Unroll

@Integration
@Stepwise
@Unroll
class MessageFunctionalSpec extends GebSpec {

    RESTClient restClient

    def setup() {
        restClient = new RESTClient(baseUrl)
    }

    def "create a message with an id"() {
        given: "an account that has been saved and a message in json form"
        def accountJSON = '{"handle": "decoding", "password": "TestPass1", "email": "uniqueemail@gmail.com", "realName": "coding guy"}'
        def accountResponse = restClient.post(path: "/api/accounts", requestContentType: "application/json", body: accountJSON)
        def messageJson = '{"content": "twitter message content", "account": ' + accountResponse.data.id.toString() + '}'
        assert accountResponse.status == 201


        when: "creating the message via rest endpoint"
        def response = restClient.post(path: "/api/accounts/${accountResponse.data.id.toString()}/messages", requestContentType: "application/json", body: messageJson)

        then: "a 201 should be received and the account should have a message"
        response.status == 201
        response.data.content != null
    }

    @Unroll('#description')
    def "Return an error response from the the create message end point"() {
        given: "a message"
        def messageJson = '{"content": ' + messageContent + ', "account": ' + accountId + '}'

        when: "creating the message via reset endpoint"
        def createMessageResponse = restClient.post(path: "/api/accounts/1/messages", requestContentType: "application/json", body: messageJson)

        then: "a 422 should be received"
        HttpResponseException e = thrown()
        e.statusCode == 422

        where:
        description                                                      | accountId | messageContent
        "Error when message has 41 chars"                                | '1'       | 'f' * 41
        "Error when message has blank content"                           | '1'       | ''
        "Error when invalid account is specified but with valid message" | '0'       | 'f' * 10
        "Error when invalid account and invalid message are specified"   | '0'       | ''
    }
}
