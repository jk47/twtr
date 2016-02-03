package twtr

import grails.test.mixin.TestFor
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.domain.DomainClassUnitTestMixin} for usage instructions
 */
@TestFor(Account)
class AccountSpec extends Specification {

    def setup() {

    }

    def cleanup() {
    }


    def "invalid passwords will not be saved to db"() {
        given: "an account with invalid password"
            def account = new Account(handle: 'coding', password:'Test1', email: 'test@gmail.com', realName: 'coding guy')
        when: "attempting to save"
            account.save()
        then: "an error will be attached to account, it will not save"
            account.errors.errorCount > 0
    }


    def "attempts to save account without required handle, email, and/or password will fail"(){
        given: "an account with a missing handle field"
            def account = new Account(password:'Test1', email: 'test@gmail.com', realName: 'coding guy')
        when: "attempting to save"
            account.save()
        then: "an error will be attached to the account, it will not save"
            account.errors.errorCount > 0
    }
}
