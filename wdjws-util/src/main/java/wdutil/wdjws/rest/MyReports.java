package wdutil.wdjws.rest;

import java.io.ByteArrayInputStream;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

public class MyReports {

	private static Client client;

	public static Client getClient() throws Exception {
		if (client == null) {
			client = ClientBuilder.newClient();
		}
		return client;
	}

	public static void listMyReports(String serviceHost, String uid, String clientId, String password) throws Exception {

		Client client = getClient();
		Authenticator auth = new Authenticator(String.format("%s@%s", uid, clientId), password);
		client.register(auth);

		Response response = client.target(getMyReportsURL(serviceHost, clientId)).request().header("X-Tenant", clientId).get();

	}

	public static void downloadFile(String fileURL, String uid, String clientId, String password, OutputStream out) throws Exception {

		Client client = ClientBuilder.newClient();
		Authenticator auth = new Authenticator(String.format("%s@%s", uid, clientId), password);
		Response response = client.target(fileURL).register(auth).request().header("X-Tenant", clientId).get();
		try (InputStream in = response.readEntity(InputStream.class);) {
			int n;
			byte[] buffer = new byte[1024];
			while ((n = in.read(buffer)) > -1) {
				out.write(buffer, 0, n);
			}
			out.close();
		} finally {
			response.close();
			client.close();
		}
	}

	public static byte[] downloadFile(String fileURL, String uid, String clientId, String password) throws Exception {
		Client client = ClientBuilder.newBuilder().connectTimeout(90, TimeUnit.SECONDS).readTimeout(90, TimeUnit.SECONDS).build();
		Authenticator auth = new Authenticator(String.format("%s@%s", uid, clientId), password);
		Response response = client.target(fileURL).register(auth).request().header("X-Tenant", clientId).get();
		try {
			return response.readEntity(byte[].class);
		} finally {
			response.close();
			client.close();
		}
	}

	public static byte[] uploadFile(String fileURL, byte[] contents, String mimeType, String uid, String clientId, String password) throws Exception {
		Client client = ClientBuilder.newBuilder().connectTimeout(90, TimeUnit.SECONDS).readTimeout(90, TimeUnit.SECONDS).build();
		Authenticator auth = new Authenticator(String.format("%s@%s", uid, clientId), password);
		Response response = client.target(fileURL).register(auth).request().header("X-Tenant", clientId).put(Entity.entity(new ByteArrayInputStream(contents), mimeType));
		try {
			return response.readEntity(byte[].class);
		} finally {
			response.close();
			client.close();
		}
	}

	public static String getMyReportsURL(String serviceHost, String clientId) {
		return String.format("https://%s/ccx/cc-blobitory/%s", serviceHost, clientId);
	}

	public static String getMyReportsFileURL(String serviceHost, String clientId, String docID) {
		return String.format("%s/%s", getMyReportsURL(serviceHost, clientId), docID);
	}

}
