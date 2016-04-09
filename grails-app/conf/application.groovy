grails.plugin.springsecurity.filterChain.chainMap = [

        //Stateless chain
        [
                pattern: '/api/**',
                filters: 'JOINED_FILTERS,-anonymousAuthenticationFilter,-exceptionTranslationFilter,-authenticationProcessingFilter,-securityContextPersistenceFilter,-rememberMeAuthenticationFilter'
        ]

]

grails.plugin.springsecurity.rest.token.storage.useGorm = true
grails.plugin.springsecurity.rest.token.storage.gorm.tokenDomainClassName = 'twtr.AuthenticationToken'
grails.plugin.springsecurity.rest.token.validation.useBearerToken = false
grails.plugin.springsecurity.rest.token.validation.headerName = 'X-Auth-Token'

grails.plugin.springsecurity.userLookup.userDomainClassName = 'twtr.Account'
grails.plugin.springsecurity.userLookup.authorityJoinClassName = 'twtr.AccountRole'
grails.plugin.springsecurity.authority.className = 'twtr.Role'
grails.plugin.springsecurity.userLookup.usernamePropertyName = 'handle'
grails.plugin.springsecurity.userLookup.passwordPropertyName = 'password'
grails.plugin.springsecurity.securityConfigType = 'InterceptUrlMap'
grails.plugin.springsecurity.interceptUrlMap = [
        [
                [pattern: '/api/**', access: ['ROLE_READ']]
        ]
]