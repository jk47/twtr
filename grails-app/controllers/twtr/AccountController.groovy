package twtr

import grails.converters.JSON
import grails.rest.RestfulController
import groovy.json.JsonSlurper
import grails.transaction.Transactional
import java.text.SimpleDateFormat

class AccountController extends RestfulController<Account>{
    static responseFormats = ['json']

    AccountController(){
        super(Account)
    }

    def follow(){
        //respond getParams()
        def currentAccount = Account.findById(params.id)
        def accountGettingFollowed = Account.get(params.accountToFollow)
        currentAccount.addToFollowing(accountGettingFollowed)
        accountGettingFollowed.addToFollowers(currentAccount)
        currentAccount.save()
        accountGettingFollowed.save()
        render currentAccount.following as JSON
    }

    def unfollow() {
        respond getParams()
    }

    def show() {
        def id
        def handle
        def account

        if (params.id.toString().contains("handle=")){
            response.setContentType('application/json')
            handle = params.id.replace("handle=","")
            account = Account.findByHandle(handle)
        }
        else {
            response.setContentType('application/json')
            account = Account.get(params.id)
        }
        // add followers and following count

        if (account) {
            def slurper = new JsonSlurper()
            def accountJson = (account as JSON).toString()
            def slurpedJson = slurper.parseText(accountJson)
            slurpedJson << [followerCount: "${account.followers.size()}", followingCount: "${account.following.size()}"]
            render slurpedJson as JSON
        }
        else {
            response.status = 404
        }
    }

    def following(){

    }

    def followers(){

        int maximum = params.max == null ? 10 : Integer.parseInt(params.max)
        int offset = params.offset == null ? 0 : Integer.parseInt(params.offset)
        long accountId = Long.parseLong(params.id)

        respond Account.findAll("from Account as a where a.id in (:accounts) order by a.id",
                [accounts: Account.get(accountId).followers*.id], [max: maximum, offset: offset])
    }

    @Override
    @Transactional
    def delete() {
        Account account = Account.get(params.id)
        if (account == null) {
            render status: 404
            return
        }

        account.followers.each { it -> it.removeFromFollowing(account) }
        account.following.each { it -> it.removeFromFollowers(account) }
        account.followers.clear()
        account.following.clear()
        account.messages.clear()
        account.save(flush: true)
        account.delete(flush: true)

        render status: 204
    }

    def feed() {
        int maximum = params.max == null ? 10 : Integer.parseInt(params.max)
        int offset = params.offset == null ? 0 : Integer.parseInt(params.offset)
        def fromDate = params.fromDate
        long accountId = Long.parseLong(params.id)

        def accountIds = Account.get(accountId).following*.id

        respond Message.createCriteria().list(max: maximum, offset: offset) {
            'in'('account', Account.get(accountId).following)
            if (fromDate != null) {
                gte('dateCreated', new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX").parse(fromDate))
            }
            order('dateCreated', 'desc')
        }
    }
}




