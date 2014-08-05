-keepparameternames

-keep public class com.pentaho.metaverse.api.** {
  public <methods>;
}

-keep !abstract class com.pentaho.metaverse.** implements com.pentaho.metaverse.api.** {
  public <methods>;
}

