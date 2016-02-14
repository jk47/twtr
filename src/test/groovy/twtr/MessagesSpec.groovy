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
        def accountsBefore = Account.count()
        account.save(failOnError: false)
        def accountsAfter = Account.count()

        when: "Retrieve user account"
        def foundAccount = Account.get(account.id)
        def errorSaving = foundAccount == null

        then:
        errorSaving == expectedValidationError
        accountsAfter == accountsAfterExp
        accountsBefore == accountsBeforeExp

        where:
        description                                         | testMessage | expectedValidationError |   accountsAfterExp   | accountsBeforeExp
        "Message with 1 chars"                              | 'f'         | false                   |    1              |   0
        "Message with 39 chars"                             | 'f'*39      | false                   |    1              |   0
        "Message with 40 chars"                             | 'f'*40      | false                   |    1              |   0
        "Validation error when message has 41 chars"        | 'f'*41      | true                    |    0              |   0
        "Validation error when message has blank content"   | ''          | true                    |    0              |   0
    }
}
