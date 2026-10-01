package org.starbornag.api.rest.bed

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

/**
 * Every address under /beds answers a browser with the page and an agent with HAL, so caches must
 * keep the views apart by the headers that choose them. Set here, before the controller runs,
 * because Spring drops a Vary set on a ResponseEntity once CORS has written its own.
 */
@Component
class VaryByView : OncePerRequestFilter() {
    override fun shouldNotFilter(request: HttpServletRequest) = !request.requestURI.startsWith("/beds/")

    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, chain: FilterChain) {
        response.addHeader(HttpHeaders.VARY, "${HttpHeaders.ACCEPT}, ${Asking.HX_REQUEST}")
        chain.doFilter(request, response)
    }
}
