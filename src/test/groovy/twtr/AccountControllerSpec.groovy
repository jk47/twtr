package twtr

import grails.test.mixin.TestFor
import grails.test.mixin.Mock
import spock.lang.Specification

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

    void "save an account with JSON request"(){
        given: "an account in JSON form to save save"
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

}
