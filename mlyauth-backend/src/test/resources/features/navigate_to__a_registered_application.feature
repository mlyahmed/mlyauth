Feature: Navigate to a registered application

Background: context
  Given Ahmed, EL IDRISSI is a new person with ahmed as username and ahmed.elidrissi@gmail.com as email
  Given Policy is a registered Application

Scenario: A Connected user can navigate to a registered application
  Given Policy has the basic authentication aspect
  Given Policy is asigned to ahmed
  When ahmed navigates to Policy
  Then ahmed is posted to Policy

Scenario: A connected to user couldn't navigate to a registered application if he is not assigned to
  Given Policy is not asigned to ahmed
  When ahmed navigates to Policy
  Then APP_NOT_ASSIGNED error

