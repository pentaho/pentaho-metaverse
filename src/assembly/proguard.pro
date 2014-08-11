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

-keep abstract class com.pentaho.metaverse.locator.** {
  public <methods>;
}

-keep public class com.pentaho.metaverse.locator.** extends com.pentaho.metaverse.locator.** {
  public <methods>;
}

-keep public class com.pentaho.metaverse.** implements org.pentaho.platform.api.metaverse.IRequiresMetaverseBuilder {
  public <methods>;
}

-keep public class com.pentaho.metaverse.analyzer.kettle.step.IStepAnalyzerProvider {}
