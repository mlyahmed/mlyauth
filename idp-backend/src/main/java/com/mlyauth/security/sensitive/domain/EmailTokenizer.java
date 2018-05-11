package com.mlyauth.security.sensitive.domain;

import static org.apache.commons.lang3.StringUtils.rightPad;

public class EmailTokenizer {

    public static EmailTokenizer newInstance(){
        return new EmailTokenizer();
    }

    public String tokenizeEmailAddress(String value) {
        final String plain = getUsername(value).substring(0, getUsername(value).length() / 3);
        return rightPad(plain, getUsername(value).length(), '*')  + "@" + getDomain(value);
    }

    private String getUsername(Object emailAddress){
        return splitEmail(emailAddress)[0];
    }

    private String getDomain(Object emailAddress){
        return splitEmail(emailAddress)[1];
    }

    private String[] splitEmail(Object emailAddress) {
        return emailAddress.toString().split("@");
    }


}
