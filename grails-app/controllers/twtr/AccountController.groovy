package twtr

import grails.converters.JSON
import grails.rest.RestfulController

class AccountController extends RestfulController<Account>{
    static responseFormats = ['json']

    AccountController(){
        super(Account)
    }

    def follow(){
        respond getParams()
        /*def currentAccount = Account.findById(params.id)
        def accountGettingFollowed = Account.get(params.accountToFollow)
        currentAccount.addToFollowing(Account.findById(params.accountToFollow))
        accountGettingFollowed.addToFollowers(currentAccount)*/

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

        if (account) {
            render account as JSON
        }
        else {
            response.status = 404
        }
    }

}




