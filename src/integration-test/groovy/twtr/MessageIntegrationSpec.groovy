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
        given: "A user with several posts"//from bootstrap
        def account = Account.findByHandle("john")

        when: "The user is retrieved by their id"
        def foundAccount = Account.get(account.id)
        def sortedMessageContent = foundAccount.messages.collect {
            it.content
        }.sort()

        then: "The posts appear on the retrieved user"
        sortedMessageContent.size() == 10
    }
}
