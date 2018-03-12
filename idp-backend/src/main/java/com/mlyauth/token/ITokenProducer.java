package com.mlyauth.token;

import com.mlyauth.domain.Application;

public interface ITokenProducer {
    IDPToken produce(Application app);
}

