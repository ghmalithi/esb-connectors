/**
 * Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * 
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.connector.integration.test.jira;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axis2.context.ConfigurationContext;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.api.clients.localentry.LocalEntriesAdminClient;
import org.wso2.carbon.automation.api.clients.proxy.admin.ProxyServiceAdminClient;
import org.wso2.carbon.automation.api.clients.utils.AuthenticateStub;
import org.wso2.carbon.automation.utils.axis2client.ConfigurationContextProvider;
import org.wso2.carbon.connector.integration.test.common.ConnectorIntegrationUtil;
import org.wso2.carbon.connector.integration.test.common.JiraRestClient;
import org.wso2.carbon.esb.ESBIntegrationTest;
import org.wso2.carbon.mediation.library.stub.MediationLibraryAdminServiceStub;
import org.wso2.carbon.mediation.library.stub.upload.MediationLibraryUploaderStub;

import javax.activation.DataHandler;
import java.net.URL;
import java.util.Properties;

public class JiraConnectorIntegrationTest extends ESBIntegrationTest {

	private static final String CONNECTOR_NAME = "jira";

	private MediationLibraryUploaderStub mediationLibUploadStub = null;

	private MediationLibraryAdminServiceStub adminServiceStub = null;

	private ProxyServiceAdminClient proxyAdmin;

	private String repoLocation = null;

	private String connectorFileName = CONNECTOR_NAME + ".zip";

	private Properties connectorProperties = null;

	private String pathToProxiesDirectory = null;

	private String pathToRequestsDirectory = null;

	private LocalEntriesAdminClient localEntryAdmin = null;

    private JiraRestClient jiraclient;

	@BeforeClass(alwaysRun = true)
	public void setEnvironment() throws Exception {

		super.init();
		ConfigurationContextProvider configurationContextProvider = ConfigurationContextProvider
				.getInstance();
		ConfigurationContext cc = configurationContextProvider
				.getConfigurationContext();

		mediationLibUploadStub = new MediationLibraryUploaderStub(cc,
				esbServer.getBackEndUrl() + "MediationLibraryUploader");
		AuthenticateStub.authenticateStub("admin", "admin",
				mediationLibUploadStub);

		adminServiceStub = new MediationLibraryAdminServiceStub(cc,
				esbServer.getBackEndUrl() + "MediationLibraryAdminService");

		AuthenticateStub.authenticateStub("admin", "admin", adminServiceStub);

		if (System.getProperty("os.name").toLowerCase().contains("windows")) {
			repoLocation = System.getProperty("connector_repo").replace("\\",
					"/");
		} else {
			repoLocation = System.getProperty("connector_repo").replace("/",
					"/");
		}

		proxyAdmin = new ProxyServiceAdminClient(esbServer.getBackEndUrl(),
				esbServer.getSessionCookie());

		ConnectorIntegrationUtil.uploadConnector(repoLocation,
				mediationLibUploadStub, connectorFileName);
		log.info("Sleeping for " + 30000 / 1000
				+ " seconds while waiting for synapse import");
		Thread.sleep(30000);

		adminServiceStub.updateStatus("{org.wso2.carbon.connectors}"
				+ CONNECTOR_NAME, CONNECTOR_NAME, "org.wso2.carbon.connectors",
				"enabled");

		connectorProperties = ConnectorIntegrationUtil
				.getConnectorConfigProperties(CONNECTOR_NAME);

		pathToProxiesDirectory = repoLocation
				+ connectorProperties.getProperty("proxyDirectoryRelativePath");
		pathToRequestsDirectory = repoLocation
				+ connectorProperties
						.getProperty("requestDirectoryRelativePath");

		localEntryAdmin = new LocalEntriesAdminClient(
				esbServer.getBackEndUrl(), esbServer.getSessionCookie());

		final String configKeyFilePath = pathToProxiesDirectory
				+ "configKey.xml";
		OMElement localEntry = AXIOMUtil.stringToOM(String.format(
				ConnectorIntegrationUtil.getFileContent(configKeyFilePath),
				connectorProperties.getProperty("username"),
				connectorProperties.getProperty("password"),
				connectorProperties.getProperty("uri")));
		localEntryAdmin.addLocalEntry(localEntry);
		Thread.sleep(20000);

        jiraclient =new JiraRestClient(connectorProperties.getProperty("resturidirect"));
        jiraclient.setAuthHeader(connectorProperties.getProperty("authheader"));

	}

	@Override
	protected void cleanup() {
		axis2Client.destroy();
	}


/*
    @Test(priority = 1, groups = { "wso2.esb" }, description = "jira [getGroup] method positive.")
    public void testGetGroup() throws Exception {

        String methodName = "getGroup";
        String jsonRequestFilePath = pathToRequestsDirectory
                + methodName +".txt";

        final String jsonString = ConnectorIntegrationUtil
                .getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory
                + methodName + ".xml";

        String p0=connectorProperties.getProperty("groupName");

        String modifiedJsonString = String.format(jsonString,p0); //todo

        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));

        try {
            JSONObject jsonObject = ConnectorIntegrationUtil.sendRequest(
                    getProxyServiceURL(methodName), modifiedJsonString);
            Assert.assertTrue(jsonObject.has("users"),jsonObject.toString());
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }

    }
*/


    @Test(priority = 1, groups = { "wso2.esb" }, description = "jira [addGroupToProjectRole] method positive.") //todo
    public void testAddGroupToProjectRole() throws Exception {

        String methodName = "addGroupToProjectRole";
        String jsonRequestFilePath = pathToRequestsDirectory
                + methodName +".txt"; //todo

        final String jsonString = ConnectorIntegrationUtil
                .getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory
                + methodName + ".xml";

        String p0=connectorProperties.getProperty("validGroupName"); //todo
        String p1=connectorProperties.getProperty("validProjectId");
        String p2=connectorProperties.getProperty("validRoleId");

        String modifiedJsonString = String.format(jsonString,p0,p1,p2); //todo

        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));

        try {
            JSONObject jsonObject = ConnectorIntegrationUtil.sendRequest(
                    getProxyServiceURL(methodName), modifiedJsonString);

            JSONObject response=jiraclient.sendGetRequest("/project/"+p1+"/role/"+p2); //todo
            Assert.assertTrue(response.getJSONObject("response").getJSONArray("actors").toString().contains(p0),response.toString()); //todo
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }

    }


    @Test(priority = 1, groups = { "wso2.esb" }, description = "jira [addGroupToProjectRole] method negative with invalid group.") //todo
    public void testAddGroupToProjectRoleNegative() throws Exception {

        String methodName = "addGroupToProjectRole";
        String jsonRequestFilePath = pathToRequestsDirectory
                + methodName +".txt"; //todo

        final String jsonString = ConnectorIntegrationUtil
                .getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory
                + methodName + ".xml"; //todo

        String p0=connectorProperties.getProperty("invalidGroupName"); //todo
        String p1=connectorProperties.getProperty("validProjectId");
        String p2=connectorProperties.getProperty("validRoleId");

        String modifiedJsonString = String.format(jsonString,p0,p1,p2); //todo

        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));

        try {

            String statusCode = ConnectorIntegrationUtil
                    .sendRequestToRetriveHeaders(
                            getProxyServiceURL(methodName), modifiedJsonString) + "";

            JSONObject response = jiraclient.sendPostRequest("/project/" + p1 + "/role/" + p2, "{\"group\":[\"" + p0 + "\"]}");;//todo
            String directStatusCode =response.getString("httpsc");
            Assert.assertEquals(statusCode, directStatusCode);

        } finally {
            proxyAdmin.deleteProxy(methodName);
        }

    }


    @Test(priority = 1, groups = { "wso2.esb" }, description = "jira [addUserToGroup] method positive.")
    public void testAddUserToGroup() throws Exception {

        String methodName = "addUserToGroup";
        String jsonRequestFilePath = pathToRequestsDirectory
                + methodName +".txt";

        final String jsonString = ConnectorIntegrationUtil
                .getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory
                + methodName + ".xml";

        String p0=connectorProperties.getProperty("validGroupName");
        String p1=connectorProperties.getProperty("validUserName");

        String modifiedJsonString = String.format(jsonString,p0,p1);

        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));

        try {
            JSONObject jsonObject = ConnectorIntegrationUtil.sendRequest(
                    getProxyServiceURL(methodName), modifiedJsonString);

            JSONObject response=jiraclient.sendGetRequest("/group?groupname="+ p0 +"&expand=users[0:50]");

            Assert.assertTrue(response.getJSONObject("response").getJSONObject("users").toString().contains(p1));

        } finally {
            proxyAdmin.deleteProxy(methodName);
        }

    }


    @Test(priority = 1, groups = { "wso2.esb" }, description = "jira [addUserToGroup] method negative with invalid user.")
    public void testAddUserToGroupNegative() throws Exception {

        String methodName = "addUserToGroup";
        String jsonRequestFilePath = pathToRequestsDirectory
                + methodName +".txt";

        final String jsonString = ConnectorIntegrationUtil
                .getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory
                + methodName + ".xml";

        String p0=connectorProperties.getProperty("validGroupName");
        String p1=connectorProperties.getProperty("invalidUserName");

        String modifiedJsonString = String.format(jsonString,p0,p1);

        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));

        try {

            String statusCode = ConnectorIntegrationUtil
                    .sendRequestToRetriveHeaders(
                            getProxyServiceURL(methodName), modifiedJsonString) + "";

            JSONObject response=jiraclient.sendPostRequest("/group/user?groupname=" + p0, "{\"name\":\"" + p1 + "\"}");
            String directStatusCode =response.getString("httpsc");
            Assert.assertEquals(statusCode, directStatusCode);

        } finally {
            proxyAdmin.deleteProxy(methodName);
        }

    }



    @Test(priority = 1, groups = { "wso2.esb" }, description = "jira [addUserToProjectRole] method positive.")
    public void testAddUserToProjectRole() throws Exception {

        String methodName = "addUserToProjectRole";
        String jsonRequestFilePath = pathToRequestsDirectory
                + methodName +".txt";

        final String jsonString = ConnectorIntegrationUtil
                .getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory
                + methodName + ".xml";

        String p0=connectorProperties.getProperty("validUserName");
        String p1=connectorProperties.getProperty("validProjectId");
        String p2=connectorProperties.getProperty("validRoleId");

        String modifiedJsonString = String.format(jsonString,p0,p1,p2);

        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));

        try {
            JSONObject jsonObject = ConnectorIntegrationUtil.sendRequest(
                    getProxyServiceURL(methodName), modifiedJsonString);

            JSONObject response=jiraclient.sendGetRequest("/project/"+p1+"/role/"+p2);
            Assert.assertTrue(response.getJSONObject("response").getJSONArray("actors").toString().contains(p0),response.toString());
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }

    }


    @Test(priority = 1, groups = { "wso2.esb" }, description = "jira [addUserToProjectRole] method negative with invalid user.")
    public void testAddUserToProjectRoleNegative() throws Exception {

        String methodName = "addUserToProjectRole";
        String jsonRequestFilePath = pathToRequestsDirectory
                + methodName +".txt";

        final String jsonString = ConnectorIntegrationUtil
                .getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory
                + methodName + ".xml";

        String p0=connectorProperties.getProperty("invalidUserName");
        String p1=connectorProperties.getProperty("validProjectId");
        String p2=connectorProperties.getProperty("validRoleId");

        String modifiedJsonString = String.format(jsonString,p0,p1,p2);

        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));

        try {

            String statusCode = ConnectorIntegrationUtil
                    .sendRequestToRetriveHeaders(
                            getProxyServiceURL(methodName), modifiedJsonString) + "";

            JSONObject response = jiraclient.sendPostRequest("/project/" + p1 + "/role/" + p2, "{\"user\":[\"" + p0 + "\"]}");
            String directStatusCode =response.getString("httpsc");
            Assert.assertEquals(statusCode, directStatusCode);

        } finally {
            proxyAdmin.deleteProxy(methodName);
        }

    }


    @Test(priority = 1, groups = { "wso2.esb" }, description = "jira [createGroup] method positive.")
    public void testCreateGroup() throws Exception {

        String methodName = "createGroup";
        String jsonRequestFilePath = pathToRequestsDirectory
                + methodName +".txt";

        final String jsonString = ConnectorIntegrationUtil
                .getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory
                + methodName + ".xml";

        String p0=connectorProperties.getProperty("createGroupName");

        String modifiedJsonString = String.format(jsonString,p0);

        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));

        try {
            JSONObject jsonObject = ConnectorIntegrationUtil.sendRequest(
                    getProxyServiceURL(methodName), modifiedJsonString);

            JSONObject response=jiraclient.sendGetRequest("/group?groupname="+p0);
            Assert.assertTrue(response.getJSONObject("response").getString("name").equals(p0),response.toString());
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }

    }

    @Test(priority = 1, groups = { "wso2.esb" }, description = "jira [createGroup] method negative with existing group.")
    public void testCreateGroupNegative() throws Exception {

        String methodName = "createGroup";
        String jsonRequestFilePath = pathToRequestsDirectory
                + methodName +".txt";

        final String jsonString = ConnectorIntegrationUtil
                .getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory
                + methodName + ".xml";

        String p0=connectorProperties.getProperty("createGroupName2");

        String modifiedJsonString = String.format(jsonString,p0);

        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));

        try {

            String statusCode = ConnectorIntegrationUtil
                    .sendRequestToRetriveHeaders(
                            getProxyServiceURL(methodName), modifiedJsonString) + "";
            JSONObject response=jiraclient.sendPostRequest("/group", "{\"name\":\"" + p0 + "\"}");
            String directStatusCode=response.getString("httpsc");

            Assert.assertEquals(statusCode, directStatusCode);
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }

/*
    @Test(priority = 1, groups = { "wso2.esb" }, description = "jira [createUser] method positive.")
    public void testCreateUser() throws Exception {

        String methodName = "createUser";
        String jsonRequestFilePath = pathToRequestsDirectory
                + methodName +".txt";

        final String jsonString = ConnectorIntegrationUtil
                .getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory
                + methodName + ".xml";

        String p0=connectorProperties.getProperty("createUsername");
        String p1=connectorProperties.getProperty("createUserPassword");
        String p2=connectorProperties.getProperty("createUserEmail");
        String p3=connectorProperties.getProperty("createUserDisplayName");

        String modifiedJsonString = String.format(jsonString,p0,p1,p2,p3);

        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));

        try {
            ConnectorIntegrationUtil.sendRequestToRetriveHeaders(
                    getProxyServiceURL(methodName), modifiedJsonString);

            JSONObject response=jiraclient.sendGetRequest("/user?username=" + p0 );
            Assert.assertTrue(response.getJSONObject("response").getString("name").equals(p0),response.toString());
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }

    }


    @Test(priority = 1, groups = { "wso2.esb" }, description = "jira [createUser] method negative with existing user.")
    public void testCreateUserNegative() throws Exception {

        String methodName = "createUser";
        String jsonRequestFilePath = pathToRequestsDirectory
                + methodName +".txt";

        final String jsonString = ConnectorIntegrationUtil
                .getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory
                + methodName + ".xml";

        String p0=connectorProperties.getProperty("createUsername2");
        String p1=connectorProperties.getProperty("createUserPassword2");
        String p2=connectorProperties.getProperty("createUserEmail2");
        String p3=connectorProperties.getProperty("createUserDisplayName2");

        String modifiedJsonString = String.format(jsonString,p0,p1,p2,p3);

        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));

        try {

            String statusCode = ConnectorIntegrationUtil
                    .sendRequestToRetriveHeaders(
                            getProxyServiceURL(methodName), modifiedJsonString) + "";
            JSONObject response=jiraclient.sendPostRequest("/user", " {\"name\": \"" + p0 + "\",\"password\": \"" + p1 + "\",\"emailAddress\":\"" + p2 + "\",\"displayName\": \"" + p3 + "\"}");
            String directStatusCode =response.getString("httpsc");
            Assert.assertEquals(statusCode, directStatusCode);

        } finally {
            proxyAdmin.deleteProxy(methodName);
        }

    }
*/


    @Test(priority = 1, groups = { "wso2.esb" }, description = "jira [deleteGroup] method positive.")
    public void testDeleteGroup() throws Exception {

        String methodName = "deleteGroup";
        String jsonRequestFilePath = pathToRequestsDirectory
                + methodName +".txt";

        final String jsonString = ConnectorIntegrationUtil
                .getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory
                + methodName + ".xml";


        String p0=connectorProperties.getProperty("deleteGroupName");
        String p1=connectorProperties.getProperty("deleteGroupSwapGroup");

        String modifiedJsonString = String.format(jsonString,p0,p1);

        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));

        try {
            JSONObject response=jiraclient.sendGetRequest("/group?groupname="+p0);
            Assert.assertEquals(response.getString("httpsc"),"200");
            JSONObject jsonObject = ConnectorIntegrationUtil.sendRequest(
                    getProxyServiceURL(methodName), modifiedJsonString);
            response=jiraclient.sendGetRequest("/group?groupname="+p0);
            Assert.assertEquals(response.getString("httpsc"), "404");
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }

    }

    @Test(priority = 1, groups = { "wso2.esb" }, description = "jira [deleteGroup] method negative with invalid group.")
    public void testDeleteGroupNegative() throws Exception {

        String methodName = "deleteGroup";
        String jsonRequestFilePath = pathToRequestsDirectory
                + methodName +".txt";

        final String jsonString = ConnectorIntegrationUtil
                .getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory
                + methodName + ".xml";

        String p0=connectorProperties.getProperty("invalidGroupName");
        String p1=connectorProperties.getProperty("deleteGroupSwapGroup");

        String modifiedJsonString = String.format(jsonString,p0,p1);

        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));

        try {
            String statusCode = ConnectorIntegrationUtil
                    .sendRequestToRetriveHeaders(
                            getProxyServiceURL(methodName), modifiedJsonString) + "";

            JSONObject  response=jiraclient.sendDeleteRequest("/group?groupname=" + p0 + "&swapGroup=" + p1);
            String directStatusCode =response.getString("httpsc");
            Assert.assertEquals(statusCode, directStatusCode);

        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }



    @Test(priority = 1, groups = { "wso2.esb" }, description = "jira [deleteUser] method positive.")
    public void testDeleteUser() throws Exception {

        String methodName = "deleteUser";
        String jsonRequestFilePath = pathToRequestsDirectory
                + methodName +".txt";

        final String jsonString = ConnectorIntegrationUtil
                .getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory
                + methodName + ".xml";


        String p0=connectorProperties.getProperty("deleteUserName");

        String modifiedJsonString = String.format(jsonString,p0);

        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));

        try {
            JSONObject response=jiraclient.sendGetRequest("/user?username="+p0);
            Assert.assertEquals(response.getString("httpsc"),"200");
            JSONObject jsonObject = ConnectorIntegrationUtil.sendRequest(
                    getProxyServiceURL(methodName), modifiedJsonString);
            response=jiraclient.sendGetRequest("/user?username="+p0);
            Assert.assertEquals(response.getString("httpsc"), "404");
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }

    }

    @Test(priority = 1, groups = { "wso2.esb" }, description = "jira [deleteUser] method negative with invalid username.")
    public void testDeleteUserNegative() throws Exception {

        String methodName = "deleteUser";
        String jsonRequestFilePath = pathToRequestsDirectory
                + methodName +".txt";

        final String jsonString = ConnectorIntegrationUtil
                .getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory
                + methodName + ".xml";

        String p0=connectorProperties.getProperty("deleteUserNameInvalid");

        String modifiedJsonString = String.format(jsonString,p0);

        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));

        try {
            String statusCode = ConnectorIntegrationUtil
                    .sendRequestToRetriveHeaders(
                            getProxyServiceURL(methodName), modifiedJsonString) + "";

            JSONObject  response=jiraclient.sendDeleteRequest("/user?username="+p0);
            String directStatusCode =response.getString("httpsc");
            Assert.assertEquals(statusCode, directStatusCode);

        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }

    @Test(priority = 1, groups = { "wso2.esb" }, description = "jira [getAllProjects] method positive.")
    public void testGetAllProjects() throws Exception {

        String methodName = "getAllProjects";

        final String proxyFilePath = "file:///" + pathToProxiesDirectory
                + methodName + ".xml";

        String modifiedJsonString = "";

        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));

        try {
            String status = ConnectorIntegrationUtil.sendRequestToRetriveHeaders(
                    getProxyServiceURL(methodName), modifiedJsonString) +"";

            JSONObject response=jiraclient.sendGetRequest("/project");
            Assert.assertEquals(response.getString("httpsc"), status);
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }

    }

    @Test(priority = 1, groups = { "wso2.esb" }, description = "jira [getGroups] method positive.")
    public void testGetGroups() throws Exception {

        String methodName = "getGroups";

        final String proxyFilePath = "file:///" + pathToProxiesDirectory
                + methodName + ".xml";

        String modifiedJsonString ="";

        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));

        try {
            String status = ConnectorIntegrationUtil.sendRequestToRetriveHeaders(
                    getProxyServiceURL(methodName), modifiedJsonString) +"";

            JSONObject response=jiraclient.sendGetRequest("/groups/picker");
            Assert.assertEquals(response.getString("httpsc"),status);
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }

    }


    @Test(priority = 1, groups = { "wso2.esb" }, description = "jira [getGroupUsers] method positive.")
    public void testGetGroupUsers() throws Exception {


        String methodName = "getGroupUsers";
        String jsonRequestFilePath = pathToRequestsDirectory
                + methodName +".txt";

        final String jsonString = ConnectorIntegrationUtil
                .getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory
                + methodName + ".xml";

        String p0=connectorProperties.getProperty("groupName");

        String modifiedJsonString = String.format(jsonString,p0,"","");

        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));

        try {
            String status = ConnectorIntegrationUtil.sendRequestToRetriveHeaders(
                    getProxyServiceURL(methodName), modifiedJsonString) +"";

            JSONObject response=jiraclient.sendGetRequest("/group?groupname="+p0+"&expand=users[:]");
            Assert.assertEquals(response.getString("httpsc"), status);
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }

    }


    @Test(priority = 1, groups = { "wso2.esb" }, description = "jira [getGroupUsers] method positive with optional parameters.")
    public void testGetGroupUsersOptional() throws Exception {


        String methodName = "getGroupUsers";
        String jsonRequestFilePath = pathToRequestsDirectory
                + methodName +".txt";

        final String jsonString = ConnectorIntegrationUtil
                .getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory
                + methodName + ".xml";

        String p0=connectorProperties.getProperty("groupName");
        String p1=connectorProperties.getProperty("startIndex");
        String p2=connectorProperties.getProperty("endIndex");


        String modifiedJsonString = String.format(jsonString,p0,p1,p2);

        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));

        try {
            String status = ConnectorIntegrationUtil.sendRequestToRetriveHeaders(
                    getProxyServiceURL(methodName), modifiedJsonString) +"";

            JSONObject response=jiraclient.sendGetRequest("/group?groupname="+p0+"&expand=users["+p1+":"+p2+"]");
            Assert.assertEquals(response.getString("httpsc"), status);
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }

    }

    @Test(priority = 1, groups = { "wso2.esb" }, description = "jira [getGroupUsers] method negative with invalid group name.")
    public void testGetGroupUsersNegative() throws Exception {


        String methodName = "getGroupUsers";
        String jsonRequestFilePath = pathToRequestsDirectory
                + methodName +".txt";

        final String jsonString = ConnectorIntegrationUtil
                .getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory
                + methodName + ".xml";

        String p0=connectorProperties.getProperty("invalidGroupName");
        String p1=connectorProperties.getProperty("startIndex");
        String p2=connectorProperties.getProperty("endIndex");


        String modifiedJsonString = String.format(jsonString,p0,p1,p2);

        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));

        try {
            String status = ConnectorIntegrationUtil.sendRequestToRetriveHeaders(
                    getProxyServiceURL(methodName), modifiedJsonString) +"";

            JSONObject response=jiraclient.sendGetRequest("/group?groupname="+p0+"&expand=users["+p1+":"+p2+"]");
            Assert.assertEquals(response.getString("httpsc"), status);
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }

    }

    @Test(priority = 1, groups = { "wso2.esb" }, description = "jira [getUserGroups] method positive.")
    public void testGetUserGroups() throws Exception {

        String methodName = "getUserGroups";
        String jsonRequestFilePath = pathToRequestsDirectory
                + methodName +".txt";

        final String jsonString = ConnectorIntegrationUtil
                .getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory
                + methodName + ".xml";

        String p0=connectorProperties.getProperty("validUserName");

        String modifiedJsonString = String.format(jsonString,p0);

        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));

        try {
            String status = ConnectorIntegrationUtil.sendRequestToRetriveHeaders(
                    getProxyServiceURL(methodName), modifiedJsonString) +"";

            JSONObject response=jiraclient.sendGetRequest("/user?username="+p0+"&expand=groups");
            Assert.assertEquals(response.getString("httpsc"), status);
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }

    }


    @Test(priority = 1, groups = { "wso2.esb" }, description = "jira [getUserGroups] method negative with invalid username.")
    public void testGetUserGroupsNegative() throws Exception {

        String methodName = "getUserGroups";
        String jsonRequestFilePath = pathToRequestsDirectory
                + methodName +".txt";

        final String jsonString = ConnectorIntegrationUtil
                .getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory
                + methodName + ".xml";

        String p0=connectorProperties.getProperty("invalidUserName");

        String modifiedJsonString = String.format(jsonString,p0);

        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));

        try {
            String status = ConnectorIntegrationUtil.sendRequestToRetriveHeaders(
                    getProxyServiceURL(methodName), modifiedJsonString) +"";

            JSONObject response=jiraclient.sendGetRequest("/user?username="+p0+"&expand=groups");
            Assert.assertEquals(response.getString("httpsc"), status);
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }

    }

    @Test(priority = 1, groups = { "wso2.esb" }, description = "jira [removeGroupFromProjectRole] method positive.")
    public void testRemoveGroupFromProjectRole() throws Exception {

        String methodName = "removeGroupFromProjectRole";
        String jsonRequestFilePath = pathToRequestsDirectory
                + methodName +".txt";

        final String jsonString = ConnectorIntegrationUtil
                .getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory
                + methodName + ".xml";

        String p0=connectorProperties.getProperty("removeRoleGroup");
        String p1=connectorProperties.getProperty("validProjectId");
        String p2=connectorProperties.getProperty("removeGroupRoleId");

        String modifiedJsonString = String.format(jsonString,p0,p1,p2);

        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));

        try {
            JSONObject jsonObject = ConnectorIntegrationUtil.sendRequest(
                    getProxyServiceURL(methodName), modifiedJsonString);

            JSONObject response=jiraclient.sendGetRequest("/project/"+p1+"/role/"+p2);
            Assert.assertFalse(response.getJSONObject("response").getJSONArray("actors").toString().contains(p0), response.toString());
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }

    }

    @Test(priority = 1, groups = { "wso2.esb" }, description = "jira [removeGroupFromProjectRole] method negative with invalid project id.")
    public void testRemoveGroupFromProjectRoleNegative() throws Exception {

        String methodName = "removeGroupFromProjectRole";
        String jsonRequestFilePath = pathToRequestsDirectory
                + methodName +".txt";

        final String jsonString = ConnectorIntegrationUtil
                .getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory
                + methodName + ".xml";

        String p0=connectorProperties.getProperty("removeRoleGroup");
        String p1=connectorProperties.getProperty("invalidProjectId");
        String p2=connectorProperties.getProperty("removeGroupRoleId");

        String modifiedJsonString = String.format(jsonString,p0,p1,p2);

        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));

        try {
            String status = ConnectorIntegrationUtil.sendRequestToRetriveHeaders(
                    getProxyServiceURL(methodName), modifiedJsonString) +"";

            JSONObject response=jiraclient.sendDeleteRequest("/project/" + p1 + "/role/" + p2 + "?group=" + p0);
            Assert.assertEquals(response.getString("httpsc"), status);
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }

    }

    @Test(priority = 1, groups = { "wso2.esb" }, description = "jira [removeUserFromGroup] method positive.")
    public void testRemoveUserFromGroup() throws Exception {

        String methodName = "removeUserFromGroup";
        String jsonRequestFilePath = pathToRequestsDirectory
                + methodName +".txt";

        final String jsonString = ConnectorIntegrationUtil
                .getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory
                + methodName + ".xml";

        String p0=connectorProperties.getProperty("removeUserGroup");
        String p1=connectorProperties.getProperty("removeUserGroupUsername");

        String modifiedJsonString = String.format(jsonString,p0,p1);

        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));

        try {
            JSONObject jsonObject = ConnectorIntegrationUtil.sendRequest(
                    getProxyServiceURL(methodName), modifiedJsonString);

            JSONObject response=jiraclient.sendGetRequest("/group?groupname="+ p0 +"&expand=users[1:50]");
            Assert.assertFalse(response.getJSONObject("response").getJSONObject("users").toString().contains(p1));        } finally {

            proxyAdmin.deleteProxy(methodName);
        }

    }


    @Test(priority = 1, groups = { "wso2.esb" }, description = "jira [removeUserFromGroup] method negative with invalid username.")
    public void testRemoveUserFromGroupNegative() throws Exception {

        String methodName = "removeUserFromGroup";
        String jsonRequestFilePath = pathToRequestsDirectory
                + methodName +".txt";

        final String jsonString = ConnectorIntegrationUtil
                .getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory
                + methodName + ".xml";

        String p0=connectorProperties.getProperty("invalidGroupName");
        String p1=connectorProperties.getProperty("removeUserGroupUsername");

        String modifiedJsonString = String.format(jsonString,p0,p1);

        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));

        try {
            String status = ConnectorIntegrationUtil.sendRequestToRetriveHeaders(
                    getProxyServiceURL(methodName), modifiedJsonString) +"";

            JSONObject response=jiraclient.sendDeleteRequest("/group/user?groupname=" + p0 + "&username=" + p1);
            Assert.assertEquals(response.getString("httpsc"), status);
        } finally {

            proxyAdmin.deleteProxy(methodName);
        }

    }


    @Test(priority = 1, groups = { "wso2.esb" }, description = "jira [removeUserFromProjectRole] method positive.")
    public void testRemoveUserFromProjectRole() throws Exception {

        String methodName = "removeUserFromProjectRole";
        String jsonRequestFilePath = pathToRequestsDirectory
                + methodName +".txt";

        final String jsonString = ConnectorIntegrationUtil
                .getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory
                + methodName + ".xml";


        String p0=connectorProperties.getProperty("removeRoleUser");
        String p1=connectorProperties.getProperty("validProjectId");
        String p2=connectorProperties.getProperty("removeUserRoleId");

        String modifiedJsonString = String.format(jsonString,p0,p1,p2);

        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));

        try {
            JSONObject jsonObject = ConnectorIntegrationUtil.sendRequest(
                    getProxyServiceURL(methodName), modifiedJsonString);

            JSONObject response=jiraclient.sendGetRequest("/project/"+p1+"/role/"+p2);
            Assert.assertFalse(response.getJSONObject("response").getJSONArray("actors").toString().contains(p0), response.toString());
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }

    }

    @Test(priority = 1, groups = { "wso2.esb" }, description = "jira [removeUserFromProjectRole] method negative with invalid project id.")
    public void testRemoveUserFromProjectRoleNegative() throws Exception {

        String methodName = "removeUserFromProjectRole";
        String jsonRequestFilePath = pathToRequestsDirectory
                + methodName +".txt";

        final String jsonString = ConnectorIntegrationUtil
                .getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory
                + methodName + ".xml";

        String p0=connectorProperties.getProperty("removeRoleUser");
        String p1=connectorProperties.getProperty("invalidProjectId");
        String p2=connectorProperties.getProperty("removeUserRoleId");

        String modifiedJsonString = String.format(jsonString,p0,p1,p2);

        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));

        try {
            String status = ConnectorIntegrationUtil.sendRequestToRetriveHeaders(
                    getProxyServiceURL(methodName), modifiedJsonString) +"";

            JSONObject response=jiraclient.sendDeleteRequest("/project/" + p1 + "/role/" + p2 + "?user=" + p0);
            Assert.assertEquals(response.getString("httpsc"), status);
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }

    }





}