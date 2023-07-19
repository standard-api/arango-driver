package ai.stapi.arangograph.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("arango.arangodb")
public class ArangoConfigurationProperties {

  private String host = "127.0.0.1";
  private String user;
  private String password;
  private String port = "8529";

  public String getHost() {
    return host;
  }

  public void setHost(String host) {
    this.host = host;
  }

  public String getUser() {
    return user;
  }

  public void setUser(String user) {
    this.user = user;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getPort() {
    return port;
  }

  public void setPort(String port) {
    this.port = port;
  }
}
