vital-cytoscape
===============

vital endpoint plugin for cytoscape

Requirements:
- maven
- Vital AI software with $VITAL_HOME environment variable set


Build and package 

    mvn clean package

the plugin jar will be available at
<project.root>/target/vital-cytoscape-<version>.jar



Notes:
* Uses a VitalService implementation as the storage/query/computation backend
* VitalService LuceneMemory is available, but only ephemoral
* VitalService LuceneDisk is available
* VitalService DynamoDB is not available due to a dependency conflict, which may be resolved in the future
* VitalService Triplestore is available
* VitalService IndexDB is available (but not with DynamoDB as the database implementation as above)
* VitalService VitalPrime is available (VitalPrime can use DynamoDB as its storage backend)
* The VitalPrime implementation makes available DataScripts such that computation can occur in a cluster.

