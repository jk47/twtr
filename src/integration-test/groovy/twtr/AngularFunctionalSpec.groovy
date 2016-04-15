package twtr

import geb.spock.GebSpec
import grails.test.mixin.integration.Integration
import spock.lang.Stepwise

@Integration
@Stepwise
class AngularFunctionalSpec extends GebSpec {

    def signIn(){
        go("localhost:8080")
        waitFor {
            $('#usernameLoginBox').isDisplayed()
        }
        $('#usernameLoginBox').value("john")
        $('#passwordLoginBox').value("Password1")
        $('#loginButton').click()
    }

    def 'L1/L2: route to login page when not logged in'() {
        when: 'not logged in and going to non login page'
        go("#/detail")

        then: 'login dialog displayed'
        $('#usernameLoginBox').isDisplayed()
        $('#passwordLoginBox').isDisplayed()
    }

    def 'L3: error displayed for invalid login'() {
        when: 'typing wrong password'
        go('localhost:8080')
        waitFor {
            $('#usernameLoginBox').isDisplayed()
        }
        $('#usernameLoginBox').value("john")
        $('#passwordLoginBox').value("bogusPassword")
        $('#loginButton').click()


        then: 'error is shown to the user'
        waitFor {
            $('#errorMessage').isDisplayed()
        }

    }

    def 'S1: search box allows user to search messages'() {
        when: 'logged in'
        signIn()

        then: 'search box is displayed'
        waitFor {
            $('#searchBox').isDisplayed()
        }

    }

    def 'S2: messages are displayed in scrollable area'() {
        given: 'logged in'
        signIn()

        when: 'searching'
        Thread.sleep(2000)
        $('#searchBox').value('#')
        $('#searchButton').click()

        then: 'results in scrollable list'
        waitFor {
            $('#resultsDiv').isDisplayed()
        }
        $('#resultsDiv').height < $('#searchResultsTable').height

    }
}