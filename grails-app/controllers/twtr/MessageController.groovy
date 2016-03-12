package twtr

import grails.converters.JSON
import grails.rest.RestfulController

class MessageController extends RestfulController<Message>{
    //static allowedMethods = [update: "PUT", show: "GET", save: "POST", delete: "DELETE"]
    static responseFormats = ['json']

    MessageController(){
        super(Message)
    }

    @Override
    protected Message queryForResource(Serializable id) {
        def accountId = params.accountId
        Message.where {
            id == id && account.id == accountId
        }.find()
    }

    def index()
    {
        def accountId = params.accountId
        Account account = Account.get(accountId)
        if (account == null) {
            render status: 404
            return
        }

        def messages = Message.where {
            account.id == accountId
        }.findAll()

        render(messages as JSON)
    }
}
