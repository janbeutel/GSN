error id: file://<WORKSPACE>/gsn-core/src/main/java/ch/epfl/gsn/config/GsnConf.java
file://<WORKSPACE>/gsn-core/src/main/java/ch/epfl/gsn/config/GsnConf.java
### com.thoughtworks.qdox.parser.ParseException: syntax error @[39,6]

error in qdox parser
file content:
```java
offset: 1586
uri: file://<WORKSPACE>/gsn-core/src/main/java/ch/epfl/gsn/config/GsnConf.java
text:
```scala
package ch.epfl.gsn.config;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import java.util.Optional;

@Data
public class GsnConf {
    private final int monitorPort;
    private final String timeFormat;
    private final ZmqConf zmqConf;
    private final StorageConf storageConf;
    private final Optional<StorageConf> slidingConf;
    private final int maxDBConnections;
    private final int maxSlidingDBConnections;
    private final BacklogCommandsConf backlogCommandsConf;

    @JsonCreator
    public GsnConf(
        @JsonProperty("monitor-port") int monitorPort,
        @JsonProperty("time-format") String timeFormat,
        @JsonProperty("zmq-conf") ZmqConf zmqConf, // Assuming tag name
        @JsonProperty("storage") StorageConf storageConf,
        @JsonProperty("sliding") Optional<StorageConf> slidingConf,
        @JsonProperty("max-db-connections") int maxDBConnections,
        @JsonProperty("max-sliding-db-connections") int maxSlidingDBConnections,
        @JsonProperty("backlog-commands") BacklogCommandsConf backlogCommandsConf
    ) {
        this.monitorPort = monitorPort;
        this.timeFormat = timeFormat;
        this.zmqConf = zmqConf;
        this.storageConf = storageConf;
        this.slidingConf = slidingConf;
        this.maxDBConnections = maxDBConnections;
        this.maxSlidingDBConnections = maxSlidingDBConnections;
        this.backlogCommandsConf = backlogCommandsConf;
    }@@
```

```



#### Error stacktrace:

```
com.thoughtworks.qdox.parser.impl.Parser.yyerror(Parser.java:2025)
	com.thoughtworks.qdox.parser.impl.Parser.yyparse(Parser.java:2147)
	com.thoughtworks.qdox.parser.impl.Parser.parse(Parser.java:2006)
	com.thoughtworks.qdox.library.SourceLibrary.parse(SourceLibrary.java:232)
	com.thoughtworks.qdox.library.SourceLibrary.parse(SourceLibrary.java:190)
	com.thoughtworks.qdox.library.SourceLibrary.addSource(SourceLibrary.java:94)
	com.thoughtworks.qdox.library.SourceLibrary.addSource(SourceLibrary.java:89)
	com.thoughtworks.qdox.library.SortedClassLibraryBuilder.addSource(SortedClassLibraryBuilder.java:162)
	com.thoughtworks.qdox.JavaProjectBuilder.addSource(JavaProjectBuilder.java:174)
	scala.meta.internal.mtags.JavaMtags.indexRoot(JavaMtags.scala:49)
	scala.meta.internal.metals.SemanticdbDefinition$.foreachWithReturnMtags(SemanticdbDefinition.scala:99)
	scala.meta.internal.metals.Indexer.indexSourceFile(Indexer.scala:560)
	scala.meta.internal.metals.Indexer.$anonfun$reindexWorkspaceSources$3(Indexer.scala:691)
	scala.meta.internal.metals.Indexer.$anonfun$reindexWorkspaceSources$3$adapted(Indexer.scala:688)
	scala.collection.IterableOnceOps.foreach(IterableOnce.scala:630)
	scala.collection.IterableOnceOps.foreach$(IterableOnce.scala:628)
	scala.collection.AbstractIterator.foreach(Iterator.scala:1313)
	scala.meta.internal.metals.Indexer.reindexWorkspaceSources(Indexer.scala:688)
	scala.meta.internal.metals.MetalsLspService.$anonfun$onChange$2(MetalsLspService.scala:940)
	scala.runtime.java8.JFunction0$mcV$sp.apply(JFunction0$mcV$sp.scala:18)
	scala.concurrent.Future$.$anonfun$apply$1(Future.scala:691)
	scala.concurrent.impl.Promise$Transformation.run(Promise.scala:500)
	java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1136)
	java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:635)
	java.base/java.lang.Thread.run(Thread.java:840)
```
#### Short summary: 

QDox parse error in file://<WORKSPACE>/gsn-core/src/main/java/ch/epfl/gsn/config/GsnConf.java