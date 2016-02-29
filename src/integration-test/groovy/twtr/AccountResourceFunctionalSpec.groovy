package twtr


import geb.spock.GebSpec
import grails.test.mixin.integration.Integration
import spock.lang.Stepwise
import groovyx.net.http.RESTClient


@Integration
@Stepwise
class AccountFunctionalSpec extends GebSpec {

    RESTClient restClient

    def setup() {
        restClient = new RESTClient(baseUrl)
    }

    def "create an account"(){
        given: "an account in JSON form"
        def accountJSON = '{"handle": "coding", "password": "TestPass1", "email": "test@gmail.com", "realName": "coding guy"}'

        when: "trying to post the account via rest"
        def resp = restClient.post(path: "/api/accounts", requestContentType: 'application/json', body: accountJSON)

        then:
        resp.status == 201
    }
}
