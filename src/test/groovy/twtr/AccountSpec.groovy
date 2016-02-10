package twtr

import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.domain.DomainClassUnitTestMixin
import spock.lang.Specification
import spock.lang.Unroll

/**
 * See the API for {@link grails.test.mixin.domain.DomainClassUnitTestMixin} for usage instructions
 */
@TestFor(Account)
@TestMixin(DomainClassUnitTestMixin)
class AccountSpec extends Specification {

    def setup() {

    }

    def cleanup() {
    }

    def "saving account with valid constraints to database will succeed"() {
        given: "an account"
        def account = new Account(handle: 'coding', password:'TestPass1', email: 'test@gmail.com', realName: 'coding guy')
        when: "the account is saved"
        account.save()
        then: "account is saved successfully and can be found in database"
        account.errors.errorCount == 0
        account.id != null
        Account.get(account.id).realName == account.realName
    }

    @Unroll('#description')
    def "invalid passwords will not be saved to db"() {
        given: "an account with invalid password"
            def account = new Account(handle: 'coding', password:inputPassword, email: 'test@gmail.com', realName: 'coding guy')
        when: "attempting to save"
            account.save()
        then: "an error will be attached to account, it will not save"
            account.errors.errorCount > 0 == expectedValidationError

        where:
        description                                             | inputPassword                 | expectedValidationError
        'Fail on password with 7 valid characters'              | 'M'*3 + 'j'*2 + '23'          | true
        'Pass on password with 8 valid characters'              | 'M'*3 + 'j'*3 + '23'          | false
        'Pass on password with 9 valid characters'              | 'M'*3 + 'j'*4 + '23'          | false
        'Pass on password with 16 valid characters'             | 'M'*7 + 'j'*7 + '23'          | false
        'Fail on password with 17 valid characters'             | 'M'*7 + 'j'*8 + '23'          | true
        'Fail with password that contains no numbers'           | 'M'*7 + 'j'*7                 | true
        'Fail with password that contains no upper-case'        | 'm'*4 + 'j'*4 + '23'          | true
        'Fail with password that contains no lower-case'        | 'M'*4 + 'J'*4 + '23'          | true
    }

    @Unroll('#description')
    def "attempts to save account without required handle, email, and/or password will fail"(){
        given: "an account with a missing handle field"
            def account = new Account(handle: handle, password:password, email: email, realName: name)
        when: "attempting to save"
            account.save()
        then: "an error will be attached to the account, it will not save"
            account.errors.errorCount > 0 == expectedValidationError

        where:
        description                 | handle    | password              | email                 | name              | expectedValidationError
        'Fail on missing handle'    | null      | 'M'*3 + 'j'*3 + '23'  | 'emjay23@gmail.com'   | 'Michael Jordan'  | true
        'Fail on missing email'     | 'MJ23'   | 'M'*3 + 'j'*3 + '23'   | null                  | 'Michael Jordan'  | true
        'Fail on missing password'  | 'MJ23'   | null                   | 'emjay23@gmail.com'   | 'Michael Jordan'  | true
        'Fail on missing name'      | 'MJ23'   | 'M'*3 + 'j'*3 + '23'   | 'emjay23@gmail.com'   | null              | true
        'Pass on saving account with all valid constraints'    | 'MJ23'   | 'M'*3 + 'j'*3 + '23'   | 'emjay23@gmail.com'   | 'Michael Jordan'  | false
    }
}
