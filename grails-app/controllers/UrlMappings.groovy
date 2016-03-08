class UrlMappings {

    static mappings = {
        "/$controller/$action?/$id?(.$format)?"{
            constraints {
                // apply constraints here
            }
        }

        "/"(view:"/index")
        "500"(view:'/error')
        "404"(view:'/notFound')
        // add 400, 422 and others, route to better error messaging json

        // Rest Service API
        "/api/accounts"(resources: "account"){
            "/messages"(resources: "message")
            post "/follow"(controller: "account", action:"follow")
        }



    }
}
