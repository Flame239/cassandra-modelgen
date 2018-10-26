# cassandra-modelgen
Generate static metamodel classes from spring-annotated cassandra entities which allows queries to be
        constructed in a strongly-typed manner.

## Usage
```xml
<plugin>
  <artifactId>maven-compiler-plugin</artifactId>
  <version>${maven-compiler-plugin.version}</version>
  <configuration>
    <source>1.8</source>
    <target>1.8</target>
    <annotationProcessors>
      <annotationProcessor>com.flame239.cassandra.modelgen.CassandraModelProcessor</annotationProcessor>
    </annotationProcessors>
  </configuration>
</plugin>
...
<dependency>
  <groupId>com.flame239</groupId>
  <artifactId>cassandra-modelgen</artifactId>
  <version>1.0.0</version>
</dependency>

```

After compile you can use generated metamodel classes with _ postfix to create queries.

In @Accessor annotated class:
```java
    @Query("SELECT * FROM " + UserData_._table + " " +
    "WHERE " + UserDat_.userId + " = :userId")
    Result<UserData> findUserById(String userId);
```

Using `QueryBuilder`:
```java
     Statement refresh = update(RoomDweller_._table)
            .with(set(RoomDweller_.lastUpdate, System.currentTimeMillis()))
            .where(eq(RoomDweller_.userId, userId));
```

