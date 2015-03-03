# Simple JSF Exporter

## What is it?
This is JSF component for exporting data from other components like Primeface's datatable

The problem with existing PrimeFace data table exporters is that it is difficult to customize it (like file format, headers row format, etc.)
If you don't want to use complicated frameworks like com.lapis.jsfexporter, then simple jsf-exporter is what you need.

## License
Apache 2.0, so you can use this in commercial projects.

## Release
1. mvn clean install
2. mvn release:prepare
3. checkout the newly created tag
4. release.bat mysecretpassword


Step 2 can be done manually:
a) remove -SNAPSHOT from the version in all pom.xml files (the parent pom.xml and all module's pom.xml)
b) commit the changes and create new tag with the version
c) add -SNAPSHOT to all pom.xml files and increase the version (e.g. 1.0.0 to 1.0.1-SNAPSHOT)

