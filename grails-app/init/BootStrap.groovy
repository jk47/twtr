import grails.converters.JSON

import java.text.SimpleDateFormat
import twtr.*

class BootStrap {

    def init = { servletContext ->
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
