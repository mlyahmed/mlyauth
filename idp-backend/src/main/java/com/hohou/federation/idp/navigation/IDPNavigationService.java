package com.hohou.federation.idp.navigation;

import com.hohou.federation.idp.constants.AspectType;

public interface IDPNavigationService {

    NavigationBean newNavigation(String appname);

    AspectType getSupportedAspect();

}
