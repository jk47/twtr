package twtr


import grails.test.mixin.integration.Integration
import grails.transaction.*
import spock.lang.*

@Integration
@Rollback
class AccountIntegrationSpec extends Specification {

    def setup() {
    }

    def cleanup() {
    }

    def "saving account to database"() {
        given: "an account"
            def account = new Account(handle: 'coding', password:'TestPass1', email: 'test@gmail.com', realName: 'coding guy')
        when: "the account is saved"
            account.save()
        then: "account is saved successfully and can be found in database"
            account.errors.errorCount == 0
            account.id != null
            Account.get(account.id).realName == account.realName
    }

    @Unroll
    def 'saving an account with #description will fail'() {
        given: "2 accounts to save"
            def account = new Account(handle: hndl, password: "Testing123", email: eml, realName: "nameOfGuy")
            def account2 = new Account(handle: hndl2, password: "Testing123", email: eml2, realName: "nameOfGirl")
        when: "attempting to save"
            account.save()
            account2.save()
        then: "the account saved second should have an error, the first account should save"
            account.errors.errorCount == 0
            account.id != null
            Account.get(account.id).realName == account.realName
            account2.errors.errorCount == expected
        where:
            description            | hndl  | hndl2 |       eml        |     eml2        | expected
            'two identical emails' |'abc'  | 'cde' | 'abc@gmail.com'  | 'abc@gmail.com' |   1
            'two identical handles'|'def'  | 'def' | 'def@gmail.com'  | 'ghi@gmail.com' |   1
    }

    def "an account may have multiple followers"(){
        given: "A set of baseline users"
            def joe = new Account(handle: 'joe', password:'Testing123', email:'joe@gmail.com', realName:'joe guy').save()
            def jane = new Account(handle: 'jane', password:'Testing123', email:'jane@gmail.com', realName:'jane girl').save()
            def jill = new Account(handle: 'jill', password:'Testing123',email:'jill@gmail.com', realName:'jill girl').save()
        when: "Joe follows Jane & Jill, and Jill follows Jane"
            joe.addToFollowing(jane)
            joe.addToFollowing(jill)
            jill.addToFollowing(jane)
        then: "Follower counts should match following people"
            2 == joe.following.size()
            1 == jill.following.size()

    }
}
