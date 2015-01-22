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

    @Test(priority = 1, groups = { "wso2.esb" }, description = "jira [createComponent] method positive.")
    public void testCreateComponent() throws Exception {

        String methodName = "createComponent";
        String jsonRequestFilePath = pathToRequestsDirectory
                + methodName +".txt";

        final String jsonString = ConnectorIntegrationUtil
                .getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory
                + methodName + ".xml";

        String p0=connectorProperties.getProperty("createComponentName");
        String p1=connectorProperties.getProperty("validProjectId");

        String modifiedJsonString = String.format(jsonString,p0,p1);

        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));

        try {
            JSONObject jsonObject = ConnectorIntegrationUtil.sendRequest(
                    getProxyServiceURL(methodName), modifiedJsonString);
            String newComponentId = jsonObject.getString("id");

            JSONObject response=jiraclient.sendGetRequest("/component/"+newComponentId);
            jiraclient.sendDeleteRequest("/component/"+newComponentId);

            Assert.assertEquals(response.getString("httpsc"),"200");


        } finally {
            proxyAdmin.deleteProxy(methodName);
        }

    }



    @Test(priority = 1, groups = { "wso2.esb" }, description = "jira [createComponent] method negative with invalid project id.")
    public void testCreateComponentNegative() throws Exception {

        String methodName = "createComponent";
        String jsonRequestFilePath = pathToRequestsDirectory
                + methodName +".txt";

        final String jsonString = ConnectorIntegrationUtil
                .getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory
                + methodName + ".xml";

        String jsonDirectRequestFilePath = pathToRequestsDirectory
                + "createComponentDirect.txt";
        final String jsonDirectString = ConnectorIntegrationUtil
                .getFileContent(jsonDirectRequestFilePath);

        String p0=connectorProperties.getProperty("createComponentName");
        String p1=connectorProperties.getProperty("invalidProjectId");

        String modifiedJsonString = String.format(jsonString,p0,p1);
        String modifiedDirectRequest = String.format(jsonDirectString,p0,p1);

        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));

        try {
            String status = ConnectorIntegrationUtil.sendRequestToRetriveHeaders(
                    getProxyServiceURL(methodName), modifiedJsonString) +"";

            JSONObject response=jiraclient.sendPostRequest("/component",modifiedDirectRequest);

            String directStatusCode=response.getString("httpsc");

            Assert.assertEquals(directStatusCode, status);
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }

    }

    @Test(priority = 1, groups = { "wso2.esb" }, description = "jira [createComponent] method with optional parameters.")
    public void testCreateComponentOptional() throws Exception {

        String methodName = "createComponent";
        String jsonRequestFilePath = pathToRequestsDirectory
                + methodName +"Optional.txt";

        final String jsonString = ConnectorIntegrationUtil
                .getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory
                + methodName + ".xml";

        String p0=connectorProperties.getProperty("createComponentName2");
        String p1=connectorProperties.getProperty("validProjectId");
        String p2=connectorProperties.getProperty("componentDescription");
        String p3=connectorProperties.getProperty("validUserName");

        String modifiedJsonString = String.format(jsonString,p0,p1,p2,p3);

        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));

        try {

            JSONObject jsonObject = ConnectorIntegrationUtil.sendRequest(
                    getProxyServiceURL(methodName), modifiedJsonString);
            String newComponentId = jsonObject.getString("id");

            JSONObject response=jiraclient.sendGetRequest("/component/"+newComponentId);
            jiraclient.sendDeleteRequest("/component/"+newComponentId);

            Assert.assertEquals(response.getString("httpsc"),"200");

        } finally {
            proxyAdmin.deleteProxy(methodName);
        }

    }


    @Test(priority = 1, groups = { "wso2.esb" }, description = "jira [deleteComponent] method positive.")
    public void testDeleteComponent() throws Exception {

        String methodName = "deleteComponent";
        String jsonRequestFilePath = pathToRequestsDirectory
                + methodName +".txt";

        final String jsonString = ConnectorIntegrationUtil
                .getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory
                + methodName + ".xml";

        String jsonDirectRequestFilePath = pathToRequestsDirectory
                + "createComponentDirect.txt";
        final String jsonDirectString = ConnectorIntegrationUtil
                .getFileContent(jsonDirectRequestFilePath);

        String p0=connectorProperties.getProperty("createComponentName3");
        String p1=connectorProperties.getProperty("validProjectId");

        String modifiedDirectRequest = String.format(jsonDirectString,p0,p1);

        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));

        try {

            JSONObject response=jiraclient.sendPostRequest("/component",modifiedDirectRequest);
            String p00=response.getJSONObject("response").getString("id");

            String modifiedJsonString = String.format(jsonString,p00);

            ConnectorIntegrationUtil.sendRequestToRetriveHeaders(
                    getProxyServiceURL(methodName), modifiedJsonString);

            response=jiraclient.sendGetRequest("/component/"+p00);
            Assert.assertNotEquals(response.getString("httpsc"),"200");


        } finally {
            proxyAdmin.deleteProxy(methodName);
        }

    }


    @Test(priority = 1, groups = { "wso2.esb" }, description = "jira [deleteComponent] method negative with invalid component id.")
    public void testDeleteComponentNegative() throws Exception {

        String methodName = "deleteComponent";
        String jsonRequestFilePath = pathToRequestsDirectory
                + methodName +".txt";

        final String jsonString = ConnectorIntegrationUtil
                .getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory
                + methodName + ".xml";

        String p0=connectorProperties.getProperty("invalidDeleteComponentId");

        String modifiedJsonString = String.format(jsonString,p0);

        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));

        try {
            String status = ConnectorIntegrationUtil.sendRequestToRetriveHeaders(
                    getProxyServiceURL(methodName), modifiedJsonString) +"";

            JSONObject response=jiraclient.sendDeleteRequest("/component/"+p0);

            String directStatusCode=response.getString("httpsc");

            Assert.assertEquals(directStatusCode, status);
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }

    }


    @Test(priority = 1, groups = { "wso2.esb" }, description = "jira [deleteComponent] method with optional parameter(s).")
    public void testDeleteComponentOptional() throws Exception {

        String methodName = "deleteComponent";
        String jsonRequestFilePath = pathToRequestsDirectory
                + methodName +"Optional.txt";

        final String jsonString = ConnectorIntegrationUtil
                .getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory
                + methodName + ".xml";


        String jsonDirectRequestFilePath = pathToRequestsDirectory
                + "createComponentDirect.txt";
        final String jsonDirectString = ConnectorIntegrationUtil
                .getFileContent(jsonDirectRequestFilePath);

        String p0=connectorProperties.getProperty("createComponentName4");
        String p01=connectorProperties.getProperty("createComponentName5");
        String p1=connectorProperties.getProperty("validProjectId");

        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));

        String modifiedDirectRequest1 = String.format(jsonDirectString,p0,p1);
        String modifiedDirectRequest2 = String.format(jsonDirectString,p01,p1);

        try {

            JSONObject response=jiraclient.sendPostRequest("/component",modifiedDirectRequest1);
            String componentId1=response.getJSONObject("response").getString("id");

            response=jiraclient.sendPostRequest("/component",modifiedDirectRequest2);
            String componentId2=response.getJSONObject("response").getString("id");

            String modifiedJsonString = String.format(jsonString,componentId1,componentId2);

            //delete component p0 and moves issues to p01
            int status = ConnectorIntegrationUtil.sendRequestToRetriveHeaders(
                    getProxyServiceURL(methodName), modifiedJsonString);

            Assert.assertEquals(status,204);
            jiraclient.sendDeleteRequest("/component/"+componentId2);

        } finally {
            proxyAdmin.deleteProxy(methodName);
        }

    }




    @Test(priority = 1, groups = { "wso2.esb" }, description = "jira [getComponent] method positive.")
    public void testGetComponent() throws Exception {

        String methodName = "getComponent";
        String jsonRequestFilePath = pathToRequestsDirectory
                + methodName +".txt";

        final String jsonString = ConnectorIntegrationUtil
                .getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory
                + methodName + ".xml";

        String jsonDirectRequestFilePath = pathToRequestsDirectory
                + "createComponentDirect.txt";
        final String jsonDirectString = ConnectorIntegrationUtil
                .getFileContent(jsonDirectRequestFilePath);

        String p0=connectorProperties.getProperty("createComponentName6");
        String p1=connectorProperties.getProperty("validProjectId");

        String modifiedDirectRequest = String.format(jsonDirectString,p0,p1);

        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));

        try {

            JSONObject response=jiraclient.sendPostRequest("/component",modifiedDirectRequest);

            String p00=response.getJSONObject("response").getString("id");

            String modifiedJsonString = String.format(jsonString,p00);

            ConnectorIntegrationUtil.sendRequestToRetriveHeaders(
                    getProxyServiceURL(methodName), modifiedJsonString);

            response=jiraclient.sendGetRequest("/component/"+p00);
            jiraclient.sendDeleteRequest("/component/"+p00);

            Assert.assertEquals(response.getString("httpsc"),"200");

        } finally {
            proxyAdmin.deleteProxy(methodName);
        }

    }


    @Test(priority = 1, groups = { "wso2.esb" }, description = "jira [getComponent] method negative with invalid component id.")
    public void testGetComponentNegative() throws Exception {

        String methodName = "getComponent";
        String jsonRequestFilePath = pathToRequestsDirectory
                + methodName +".txt";

        final String jsonString = ConnectorIntegrationUtil
                .getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory
                + methodName + ".xml";

        String p0=connectorProperties.getProperty("invalidGetComponentId");

        String modifiedJsonString = String.format(jsonString,p0);

        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));

        try {
            String status = ConnectorIntegrationUtil.sendRequestToRetriveHeaders(
                    getProxyServiceURL(methodName), modifiedJsonString) +"";

            JSONObject response=jiraclient.sendDeleteRequest("/component/"+p0);

            String directStatusCode=response.getString("httpsc");

            Assert.assertEquals(directStatusCode, status);
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }

    }




    @Test(priority = 1, groups = { "wso2.esb" }, description = "jira [getComponentRelatedIssueCount] method positive.")
    public void testGetComponentRelatedIssueCount() throws Exception {

        String methodName = "getComponentRelatedIssueCount";
        String jsonRequestFilePath = pathToRequestsDirectory
                + methodName +".txt";

        final String jsonString = ConnectorIntegrationUtil
                .getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory
                + methodName + ".xml";


        String jsonDirectRequestFilePath = pathToRequestsDirectory
                + "createComponentDirect.txt";
        final String jsonDirectString = ConnectorIntegrationUtil
                .getFileContent(jsonDirectRequestFilePath);

        String p0=connectorProperties.getProperty("createComponentName7");
        String p1=connectorProperties.getProperty("validProjectId");

        String modifiedDirectRequest = String.format(jsonDirectString,p0,p1);

        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));

        try {

            JSONObject response=jiraclient.sendPostRequest("/component",modifiedDirectRequest);

            String p00=response.getJSONObject("response").getString("id");

            String modifiedJsonString = String.format(jsonString,p00);

            response = ConnectorIntegrationUtil.sendRequest(
                    getProxyServiceURL(methodName), modifiedJsonString);

            jiraclient.sendDeleteRequest("/component/"+p00);

            Assert.assertEquals(response.getString("issueCount"),"0");

        } finally {
            proxyAdmin.deleteProxy(methodName);
        }

    }



    @Test(priority = 1, groups = { "wso2.esb" }, description = "jira [getComponentRelatedIssueCount] method negative with invalid component id.")
    public void testGetComponentRelatedIssueCountNegative() throws Exception {

        String methodName = "getComponentRelatedIssueCount";
        String jsonRequestFilePath = pathToRequestsDirectory
                + methodName +".txt";

        final String jsonString = ConnectorIntegrationUtil
                .getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory
                + methodName + ".xml";

        String p0=connectorProperties.getProperty("invalidDeleteComponentId");

        String modifiedJsonString = String.format(jsonString,p0);

        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));

        try {
            String status = ConnectorIntegrationUtil.sendRequestToRetriveHeaders(
                    getProxyServiceURL(methodName), modifiedJsonString) +"";

            JSONObject response=jiraclient.sendGetRequest("/component/"+p0+"/relatedIssueCounts");

            String directStatusCode=response.getString("httpsc");

            Assert.assertEquals(directStatusCode, status);
        } finally {
            proxyAdmin.deleteProxy(methodName);
        }

    }





    @Test(priority = 1, groups = { "wso2.esb" }, description = "jira [updateComponent] method positive.")
    public void testUpdateComponent() throws Exception {

        String methodName = "updateComponent";
        String jsonRequestFilePath = pathToRequestsDirectory
                + methodName +".txt";

        final String jsonString = ConnectorIntegrationUtil
                .getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory
                + methodName + ".xml";

        String jsonDirectRequestFilePath = pathToRequestsDirectory
                + "createComponentDirect.txt";
        final String jsonDirectString = ConnectorIntegrationUtil
                .getFileContent(jsonDirectRequestFilePath);

        String p0=connectorProperties.getProperty("createComponentName8");
        String p1=connectorProperties.getProperty("validProjectId");

        String modifiedDirectRequest = String.format(jsonDirectString,p0,p1);

        String p11=connectorProperties.getProperty("updateComponentNewName");

        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));

        try {

            JSONObject response=jiraclient.sendPostRequest("/component",modifiedDirectRequest);
            String p00=response.getJSONObject("response").getString("id");

            String modifiedJsonString = String.format(jsonString,p00,p11);

            JSONObject jsonObject = ConnectorIntegrationUtil.sendRequest(
                    getProxyServiceURL(methodName), modifiedJsonString);

            response=jiraclient.sendGetRequest("/component/" + p00);
            String updatedComponentName=response.getJSONObject("response").getString("name");
            jiraclient.sendDeleteRequest("/component/"+p00);

            Assert.assertEquals(updatedComponentName.trim(),p11.trim());

        } finally {
            proxyAdmin.deleteProxy(methodName);
        }

    }

    @Test(priority = 1, groups = { "wso2.esb" }, description = "jira [updateComponent] method negative with invalid component Id")
    public void testUpdateComponentNegative() throws Exception {

        String methodName = "updateComponent";
        String jsonRequestFilePath = pathToRequestsDirectory
                + methodName +".txt";

        final String jsonString = ConnectorIntegrationUtil
                .getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory
                + methodName + ".xml";

        String p0=connectorProperties.getProperty("invalidGetComponentId");
        String p1=connectorProperties.getProperty("updateComponentNewName");

        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));

        try {

            String modifiedJsonString = String.format(jsonString,p0,p1);

            int responseCode = ConnectorIntegrationUtil.sendRequestToRetriveHeaders(
                    getProxyServiceURL(methodName), modifiedJsonString);


            Assert.assertTrue(responseCode >= 400 && responseCode < 600);

        } finally {
            proxyAdmin.deleteProxy(methodName);
        }

    }

    @Test(priority = 1, groups = { "wso2.esb" }, description = "jira [updateComponent] method optional.")
    public void testUpdateComponentOptional() throws Exception {

        String methodName = "updateComponent";
        String jsonRequestFilePath = pathToRequestsDirectory
                + methodName +"Optional.txt";

        final String jsonString = ConnectorIntegrationUtil
                .getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory
                + methodName + ".xml";

        String jsonDirectRequestFilePath = pathToRequestsDirectory
                + "createComponentDirect.txt";
        final String jsonDirectString = ConnectorIntegrationUtil
                .getFileContent(jsonDirectRequestFilePath);

        String p0=connectorProperties.getProperty("createComponentName9");
        String p1=connectorProperties.getProperty("validProjectId");

        String modifiedDirectRequest = String.format(jsonDirectString,p0,p1);

        String p11=connectorProperties.getProperty("updateComponentNewName");
        String p12=connectorProperties.getProperty("updateComponentNewDescription");

        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));

        try {

            JSONObject response=jiraclient.sendPostRequest("/component",modifiedDirectRequest);
            String p00=response.getJSONObject("response").getString("id");

            String modifiedJsonString = String.format(jsonString,p00,p11,p12);

            JSONObject jsonObject = ConnectorIntegrationUtil.sendRequest(
                    getProxyServiceURL(methodName), modifiedJsonString);


            response=jiraclient.sendGetRequest("/component/"+p00);
            String updatedComponentDescription=response.getJSONObject("response").getString("description");
            jiraclient.sendDeleteRequest("/component/"+p00);

            Assert.assertEquals(updatedComponentDescription.trim(),p12.trim());

        } finally {
            proxyAdmin.deleteProxy(methodName);
        }

    }






    @Test(priority = 1, groups = { "wso2.esb" }, description = "jira [createProjectVersion] method positive.")
    public void testCreateProjectVersion() throws Exception {

        String methodName = "createProjectVersion";
        String jsonRequestFilePath = pathToRequestsDirectory
                + methodName +".txt";

        final String jsonString = ConnectorIntegrationUtil
                .getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory
                + methodName + ".xml";

        String p0=connectorProperties.getProperty("createVersionName");
        String p1=connectorProperties.getProperty("validProjectId");
        String p2=connectorProperties.getProperty("versionDescription");
        String p3=connectorProperties.getProperty("versionIsArchived");
        String p4=connectorProperties.getProperty("versionIsReleased");

        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));

        try {

            String modifiedJsonString = String.format(jsonString,p0,p1,p2,p3,p4);

            JSONObject jsonObject = ConnectorIntegrationUtil.sendRequest(
                    getProxyServiceURL(methodName), modifiedJsonString);

            Assert.assertTrue(jsonObject.has("id"),jsonObject.toString());

            String id = jsonObject.getString("id");
            jiraclient.sendDeleteRequest("/version/"+id);

        } finally {
            proxyAdmin.deleteProxy(methodName);
        }

    }

    @Test(priority = 1, groups = { "wso2.esb" }, description = "jira [createProjectVersion] method optional with releaseDate.")
    public void testCreateProjectVersionOptional() throws Exception {

        String methodName = "createProjectVersion";
        String jsonRequestFilePath = pathToRequestsDirectory
                + methodName +"Optional.txt";

        final String jsonString = ConnectorIntegrationUtil
                .getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory
                + methodName + ".xml";

        String p0=connectorProperties.getProperty("createVersionName2");
        String p1=connectorProperties.getProperty("validProjectId");
        String p2=connectorProperties.getProperty("versionDescription");
        String p3=connectorProperties.getProperty("versionIsArchived");
        String p4=connectorProperties.getProperty("versionIsReleased");
        String p5=connectorProperties.getProperty("releaseDate");

        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));

        try {

            String modifiedJsonString = String.format(jsonString,p0,p1,p2,p3,p4,p5);

            JSONObject jsonObject = ConnectorIntegrationUtil.sendRequest(
                    getProxyServiceURL(methodName), modifiedJsonString);

            Assert.assertTrue(jsonObject.has("releaseDate"),jsonObject.toString());

            String id = jsonObject.getString("id");
            jiraclient.sendDeleteRequest("/version/"+id);

        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }

    @Test(priority = 1, groups = { "wso2.esb" }, description = "jira [createProjectVersion] method negative with invalid project id.")
    public void testCreateProjectVersionNegative() throws Exception {

        String methodName = "createProjectVersion";
        String jsonRequestFilePath = pathToRequestsDirectory
                + methodName +".txt";

        final String jsonString = ConnectorIntegrationUtil
                .getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory
                + methodName + ".xml";

        String p0=connectorProperties.getProperty("createVersionName2");
        String p1=connectorProperties.getProperty("invalidProjectId");
        String p2=connectorProperties.getProperty("versionDescription");
        String p3=connectorProperties.getProperty("versionIsArchived");
        String p4=connectorProperties.getProperty("versionIsReleased");

        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));

        try {

            String modifiedJsonString = String.format(jsonString,p0,p1,p2,p3,p4);

            int responseCode = ConnectorIntegrationUtil.sendRequestToRetriveHeaders(
                    getProxyServiceURL(methodName), modifiedJsonString);

            Assert.assertTrue(!(responseCode>=200 && responseCode < 300));

        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }


    @Test(priority = 1, groups = { "wso2.esb" }, description = "jira [deleteProjectVersion] method positive.")
    public void testDeleteProjectVersion() throws Exception {

        String methodName = "deleteProjectVersion";
        String jsonRequestFilePath = pathToRequestsDirectory
                + methodName +".txt";

        String jsonDirectRequestFilePath = pathToRequestsDirectory
                + "createProjectVersionDirect.txt";

        final String jsonString = ConnectorIntegrationUtil
                .getFileContent(jsonRequestFilePath);

        final String jsonStringDirect = ConnectorIntegrationUtil
                .getFileContent(jsonDirectRequestFilePath);

        final String proxyFilePath = "file:///" + pathToProxiesDirectory
                + methodName + ".xml";


        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
        String versionID="";

        String p0=connectorProperties.getProperty("createVersionName3");
        String p1=connectorProperties.getProperty("validProjectId");
        String p2=connectorProperties.getProperty("versionDescription");
        String p3=connectorProperties.getProperty("versionIsArchived");
        String p4=connectorProperties.getProperty("versionIsReleased");

        String createRequest = String.format(jsonStringDirect,p0,p1,p2,p3,p4);



        try {

            JSONObject createResponse=jiraclient.sendPostRequest("/version",createRequest);
            versionID=createResponse.getJSONObject("response").getString("id"); //If creation was not successful, a JSONException will occur

            String modifiedJsonString = String.format(jsonString,versionID);
            ConnectorIntegrationUtil.sendRequestToRetriveHeaders(
                    getProxyServiceURL(methodName), modifiedJsonString);

            JSONObject getResponse=jiraclient.sendGetRequest("/version/"+versionID);
            int responseCode = Integer.parseInt(getResponse.getString("httpsc"));

            Assert.assertTrue(responseCode>300);
            jiraclient.sendDeleteRequest("/version/"+versionID);

        } finally {
            proxyAdmin.deleteProxy(methodName);
        }

    }


    @Test(priority = 1, groups = { "wso2.esb" }, description = "jira [deleteProjectVersion] method optional")
    public void testDeleteProjectVersionOptional() throws Exception {

        String methodName = "deleteProjectVersion";

        String jsonRequestFilePath = pathToRequestsDirectory
                + methodName +"Optional.txt";

        final String jsonString = ConnectorIntegrationUtil
                .getFileContent(jsonRequestFilePath);

        String jsonDirectRequestFilePath = pathToRequestsDirectory
                + "createProjectVersionDirect.txt";

        final String jsonStringDirect = ConnectorIntegrationUtil
                .getFileContent(jsonDirectRequestFilePath);


        final String proxyFilePath = "file:///" + pathToProxiesDirectory
                + methodName + ".xml";

        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));

        String versionID1 = "";
        String versionID2 = "";

        String p00=connectorProperties.getProperty("createVersionName4");
        String p01=connectorProperties.getProperty("validProjectId");
        String p02=connectorProperties.getProperty("versionDescription");
        String p03=connectorProperties.getProperty("versionIsArchived");
        String p04=connectorProperties.getProperty("versionIsReleased");

        String p10=connectorProperties.getProperty("createVersionName5");

        String createRequest1 = String.format(jsonStringDirect,p00,p01,p02,p03,p04);
        String createRequest2 = String.format(jsonStringDirect,p10,p01,p02,p03,p04);


        try {

            JSONObject createResponse1=jiraclient.sendPostRequest("/version",createRequest1);
            versionID1=createResponse1.getJSONObject("response").getString("id");  //If creation was not successful, a JSONException will occur

            JSONObject createResponse2=jiraclient.sendPostRequest("/version",createRequest2);
            versionID2=createResponse2.getJSONObject("response").getString("id");  //If creation was not successful, a JSONException will occur

            String modifiedJsonString = String.format(jsonString,versionID1,versionID2);

            ConnectorIntegrationUtil.sendRequestToRetriveHeaders(
                    getProxyServiceURL(methodName), modifiedJsonString);

            JSONObject getResponse=jiraclient.sendGetRequest("/version/"+versionID1);
            int responseCode = Integer.parseInt(getResponse.getString("httpsc"));
            jiraclient.sendDeleteRequest("/version/"+versionID1);
            jiraclient.sendDeleteRequest("/version/"+versionID2);

            Assert.assertTrue(responseCode > 300);

        } finally {
            proxyAdmin.deleteProxy(methodName);
        }
    }

    @Test(priority = 1, groups = { "wso2.esb" }, description = "jira [deleteProjectVersion] method negative with invalid version id.")
    public void testDeleteProjectVersionNegative() throws Exception {

        String methodName = "deleteProjectVersion";
        String jsonRequestFilePath = pathToRequestsDirectory
                + methodName +".txt";

        final String jsonString = ConnectorIntegrationUtil
                .getFileContent(jsonRequestFilePath);


        final String proxyFilePath = "file:///" + pathToProxiesDirectory
                + methodName + ".xml";


        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
        String versionID=connectorProperties.getProperty("invalidDeleteVersionID");


        String modifiedJsonString = String.format(jsonString,versionID);

        try {

            JSONObject response = ConnectorIntegrationUtil.sendRequest(
                    getProxyServiceURL(methodName), modifiedJsonString);

            Assert.assertTrue(response.has("errorMessages"));

        } finally {
            proxyAdmin.deleteProxy(methodName);
        }

    }

    @Test(priority = 1, groups = { "wso2.esb" }, description = "jira [updateProjectVersion] method positive.")
    public void testUpdateProjectVersion() throws Exception {

        String methodName = "updateProjectVersion";
        String jsonRequestFilePath = pathToRequestsDirectory
                + methodName +".txt";

        String jsonDirectCreateRequestFilePath = pathToRequestsDirectory
                + "createProjectVersionDirect.txt";

        final String jsonString = ConnectorIntegrationUtil
                .getFileContent(jsonRequestFilePath);

        final String jsonStringDirect = ConnectorIntegrationUtil
                .getFileContent(jsonDirectCreateRequestFilePath);

        final String proxyFilePath = "file:///" + pathToProxiesDirectory
                + methodName + ".xml";


        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));
        String versionID="";

        String p0=connectorProperties.getProperty("createVersionName6");
        String p1=connectorProperties.getProperty("validProjectId");
        String p2=connectorProperties.getProperty("versionDescription");
        String p3=connectorProperties.getProperty("versionIsArchived");
        String p4=connectorProperties.getProperty("versionIsReleased");

        String p11=connectorProperties.getProperty("updatedProVersionName");
        String p12=connectorProperties.getProperty("updatedProVersionDescription");
        String p15=connectorProperties.getProperty("updatedReleaseDate");

        String createRequest = String.format(jsonStringDirect,p0,p1,p2,p3,p4);

        try {

            JSONObject createResponse=jiraclient.sendPostRequest("/version",createRequest);
            versionID=createResponse.getJSONObject("response").getString("id"); //If creation was not successful, a JSONException will occur

            String modifiedJsonString = String.format(jsonString,versionID,p11,p1,p12,p3,p4,p15);
            ConnectorIntegrationUtil.sendRequestToRetriveHeaders(
                    getProxyServiceURL(methodName), modifiedJsonString);

            JSONObject getResponse=jiraclient.sendGetRequest("/version/"+versionID);
            String newName = getResponse.getJSONObject("response").getString("name");

            jiraclient.sendDeleteRequest("/version/"+versionID);
            Assert.assertTrue(newName.equals(p11));

        } finally {
            proxyAdmin.deleteProxy(methodName);
        }

    }

    @Test(priority = 1, groups = { "wso2.esb" }, description = "jira [updateProjectVersion] method negative with invalid project version ID.")
    public void testUpdateProjectVersionNegative() throws Exception {

        String methodName = "updateProjectVersion";
        String jsonRequestFilePath = pathToRequestsDirectory
                + methodName +".txt";

        final String jsonString = ConnectorIntegrationUtil
                .getFileContent(jsonRequestFilePath);

        final String proxyFilePath = "file:///" + pathToProxiesDirectory
                + methodName + ".xml";


        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));

        String invalidVersionID = connectorProperties.getProperty("invalidVersionID");
        String p0=connectorProperties.getProperty("createVersionName6");
        String p1=connectorProperties.getProperty("validProjectId");
        String p2=connectorProperties.getProperty("versionDescription");
        String p3=connectorProperties.getProperty("versionIsArchived");
        String p4=connectorProperties.getProperty("versionIsReleased");
        String p5=connectorProperties.getProperty("releaseDate");

        try {

            String modifiedJsonString = String.format(jsonString,invalidVersionID,p0,p1,p2,p3,p4,p5);

            int responseCode = ConnectorIntegrationUtil.sendRequestToRetriveHeaders(
                    getProxyServiceURL(methodName), modifiedJsonString);

            Assert.assertTrue(responseCode > 300);

        } finally {
            proxyAdmin.deleteProxy(methodName);
        }

    }


    @Test(priority = 1, groups = { "wso2.esb" }, description = "jira [getRelatedIssueCountOfVersion] method positive.")
    public void testGetRelatedIssueCountOfVersion() throws Exception {

        String methodName = "getRelatedIssueCountOfVersion";
        String jsonRequestFilePath = pathToRequestsDirectory
                + methodName +".txt";
        String jsonDirectCreateRequestFilePath = pathToRequestsDirectory
                + "createProjectVersionDirect.txt";

        final String jsonString = ConnectorIntegrationUtil
                .getFileContent(jsonRequestFilePath);

        final String jsonStringDirect = ConnectorIntegrationUtil
                .getFileContent(jsonDirectCreateRequestFilePath);

        final String proxyFilePath = "file:///" + pathToProxiesDirectory
                + methodName + ".xml";

        String p0=connectorProperties.getProperty("createVersionName7");
        String p1=connectorProperties.getProperty("validProjectId");
        String p2=connectorProperties.getProperty("versionDescription");
        String p3=connectorProperties.getProperty("versionIsArchived");
        String p4=connectorProperties.getProperty("versionIsReleased");

        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));

        String createRequest = String.format(jsonStringDirect,p0,p1,p2,p3,p4);

        try {

            JSONObject createResponse=jiraclient.sendPostRequest("/version",createRequest);
            String versionID=createResponse.getJSONObject("response").getString("id"); //If creation was not successful, a JSONException will occur

            String modifiedJsonString = String.format(jsonString,versionID);

            JSONObject response = ConnectorIntegrationUtil.sendRequest(
                    getProxyServiceURL(methodName), modifiedJsonString);

            jiraclient.sendDeleteRequest("/version/"+versionID);
            Assert.assertEquals(response.getInt("issuesFixedCount"),0);

        } finally {
            proxyAdmin.deleteProxy(methodName);
        }

    }


    @Test(priority = 1, groups = { "wso2.esb" }, description = "jira [getRelatedIssueCountOfVersion] method negative with invalid version id.")
    public void testGetRelatedIssueCountOfVersionNegative() throws Exception {

        String methodName = "getRelatedIssueCountOfVersion";
        String jsonRequestFilePath = pathToRequestsDirectory
                + methodName +".txt";

        final String jsonString = ConnectorIntegrationUtil
                .getFileContent(jsonRequestFilePath);


        final String proxyFilePath = "file:///" + pathToProxiesDirectory
                + methodName + ".xml";

        String versionID = connectorProperties.getProperty("invalidVersionID");

        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));


        try {

            String modifiedJsonString = String.format(jsonString,versionID);

            int responseCode = ConnectorIntegrationUtil.sendRequestToRetriveHeaders(
                    getProxyServiceURL(methodName), modifiedJsonString);

            Assert.assertTrue(responseCode > 300);

        } finally {
            proxyAdmin.deleteProxy(methodName);
        }

    }

    @Test(priority = 1, groups = { "wso2.esb" }, description = "jira [getUnresolvedIssueCountOfVersion] method positive.")
    public void testGetUnresolvedIssueCountOfVersion() throws Exception {

        String methodName = "getUnresolvedIssueCountOfVersion";
        String jsonRequestFilePath = pathToRequestsDirectory
                + methodName +".txt";
        String jsonDirectCreateRequestFilePath = pathToRequestsDirectory
                + "createProjectVersionDirect.txt";

        final String jsonString = ConnectorIntegrationUtil
                .getFileContent(jsonRequestFilePath);

        final String jsonStringDirect = ConnectorIntegrationUtil
                .getFileContent(jsonDirectCreateRequestFilePath);

        final String proxyFilePath = "file:///" + pathToProxiesDirectory
                + methodName + ".xml";

        String p0=connectorProperties.getProperty("createVersionName8");
        String p1=connectorProperties.getProperty("validProjectId");
        String p2=connectorProperties.getProperty("versionDescription");
        String p3=connectorProperties.getProperty("versionIsArchived");
        String p4=connectorProperties.getProperty("versionIsReleased");

        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));

        String createRequest = String.format(jsonStringDirect,p0,p1,p2,p3,p4);

        try {

            JSONObject createResponse=jiraclient.sendPostRequest("/version",createRequest);
            String versionID=createResponse.getJSONObject("response").getString("id"); //If creation was not successful, a JSONException will occur

            String modifiedJsonString = String.format(jsonString,versionID);

            JSONObject response = ConnectorIntegrationUtil.sendRequest(
                    getProxyServiceURL(methodName), modifiedJsonString);

            jiraclient.sendDeleteRequest("/version/"+versionID);
            Assert.assertEquals(response.getInt("issuesUnresolvedCount"),0);

        } finally {
            proxyAdmin.deleteProxy(methodName);
        }

    }


    @Test(priority = 1, groups = { "wso2.esb" }, description = "jira [getUnresolvedIssueCountOfVersion] method negative with invalid version id.")
    public void testGetUnresolvedIssueCountOfVersionNegative() throws Exception {

        String methodName = "getUnresolvedIssueCountOfVersion";
        String jsonRequestFilePath = pathToRequestsDirectory
                + methodName +".txt";

        final String jsonString = ConnectorIntegrationUtil
                .getFileContent(jsonRequestFilePath);


        final String proxyFilePath = "file:///" + pathToProxiesDirectory
                + methodName + ".xml";

        String versionID = connectorProperties.getProperty("invalidVersionID");

        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));


        try {

            String modifiedJsonString = String.format(jsonString,versionID);

            int responseCode = ConnectorIntegrationUtil.sendRequestToRetriveHeaders(
                    getProxyServiceURL(methodName), modifiedJsonString);

            Assert.assertTrue(responseCode > 300);

        } finally {
            proxyAdmin.deleteProxy(methodName);
        }

    }
    @Test(priority = 1, groups = { "wso2.esb" }, description = "jira [getVersionById] method positive.")
    public void testGetVersionById() throws Exception {

        String methodName = "getVersionById";
        String jsonRequestFilePath = pathToRequestsDirectory
                + methodName +".txt";
        String jsonDirectCreateRequestFilePath = pathToRequestsDirectory
                + "createProjectVersionDirect.txt";

        final String jsonString = ConnectorIntegrationUtil
                .getFileContent(jsonRequestFilePath);

        final String jsonStringDirect = ConnectorIntegrationUtil
                .getFileContent(jsonDirectCreateRequestFilePath);

        final String proxyFilePath = "file:///" + pathToProxiesDirectory
                + methodName + ".xml";

        String p0=connectorProperties.getProperty("createVersionName9");
        String p1=connectorProperties.getProperty("validProjectId");
        String p2=connectorProperties.getProperty("versionDescription");
        String p3=connectorProperties.getProperty("versionIsArchived");
        String p4=connectorProperties.getProperty("versionIsReleased");

        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));

        String createRequest = String.format(jsonStringDirect,p0,p1,p2,p3,p4);

        try {

            JSONObject createResponse=jiraclient.sendPostRequest("/version",createRequest);
            String versionID=createResponse.getJSONObject("response").getString("id"); //If creation was not successful, a JSONException will occur

            String modifiedJsonString = String.format(jsonString,versionID);

            JSONObject response = ConnectorIntegrationUtil.sendRequest(
                    getProxyServiceURL(methodName), modifiedJsonString);

            jiraclient.sendDeleteRequest("/version/"+versionID);
            Assert.assertTrue(response.has("self"));

        } finally {
            proxyAdmin.deleteProxy(methodName);
        }

    }


    @Test(priority = 1, groups = { "wso2.esb" }, description = "jira [getVersionById] method negative with invalid version id.")
    public void testGetVersionByIdNegative() throws Exception {

        String methodName = "getVersionById";
        String jsonRequestFilePath = pathToRequestsDirectory
                + methodName +".txt";

        final String jsonString = ConnectorIntegrationUtil
                .getFileContent(jsonRequestFilePath);


        final String proxyFilePath = "file:///" + pathToProxiesDirectory
                + methodName + ".xml";

        String versionID = connectorProperties.getProperty("invalidVersionID");

        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));


        try {

            String modifiedJsonString = String.format(jsonString,versionID);

            int responseCode = ConnectorIntegrationUtil.sendRequestToRetriveHeaders(
                    getProxyServiceURL(methodName), modifiedJsonString);

            Assert.assertTrue(responseCode > 300);

        } finally {
            proxyAdmin.deleteProxy(methodName);
        }

    }




    @Test(priority = 1, groups = { "wso2.esb" }, description = "jira [createIssueLink] method positive.")
    public void testCreateIssueLink() throws Exception {

        String methodName = "createIssueLink";
        String jsonRequestFilePath = pathToRequestsDirectory
                + methodName +".txt";

        final String jsonString = ConnectorIntegrationUtil
                .getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory
                + methodName + ".xml";

        String p0=connectorProperties.getProperty("issueLinkType");
        String p1=connectorProperties.getProperty("inwardIssueKey1");
        String p2=connectorProperties.getProperty("outwardIssueKey1");
        String p3=connectorProperties.getProperty("commentBody");

        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));

        try {

            String modifiedJsonString = String.format(jsonString,p0,p1,p2,p3);

            int responseCode = ConnectorIntegrationUtil.sendRequestToRetriveHeaders(
                    getProxyServiceURL(methodName), modifiedJsonString);

            Assert.assertTrue(responseCode>200 && responseCode <=300);

        } finally {
            proxyAdmin.deleteProxy(methodName);
        }

    }

    @Test(priority = 1, groups = { "wso2.esb" }, description = "jira [createIssueLink] method negative with invalid issueLink type.")
    public void testCreateIssueLinkNegative() throws Exception {

        String methodName = "createIssueLink";
        String jsonRequestFilePath = pathToRequestsDirectory
                + methodName +".txt";

        final String jsonString = ConnectorIntegrationUtil
                .getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory
                + methodName + ".xml";

        String p0=connectorProperties.getProperty("invalidIssueLinkType");
        String p1=connectorProperties.getProperty("inwardIssueKey1");
        String p2=connectorProperties.getProperty("outwardIssueKey1");
        String p3=connectorProperties.getProperty("commentBody");

        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));

        try {

            String modifiedJsonString = String.format(jsonString,p0,p1,p2,p3);

            int responseCode = ConnectorIntegrationUtil.sendRequestToRetriveHeaders(
                    getProxyServiceURL(methodName), modifiedJsonString);

            Assert.assertFalse(responseCode>200 && responseCode <=300);

        } finally {
            proxyAdmin.deleteProxy(methodName);
        }

    }

    @Test(priority = 1, groups = { "wso2.esb" }, description = "jira [getIssueLink] method positive.")
    public void testGetIssueLink() throws Exception {

        String methodName = "getIssueLink";
        String jsonRequestFilePath = pathToRequestsDirectory
                + methodName +".txt";

        String jsonDirectRequestFilePath = pathToRequestsDirectory
                 +"createIssueLinkDirect.txt";

        final String jsonString = ConnectorIntegrationUtil
                .getFileContent(jsonRequestFilePath);

        final String jsonDirectString = ConnectorIntegrationUtil
                .getFileContent(jsonDirectRequestFilePath);

        final String proxyFilePath = "file:///" + pathToProxiesDirectory
                + methodName + ".xml";

        String p0=connectorProperties.getProperty("issueLinkType");
        String p1=connectorProperties.getProperty("inwardIssueKey2");
        String p2=connectorProperties.getProperty("outwardIssueKey2");

        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));

        try {

            String modifiedJsonString = String.format(jsonDirectString,p0,p1,p2);
            JSONObject response=jiraclient.sendPostRequest("/issueLink",modifiedJsonString);
            String[] temp= response.getJSONObject("headers").getString("Location").split("/");
            String issueLinkId = temp[temp.length-1];

            modifiedJsonString= String.format(jsonString,issueLinkId);

            response = ConnectorIntegrationUtil.sendRequest(
                    getProxyServiceURL(methodName), modifiedJsonString);

            jiraclient.sendDeleteRequest("/issueLink/"+issueLinkId);
            Assert.assertTrue(response.has("id"));

        } finally {
            proxyAdmin.deleteProxy(methodName);
        }

    }


    @Test(priority = 1, groups = { "wso2.esb" }, description = "jira [getIssueLink] method negative with invalid issue link id.")
    public void testGetIssueLinkNegative() throws Exception {

        String methodName = "getIssueLink";
        String jsonRequestFilePath = pathToRequestsDirectory
                + methodName +".txt";

        final String jsonString = ConnectorIntegrationUtil
                .getFileContent(jsonRequestFilePath);

        final String proxyFilePath = "file:///" + pathToProxiesDirectory
                + methodName + ".xml";

        String p0=connectorProperties.getProperty("invalidIssueLinkId");

        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));

        try {


            String modifiedJsonString= String.format(jsonString,p0);

            int responseCode = ConnectorIntegrationUtil.sendRequestToRetriveHeaders(
                    getProxyServiceURL(methodName), modifiedJsonString);

            Assert.assertFalse(responseCode>=200 && responseCode <300);

        } finally {
            proxyAdmin.deleteProxy(methodName);
        }

    }


    @Test(priority = 1, groups = { "wso2.esb" }, description = "jira [deleteIssueLink] method positive.")
    public void testDeleteIssueLink() throws Exception {

        String methodName = "deleteIssueLink";
        String jsonRequestFilePath = pathToRequestsDirectory
                + methodName +".txt";

        String jsonDirectRequestFilePath = pathToRequestsDirectory
                +"createIssueLinkDirect.txt";

        final String jsonString = ConnectorIntegrationUtil
                .getFileContent(jsonRequestFilePath);

        final String jsonDirectString = ConnectorIntegrationUtil
                .getFileContent(jsonDirectRequestFilePath);

        final String proxyFilePath = "file:///" + pathToProxiesDirectory
                + methodName + ".xml";

        String p0=connectorProperties.getProperty("issueLinkType");
        String p1=connectorProperties.getProperty("inwardIssueKey2");
        String p2=connectorProperties.getProperty("outwardIssueKey2");

        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));

        try {

            String modifiedJsonString = String.format(jsonDirectString,p0,p1,p2);
            JSONObject response=jiraclient.sendPostRequest("/issueLink",modifiedJsonString);
            String[] temp= response.getJSONObject("headers").getString("Location").split("/");
            String issueLinkId = temp[temp.length-1];

            modifiedJsonString= String.format(jsonString,issueLinkId);

            int responseCode = ConnectorIntegrationUtil.sendRequestToRetriveHeaders(
                    getProxyServiceURL(methodName), modifiedJsonString);

            jiraclient.sendDeleteRequest("/issueLink/"+issueLinkId);
            Assert.assertTrue(responseCode>=200 && responseCode <300);

        } finally {
            proxyAdmin.deleteProxy(methodName);
        }

    }


    @Test(priority = 1, groups = { "wso2.esb" }, description = "jira [deleteIssueLink] method negative with invalid issue link id.")
    public void testDeleteIssueLinkNegative() throws Exception {

        String methodName = "deleteIssueLink";
        String jsonRequestFilePath = pathToRequestsDirectory
                + methodName +".txt";

        final String jsonString = ConnectorIntegrationUtil
                .getFileContent(jsonRequestFilePath);

        final String proxyFilePath = "file:///" + pathToProxiesDirectory
                + methodName + ".xml";

        String p0=connectorProperties.getProperty("invalidIssueLinkId");

        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));

        try {


            String modifiedJsonString= String.format(jsonString,p0);

            int responseCode = ConnectorIntegrationUtil.sendRequestToRetriveHeaders(
                    getProxyServiceURL(methodName), modifiedJsonString);

            Assert.assertFalse(responseCode>=200 && responseCode <300);

        } finally {
            proxyAdmin.deleteProxy(methodName);
        }

    }



    @Test(priority = 1, groups = { "wso2.esb" }, description = "jira [createIssueLinkType] method positive.")
    public void testCreateIssueLinkType() throws Exception {

        String methodName = "createIssueLinkType";
        String jsonRequestFilePath = pathToRequestsDirectory
                + methodName +".txt";

        final String jsonString = ConnectorIntegrationUtil
                .getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory
                + methodName + ".xml";

        String p0=connectorProperties.getProperty("issueLinkTypeName");
        String p1=connectorProperties.getProperty("inwardLabel");
        String p2=connectorProperties.getProperty("outwardLabel");

        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));

        try {

            String modifiedJsonString = String.format(jsonString,p0,p1,p2);

            int responseCode = ConnectorIntegrationUtil.sendRequestToRetriveHeaders(
                    getProxyServiceURL(methodName), modifiedJsonString);

            Assert.assertTrue(responseCode>=200 && responseCode <300);

        } finally {
            proxyAdmin.deleteProxy(methodName);
        }

    }

    @Test(priority = 1, groups = { "wso2.esb" }, description = "jira [createIssueLinkType] method negative with existing type.")
    public void testCreateIssueLinkTypeNegative() throws Exception {

        String methodName = "createIssueLinkType";
        String jsonRequestFilePath = pathToRequestsDirectory
                + methodName +".txt";

        final String jsonString = ConnectorIntegrationUtil
                .getFileContent(jsonRequestFilePath);
        final String proxyFilePath = "file:///" + pathToProxiesDirectory
                + methodName + ".xml";

        String p0=connectorProperties.getProperty("invalidIssueLinkTypeName");
        String p1=connectorProperties.getProperty("inwardLabel");
        String p2=connectorProperties.getProperty("outwardLabel");

        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));

        try {

            String modifiedJsonString = String.format(jsonString,p0,p1,p2);

            int responseCode = ConnectorIntegrationUtil.sendRequestToRetriveHeaders(
                    getProxyServiceURL(methodName), modifiedJsonString);

            Assert.assertTrue(responseCode>=400 && responseCode<600);

        } finally {
            proxyAdmin.deleteProxy(methodName);
        }

    }

    @Test(priority = 1, groups = { "wso2.esb" }, description = "jira [getAllIssueLinkTypes] method positive.")
    public void testGetAllIssueLinkTypes() throws Exception {

        String methodName = "getAllIssueLinkTypes";

        final String proxyFilePath = "file:///" + pathToProxiesDirectory
                + methodName + ".xml";

        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));

        try {

            String modifiedJsonString = "";

            JSONObject response = ConnectorIntegrationUtil.sendRequest(
                    getProxyServiceURL(methodName), modifiedJsonString);

            Assert.assertTrue(response.has("issueLinkTypes"));

        } finally {
            proxyAdmin.deleteProxy(methodName);
        }

    }

    @Test(priority = 1, groups = { "wso2.esb" }, description = "jira [getIssueLinkTypeById] method positive.")
    public void testGetIssueLinkTypeById() throws Exception {

        String methodName = "getIssueLinkTypeById";
        String jsonRequestFilePath = pathToRequestsDirectory
                + methodName +".txt";

        String jsonDirectRequestFilePath = pathToRequestsDirectory
                +"createIssueLinkTypeDirect.txt";

        final String jsonString = ConnectorIntegrationUtil
                .getFileContent(jsonRequestFilePath);

        final String jsonDirectString = ConnectorIntegrationUtil
                .getFileContent(jsonDirectRequestFilePath);

        final String proxyFilePath = "file:///" + pathToProxiesDirectory
                + methodName + ".xml";

        String p0=connectorProperties.getProperty("issueLinkTypeName2");
        String p1=connectorProperties.getProperty("inwardLabel2");
        String p2=connectorProperties.getProperty("outwardLabel2");

        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));

        try {

            String modifiedJsonString = String.format(jsonDirectString,p0,p1,p2);
            JSONObject response=jiraclient.sendPostRequest("/issueLinkType",modifiedJsonString);
            String issueLinkTypeId = response.getJSONObject("response").getString("id");

            modifiedJsonString= String.format(jsonString,issueLinkTypeId);

            response = ConnectorIntegrationUtil.sendRequest(
                    getProxyServiceURL(methodName), modifiedJsonString);

            jiraclient.sendDeleteRequest("/issueLinkType/"+issueLinkTypeId);
            Assert.assertTrue(response.has("id"));

        } finally {
            proxyAdmin.deleteProxy(methodName);
        }

    }



    @Test(priority = 1, groups = { "wso2.esb" }, description = "jira [getIssueLinkTypeById] method negative with invalid issue link type id.")
    public void testGetIssueLinkTypeByIdNegative() throws Exception {

        String methodName = "getIssueLinkTypeById";
        String jsonRequestFilePath = pathToRequestsDirectory
                + methodName +".txt";

        final String jsonString = ConnectorIntegrationUtil
                .getFileContent(jsonRequestFilePath);

        final String proxyFilePath = "file:///" + pathToProxiesDirectory
                + methodName + ".xml";

        String p0=connectorProperties.getProperty("invalidIssueLinkTypeId");

        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));

        try {


            String modifiedJsonString= String.format(jsonString,p0);

            int responseCode = ConnectorIntegrationUtil.sendRequestToRetriveHeaders(
                    getProxyServiceURL(methodName), modifiedJsonString);

            Assert.assertTrue(responseCode>=400 && responseCode <600);

        } finally {
            proxyAdmin.deleteProxy(methodName);
        }

    }

    @Test(priority = 1, groups = { "wso2.esb" }, description = "jira [deleteIssueLinkType] method positive.")
    public void testDeleteIssueLinkType() throws Exception {

        String methodName = "deleteIssueLinkType";
        String jsonRequestFilePath = pathToRequestsDirectory
                + methodName +".txt";

        String jsonDirectRequestFilePath = pathToRequestsDirectory
                +"createIssueLinkTypeDirect.txt";

        final String jsonString = ConnectorIntegrationUtil
                .getFileContent(jsonRequestFilePath);

        final String jsonDirectString = ConnectorIntegrationUtil
                .getFileContent(jsonDirectRequestFilePath);

        final String proxyFilePath = "file:///" + pathToProxiesDirectory
                + methodName + ".xml";

        String p0=connectorProperties.getProperty("issueLinkTypeName2");
        String p1=connectorProperties.getProperty("inwardLabel2");
        String p2=connectorProperties.getProperty("outwardLabel2");

        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));

        try {

            String modifiedJsonString = String.format(jsonDirectString,p0,p1,p2);
            JSONObject response=jiraclient.sendPostRequest("/issueLinkType",modifiedJsonString);
            String issueLinkTypeId = response.getJSONObject("response").getString("id");

            modifiedJsonString= String.format(jsonString,issueLinkTypeId);

            int responseCode = ConnectorIntegrationUtil.sendRequestToRetriveHeaders(
                    getProxyServiceURL(methodName), modifiedJsonString);

            jiraclient.sendDeleteRequest("/issueLinkType/"+issueLinkTypeId);
            Assert.assertTrue(responseCode>=200 && responseCode <300);

        } finally {
            proxyAdmin.deleteProxy(methodName);
        }

    }


    @Test(priority = 1, groups = { "wso2.esb" }, description = "jira [deleteIssueLinkType] method negative with invalid issue link type id.")
    public void testDeleteIssueLinkTypeNegative() throws Exception {

        String methodName = "deleteIssueLinkType";
        String jsonRequestFilePath = pathToRequestsDirectory
                + methodName +".txt";

        final String jsonString = ConnectorIntegrationUtil
                .getFileContent(jsonRequestFilePath);

        final String proxyFilePath = "file:///" + pathToProxiesDirectory
                + methodName + ".xml";

        String p0=connectorProperties.getProperty("invalidIssueLinkTypeId");

        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));

        try {


            String modifiedJsonString= String.format(jsonString,p0);

            int responseCode = ConnectorIntegrationUtil.sendRequestToRetriveHeaders(
                    getProxyServiceURL(methodName), modifiedJsonString);

            Assert.assertFalse(responseCode>=200 && responseCode <300);

        } finally {
            proxyAdmin.deleteProxy(methodName);
        }

    }


    @Test(priority = 1, groups = { "wso2.esb" }, description = "jira [updateIssueLinkType] method positive.")
    public void testUpdateIssueLinkType() throws Exception {

        String methodName = "updateIssueLinkType";
        String jsonRequestFilePath = pathToRequestsDirectory
                + methodName +".txt";

        String jsonDirectCreateRequestFilePath = pathToRequestsDirectory
                + "createIssueLinkTypeDirect.txt";

        final String jsonString = ConnectorIntegrationUtil
                .getFileContent(jsonRequestFilePath);

        final String jsonDirectString = ConnectorIntegrationUtil
                .getFileContent(jsonDirectCreateRequestFilePath);

        final String proxyFilePath = "file:///" + pathToProxiesDirectory
                + methodName + ".xml";


        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));

        String p0=connectorProperties.getProperty("issueLinkTypeName2");
        String p1=connectorProperties.getProperty("inwardLabel2");
        String p2=connectorProperties.getProperty("outwardLabel2");


        String p11=connectorProperties.getProperty("issueLinkTypeName3");
        String p12=connectorProperties.getProperty("inwardLabel3");
        String p13=connectorProperties.getProperty("outwardLabel3");


        try {

            String modifiedJsonString = String.format(jsonDirectString,p0,p1,p2);
            JSONObject response=jiraclient.sendPostRequest("/issueLinkType",modifiedJsonString);
            String issueLinkTypeId = response.getJSONObject("response").getString("id");

            modifiedJsonString = String.format(jsonString,issueLinkTypeId,p11,p12,p13);
            ConnectorIntegrationUtil.sendRequestToRetriveHeaders(
                    getProxyServiceURL(methodName), modifiedJsonString);

            JSONObject getResponse=jiraclient.sendGetRequest("/issueLinkType/"+issueLinkTypeId);
            String newName = getResponse.getJSONObject("response").getString("name");

            jiraclient.sendDeleteRequest("/issueLinkType/"+issueLinkTypeId);
            Assert.assertTrue(newName.equals(p11));

        } finally {
            proxyAdmin.deleteProxy(methodName);
        }

    }

    @Test(priority = 1, groups = { "wso2.esb" }, description = "jira [updateIssueLinkType] method negative with invalid issue link ID.")
    public void testUpdateIssueLinkTypeNegative() throws Exception {

        String methodName = "updateIssueLinkType";
        String jsonRequestFilePath = pathToRequestsDirectory
                + methodName +".txt";

        final String jsonString = ConnectorIntegrationUtil
                .getFileContent(jsonRequestFilePath);

        final String proxyFilePath = "file:///" + pathToProxiesDirectory
                + methodName + ".xml";


        proxyAdmin.addProxyService(new DataHandler(new URL(proxyFilePath)));

        String p0=connectorProperties.getProperty("invalidIssueLinkTypeId");
        String p1=connectorProperties.getProperty("issueLinkTypeName2");
        String p2=connectorProperties.getProperty("inwardLabel2");
        String p3=connectorProperties.getProperty("outwardLabel2");

        try {

            String modifiedJsonString = String.format(jsonString,p0,p1,p2,p3);

            int responseCode = ConnectorIntegrationUtil.sendRequestToRetriveHeaders(
                    getProxyServiceURL(methodName), modifiedJsonString);

            Assert.assertTrue(responseCode >= 400 && responseCode < 600);

        } finally {
            proxyAdmin.deleteProxy(methodName);
        }

    }


}
