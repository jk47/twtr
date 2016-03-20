package twtr

import geb.spock.GebSpec
import grails.converters.JSON
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

    def accounts = []

    def setup() {
        restClient = new RESTClient(baseUrl)
        (1..2).each { i ->
              def json = ([handle: "account${i}", password: "TestPass${i}", email: "account${i}@gmail.com", realName: "account${i} guy"] as JSON).toString()
              accounts << restClient.post(path: "/api/accounts", requestContentType: "application/json", body: json)
              assert accounts[i-1].status == 201
          }
    }

    def cleanup() {
      (1..2).each { i ->
        restClient.delete(path: "/api/accounts/${accounts[i-1].data.id}")
      }
    }

    def "Create a message with an id"() {
        given: "an account that has been saved and a message in json form"
        def messageJson = '{"content": "twitter message content", "account": ' + accounts[0].data.id.toString() + '}'

        when: "creating the message via rest endpoint"
        def response = restClient.post(path: "/api/accounts/${accounts[0].data.id}/messages", requestContentType: "application/json", body: messageJson)

        then: "a 201 should be received and the account should have a message"
        response.status == 201
        response.data.content != null
    }

    def "Return error response when #description"() {
        given: "a message"
        def messageJson = '{"content": ' + messageContent + ', "account": ' + accountId + '}'

        when: "creating the message via rest endpoint"
        restClient.post(path: "/api/accounts/${accountId}/messages", requestContentType: "application/json", body: messageJson)

        then: "a 422 should be received"
        HttpResponseException e = thrown()
        e.statusCode == 422

        where:
        description                                           | accountId | messageContent
        "message has 41 chars"                                | '1'       | 'f' * 41
        "message has blank content"                           | '1'       | ''
        "invalid account is specified but with valid message" | '0'       | 'f' * 10
        "invalid account and invalid message are specified"   | '0'       | ''
    }

    def "Messages for a user are retrieved"() {
        given: "a message"
        def messageJson = '{"content": "1", "account": ' + accounts[0].data.id.toString() + '}'

        when: "create 20 messages"
        for (int i = 0; i < 20; i++) {
            def createMessageResponse = restClient.post(path: "/api/accounts/${accounts[0].data.id.toString()}/messages", requestContentType: "application/json", body: messageJson)
            assert createMessageResponse.status == 201
            assert createMessageResponse.data.content != null
        }

        def getMessagesResponse = restClient.get(path: "/api/accounts/${accounts[0].data.id.toString()}/messages", requestContentType: "application/json")

        then: "200 should be received"
        getMessagesResponse.status == 200
        getMessagesResponse.data.size == 20 // total should be 20
    }

    def "Return the most recent messages for an Account using the default limit"() {
        given: "20 messages that got posted to account1"
        def messageJson = '{"content": "twitter message", "account": ' + accounts[0].data.id.toString() + '}'
        int[] createdMessageIds = new int [20]
        for (int i = 0; i < 20; i++) {
            def createMessageResponse = restClient.post(path: "/api/accounts/${accounts[0].data.id.toString()}/messages", requestContentType: "application/json", body: messageJson)
            assert createMessageResponse.status == 201
            assert createMessageResponse.data.content != null
            createdMessageIds[i] = createMessageResponse.data.id
        }

        when: "get the most recent messages via rest endpoint"
        def recentMessagesResponse = restClient.get(path: "/api/accounts/${accounts[0].data.id.toString()}/messages/recent", requestContentType: "application/json")

        then: "200 should be received and the returned messages should contain message ids ordered in an descending order, which represents chronological order of when the messages were created"
        recentMessagesResponse.status == 200
        // size should be 10 as that is the default limit
        recentMessagesResponse.data.size == 10

        // response that includes most recent message id should match the last stored createdMessageId in the array
        def messageIndex = createdMessageIds.size() - 1;
        (0..9).each { i ->
          assert recentMessagesResponse.data[i].id == createdMessageIds[messageIndex--]
        }
    }

    def "Return the most recent messages for an Account with specified limits: #messageLimit"() {
        given: "20 messages posted to account1"
        def messageJson = '{"content": "twitter message", "account": ' + accounts[0].data.id.toString() + '}'
        def responses = []

        when:
        for (int i = 0; i < 20; i++) {
            responses << restClient.post(path: "/api/accounts/${accounts[0].data.id.toString()}/messages", requestContentType: "application/json", body: messageJson)
        }

        then:
        responses.each { response ->
            assert response.status == 201
            assert response.data.content != null
        }

        when: "get the most recent messages via rest endpoint"
        def recentMessagesResponse = restClient.get(path: "/api/accounts/${accounts[0].data.id.toString()}/messages/recent", query: [limit: messageLimit], requestContentType: "application/json")

        then: "200 should be received"
        recentMessagesResponse.status == 200
        recentMessagesResponse.data.size == messageLimit

        where:
        messageLimit  << [10, 5, 1]
    }

    def "Return the most recent messages for an Account with specified limit=5 and offset =2"() {
        given: "20 messages posted to account1"

        def messageJson = '{"content": "twitter message", "account": ' + accounts[0].data.id.toString() + '}'
        int[] createdMessageIds = new int [20]
        for (int i = 0; i < 20; i++) {
            def createMessageResponse = restClient.post(path: "/api/accounts/${accounts[0].data.id.toString()}/messages", requestContentType: "application/json", body: messageJson)
            assert createMessageResponse.status == 201
            assert createMessageResponse.data.content != null
            createdMessageIds[i] = createMessageResponse.data.id
        }

        when: "get the most recent messages via rest endpoint and specifying both limit and offset queries"
        def recentMessagesResponse = restClient.get(path: "/api/accounts/${accounts[0].data.id.toString()}/messages/recent", query: [limit: 5, offset: 2], requestContentType: "application/json")

        then: "200 should be received and the order of the returned message ids should correspond to the specified limit and offset values in the query"
        recentMessagesResponse.status == 200
        recentMessagesResponse.data.size == 5
        // cross check message ids ensure limit and offset is functioning correctly
        // With this test, message ids represent the chronological order of most recently created messages.
        // So, with 20 messages, limit = 5, offset = 2, most recent messageId = lastPostedMessageId
        def messageIndex = (createdMessageIds.size() - 1) - (5 * 2)
        (0..4).each { i ->
          assert recentMessagesResponse.data[i].id == createdMessageIds[messageIndex--]
        }
    }

    def "Return messages with the associated handle that contains specified search terms."() {
        given: "six messages posted with 2 different accounts"
        def id0 = accounts[0].data.id as String
        def id1 = accounts[1].data.id as String
        def messageJson1 = '{"content": "Jordan with soul, performance and style", "account": ' + id0 + '}'
        def messageJson2 = '{"content": "See jordan himself in action", "account": ' + id0 + '}'
        def messageJson3 = '{"content": "twitter message", "account": ' + id0 + '}'
        def messageJson4 = '{"content": "twitter message", "account": ' + id1 + '}'
        def messageJson5 = '{"content": "Rocking the Jordan UltraFly", "account": ' + id1 + '}'
        def messageJson6 = '{"content": "twitter message", "account": ' + id0 + '}'
        def messageCreated1Response = restClient.post(path: "/api/accounts/${id0}/messages", requestContentType: "application/json", body: messageJson1)
        def messageCreated2Response = restClient.post(path: "/api/accounts/${id0}/messages", requestContentType: "application/json", body: messageJson2)
        restClient.post(path: "/api/accounts/${id0}/messages", requestContentType: "application/json", body: messageJson3)
        restClient.post(path: "/api/accounts/${id1}/messages", requestContentType: "application/json", body: messageJson4)
        def messageCreated5Response = restClient.post(path: "/api/accounts/${id1}/messages", requestContentType: "application/json", body: messageJson5)
        restClient.post(path: "/api/accounts/${id1}/messages", requestContentType: "application/json", body: messageJson6)

        when: "search messages via rest endpoint using the search term jordan"
        def searchMessagesResponse = restClient.get(path: "/api/messages/search", query: [term: 'jordan'], requestContentType: "application/json")

        then: "200 should be received and message 1,2 and 5, which contains matching terms should be returned with handle of an account"
        searchMessagesResponse.status == 200
        searchMessagesResponse.data.size == 3

        // validate that message1(posted to account1), which contains the searched term is part of the returned response
        def responseObject1 = searchMessagesResponse.data.find{it.message.id == messageCreated1Response.data.id}
        responseObject1.handle == accounts[0].data.handle
        responseObject1.message.content == messageCreated1Response.data.content
        responseObject1.message.dateCreated == messageCreated1Response.data.dateCreated

        // validate that message2(posted to account1), which contains the searched term is part of the returned response
        def responseObject2 = searchMessagesResponse.data.find{it.message.id == messageCreated2Response.data.id}
        responseObject2.handle == accounts[0].data.handle
        responseObject2.message.content == messageCreated2Response.data.content
        responseObject2.message.dateCreated == messageCreated2Response.data.dateCreated

        // validate that message5(posted to account2), which contains the searched term is part of the returned response
        def responseObject3 = searchMessagesResponse.data.find{it.message.id == messageCreated5Response.data.id}
        responseObject3.handle == accounts[1].data.handle
        responseObject3.message.content == messageCreated5Response.data.content
        responseObject3.message.dateCreated == messageCreated5Response.data.dateCreated
    }
}
