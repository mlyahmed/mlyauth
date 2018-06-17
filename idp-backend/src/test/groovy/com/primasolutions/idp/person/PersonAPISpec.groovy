package com.primasolutions.idp.person

import com.fasterxml.jackson.databind.ObjectMapper
import com.primasolutions.idp.SpecificationsConfig
import com.primasolutions.idp.constants.RoleCode
import com.primasolutions.idp.person.model.PersonBean
import com.primasolutions.idp.tools.AccessTokenForTests
import com.primasolutions.idp.tools.RandomForTests
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

import javax.servlet.Filter

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post

class PersonAPISpec extends SpecificationsConfig {

    @Autowired
    AccessTokenForTests accessTokenGenerator

    @Autowired
    WebApplicationContext context

    @Autowired
    Filter springSecurityFilterChain

    @Autowired
    ObjectMapper mapper

    MockMvc mockMvc

    String access_token


    @BeforeEach
    def setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).addFilters(springSecurityFilterChain).build();
        access_token = accessTokenGenerator.generateMasterToken()
    }

    def "Person creation throw POST on /domain/person"() {
        given: 'a new person to create'
        def person = PersonBean.newInstance()
                .setRole(RoleCode.CLIENT.getValue())
                .setExternalId(RandomForTests.randomString())
                .setFirstname(RandomForTests.randomName().firstName)
                .setLastname(RandomForTests.randomName().lastName)
                .setBirthdate(RandomForTests.randomBirthdate())
                .setEmail(RandomForTests.randomEmail())

        when: 'POST person to be created'
        def result = mockMvc.perform(post("/domain/person")
                .content(mapper.writeValueAsString(person))
                .header("Authorization", "Bearer " + access_token)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("UTF-8"))
                .andReturn().response

        then: 'he is created'
        result.status == HttpStatus.CREATED.value()
    }

}
