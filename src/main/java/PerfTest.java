import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.AsyncHttpClientConfig;
import org.asynchttpclient.DefaultAsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClientConfig;
import org.asynchttpclient.ListenableFuture;
import org.asynchttpclient.Request;
import org.asynchttpclient.Response;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

public class PerfTest extends Thread{

  private Executor executor = new ThreadPoolExecutor(
      4, Math.max(Runtime.getRuntime().availableProcessors(), 16), 10, TimeUnit.SECONDS,
      new LinkedBlockingDeque<Runnable>(1000));

  private String connectionTimeout= "5000";
  private String readTimeout="15000";
  private String pooledConnectionTimeout="300000";
  private String retryOnConnectionFailure="true";
  private String followRedirect="true";
  private String maxRedirects="5";

  AsyncHttpClientConfig config = new DefaultAsyncHttpClientConfig.Builder()
      .setConnectTimeout(Integer.parseInt(connectionTimeout))
      .setReadTimeout(Integer.parseInt(readTimeout))
      .setPooledConnectionIdleTimeout(
          Integer.parseInt(pooledConnectionTimeout))
      .setMaxRequestRetry(Boolean.valueOf(retryOnConnectionFailure) ? 1 : 0)
      .setFollowRedirect(Boolean.valueOf(followRedirect))
      .setMaxRedirects(Integer.parseInt(maxRedirects))
//      .setMaxConnections(1)
      .build();

  AsyncHttpClient client = new DefaultAsyncHttpClient(config);


  public static void main(String[] args) {
    PerfTest perfTest = new PerfTest();
    for (int i =0; i< 10000; i++){
      System.out.println("iteration: " + i);
      perfTest.runTest(i);
      if(i%30 == 0) {
        try {
          System.out.println("sleep for 1 sec");
          Thread.sleep(1000L);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    }
  }

  public void runTest(int i){
    JSONObject data = new JSONObject();
    data.put("query", "What tool will become my default browser and then prompt me for which browser to open a link on MacOS");
    data.put("user_id", "perf_user_id_5_774");
    data.put("bot_ref", 748);
    data.put("bot_locale", "en");
    data.put("api_ai_client_token", "123123212332121");
    data.put("custom_logic", null);

    if(client.isClosed()){
      client = new DefaultAsyncHttpClient(config);
    }
    Request request =
        client.preparePost( "http://172.16.28.71:1933/api/v2/get_matching_response")
            .setBody(data.toJSONString()).setHeader("Content-Type", "application/json").build();
    ListenableFuture<Response> response = client.executeRequest(request);
    response.addListener(() -> {
      try {
        Response response1 = response.get();
        System.out.println("Response for iteration " + i +"  res: "+ response1.getStatusCode());
//        Thread.sleep(100L);
      } catch (Exception e) {
        System.out.println("Failed while processing answers response iter: " + i + " exec:" + e);

      }

    }, executor);

  }
}
