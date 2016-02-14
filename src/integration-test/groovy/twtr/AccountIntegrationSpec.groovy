package twtr

import grails.test.mixin.integration.Integration
import grails.transaction.*
import spock.lang.*

@Integration
@Rollback
@Unroll
class AccountIntegrationSpec extends Specification {

    def setup() {
    }

    def cleanup() {
    }

    def 'saving an account with #description will fail'() {
        given: "2 accounts to save"
            def account = new Account(handle: hndl, password: "Testing123", email: eml, realName: "nameOfGuy")
            def account2 = new Account(handle: hndl2, password: "Testing123", email: eml2, realName: "nameOfGirl")
        when: "attempting to save"
            account.save()
            def accountsBefore = Account.count()
            account2.save()
        then: "the account saved second should have an error, the first account should save"
            account.errors.errorCount == 0
            account.id != null
            Account.get(account.id).realName == account.realName
            account2.errors.errorCount == expectedErrors
            Account.get(account2.id) == null
            accountsBefore == accountsAfter
        where:
            description            | hndl  | hndl2 |       eml        |     eml2        | expectedErrors    | accountsAfter
            "two identical emails" |'abc'  | 'cde' | 'abc@gmail.com'  | 'abc@gmail.com' |   1               |   1
            "two identical handles"|'def'  | 'def' | 'def@gmail.com'  | 'ghi@gmail.com' |   1               |   1
    }

    def "an account may have multiple followers"(){
        given: "A set of baseline users"
            def joe = new Account(handle: 'joe', password:'Testing123', email:'joe@gmail.com', realName:'joe guy').save()
            def jane = new Account(handle: 'jane', password:'Testing123', email:'jane@gmail.com', realName:'jane girl').save()
            def jill = new Account(handle: 'jill', password:'Testing123',email:'jill@gmail.com', realName:'jill girl').save()

        when: "Joe follows Jane & Jill, and Jill follows Jane"
            // joe follows jane
            joe.addToFollowing(jane)
            jane.addToFollowers(joe)

            // joe follows jill
            joe.addToFollowing(jill)
            jill.addToFollowers(joe)

            // jill follows jane
            jill.addToFollowing(jane)
            jane.addToFollowers(jill)

        then: "Follower counts should match following people"
            2 == joe.following.size()
            1 == jill.following.size()
            2 == jane.followers.size()
            1 == jill.followers.size()

    }

    def "two accounts may follow each other"(){
        given: "A set of baseline users"
        def joe = new Account(handle: 'joe', password:'Testing123', email:'joe@gmail.com', realName:'joe guy').save()
        def jane = new Account(handle: 'jane', password:'Testing123', email:'jane@gmail.com', realName:'jane girl').save()

        when: "Joe follows Jane & Jane follows Joe"
        // joe follows jane
        joe.addToFollowing(jane)
        jane.addToFollowers(joe)

        // jane follows joe
        jane.addToFollowing(joe)
        joe.addToFollowers(jane)
        joe.save()
        jane.save()

        then: "jane should be following joe  and be followed by joe. joe should be following jane and followed by jane"
        joe.followers.find { it.id == jane.id }
        jane.followers.find { it.id == joe.id }
        joe.following.find { it.id == jane.id }
        jane.following.find { it.id == joe.id }
    }
}
