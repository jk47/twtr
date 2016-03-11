package twtr

import grails.converters.JSON
import grails.rest.RestfulController
import groovy.json.JsonSlurper
import grails.transaction.Transactional

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

    @Override
    @Transactional
    def delete() {
        if (handleReadOnly()) {
            return
        }

        Account account = queryForResource(params.id)
        if (account == null) {
            transactionStatus.setRollbackOnly()
            notFound()
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

}




