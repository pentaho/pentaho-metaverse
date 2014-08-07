-keepparameternames

-keep public class com.pentaho.metaverse.api.** {
  public <methods>;
}

-keep !abstract class com.pentaho.metaverse.** implements com.pentaho.metaverse.api.** {
  public <methods>;
}

-keep public class com.pentaho.metaverse.graph.SynchronizedGraph {
  public <methods>;
}

-keep public class com.pentaho.metaverse.graph.SynchronizedGraphFactory {
  public <methods>;
}
