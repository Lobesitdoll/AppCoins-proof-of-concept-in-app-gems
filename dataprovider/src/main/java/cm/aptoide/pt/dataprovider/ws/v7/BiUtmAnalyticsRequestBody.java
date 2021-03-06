package cm.aptoide.pt.dataprovider.ws.v7;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Created by pedroribeiro on 28/06/17.
 */

@Data public class BiUtmAnalyticsRequestBody extends BaseBody {

  private final Data data;

  public BiUtmAnalyticsRequestBody(Data data) {
    this.data = data;
  }

  @lombok.Data public static class Data {
    private String entryPoint;
    private String siteVersion;
    private App app;
    private UTM utm;
    private String userAgent;
  }

  @lombok.Data public static class App {
    private String url;
    @JsonProperty("package") private String packageName;
  }

  @lombok.Data public static class UTM {
    private String source;
    private String medium;
    private String campaign;
    private String content;
  }
}
