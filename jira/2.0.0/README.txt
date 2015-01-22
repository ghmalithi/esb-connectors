Product: Integration tests for WSO2 ESB JIRA connector 2.0.0
Pre-requisites:

- Maven 3.x
- Java 1.6 or above

Tested Platform: 

- Ubuntu 14.04
- WSO2 ESB 4.8.1
		  
STEPS:

1. Make sure the ESB 4.8.1 zip file with latest patches available at "Integration_Test\products\esb\4.8.1\modules\distribution\target\".

2. This ESB should be configured as below;
	In Axis configurations (\repository\conf\axis2\axis2.xml).

   i) Enable message formatter for "text/html"
		<messageFormatters>
			<messageFormatter contentType="text/html" class="org.wso2.carbon.relay.ExpandingMessageFormatter"/>\
		</messageFormatters>

   ii) Enable message builder for "text/html"
		<messageBuilders>
			<messageBuilder contentType="text/html" class="org.wso2.carbon.relay.BinaryRelayBuilder"/>
		</messageBuilders>

3. Copy jira connector zip file (jira.zip) to the location "Integration_Test\products\esb\4.8.1\modules\integration\connectors\repository\"

4. Add following code block, just after the listeners block (Remove or comment all the other test blocks) in following file - "Integration_Test\products\esb\4.8.1\modules\integration\connectors\src\test\resources\testng.xml"

    <test name="Jira-Connector-Test" preserve-order="true" verbose="2">
        <packages>
            <package name="org.wso2.carbon.connector.integration.test.jira"/>
        </packages>
    </test>

5. Copy proxy files to following location "Integration_Test\products\esb\4.8.1\modules\integration\connectors\src\test\resources\artifacts\ESB\config\proxies\jira\"

6. Copy request files to following "Integration_Test\products\esb\4.8.1\modules\integration\connectors\src\test\resources\artifacts\ESB\config\requests\jira\" 

7. Edit the "jira.properties" at Integration_Test\products\esb\4.8.1\modules\integration\connectors\src\test\resources\artifacts\ESB\connector\config using valid and relevant data. Parameters to be changed are mentioned below. 
	
	- username,password,authheader: Create a system admin account in jira and replace the username and password. Use the Base64 encoding of the password as the authheader (Basic <Base64 encoding of the password>)	
	- url,resturidirect: URL of jira, REST API url of jira ; <url>/rest/api/2
	- validGroupName,createGroupName2,deleteGroupName,removeUserGroup,removeRoleGroup : create 5 groups in jira and assign their names to those variables
	- validUserName,createUsername2,deleteUserName,removeRoleUser : create 4 users in jira and assign their names to those variables

8. Configure Atlassian Jira with these configurations. Use names mentioned in {<name>} from your configurations at jira.properties

	- create project with the key "PROJ"
	- create 5 issues in project "PROJ"
	- add {validUserName} to {removeUserGroup}
	- add {removeRoleUser} to admin role of project "PROJ"
	- add {removeRoleGroup} to developer role of project "PROJ" 

8. Navigate to "Integration_Test\products\esb\4.8.1\modules\integration\connectors\” and run the following command.
     $ mvn clean install