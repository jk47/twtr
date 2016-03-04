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

        // Rest Service API
        "/api/accounts"(resources: 'account'){
            "/messages"(resources: "message")
            // example: GET	/books/${bookId}/authors/create	create
            // example: POST	/books/${bookId}/authors	save
        }
    }
}
