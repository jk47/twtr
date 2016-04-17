package twtr

import geb.spock.GebSpec
import grails.test.mixin.integration.Integration
import spock.lang.Stepwise

@Integration
@Stepwise
class AngularFunctionalSpec extends GebSpec {

    def signIn(){
        go("localhost:8080")
        Thread.sleep(1000)
        waitFor {

            $('#usernameLoginBox').isDisplayed()
        }
        $('#usernameLoginBox').value("john")
        $('#passwordLoginBox').value("Password1")
        $('#loginButton').click()
    }

    def signOut(){
        Thread.sleep(1000)
        $('#logout').click()
        $('#exitMessage').isDisplayed()
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
            Thread.sleep(1000)
            $('#searchBox').isDisplayed()
        }

        cleanup:
        signOut()

    }

    def 'S2/S3/S4: messages are displayed in scrollable area'() {
        given: 'logged in'
        signIn()

        when: 'searching'
        Thread.sleep(3000)
        $('#searchBox').value('#')
        $('#searchButton').click()

        then: 'results in scrollable list'
        Thread.sleep(2000)
        $('#resultsDiv').height < $('#searchResultsTable').height

        and: 'content and author are displayed'
        $('#messageContent').isDisplayed()
        $('#messagePoster').isDisplayed()

        and: 'clicking user link will bring user to details page'
        $('#nameLink').click()
        waitFor {
            $('#detailsHeader').isDisplayed()
        }

        cleanup:
        signOut()

    }

    def 'U1/U2: detail page will display user’s name and a scrollable list of that user’s postings and follow button'(){
        when: 'signed in and at details page'
        signIn()
        Thread.sleep(1000)
        go('/#/userDetail?handle=admin')
        Thread.sleep(1000)

        then: 'name and postings are displayed as well as follow'
        $('#postingContent').isDisplayed()
        $('#detailName').isDisplayed()
        $('#follow').isDisplayed()

        cleanup:
        signOut()
    }
}