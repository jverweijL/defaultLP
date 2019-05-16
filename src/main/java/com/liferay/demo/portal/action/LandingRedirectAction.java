package com.liferay.demo.portal.action;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.liferay.expando.kernel.model.ExpandoBridge;
import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.kernel.security.permission.PermissionCheckerFactoryUtil;
import com.liferay.portal.kernel.security.permission.PermissionThreadLocal;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.liferay.portal.kernel.events.ActionException;
import com.liferay.portal.kernel.events.LifecycleAction;
import com.liferay.portal.kernel.events.LifecycleEvent;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.Organization;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.struts.LastPath;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.PrefsPropsUtil;
import com.liferay.portal.kernel.util.PropsKeys;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.Validator;


@Component(
		immediate = true, 
		property = {"key=login.events.post"},
	service = LifecycleAction.class
 )

public class LandingRedirectAction implements LifecycleAction {
	
	@Reference
	UserLocalService userlocalService;

	private String PUBLIC_PAGE_CONTEXT = "/web";
	private String PRIVATE_PAGE_CONTEXT = "/group";
	
	 @Override
	 public void processLifecycleEvent(LifecycleEvent lifecycleEvent) throws ActionException {
		 System.out.println("The login event post action is running");
		 HttpServletRequest request = lifecycleEvent.getRequest();
		 HttpSession session = request.getSession();
		 User user;
			try {
				user = PortalUtil.getUser(request);

				String path="";

				List<Organization> orgs;

				try {

					ExpandoBridge expandoBridge = user.getExpandoBridge();
					PermissionChecker checker = PermissionCheckerFactoryUtil.create(user);
					PermissionThreadLocal.setPermissionChecker(checker);

					Map<String,Serializable> attributeNameValuePair =  expandoBridge.getAttributes();

					Long defaultSite = new Long(0);
					if (attributeNameValuePair.containsKey("default-site")) {
						defaultSite = Long.parseLong(expandoBridge.getAttribute("default-site").toString());
					}
					System.out.println("default site: " + defaultSite);

					path = getOrganizationPage(user, defaultSite);

				    //Sites the user has access to
				    if(Validator.isNull(path)) {
				        path = this.getSitePage(user, defaultSite);
				    }
				      
				    //Default landing page to the main instance site
				    if(Validator.isNull(path)) {
				        path = PrefsPropsUtil.getString(PortalUtil.getCompanyId(request), PropsKeys.DEFAULT_LANDING_PAGE_PATH);
				    }
				     
				    session.setAttribute("LAST_PATH", new LastPath(StringPool.BLANK, path));
					 
				} catch (PortalException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}
			} catch (PortalException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
	 }

	private String getOrganizationPage(User user, Long groupId) throws PortalException {
		String path = "";
		List<Organization> orgs;
		orgs = user.getOrganizations();
		if (orgs != null && !orgs.isEmpty()) {
			for (Organization org : orgs) {
				Group orgSite = org.getGroup();
				int publicPageCount = orgSite.getPublicLayoutsPageCount();
				int privatePageCount = orgSite.getPrivateLayoutsPageCount();
				if (publicPageCount > 0) {
					path = PUBLIC_PAGE_CONTEXT + orgSite.getFriendlyURL();
					if (groupId < 1 || groupId.equals(orgSite.getGroupId()) ) {
						break;
					}
				} else if (privatePageCount > 0) {
					path = PRIVATE_PAGE_CONTEXT + orgSite.getFriendlyURL();
					if (groupId < 1 || groupId.equals(orgSite.getGroupId())) {
						break;
					}
				}
			}
		}
		return path;
	}

	private String getSitePage(User user, Long groupId) {
	 	// if groupId > 0 check for specific site
	 	String path = "";
		 List<Group> sites = user.getGroups();
		 if(sites != null && !sites.isEmpty()) {
			 for(Group site : sites) {
				 int publicPageCount = site.getPublicLayoutsPageCount();
				 int privatePageCount = site.getPrivateLayoutsPageCount();
				 if(publicPageCount > 0) {
					 path = PUBLIC_PAGE_CONTEXT + site.getFriendlyURL();
					 if (groupId < 1 || groupId.equals(site.getGroupId())) {
						 break;
					 }
				 } else if(privatePageCount > 0) {
					 path = PRIVATE_PAGE_CONTEXT + site.getFriendlyURL();
					 if (groupId < 1 || groupId.equals(site.getGroupId())) {
						 break;
					 }
				 }
			 }
		 }

		 return path;
	 }
}
	    
	
	
	 