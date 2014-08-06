package org.wso2.carbon.connector.integration.test.common;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;


public class JiraRestClient {

    private String authHeader="";
    private String apiURL="";

    public JiraRestClient(String apiURL){
        this.apiURL=apiURL;
    }

    public void setAuthHeader(String authHeader) {
        this.authHeader = authHeader;
    }

    private JSONObject sendRequestWithoutContent(String resource,String method) throws IOException, JSONException {

        HttpURLConnection connection = (HttpURLConnection)new URL(apiURL + resource).openConnection();

        String charset = "UTF-8";
        connection.setRequestProperty("Accept-Charset", charset);
        connection.setRequestProperty("Content-Type", "application/json;charset=" + charset);
        connection.setRequestProperty("Authorization",authHeader);
        connection.setRequestMethod(method);

        HttpURLConnection httpConn = (HttpURLConnection) connection;
        InputStream response;

        int responseCode=httpConn.getResponseCode();

        if (responseCode >= 400) {
            response = httpConn.getErrorStream();
        } else {
            response = connection.getInputStream();
        }

        String out = "{}";
        if (response != null) {
            StringBuilder sb = new StringBuilder();
            byte[] bytes = new byte[1024];
            int len;
            while ((len = response.read(bytes)) != -1) {
                sb.append(new String(bytes, 0, len));
            }
            if (!sb.toString().trim().isEmpty()) {
                out = sb.toString();
            }
        }

        out="{httpsc:"+responseCode+",response:"+out+"}";

        JSONObject jsonObject = new JSONObject(out);

        return jsonObject;
    }

    private JSONObject sendRequestWithContent(String resource, String query,String method) throws IOException, JSONException {
        String charset = "UTF-8";

        HttpURLConnection connection = (HttpURLConnection)new URL(apiURL + resource).openConnection();
        connection.setDoOutput(true);
        connection.setRequestProperty("Accept-Charset", charset);
        connection.setRequestProperty("Content-Type", "application/json;charset=" + charset);
        connection.setRequestProperty("Authorization",authHeader);
        connection.setRequestMethod(method);

        OutputStream output = null;
        try {
            output = connection.getOutputStream();
            output.write(query.getBytes(charset));
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException logOrIgnore) {
                    System.out.println("Error while closing the connection");
                }
            }
        }

        HttpURLConnection httpConn = (HttpURLConnection) connection;
        InputStream response;
        int responseCode = httpConn.getResponseCode();
        if (httpConn.getResponseCode() >= 400) {
            response = httpConn.getErrorStream();
        } else {
            response = connection.getInputStream();
        }

        String out = "{}";
        if (response != null) {
            StringBuilder sb = new StringBuilder();
            byte[] bytes = new byte[1024];
            int len;
            while ((len = response.read(bytes)) != -1) {
                sb.append(new String(bytes, 0, len));
            }

            if (!sb.toString().trim().isEmpty()) {
                out = sb.toString();
            }
        }

        out="{httpsc:"+responseCode+",response:"+out+"}";

        JSONObject jsonObject = new JSONObject(out);
        return jsonObject;
    }

    public JSONObject sendGetRequest(String resource) throws IOException, JSONException
    {
        return sendRequestWithoutContent(resource,"GET");
    }

    public JSONObject sendPostRequest(String resource, String query) throws IOException, JSONException {
        return sendRequestWithContent(resource,query,"POST");
    }

    public JSONObject sendDeleteRequest(String resource) throws IOException, JSONException {
        return sendRequestWithoutContent(resource, "DELETE");
    }

}
