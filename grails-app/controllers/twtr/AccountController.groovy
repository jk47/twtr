package twtr

import grails.rest.RestfulController

class AccountController extends RestfulController<Account>{
    static allowedMethods = [update: "PUT", show: "GET", save: "POST", delete: "DELETE"]
    static responseFormats = ['json']

    AccountController(){
        super(Account)
    }


}
