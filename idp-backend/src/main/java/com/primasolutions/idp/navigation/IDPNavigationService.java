package com.primasolutions.idp.navigation;

import com.primasolutions.idp.constants.AspectType;

public interface IDPNavigationService {

    NavigationBean newNavigation(String appname);

    AspectType getSupportedAspect();

}
