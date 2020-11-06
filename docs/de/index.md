---
title: Dokumentation
subtitle: Dokumentation des SCM-Manager Plugins für Jenkins
---

Das SCM-Manager-Plugin für den Jenkins bietet die folgende Funktionalität:

- Sobald das Plugin im Jenkins installiert wurde, werden automatisch Jobs erkannt, welche den SCM-Manager 2 nutzen.
   Startet ein solcher Job, sendet das Plugin einen `PENDING` Status für die entsprechende Revision and den SCM-Manager.
   Nachdem der Build abgeschlossen ist, wird das Ergebnis als Status an den SCM-Manager gesendet (`SUCCESS`, `UNSTABLE`
   oder `FAILURE`).
- Für sog. "**Multibranch Pipelines**" gibt es eine neue "B"ranch Source" names "SCM-Manager", so dass Pipelines in Jenkins
   erstellt werden können, die selbständig branches, tags and pull requests in Repositories finden, die im SCM-Manager
   verwaltet werden. Im Zusammenspiel mit dem
   [jenkins plugin](https://www.scm-manager.org/plugins/scm-jenkins-plugin/) im SCM-Manager erhält Jenkins sog. Hooks
   bei jeder relevanten Änderungen und stößt neue Builds an.
   
   Um eine solche Pipeline zu erstellen, muss zunächst "New Item" auf der Startseite von Jenkins gewählt werden. Danach
   muss ein Name eingetragen und der Typ "Multibranch Pipeline" gewählt werden.
   ![](assets/select-multibranch-pipeline.png)
   In der darauf folgenden Konfiguration muss in der Sektion "Branch Sources" über das Drop-Down "Add source" der zu dem
   Typen des Repositories passende "SCM-Manager" Eintrag gewählt werden.
   ![](assets/config-multibranch-pipeline-source.png)
   In dem neuen Abschnitt kann nun die Base URL der SCM-Manager-Instanz eingetragen sowie die passende Authentifizierung
   gewählt werden. Das Plugin ládt nun eine Liste aller zur Verfügung stehenden Repositories, aus der eines gewählt
   werden kann.
   ![](assets/config-multibranch-pipeline.png)
   Zum Abschluss können verschiedene Verhaltensweisen gewählt und konfiguriert werden, wie z. B. ob Branches, Tags oder
   Pull Requests gebaut werden sollen.
- Sollen für alle Repositories eines **kompletten Namespaces** im SCM-Manager Jobs erzeugt werden, kann ein "SCM-Manager
   namespace" Job genutzt werden. Dieser prüft alle Repositories in einem gegebenen Namespace und erzeugt entsprechende
   Multibranch Pipelines, wenn in einem Repository ein `Jenkinsfile` gefunden wurde. Wurde ind dem Namespace ein neues
   passendes Repository erzeugt, wird automatisch eine neue Pipeline erstellt.
   Um nicht mehr vorhandene Build Jobs zu entfernen, kann manuell "Scan Namespace Now" gestartet werden.
   
   Um einen solchen Ordner für einen kompletten Namespace zu erstellen, muss zunächst "New Item" auf der Startseite
   von Jenkins gewählt werden. Danach kann ein Name eingetragen und der Punkt "SCM-Manager Namespace" gewählt werden.
   ![](assets/select-namespace-item.png)
   In der Konfiguration muss nun die Base URL der SCM-Manager Instanz eingetragen und passende Authentifizierung
   gewählt werden. Das Plugin lädt daraufhin alle verfügbaren Namesaces, von denen einer gewählt werden kann.
   ![](assets/config-namespace-item.png)
   Abschließend können verschiedene Verhaltensweise gewählt und konfiguriert werden, wie z. B. ob Branches, Tags oder
   Pull Requests gebaut werden sollen.

## Voraussetzungen

Für die volle Funktionalität wird ein SCM-Manager v2 mit installiertem
[jenkins Plugin](https://www.scm-manager.org/plugins/scm-jenkins-plugin/) benötigt.
