package com.mlyauth.navigation;

import com.mlyauth.beans.NavigationBean;
import com.mlyauth.constants.AspectType;

public interface IDPNavigationService {

    NavigationBean newNavigation(String appname);

    AspectType getSupportedAspect();

}
