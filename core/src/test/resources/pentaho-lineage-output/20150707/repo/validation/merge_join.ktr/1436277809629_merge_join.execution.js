{
  "name" : "merge_join",
  "description" : null,
  "path" : "/Users/rfellows/code/git/pentaho-metaverse/core/src/it/resources/repo/validation/merge_join.ktr",
  "type" : "Transformation",
  "executionEngine" : {
    "name" : "Pentaho Data Integration",
    "description" : "Pentaho data integration prepares and blends data to create a complete picture of your business that drives actionable insights.",
    "version" : "6.0-SNAPSHOT"
  },
  "executionData" : {
    "startTime" : 1436277809629,
    "endTime" : null,
    "failureCount" : 0,
    "executorServer" : "localhost",
    "executorUser" : "rfellows",
    "clientExecutor" : "SPOON",
    "loggingChannelId" : null,
    "parameters" : [ ],
    "externalResources" : {
      "Table input" : [ {
        "name" : "SampleData",
        "description" : null,
        "type" : "JDBC",
        "attributes" : {
          "query" : "SELECT\n  ORDERNUMBER\n, QUANTITYORDERED\n, PRICEEACH\n, ORDERLINENUMBER\n, SALES\n, ORDERDATE\n, STATUS\n, QTR_ID\n, MONTH_ID\n, YEAR_ID\n, PRODUCTLINE\n, MSRP\n, PRODUCTCODE\n, CUSTOMERNAME\n, PHONE\n, ADDRESSLINE1\n, ADDRESSLINE2\n, CITY\n, STATE\n, POSTALCODE\n, COUNTRY\n, TERRITORY\n, CONTACTLASTNAME\n, CONTACTFIRSTNAME\n, DEALSIZE\nFROM SALES_DATA\nORDER BY COUNTRY, STATE\n"
        },
        "pluginId" : "H2",
        "port" : -1,
        "server" : null,
        "username" : null,
        "password" : "",
        "databaseName" : "${Internal.Transformation.Filename.Directory}/../SampleData",
        "input" : true,
        "output" : false
      } ]
    },
    "variables" : {
      "Internal.Transformation.Filename.Directory" : "file:///Users/rfellows/code/git/pentaho-metaverse/core/src/it/resources/repo/validation"
    },
    "arguments" : [ ],
    "artifactMetadata" : null,
    "userMetadata" : null
  }
}
