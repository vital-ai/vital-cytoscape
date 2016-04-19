vital-cytoscape
===============

vital endpoint plugin for cytoscape

Requirements:
- maven
- Vital AI software with $VITAL_HOME environment variable set

Check the release branch for any recent differences.
Current active branch is: rel-0.2.300



Build and package 

    mvn clean package

the plugin jar will be available at
<project.root>/target/vital-cytoscape-<version>.jar



Notes:
* Uses a VitalService implementation as the storage/query/computation backend
* VitalService LuceneMemory is available, but only ephemoral
* VitalService LuceneDisk is available
* VitalService DynamoDB is available
* VitalService SparQL is available
* VitalService SQL is available
* VitalService IndexDB is available 
* VitalService VitalPrime is available 
* The VitalPrime implementation makes available DataScripts such that computation can occur in a cluster.

