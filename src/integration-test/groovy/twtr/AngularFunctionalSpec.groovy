package twtr

import geb.spock.GebSpec
import grails.test.mixin.integration.Integration

@Integration
class AngularFunctionalSpec extends GebSpec {

    def 'welcome page displays welcome message'() {
        when:
        go '/'

        then: 'Static welcome displayed properly'
        driver.currentUrl == 'localhost:8080'
        $('h1').first().text() == 'Welcome to the sample Grails 3 Angular App'

        and: 'Angular generated test displayed properly'
        $('h2').first().text() == 'Hello Stranger'
    }
}