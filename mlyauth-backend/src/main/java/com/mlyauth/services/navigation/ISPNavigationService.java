package com.mlyauth.services.navigation;

import com.mlyauth.beans.NavigationBean;
import com.mlyauth.constants.AuthAspectType;

public interface ISPNavigationService {

    NavigationBean newNavigation(String appname);

    AuthAspectType getSupportedAspect();

}
