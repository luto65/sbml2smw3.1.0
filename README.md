# sbml2smw3.1.0
The SBML2SMW system had been developed in 2010. Since then pretty much changed in the SMW back end and as-is the 2010 deployment is no longer working. Purpose of this repository is to refresh the java components and make it therefore up-to-date with SMW3.1.0

# translationServer

## build

````
mvn package
````
## deploy 
ensure that the following folder structure holds:
- ts3.jar
- webapps/ROOT/WEB-INF/web.xml
- etc/jetty.xml
- etc/webdefault.html
- etc/realm.properties


## run
````
/usr/bin/java -cp ts3.jar il.org.tibc.sbml2smw.tS.TranslationServer
````


The following call will store an object of type PROTEIN with name s4 etc.
`````
GET  http://18.184.71.102:9090/store/species/x?type=PROTEIN&name=s4&hyp=false&language=en&resolve=true&annotation=&protType=GENERIC&wiki=http://18.184.71.102/&namespace=http://18.184.71.102/&language=en&resolve=true&user=#######&pass=###
````
