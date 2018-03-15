package com.mlyauth.token;

import com.mlyauth.domain.Application;

public interface ITokenProducer {
    IToken produce(Application app);
}

