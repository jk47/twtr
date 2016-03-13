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
    def "Create 19 more messages"() {
        given: "a message"
        def messageJson = '{"content": "1", "account": "1"}'

        when: "get the most recent messages via rest endpoint"
        for (int i = 1; i < 20; i++) {
            def createMessageResponse = restClient.post(path: "/api/accounts/1/messages", requestContentType: "application/json", body: messageJson)
            assert createMessageResponse.status == 201
            assert createMessageResponse.data.content != null
        }

        def getMessagesResponse = restClient.get(path: "/api/accounts/1/messages", requestContentType: "application/json")

        then: "200 should be received"
        getMessagesResponse.status == 200
        getMessagesResponse.data.size == 20 // total should be 10 as we have created 1 earlier
    }

    @Unroll('#description')
    def "Create a REST endpoint that will return the most recent messages for an Account using the default limit"() {
        given: "a message"

        when: "get the most recent messages via rest endpoint"
        def recentMessagesResponse = restClient.get(path: "/api/accounts/1/messages/recent", requestContentType: "application/json")

        then: "200 should be received"
        recentMessagesResponse.status == 200
        recentMessagesResponse.data.size == 10
    }

    @Unroll('#description')
    def "Create a REST endpoint that will return the most recent messages for an Account with specified limits"() {
        given: "a message"

        when: "get the most recent messages via rest endpoint"
        def recentMessagesResponse = restClient.get(path: "/api/accounts/1/messages/recent", query: [limit: messageLimit], requestContentType: "application/json")

        then: "200 should be received"
        recentMessagesResponse.status == 200
        recentMessagesResponse.data.size == expectedCount

        where:
        description                        | queryPath                         | messageLimit | expectedCount
        "Recent messages with limit of 10" | '/api/accounts/1/messages/recent' | 10           | 10
        "Recent messages with limit of 5"  | '/api/accounts/1/messages/recent' | 5            | 5
        "Recent messages with limit of 1"  | '/api/accounts/1/messages/recent' | 1            | 1
    }

    @Unroll('#description')
    def "Support an offset parameter into the recent Messages endpoint to provide paged responses."() {
        given: "a message"

        when: "get the most recent messages via rest endpoint and specifying both limit and offset queries"
        def recentMessagesResponse = restClient.get(path: "/api/accounts/1/messages/recent", query: [limit: 5, offset:2], requestContentType: "application/json")

        then: "200 should be received the order of the returned message ids should correspond to the specified limit and offset values in the query"
        recentMessagesResponse.status == 200
        recentMessagesResponse.data.size == 5
        recentMessagesResponse.data[0].id == 10
        recentMessagesResponse.data[1].id == 9
        recentMessagesResponse.data[2].id == 8
        recentMessagesResponse.data[3].id == 7
        recentMessagesResponse.data[4].id == 6
    }
}
