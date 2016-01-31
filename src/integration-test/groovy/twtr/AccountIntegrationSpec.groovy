package twtr


import grails.test.mixin.integration.Integration
import grails.transaction.*
import spock.lang.*

@Integration
@Rollback
class AccountIntegrationSpec extends Specification {

    def setup() {
    }

    def cleanup() {
    }

    def "saving first account to database"() {
        given: "A brand new account"
            def account = new Account(handle: 'coding', password:'test', email: 'test@gmail.com', realName: 'coding guy')
        when: "the account is saved"
            account.save()
        then: "account is saved successfully and can be found in database"
            account.errors.errorCount == 0
            account.id != null
            Account.get(account.id).realName == account.realName
    }
}
