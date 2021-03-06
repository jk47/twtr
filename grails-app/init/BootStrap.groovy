import grails.converters.JSON
import java.text.SimpleDateFormat
import twtr.Account
import twtr.Role
import twtr.AccountRole

class BootStrap {

    def init = { servletContext ->

        def admin = new Account(handle: 'admin', password: 'Password1', email: 'admin@gmail.com', realName: 'admin').save(flush: true, failOnError: true)
        def john = new Account(handle: 'john', password: 'Password1', email: 'john@gmail.com', realName: 'john').save(flush: true, failOnError: true)
        def aragorn = new Account(handle: 'kingofgondor', password: 'TheRanger', email: 'narsil@gmail.com', realName: 'Aragorn').save(flush: true, failOnError: true)
        def role = new Role(authority: 'ROLE_READ').save(flush: true, failOnError: true)
        new AccountRole(account: admin, role: role).save(flush: true, failOnError: true)
        new AccountRole(account: john, role: role).save(flush: true, failOnError: true)
        new AccountRole(account: aragorn, role: role).save(flush: true, failOnError: true)

        john.addToFollowers(aragorn)
        aragorn.addToFollowing(john)

        (1..10).each { id -> john.addToMessages(content: "John's Message #$id").save(flush:true)}
        (1..10).each { id -> admin.addToMessages(content: "Admin's Message #$id").save(flush:true)}
        (1..87).each { id -> aragorn.addToMessages(content: "Aragon's Tweet #$id").save(flush:true)}

        def dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss")

        JSON.registerObjectMarshaller(Account) { Account a ->
            return [id         : a.id,
                    handle     : a.handle,
                    email      : a.email,
                    password   : a.password,
                    realName   : a.realName,
                    dateCreated: a.dateCreated,
                    lastUpdated: a.lastUpdated
            ]
        }


    }
    def destroy = {
    }
}
