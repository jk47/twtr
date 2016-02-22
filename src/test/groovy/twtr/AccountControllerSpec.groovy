package twtr

import grails.test.mixin.TestFor
import grails.test.mixin.Mock
import spock.lang.Specification
import spock.lang.Unroll

/**
 * See the API for {@link grails.test.mixin.web.ControllerUnitTestMixin} for usage instructions
 */
@TestFor(AccountController)
@Mock(Account)
class AccountControllerSpec extends Specification {

    def setup() {
    }

    def cleanup() {
    }

    def "creates an account"() {
        given: "an account in JSON form to save should #{description}"
        def accountJSON = '{"handle": "coding", "password": "TestPass1", "email": "test@gmail.com", "realName": "coding guy"}'

        when: "saving by submitting a JSON request"
        request.JSON = accountJSON
        request.method = "POST"
        controller.save()

        then: "a 201 is received via JSON response and account is saved to db"
        response.status == 201
        response.json.id != null
        Account.get(response.json.id).realName == "coding guy"
    }

    @Unroll('#description')
    def "verify an error is returned for invalid JSON account creation request"() {
        given: "an account with invalid JSON"
        controller.request.json = json
        request.method = "POST"
        def accountsBefore = Account.count()

        when: "saving by submitting a JSON request"
        controller.save()

        then: "a 422 is received via JSON response and account is not saved to db"
        accountsBefore == Account.count()
        response.status == 422

        where:
        description                | json
        'Fail on missing handle'   | '{"password": "TestPass1", "email": "test@gmail.com", "realName": "coding guy"}'
        'Fail on missing email'    | '{"handle": "coding", "password": "TestPass1", "realName": "coding guy"}'
        'Fail on missing password' | '{"handle": "coding", "email": "test@gmail.com", "realName": "coding guy"}'
        'Fail on missing name'     | '{"handle": "coding", "password": "TestPass1", "email": "test@gmail.com"}'
    }
}
