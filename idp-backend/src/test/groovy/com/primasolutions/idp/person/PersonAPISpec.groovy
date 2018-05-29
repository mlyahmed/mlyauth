package com.primasolutions.idp.person

import com.fasterxml.jackson.databind.ObjectMapper
import com.primasolutions.idp.SpecificationsConfig
import com.primasolutions.idp.constants.RoleCode
import com.primasolutions.idp.person.model.PersonBean
import com.primasolutions.idp.tools.AccessTokenForTests
import com.primasolutions.idp.tools.RandomForTests
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

import javax.servlet.Filter

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

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

    ResultActions result

    String access_token

    PersonBean person


    @BeforeEach
    def setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).addFilters(springSecurityFilterChain).build();
        access_token = accessTokenGenerator.generateMasterToken()
    }

    def "Person creation throw POST on /domain/person"() {
        given:
            new_person()
        when:
            create_the_new_person()
        then:
            the_person_is_created()
    }

    def new_person() {
        person =  PersonBean.newInstance()
        .setRole(RoleCode.CLIENT.getValue())
        .setExternalId(RandomForTests.randomString())
        .setFirstname(RandomForTests.randomName().firstName)
        .setLastname(RandomForTests.randomName().lastName)
        .setBirthdate(RandomForTests.randomBirthdate())
        .setEmail(RandomForTests.randomEmail())
    }

    def create_the_new_person() {
        result = mockMvc.perform(post("/domain/person")
                .content(mapper.writeValueAsString(person))
                .header("Authorization", "Bearer " + access_token)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("UTF-8"))
    }

    def the_person_is_created() {
        result.andExpect(status().isCreated())
    }


}
