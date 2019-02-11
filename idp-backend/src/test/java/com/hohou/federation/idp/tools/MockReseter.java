package com.hohou.federation.idp.tools;

import java.util.LinkedList;
import java.util.List;

public final class MockReseter {

    private MockReseter() {
    }

    private static final List<ResettableMock> MOCKS = new LinkedList<>();

    public static void register(final ResettableMock mock) {
        MOCKS.add(mock);
    }

    public static void resetAllMocks() {
        MOCKS.stream().forEach(mock -> mock.reset());
    }
}
