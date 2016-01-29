package twtr

class Account {

    //Account has a valid handle, email, password and name
    String handle
    String email
    String password
    String realName
    Date dateCreated
    Date lastUpdated

    static constraints = {
        handle blank: false, unique: true
        email  blank: false
    }
}
