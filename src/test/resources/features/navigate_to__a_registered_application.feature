Feature: Navigate to a registered application

Background: context
  Given Moulay Ahmed, EL IDRISSI ATTACH is a new person withe mlyahmed as username and ahmed.elidrissi.attach@gmail.com as email
  Given Policy is a registered Application

Scenario: A Connected user can navigate to a registered application
  Given Policy has the basic authentication aspect
  When mlyahmed navigates to Policy
  Then mlyahmed is connected to Policy


Scenario: A connected to user couldn't navigate to a registered application if he is not assigned to
  Given Policy is not asigned to ahmed
  When ahmed navigates to Policy
  Then APP_NOT_ASSIGNED error




