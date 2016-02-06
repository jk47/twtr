package twtr

class Account {

    //Account has a valid handle, email, password and name
    String handle
    String email
    String password
    String realName
    Date dateCreated
    Date lastUpdated

    static hasMany = [ following : Account ]

    static constraints = {
        handle unique: true
        email unique: true
        password size: 8..16, blank: false, nullable: false, validator: { password ->
            password ==~ /.*([0-9]).*/ && password ==~ /.*([a-z]).*/ && password ==~ /.*([A-Z]).*/
        }
    }
}

