package twtr

class Message {
    String content
    Date dateCreated

    static constraints = {
        content blank: false, size: 0..40
    }
    static belongsTo = [ account : Account ]

}
