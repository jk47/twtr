package twtr

import grails.converters.JSON
import grails.rest.RestfulController

import java.text.SimpleDateFormat

class MessageController extends RestfulController<Message> {
    //static allowedMethods = [update: "PUT", show: "GET", save: "POST", delete: "DELETE"]
    static responseFormats = ['json']

    MessageController() {
        super(Message)
    }

    @Override
    protected Message queryForResource(Serializable id) {
        def accountId = params.accountId
        Message.where {
            id == id && account.id == accountId
        }.find()
    }

    def index() {
        def accountId = params.accountId

        if(!accountExists(accountId))
        {
            render status: 404

            return
        }

        def messages = Message.where {
            account.id == accountId
        }.findAll()

        render(messages as JSON)
    }
    def recent() {
        def accountId = params.id
        if(!accountExists(accountId))
        {
            render status: 404

            return
        }

        int messageLimit = params.limit == null ? 10 : Integer.parseInt(params.limit)
        int offset = params.offset == null ? 0 : Integer.parseInt(params.offset)

        respond Message.listOrderByDateCreated(max: messageLimit, order: "desc", offset: offset)
    }

    def accountExists(def id) {
        Account account = Account.get(id)

        if (account == null) {

            return false
        }

        return true
    }
}
