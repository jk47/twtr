package twtr

class Account {

    //Account has a valid handle, email, password and name
    String handle
    String email
    String password
    String realName
    Date dateCreated
    Date lastUpdated

    static hasMany = [ following : Account, followers: Account, messages: Message ]

    static constraints = {
        handle unique: true, blank: false, nullable: false
        email unique: true, blank:false, nullable: false
        realName blank:false, nullable: false
        password size: 8..16, blank: false, nullable: false, validator: { password ->
            password ==~ /.*([0-9]).*/ && password ==~ /.*([a-z]).*/ && password ==~ /.*([A-Z]).*/
        }
    }
}

