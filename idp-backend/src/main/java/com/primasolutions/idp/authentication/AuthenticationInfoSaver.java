package com.primasolutions.idp.authentication;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class AuthenticationInfoSaver {

    @Autowired
    protected AuthInfoDAO authInfoDAO;

    @Autowired
    protected AuthInfoByLoginDAO authInfoByLoginDAO;

    public void create(final AuthInfo auth) {
        final AuthInfo authInfo = authInfoDAO.saveAndFlush(auth);
        indexByLogin(authInfo);
    }

    public void update(final AuthInfo authInfo) {
        deleteByLoginIndex(authInfo);
        updateAuthInfo(authInfo);
        indexByLogin(authInfo);
    }

    private void deleteByLoginIndex(final AuthInfo authInfo) {
        final AuthInfo existing = authInfoDAO.getOne(authInfo.getId());
        final Set<AuthInfoByLogin> byLoginSet = authInfoByLoginDAO.findByLogin(existing.getLogin());
        final AuthInfoByLogin target = byLoginSet.stream()
                .filter(l -> l.getAuthInfoId() == authInfo.getId())
                .findFirst().get();
        authInfoByLoginDAO.delete(target.getId());
    }

    private void updateAuthInfo(final AuthInfo authInfo) {
        final AuthInfo existing = authInfoDAO.getOne(authInfo.getId());
        existing.setLogin(authInfo.getLogin());
        authInfoDAO.saveAndFlush(existing);
    }

    private void indexByLogin(final AuthInfo authInfo) {
        authInfoByLoginDAO.saveAndFlush(AuthInfoByLogin.newInstance()
                .setAuthInfoId(authInfo.getId())
                .setLogin(authInfo.getLogin()));
    }

}
