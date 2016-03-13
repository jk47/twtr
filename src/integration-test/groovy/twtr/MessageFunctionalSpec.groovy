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

    def "Create a message with an id"() {
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
    def "Return an error response from the create Message endpoint if user is not found or message text is not valid"() {
        given: "a message"
        def messageJson = '{"content": ' + messageContent + ', "account": ' + accountId + '}'

        when: "creating the message via rest endpoint"
        def createMessageResponse = restClient.post(path: "/api/accounts/1/messages", requestContentType: "application/json", body: messageJson)

        then: "a 422 should be received"
        HttpResponseException e = thrown()
        e.statusCode == 422

        where:
        description                                                                      | accountId | messageContent
        "Return error response when message has 41 chars"                                | '1'       | 'f' * 41
        "Return error response when message has blank content"                           | '1'       | ''
        "Return error response when invalid account is specified but with valid message" | '0'       | 'f' * 10
        "Return error response when invalid account and invalid message are specified"   | '0'       | ''
    }

    @Unroll('#description')
    def "Create a REST endpoint that will return the most recent messages for an Account. The endpoint must honor a limit parameter that caps the number of responses. The default limit is 10"() {
        given: "a message"
        def messageJson = '{"content": "1", "account": "1"}'
        for (int i = 1; i <10; i++) {
            def createMessageResponse = restClient.post(path: "/api/accounts/1/messages", requestContentType: "application/json", body: messageJson)
            assert createMessageResponse.status == 201
            assert createMessageResponse.data.content != null
        }

        when: "get the most recent messages via rest endpoint"
        def recentMessagesResponse = restClient.get(path: "/api/accounts/1/messages/recent", requestContentType: "application/json")

        then: "200 should be received"
        recentMessagesResponse.status == 200

    }
}
