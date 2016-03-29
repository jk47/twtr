package twtr

class Account {

    transient springSecurityService

    //Account has a valid handle, email, password and name
    String handle
    String username = handle
    String email
    String password
    String realName
    Date dateCreated
    Date lastUpdated
    boolean enabled = true
    boolean accountExpired = false
    boolean accountLocked = false
    boolean passwordExpired = false

    static hasMany = [ following : Account, followers: Account, messages: Message ]

    Set<Role> getAuthorities() {
        AccountRole.findAllByAccount(this)*.role
    }

    def beforeInsert() {
        //encodePassword()
        username = handle // hack for security
    }

    def beforeUpdate() {
        if (isDirty('password')) {
            encodePassword()
        }
    }

    protected void encodePassword() {
        password = springSecurityService?.passwordEncoder ?
                springSecurityService.encodePassword(password) :
                password
    }


    static constraints = {
        handle unique: true, blank: false, nullable: false
        email unique: true, blank:false, nullable: false
        realName blank:false, nullable: false
        username nullable: true// security hack
        following nullable: true, blank: true
        followers nullable: true, blank: true
        messages nullable: true, blank: true
        password size: 8..16, blank: false, nullable: false, validator: { password ->
            password ==~ /.*([0-9]).*/ && password ==~ /.*([a-z]).*/ && password ==~ /.*([A-Z]).*/
        }
    }
}

