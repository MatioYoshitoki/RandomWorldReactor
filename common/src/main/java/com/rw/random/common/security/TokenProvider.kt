package com.rw.random.common.security

import com.rw.random.common.config.RWSecurityProperties
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.util.StringUtils
import java.nio.charset.StandardCharsets
import java.security.Key
import java.util.*
import java.util.stream.Collectors

open class TokenProvider(
    rwSecurityProperties: RWSecurityProperties
) {
    private val log = LoggerFactory.getLogger(TokenProvider::class.java)
    var key: Key? = null
    private var tokenValidityInMilliseconds: Long = 0
    private var tokenValidityInMillisecondsForRememberMe: Long = 0


    init {
        val keyBytes: ByteArray
        val secret: String = rwSecurityProperties.authentication.jwt.secret
        keyBytes = if (!StringUtils.isEmpty(secret)) {
            log.warn(
                "Warning: the JWT key used is not Base64-encoded. " +
                        "We recommend using the `dandan.security.authentication.jwt.base64-secret` key for optimum security."
            )
            secret.toByteArray(StandardCharsets.UTF_8)
        } else {
            log.debug("Using a Base64-encoded JWT secret key")
            Decoders.BASE64.decode(rwSecurityProperties.authentication.jwt.base64Secret)
        }
        val keyString = String(keyBytes)
        log.info("SignKey = $keyString")
        key = Keys.hmacShaKeyFor(keyBytes)
        tokenValidityInMilliseconds = 1000 * rwSecurityProperties.authentication.jwt.tokenValidityInSeconds
        tokenValidityInMillisecondsForRememberMe =
            1000 * rwSecurityProperties.authentication.jwt.tokenValidityInSecondsForRememberMe
    }

    fun createToken(authentication: Authentication, rememberMe: Boolean): String {
        val authorities: String = authentication.authorities.stream()
            .map { it.authority }
            .collect(Collectors.joining(","))
        val now = Date().time
        val validity = if (rememberMe) {
            Date(now + tokenValidityInMillisecondsForRememberMe)
        } else {
            Date(now + tokenValidityInMilliseconds)
        }
        return Jwts.builder()
            .setSubject(authentication.name)
            .claim(AUTHORITIES_KEY, authorities)
            .signWith(key, SignatureAlgorithm.HS256)
            .setExpiration(validity)
            .compact()
    }

    fun getAuthentication(token: String?): Authentication {
        val claims = Jwts.parser()
            .setSigningKey(key)
            .parseClaimsJws(token)
            .body
        val authorities: Collection<GrantedAuthority> = claims[AUTHORITIES_KEY].toString().split(",")
            .map { SimpleGrantedAuthority(it) }
        val principal = User(claims.subject, "", authorities)
        return UsernamePasswordAuthenticationToken(principal, token, authorities)
    }

    fun validateToken(authToken: String?): Boolean {
        try {
            Jwts.parser().setSigningKey(key).parseClaimsJws(authToken)
            return true
        } catch (e: JwtException) {
            log.warn("Invalid JWT token, " + e.message)
        } catch (e: IllegalArgumentException) {
            log.warn("Invalid JWT token, " + e.message)
        }
        return false
    }

    companion object {
        private const val AUTHORITIES_KEY = "auth"
    }
}
