package twtr

import grails.rest.RestfulController

class MessageController extends RestfulController<Account>{
    //static allowedMethods = [update: "PUT", show: "GET", save: "POST", delete: "DELETE"]
    static responseFormats = ['json']

    MessageController(){
        super(Message)
    }
}
