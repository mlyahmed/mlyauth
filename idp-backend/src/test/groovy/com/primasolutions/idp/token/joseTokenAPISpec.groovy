package com.primasolutions.idp.token

import spock.lang.Specification

import static spock.util.matcher.HamcrestMatchers.closeTo

class joseTokenAPISpec extends Specification {

    def "comparing two decimal numbers"() {
        def myPi = 3.14

        expect:
        myPi closeTo(Math.PI, 0.01)
    }

}
