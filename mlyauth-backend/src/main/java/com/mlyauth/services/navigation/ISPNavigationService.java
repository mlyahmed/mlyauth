package com.mlyauth.services.navigation;

import com.mlyauth.beans.NavigationBean;
import com.mlyauth.constants.AspectType;

public interface ISPNavigationService {

    NavigationBean newNavigation(String appname);

    AspectType getSupportedAspect();

}
