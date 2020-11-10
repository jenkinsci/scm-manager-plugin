---
title: Dokumentation
subtitle: Dokumentation des SCM-Manager Plugins für Jenkins
---

Das SCM-Manager-Plugin für den Jenkins bietet mehrere Wege der Verbindung eines SCM-Managers mit Jenkins, wie die
Anzeige von Ergebnissen der Builds, sog. "Multibranch Pipelines", Ordner für komplette Namespaces und verschiedene
Navigationen.

### Build Ergebnisse
Sobald das Plugin im Jenkins installiert wurde, werden automatisch Jobs erkannt, welche den SCM-Manager 2 nutzen.
Startet ein solcher Job, sendet das Plugin einen `PENDING` Status für die entsprechende Revision and den SCM-Manager.
Nachdem der Build abgeschlossen ist, wird das Ergebnis als Status an den SCM-Manager gesendet (`SUCCESS`, `UNSTABLE`
oder `FAILURE`).

### Multibranch Pipelines
Für sog. "**Multibranch Pipelines**" gibt es eine neue "Branch Source" names "SCM-Manager", sodass Pipelines in Jenkins
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

### Namespaces
Sollen für alle Repositories eines **kompletten Namespaces** im SCM-Manager Jobs erzeugt werden, kann ein "SCM-Manager
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

### Navigation
Auf verschiedenen Seiten von Jenkins befinden sich Links zu entsprechenden Seiten im SCM-Manager:

- In Multibranch Pipelines befindet sich in der linken Hauptnavigation der Link "SCM-Manager". Dieser führt direkt
  auf die Hauptseite des Repositories im SCM-Manager.
- In einem Job für einen konkreten Branch oder einen Tag führt der Link "SCM-Manager" in der linken Hauptnavigation
  zu der Anzeige der Quellen im SCM-Manager für diesen Branch bzw. Tag.
- In einem Job für einen Pull Request führt der Link "SCM-Manager" in der linken Hauptnavigation zu dem Pull Request
  im SCM-Manager.
- Auf der Seite für einen konkreten Build führt der Link "SCM-Manager" in der linken Hauptnavigation zu der Anzeige
  der Quellen für die konkrete Revision, die gebaut wurde.
- In der Anzeige der "Changes" befinden sich Links, die zu den Details einer Revision im SCM-Manager führen, wo die
  Autoren, die Commit Nachricht sowie das Diff zu diesem Commit gezeigt werden.

## Voraussetzungen

Für die volle Funktionalität wird ein SCM-Manager v2 mit installiertem
[jenkins Plugin](https://www.scm-manager.org/plugins/scm-jenkins-plugin/) benötigt.
