package twtr

import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Unroll
import grails.test.mixin.TestMixin
import grails.test.mixin.domain.DomainClassUnitTestMixin
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.domain.DomainClassUnitTestMixin} for usage instructions
 */
@TestFor(Message)
@Mock([Account, Message])
@TestMixin(DomainClassUnitTestMixin)
class MessageSpec extends Specification {

    def setup() {
    }

    def cleanup() {
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
        def contentCharCount = 0;
        if(foundAccount)
        {
            contentCharCount = foundAccount.messages.first().content.length()
        }

        then:
        ( (errorSaving  || (!errorSaving && (contentCharCount == expectedCount))) && (errorSaving == expectedValidationError))

        where:
        description                                         | testMessage | expectedValidationError | expectedCount
        "Message with 1 chars"                              | 'f'         | false                   | 1
        "Message with 39 chars"                             | 'f'*39      | false                   | 39
        "Message with 40 chars"                             | 'f'*40      | false                   | 40
        "Validation error when message has 41 chars"        | 'f'*41      | true                    | 0
        "Validation error when message has blank content"   | ''          | true                    | 0
    }
}
