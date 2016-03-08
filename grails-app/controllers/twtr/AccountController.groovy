package twtr

import grails.converters.JSON
import grails.rest.RestfulController

class AccountController extends RestfulController<Account>{
    static responseFormats = ['json']

    AccountController(){
        super(Account)
    }

    def follow(){
        def currentAccount = Account.findById(params.id)
        currentAccount.addToFollowing(Account.findById(params.accountToFollow))
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




