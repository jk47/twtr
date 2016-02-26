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

    def "account controller returns account based on given ID"(){
        given: "a few saved accounts"
        def account1 = new Account(handle: "account1", password: "Testing123", email: "account1@gmail.com", realName: "account1guy").save()
        def account2 = new Account(handle: "account2", password: "Testing123", email: "account2@gmail.com", realName: "account2guy").save()
        def account3 = new Account(handle: "account2", password: "Testing123", email: "account3@gmail.com", realName: "account3guy").save()

        when: "requesting an account by id"
        controller.request.method = "GET"
        controller.params.id = 1
        controller.show()

        then: "response should include account for corresponding id"
        response.status == 200
        response.json.id == account1.id
        response.json.handle == account1.handle
        response.json.email == account1.email

    }

    def "account controller returns account based on a given handle"(){

    }
}
