package com.hohou.federation.idp.sensitive;

import org.springframework.util.Assert;

import static org.apache.commons.lang3.StringUtils.rightPad;

public class EmailTokenizer {

    public static EmailTokenizer newInstance() {
        return new EmailTokenizer();
    }

    public String tokenizeEmailAddress(final String value) {
        Assert.notNull(value, "email value mut not be null.");
        final String plain = getUsername(value).substring(0, getUsername(value).length() / 3);
        return rightPad(plain, getUsername(value).length(), '*')  + "@" + getDomain(value);
    }

    private String getUsername(final Object emailAddress) {
        return splitEmail(emailAddress)[0];
    }

    private String getDomain(final Object emailAddress) {
        return splitEmail(emailAddress)[1];
    }

    private String[] splitEmail(final Object emailAddress) {
        return emailAddress.toString().split("@");
    }


}
