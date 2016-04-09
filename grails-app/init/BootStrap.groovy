import grails.converters.JSON
import java.text.SimpleDateFormat
import twtr.Account
import twtr.Role
import twtr.AccountRole
import twtr.Message


class BootStrap {

    def init = { servletContext ->

        def admin = new Account(handle: 'admin', username: 'admin', password: 'R00tPass!', email: 'admin@gmail.com', realName: 'admin').save(flush: true, failOnError: true)
        def role = new Role(authority: 'ROLE_READ').save(flush: true, failOnError: true)
        new AccountRole(account: admin, role: role).save(flush: true, failOnError: true)

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
