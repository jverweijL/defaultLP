# default Landing Page
A login event post processor for Liferay DXP that sends the user to their organisation or site on login instead of the default site. All the logic is in LandingRedirectAction.java

Simply add a user-field with the key 'default-site' of type int/long and assign the siteID.
