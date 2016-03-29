class UrlMappings {

    static mappings = {
        "/$controller/$action?/$id?(.$format)?"{
            constraints {
                // apply constraints here
            }
        }
        "/api/accounts/${id}/follow/$accountToFollow"(controller: "account", action: "follow", method: "get")
        "/api/accounts/${id}/followers"(controller: "account", action: "followers", method: "get")
        "/api/accounts/${id}/feed"(controller: "account", action: "feed", method: "get")
        "/api/accounts/${id}/messages/recent"(controller: "message", action: "recent", method: "get")

        "/"(view:"/index")
        "500"(controller: 'Error', action: 'internalServerError')
        "404"(controller: 'Error', action: 'notFound')
        "401"(controller: 'Error', action: 'unauthorized')
        "403"(controller: 'Error', action: 'forbidden')

        // Rest Service API
        "/api/accounts"(resources: "account"){
            "/messages"(resources: "message") {}
        }

        "/api/messages"(resources: "message")


        "/api/messages/search"(controller: "message", action: "search", method: "GET")
    }
}
