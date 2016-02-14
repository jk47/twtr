package twtr


import grails.test.mixin.integration.Integration
import grails.transaction.*
import spock.lang.*


@Integration
@Rollback
class MessageIntegrationSpec extends Specification {

    def setup() {
    }

    def cleanup() {
    }

    def "Ensure messages linked to a user can be retrieved"() {
        given: "A user with several posts"
        def account = new Account(handle: 'john', email: "jhn@gmail.com", password: 'Testing123', realName: "john guy")
        account.addToMessages(new Message(content: "First"))
        account.addToMessages(new Message(content: "Second"))
        account.addToMessages(new Message(content: "Third"))
        account.save()

        when: "The user is retrieved by their id"
        def foundAccount = Account.get(account.id)
        def sortedMessageContent = foundAccount.messages.collect {
            it.content
        }.sort()

        then: "The posts appear on the retrieved user"
        sortedMessageContent == ['First', 'Second', 'Third']
    }
}
