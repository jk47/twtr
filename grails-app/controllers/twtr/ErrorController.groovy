package twtr

class ErrorController {

    def internalServerError() {
        response.status = 500
        render(contentType: 'application/json') {
            error = response.status
            message = 'Internal server error'
        }
    }

    def notFound() {
        response.status = 404
        render(contentType: 'application/json') {
            error = response.status
            message = 'Not found'
        }
    }

    def unauthorized() {
        response.status = 401
        render(contentType: 'application/json') {
            error = response.status
            message = 'Unauthorized'
        }
    }

    def forbidden() {
        response.status = 403
        render(contentType: 'application/json') {
            error = response.status
            message = 'Forbidden'
        }
    }
}