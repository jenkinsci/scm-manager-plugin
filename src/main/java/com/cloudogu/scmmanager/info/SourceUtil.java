package com.cloudogu.scmmanager.info;

import com.cloudogu.scmmanager.scm.ScmManagerSource;
import hudson.model.Item;
import hudson.model.Run;
import jenkins.scm.api.SCMSource;
import jenkins.scm.api.SCMSourceOwner;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class SourceUtil {

  private SourceUtil() {
  }

  static <T extends SCMSource> List<String> getSources(Run<?, ?> run, Class<T> sourceType, Function<T, String> urlExtractor) {
    return extractSourceOwner(run)
      .map(sourceOwner ->
        sourceOwner
          .getSCMSources()
          .stream()
          .filter(scmSource -> canExtract(scmSource, sourceType))
          .map(scmSource -> extract(scmSource, sourceType, urlExtractor))
          .collect(Collectors.toList())
      ).orElse(Collections.emptyList());
  }

  private static Optional<SCMSourceOwner> extractSourceOwner(Run<?, ?> run) {
    Object current = run.getParent();
    while (!(current instanceof SCMSourceOwner)) {
      if (current == null) {
        return Optional.empty();
      } else if (current instanceof Item) {
        current = ((Item) current).getParent();
      } else {
        return Optional.empty();
      }
    }
    return Optional.of((SCMSourceOwner) current);

  }

  private static boolean canExtract(SCMSource scmSource, Class<?> sourceType) {
    return sourceType.isAssignableFrom(scmSource.getClass()) || scmSource instanceof ScmManagerSource;
  }

  private static <T> String extract(SCMSource scmSource, Class<T> sourceType, Function<T, String> urlExtractor) {
    if (scmSource instanceof ScmManagerSource) {
      return ((ScmManagerSource) scmSource).getRemoteUrl();
    } else {
      return urlExtractor.apply(sourceType.cast(scmSource));
    }
  }
}
