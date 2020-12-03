---
title: Dokumentation
subtitle: Dokumentation des SCM-Manager Plugins für Jenkins
---

Das SCM-Manager-Plugin für den Jenkins bietet mehrere Möglichkeiten der Kommunikation eines SCM-Managers mit Jenkins, wie die
Anzeige von Ergebnissen der Builds, sog. "Multibranch Pipelines", Ordner für komplette Namespaces und verschiedene
Navigationen.

### Build Ergebnisse
Sobald das Plugin im Jenkins installiert wurde, werden automatisch Jobs erkannt, welche den SCM-Manager 2 nutzen.
Startet ein solcher Job, sendet das Plugin einen `PENDING` Status für die entsprechende Revision an den SCM-Manager.
Nachdem der Build Job abgeschlossen ist, wird das Ergebnis als Status an den SCM-Manager gesendet (`SUCCESS`, `UNSTABLE`
oder `FAILURE`).

### Multibranch Pipelines
Für sog. "**Multibranch Pipelines**" gibt es eine neue "Branch Source" names "SCM-Manager", sodass Pipelines in Jenkins
erstellt werden können, die selbständig Branches, Tags und Pull Requests in Repositories finden, die im SCM-Manager
verwaltet werden. Im Zusammenspiel mit dem
[Jenkins Plugin](https://www.scm-manager.org/plugins/scm-jenkins-plugin/) im SCM-Manager erhält Jenkins sog. Hooks
bei jeder relevanten Änderungen und stößt neue Builds an.

Um eine solche Pipeline zu erstellen, muss zunächst "New Item" auf der Startseite von Jenkins gewählt werden. Danach
muss ein Name eingetragen und der Typ "Multibranch Pipeline" gewählt werden.

![How to select a multibranch pipeline](assets/select-multibranch-pipeline.png)

In der darauf folgenden Konfiguration muss in der Sektion "Branch Sources" über das Drop-Down "Add source" der zu dem
Typen des Repositories passende "SCM-Manager" Eintrag gewählt werden.

![How to select a multibranch pipeline source](assets/config-multibranch-pipeline-source.png)

In dem neuen Abschnitt kann nun die Base URL der SCM-Manager-Instanz eingetragen sowie die passende Authentifizierung
gewählt werden. Anschließend wird eine Liste aller zur Verfügung stehenden Repositories geladen, von denen eines gewählt
werden muss.

![Example of a config for a multibranch pipeline](assets/config-multibranch-pipeline.png)

Zum Abschluss können verschiedene Verhaltensweisen gewählt und konfiguriert werden, wie z. B. ob Branches, Tags oder
Pull Requests gebaut werden sollen.

#### JobDSL

Um ein Build Job für ein SCM-Manager Repository mit Hilfe der JobDSL anzulegen, 
kann folgende Syntax für Mercurial und Git Repositories verwendet werden:

```groovy
multibranchPipelineJob('heart-of-gold') {
  branchSources {
    scmManager {
      id('spaceships/heart-of-gold')
      serverUrl('https://scm.hitchhiker.com')
      credentialsId('my-secret-id')
      repository('spaceships/heart-of-gold')
      discoverBranches(true)
      discoverPullRequest(true)
      discoverTags(false)
    }
  }
}
```

Die Parameter `discoverBranches`, `discoverPullRequest` und `discoverTags` sind Optional und bilden ab welche Typen gebaut werden sollen.
Das Beispiel zeigt die Standardwerte.

Die Syntax für ein Subversion Repository zeigt folgendes Beispiel:

```groovy
multibranchPipelineJob('heart-of-gold') {
  branchSources {
    scmManagerSvn {
      id('spaceships/heart-of-gold')
      serverUrl('https://scm.hitchhiker.com')
      credentialsId('my-secret-id')
      repository('spaceships/heart-of-gold')
      includes("trunk,branches/*,tags/*,sandbox/*")
      excludes("")
    }
  }
}
```

Die Parameter für `includes` und `excludes` sind ebenfalls optional und mit ihnen kann bestimmt werden, 
welche Ordner des Repositories gebaut werden.
Das Beispiel zeigt die Standardwerte.

### Namespaces
Sollen für alle Repositories eines **kompletten Namespaces** im SCM-Manager Jobs erzeugt werden, kann ein "SCM-Manager
Namespace" Job genutzt werden. Dieser prüft alle Repositories in einem gegebenen Namespace und erzeugt entsprechende
Multibranch Pipelines, wenn im Root Verzeichnis des Repositories ein `Jenkinsfile` gefunden wurde. Wird in dem Namespace
ein passendes neues Repository erzeugt, wird automatisch der dazugehörige Build Job im Jenkins erstellt.
Um nicht mehr vorhandene Build Jobs zu entfernen, kann manuell "Scan Namespace Now" gestartet werden.

Um einen solchen Ordner für einen kompletten Namespace zu erstellen, muss zunächst "New Item" auf der Startseite
von Jenkins gewählt werden. Danach kann ein Name eingetragen und der Punkt "SCM-Manager Namespace" gewählt werden.

![How to select a namespace item](assets/select-namespace-item.png)

In der Konfiguration muss nun die Instanz URL des SCM-Manager Servers eingetragen und eine gültige Authentifizierung
gewählt werden. Das Plugin lädt daraufhin alle verfügbaren Namespaces, von denen einer gewählt werden kann.

![Example of a config for a namespace item](assets/config-namespace-item.png)

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
[jenkins Plugin](https://www.scm-manager.org/plugins/scm-jenkins-plugin/) für das automatische Starten der Builds sowie
dem [CI Plugin](https://www.scm-manager.org/plugins/scm-ci-plugin/) zur Anzeige der Build Status im SCM-Manager
benötigt. Zudem können mit dem [SSH Plugin](https://www.scm-manager.org/plugins/scm-ssh-plugin/) Verbindungen zwischen
Jenkins und SCM-Manager per SSH erzeugt werden.
