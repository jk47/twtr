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

    @Unroll('#description')
    def "Message test is required to be non-blank and 40 characters or less"()
    {
        given: "Create user account"
        def account = new Account(handle: 'john', email: "jhn@gmail.com", password: 'Testing123', realName: "john guy")
        Message message = new Message(content: testMessage)
        account.addToMessages(message)
        account.save(failOnError: false)

        when: "Retrieve user account"
        def foundAccount = Account.get(account.id)
        def errorSaving = foundAccount == null

        then:
        (errorSaving == expectedValidationError) || (foundAccount.messages.first().content.length() == count)

        where:
        description                                         | testMessage | expectedValidationError | count
        "Message with 1 chars"                              | 'f'         | false                   | 1
        "Message with 39 chars"                             | 'f'*39      | false                   | 39
        "Message with 40 chars"                             | 'f'*40      | false                   | 40
        "Validation error when message has 41 chars"        | 'f'*41      | true                    | 41
        "Validation error when message has blank content"   | ''          | true                    | 0
    }
}
