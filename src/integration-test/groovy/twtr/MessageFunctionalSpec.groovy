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
    def accountResponse1
    def accountResponse2

    def setup() {
        restClient = new RESTClient(baseUrl)
        def accountJSON1 = '{"handle": "account1", "password": "TestPass1", "email": "account1@gmail.com", "realName": "account 1 guy"}'
        accountResponse1 = restClient.post(path: "/api/accounts", requestContentType: "application/json", body: accountJSON1)
        assert accountResponse1.status == 201

        def accountJSON2 = '{"handle": "account2", "password": "TestPass1", "email": "account2@gmail.com", "realName": "account 2 guy"}'
        accountResponse2 = restClient.post(path: "/api/accounts", requestContentType: "application/json", body: accountJSON2)
        assert accountResponse2.status == 201
    }

    def cleanup() {
        restClient.delete(path: "/api/accounts/${accountResponse1.data.id}")
        restClient.delete(path: "/api/accounts/${accountResponse2.data.id}")
    }

    def "Create a message with an id"() {
        given: "an account that has been saved and a message in json form"
        def messageJson = '{"content": "twitter message content", "account": ' + accountResponse1.data.id.toString() + '}'

        when: "creating the message via rest endpoint"
        def response = restClient.post(path: "/api/accounts/${accountResponse1.data.id.toString()}/messages", requestContentType: "application/json", body: messageJson)

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
    def "Create 20 messages"() {
        given: "a message"
        def messageJson = '{"content": "1", "account": ' + accountResponse1.data.id.toString() + '}'

        when: "create 20 messages"
        for (int i = 0; i < 20; i++) {
            def createMessageResponse = restClient.post(path: "/api/accounts/${accountResponse1.data.id.toString()}/messages", requestContentType: "application/json", body: messageJson)
            assert createMessageResponse.status == 201
            assert createMessageResponse.data.content != null
        }

        def getMessagesResponse = restClient.get(path: "/api/accounts/${accountResponse1.data.id.toString()}/messages", requestContentType: "application/json")

        then: "200 should be received"
        getMessagesResponse.status == 200
        getMessagesResponse.data.size == 20 // total should be 20
    }

    @Unroll('#description')
    def "Return the most recent messages for an Account using the default limit"() {
        given: "20 messages that got posted to account1"
        def messageJson = '{"content": "twitter message", "account": ' + accountResponse1.data.id.toString() + '}'
        int[] createdMessageIds = new int [20]
        for (int i = 0; i < 20; i++) {
            def createMessageResponse = restClient.post(path: "/api/accounts/${accountResponse1.data.id.toString()}/messages", requestContentType: "application/json", body: messageJson)
            assert createMessageResponse.status == 201
            assert createMessageResponse.data.content != null
            createdMessageIds[i] = createMessageResponse.data.id
        }

        when: "get the most recent messages via rest endpoint"
        def recentMessagesResponse = restClient.get(path: "/api/accounts/${accountResponse1.data.id.toString()}/messages/recent", requestContentType: "application/json")

        then: "200 should be received and the returned messages should contain message ids ordered in an descending order, which represents chronological order of when the messages were created"
        recentMessagesResponse.status == 200
        // size should be 10 as that is the default limit
        recentMessagesResponse.data.size == 10

        // response that includes most recent message id should match the last stored createdMessageId in the array
        def messageIndex = createdMessageIds.size() - 1;
        recentMessagesResponse.data[0].id == createdMessageIds[messageIndex--]
        recentMessagesResponse.data[1].id == createdMessageIds[messageIndex--]
        recentMessagesResponse.data[2].id == createdMessageIds[messageIndex--]
        recentMessagesResponse.data[3].id == createdMessageIds[messageIndex--]
        recentMessagesResponse.data[4].id == createdMessageIds[messageIndex--]
        recentMessagesResponse.data[5].id == createdMessageIds[messageIndex--]
        recentMessagesResponse.data[6].id == createdMessageIds[messageIndex--]
        recentMessagesResponse.data[7].id == createdMessageIds[messageIndex--]
        recentMessagesResponse.data[8].id == createdMessageIds[messageIndex--]
        recentMessagesResponse.data[9].id == createdMessageIds[messageIndex--]
    }

    @Unroll('#description')
    def "Return the most recent messages for an Account with specified limits"() {
        given: "20 messages posted to account1"
        def messageJson = '{"content": "twitter message", "account": ' + accountResponse1.data.id.toString() + '}'
        for (int i = 0; i < 20; i++) {
            def createMessageResponse = restClient.post(path: "/api/accounts/${accountResponse1.data.id.toString()}/messages", requestContentType: "application/json", body: messageJson)
            assert createMessageResponse.status == 201
            assert createMessageResponse.data.content != null
        }

        when: "get the most recent messages via rest endpoint"
        def recentMessagesResponse = restClient.get(path: "/api/accounts/${accountResponse1.data.id.toString()}/messages/recent", query: [limit: messageLimit], requestContentType: "application/json")

        then: "200 should be received"
        recentMessagesResponse.status == 200
        recentMessagesResponse.data.size == expectedCount

        where:
        description                        | messageLimit | expectedCount
        "Recent messages with limit of 10"  | 10           | 10
        "Recent messages with limit of 5"   | 5            | 5
        "Recent messages with limit of 1"   | 1            | 1
    }

    @Unroll('#description')
    def "Return the most recent messages for an Account with specified limit=5 and offset =2"() {
        given: "20 messages posted to account1"

        def messageJson = '{"content": "twitter message", "account": ' + accountResponse1.data.id.toString() + '}'
        int[] createdMessageIds = new int [20]
        for (int i = 0; i < 20; i++) {
            def createMessageResponse = restClient.post(path: "/api/accounts/${accountResponse1.data.id.toString()}/messages", requestContentType: "application/json", body: messageJson)
            assert createMessageResponse.status == 201
            assert createMessageResponse.data.content != null
            createdMessageIds[i] = createMessageResponse.data.id
        }

        when: "get the most recent messages via rest endpoint and specifying both limit and offset queries"
        def recentMessagesResponse = restClient.get(path: "/api/accounts/${accountResponse1.data.id.toString()}/messages/recent", query: [limit: 5, offset: 2], requestContentType: "application/json")

        then: "200 should be received and the order of the returned message ids should correspond to the specified limit and offset values in the query"
        recentMessagesResponse.status == 200
        recentMessagesResponse.data.size == 5
        // cross check message ids ensure limit and offset is functioning correctly
        // With this test, message ids represent the chronological order of most recently created messages.
        // So, with 20 messages, limit = 5, offset = 2, most recent messageId = lastPostedMessageId
        def messageIndex = (createdMessageIds.size() - 1) - (5 * 2)
        recentMessagesResponse.data[0].id == createdMessageIds[messageIndex--]
        recentMessagesResponse.data[1].id == createdMessageIds[messageIndex--]
        recentMessagesResponse.data[2].id == createdMessageIds[messageIndex--]
        recentMessagesResponse.data[3].id == createdMessageIds[messageIndex--]
        recentMessagesResponse.data[4].id == createdMessageIds[messageIndex--]
    }

    @Unroll('#description')
    def "Return messages with the associated handle that contains specified search terms."() {
        given: "six messages posted with 2 different accounts"
        def messageJson1 = '{"content": "Jordan with soul, performance and style", "account": ' + accountResponse1.data.id.toString() + '}'
        def messageJson2 = '{"content": "See jordan himself in action", "account": ' + accountResponse1.data.id.toString() + '}'
        def messageJson3 = '{"content": "twitter message", "account": ' + accountResponse1.data.id.toString() + '}'
        def messageJson4 = '{"content": "twitter message", "account": ' + accountResponse2.data.id.toString() + '}'
        def messageJson5 = '{"content": "Rocking the Jordan UltraFly", "account": ' + accountResponse2.data.id.toString() + '}'
        def messageJson6 = '{"content": "twitter message", "account": ' + accountResponse2.data.id.toString() + '}'
        def messageCreated1Response = restClient.post(path: "/api/accounts/${accountResponse1.data.id.toString()}/messages", requestContentType: "application/json", body: messageJson1)
        def messageCreated2Response = restClient.post(path: "/api/accounts/${accountResponse1.data.id.toString()}/messages", requestContentType: "application/json", body: messageJson2)
        def messageCreated3Response = restClient.post(path: "/api/accounts/${accountResponse1.data.id.toString()}/messages", requestContentType: "application/json", body: messageJson3)
        def messageCreated4Response = restClient.post(path: "/api/accounts/${accountResponse2.data.id.toString()}/messages", requestContentType: "application/json", body: messageJson4)
        def messageCreated5Response = restClient.post(path: "/api/accounts/${accountResponse2.data.id.toString()}/messages", requestContentType: "application/json", body: messageJson5)
        def messageCreated6Response = restClient.post(path: "/api/accounts/${accountResponse2.data.id.toString()}/messages", requestContentType: "application/json", body: messageJson6)

        when: "search messages via rest endpoint using the search term jordan"
        def searchMessagesResponse = restClient.get(path: "/api/messages/search", query: [term: 'jordan'], requestContentType: "application/json")

        then: "200 should be received and message 1,2 and 5 should be returned with handle of an account"
        searchMessagesResponse.status == 200
        searchMessagesResponse.data.size == 3
    }
}
