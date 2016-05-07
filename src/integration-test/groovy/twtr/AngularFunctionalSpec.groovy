package twtr

import geb.spock.GebSpec
import grails.test.mixin.integration.Integration
import spock.lang.Shared
import spock.lang.Stepwise
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat

@Integration
@Stepwise
class AngularFunctionalSpec extends GebSpec {

    @Shared
    def months = "Jan Feb Mar Apr May Jun Jul Aug Sep Oct Nov Dec".split()

    def signIn() {
        go("localhost:8080")
        Thread.sleep(1000)
        waitFor {

            $('#usernameLoginBox').isDisplayed()
        }
        $('#usernameLoginBox').value("john")
        $('#passwordLoginBox').value("Password1")
        $('#loginButton').click()
    }

    def signIn2() {
        go("localhost:8080")
        Thread.sleep(1000)
        waitFor {

            $('#usernameLoginBox').isDisplayed()
        }
        $('#usernameLoginBox').value("kingofgondor")
        $('#passwordLoginBox').value("TheRanger")
        $('#loginButton').click()
    }

    def signOut() {
        Thread.sleep(1000)
        $('#logout').click()
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
            $('#detailsRealName').isDisplayed()
        }

        cleanup:
        signOut()
    }

    def 'U1/U2: detail page will display user’s name and a scrollable list of that user’s postings and follow button'() {
        when: 'signed in and at details page'
        signIn()
        Thread.sleep(1000)
        go('/#/userDetail?handle=admin')
        Thread.sleep(1000)

        then: 'name and postings are displayed as well as follow'
        $('#postingContent').isDisplayed()
        $('#detailsHandle').isDisplayed()

        cleanup:
        signOut()
    }

    def 'U3/R4: When the logged in user is following the detail user, the detail page will display the following button'() {
        given: 'logged in'
        signIn2()

        when: 'searching'
        waitFor {
            $('#homeView').isDisplayed()
        }
        $('#searchBox').value('John')
        $('#searchButton').click()

        then: 'content and author are displayed'
        Thread.sleep(2000)
        $('#messageContent').isDisplayed()
        $('#messagePoster').isDisplayed()

        and: 'clicking user link will bring user to details page'
        $('#nameLink').click()
        waitFor {
            $('#detailsView').isDisplayed()
        }

        and: 'verify user real name and check that the following button is displayed to show that user is currently being followed'
        $('#detailsRealName').text() == 'john'
        $('#followingButton').isDisplayed()
        $('#followingDirective').isDisplayed()

        cleanup:
        signOut()
    }

    def '!U3: When the logged in user is NOT following the detail user, the detail page will display the follow button'() {
        given: 'logged in'
        signIn2()

        when: 'searching'
        waitFor {
            $('#homeView').isDisplayed()
        }
        $('#searchBox').value('Admin')
        $('#searchButton').click()

        then: 'content and author are displayed'
        Thread.sleep(2000)
        $('#messageContent').isDisplayed()
        $('#messagePoster').isDisplayed()

        and: 'clicking user link will bring user to details page'
        $('#nameLink').click()
        waitFor {
            $('#detailsView').isDisplayed()
        }

        and: 'verify user real name and check that the following button is displayed to show that user is currently being followed'
        $('#detailsRealName').text() == 'admin'
        $('#followButton').isDisplayed()

        cleanup:
        signOut()
    }

    def '!U3: When the logged in user is viewing his/her own details view, the follow and following button should not be displayed'() {
        when: 'signed in and current user link is clicked'
        signIn2()
        waitFor {
            $('#homeView').isDisplayed()
        }
        $('#currentUserDetailsLink').click()

        then: 'page will navigate to the current logged in user detail page'
        waitFor {
            $('#detailsView').isDisplayed()
        }

        and: 'displayed name in user detail view matches current logged in user'
        $('#detailsRealName').text() == 'Aragorn'

        and: 'follow and following buttons are not displayed'
        !$('#followButton').isDisplayed()
        !$('#followingButton').isDisplayed()

        cleanup:
        signOut()
    }

    def 'U4: When logged in user is on their own detail page, they can edit their name and email'() {
        when: 'signed in and on own detail page'
        signIn()
        Thread.sleep(1000)
        go('/#/userDetail?handle=john')
        Thread.sleep(1000)

        then: 'edit name and email fields are displayed'
        $('#nameInput').isDisplayed()
        $('#emailInput').isDisplayed()

        cleanup:
        signOut()
    }

    def 'U4: When name is edited, it will be reflected on the UI'(){
        given: 'signed in and on own detail page'
        signIn()
        Thread.sleep(1000)
        go('/#/userDetail?handle=john')
        Thread.sleep(1000)

        when: 'changing the name and saving'
        $('#nameInput').value("john2")
        $('#emailInput').value("john2@gmail.com")
        $('#updateSaveButton').click()
        Thread.sleep(2000)

        then: 'new name will show on details page'
        $('#detailsRealName').text() == "john2"

        cleanup:
        $('#nameInput').value("john")
        $('#emailInput').value("john@gmail.com")
        $('#updateSaveButton').click()
        Thread.sleep(2000)
        signOut()

    }

    def '!U4: When logged in user is NOT on their own detail page, they CANNOT edit name and email'() {
        when: 'signed in and on own detail page'
        signIn()
        Thread.sleep(1000)
        go('/#/userDetail?handle=admin')
        Thread.sleep(1000)

        then: 'edit name and email fields are displayed'
        !$('#nameInput').isDisplayed()
        !$('#emailInput').isDisplayed()

        cleanup:
        signOut()
    }

    def 'N1/N2: When signed in, user can navigate between user detail view and home view to search'() {
        when: 'signed in and current user link is clicked'
        signIn2()
        Thread.sleep(1000)
        $('#currentUserDetailsLink').click()

        then: 'page will navigate to the current logged in user detail page'
        waitFor {
            $('#detailsView').isDisplayed()
        }

        and: 'displayed name in user detail view matches current logged in user'
        $('#detailsRealName').text() == 'Aragorn'

        and: 'click home link to view home'
        $('#homeLink').click()
        waitFor {
            $('#homeView').isDisplayed()
        }

        and: 'search messages using search box'
        $('#searchBox').value('John')
        $('#searchButton').click()

        and: 'content and author are displayed'
        $('#messageContent').isDisplayed()
        $('#messagePoster').isDisplayed()

        and: 'clicking user link will bring user to details page'
        $('#nameLink').click()
        waitFor {
            $('#detailsView').isDisplayed()
        }

        and: 'displayed name in user detail view matches user from search result'
        $('#detailsRealName').text() == 'john'

        cleanup:
        signOut()
    }

    def 'N3: When signed out successfully, sign out message is displayed'() {
        when: 'signed out from home page'
        signIn()
        Thread.sleep(1000)
        signOut()
        Thread.sleep(1000)

        then: 'sign out message is displayed'
        $('#logoutMessage').isDisplayed()
    }

    def 'R5: Angular filter for date feed in MMM dd format'(){
        when: 'signed in and at the details page'
        signIn()
        Thread.sleep(1000)
        go('/#/userDetail?handle=john')
        Thread.sleep(1000)

        then: 'date will be displayed as MMM dd format'
        DateFormat df = new SimpleDateFormat("MMM dd");
        months.contains($('#postingDate').text().trim().substring(0,3))
        try{
            df.parse($('#postingDate').text().trim())
            true
        }
        catch(ParseException pe){
            false
        }

        cleanup:
        signOut()
    }

    def 'R0/R1: Validated tweet can be posted from the UI'(){
        when: 'signed in and at the home page'
        signIn()
        Thread.sleep(1000)

        then: 'user can post from the UI'
        $('#tweetArea').value('sample Tweet')
        $('#tweetButton').click()
        Thread.sleep(1000)
        $('#tweetAlert').isDisplayed()

        cleanup:
        signOut()
    }

    def 'R2: Too long of a tweet results in inactive tweet button'(){
        when: 'signed in and at the home page'
        signIn()
        Thread.sleep(1000)

        then: 'user attempting post of too long of a tweet will see inactive tweet button'
        $('#tweetArea').value('11111111112222222222333333333344444444441') //length of 41
        $('#tweetButton').disabled
        Thread.sleep(1000)

        cleanup:
        signOut()
    }
}