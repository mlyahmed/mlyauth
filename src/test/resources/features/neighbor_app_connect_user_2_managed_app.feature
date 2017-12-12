

Feature: A user can connect to a Co-Application

Scenario: A Connected user can navigate to a Co-Application
  Given mlyahmed is a connected user
  Given Policy is a Co-Application
  When mlyahmed navigates to Policy
  Then mlyahmed is connected to Policy


Scenario: A connected to user couldn't navigate to a Co-Application if he is not assigned to
  Given ahmed is a connected user
  Given Policy is a Co-Application
  Given Policy is not asigned to ahmed
  When ahmed navigates to Policy
  Then APP_NOT_ASSIGNED error




