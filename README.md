ElologU-GenDataBase
===================

Soft qui crée et peuple la database du projet EcologU


Pour compiler le projet taper

mvn clean install assembly:single 

éxécuter le jar file 

java -jar dbGenerator-1.0-SNAPSHOT-jar-with-dependencies.jar


Il se connecte alors à la javadb via derby et crée la base Ecolog_U et la peuple.

