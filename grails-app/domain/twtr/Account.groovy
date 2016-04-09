package twtr

class Account {

    transient springSecurityService

    //Account has a valid handle, email, password and name
    String handle
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
        encodePassword()
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
        following nullable: true, blank: true
        followers nullable: true, blank: true
        messages nullable: true, blank: true
        password blank: false, nullable: false //, validator: { password ->
            //password ==~ /.*([a-z]).*/ && password ==~ /.*([A-Z]).*/ && password ==~ /.*([0-9]).*/
            // took out size constraint for spring
        //}
    }
}

