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

        "/"(view:"/index")
        "500"(view:'/error')
        "404"(view:'/notFound')
        // add 400, 422 and others, route to better error messaging json

        // Rest Service API
        "/api/accounts"(resources: "account"){
            "/messages"(resources: "message") {}
        }
    }
}
