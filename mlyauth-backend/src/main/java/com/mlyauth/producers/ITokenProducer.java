package com.mlyauth.producers;

import com.mlyauth.domain.Application;
import com.mlyauth.token.IDPToken;

public interface ITokenProducer {
    IDPToken produce(Application app);
}

