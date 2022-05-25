package com.rw.random.common.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "random-world.security")
open class RWSecurityProperties(
    val authentication: Authentication = Authentication(),
    val clientAuthorization: ClientAuthorization = ClientAuthorization()
) {

    class Authentication(
        val permitAll: List<String> = mutableListOf(),
        val staticToken: String? = null,
        val jwt: Jwt = Jwt()
    ) {
        class Jwt {
            var secret = "rRrRrAnDoM"
            var base64Secret: String? = null
            var tokenValidityInSeconds: Long = 18000
            var tokenValidityInSecondsForRememberMe: Long = 25920000
        }
    }


    class ClientAuthorization(
        var accessTokenUri: String? = null,
        var tokenServiceId: String? = null,
        var clientId: String? = null,
        var clientSecret: String? = null
    )

}
