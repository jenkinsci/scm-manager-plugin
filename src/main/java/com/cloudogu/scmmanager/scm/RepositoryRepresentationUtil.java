package com.cloudogu.scmmanager.scm;

import com.cloudogu.scmmanager.scm.api.Repository;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RepositoryRepresentationUtil {

    private static final Pattern REPOSITORY_WITH_TYPE_PARENTHESIS_PATTERN =
            Pattern.compile("^([^/]+)/([^/]+) \\(([a-z]+)\\)$");
    private static final Pattern REPOSITORY_WITH_TYPE_SLASH_PATTERN = Pattern.compile("^([^/]+)/([^/]+)/([a-z]+)$");
    private static final Pattern REPOSITORY_WITHOUT_TYPE_PATTERN = Pattern.compile("^([^/]+)/([^/(]+)$");

    public static RepositoryRepresentation parse(String value) {
        Matcher parenMatcher = REPOSITORY_WITH_TYPE_PARENTHESIS_PATTERN.matcher(value);
        if (parenMatcher.matches()) {
            return new RepositoryRepresentation(parenMatcher.group(1), parenMatcher.group(2), parenMatcher.group(3));
        }
        Matcher slashMatcher = REPOSITORY_WITH_TYPE_SLASH_PATTERN.matcher(value);
        if (slashMatcher.matches()) {
            return new RepositoryRepresentation(slashMatcher.group(1), slashMatcher.group(2), slashMatcher.group(3));
        }
        Matcher matcher = REPOSITORY_WITHOUT_TYPE_PATTERN.matcher(value);
        if (matcher.matches()) {
            return new RepositoryRepresentation(matcher.group(1), matcher.group(2), null);
        }
        throw new IllegalArgumentException("Invalid repository representation: " + value);
    }

    public static String format(RepositoryRepresentation repositoryRepresentation) {
        return String.format(
                "%s/%s (%s)",
                repositoryRepresentation.namespace(), repositoryRepresentation.name(), repositoryRepresentation.type());
    }

    public static String format(Repository repository) {
        return format(
                new RepositoryRepresentation(repository.getNamespace(), repository.getName(), repository.getType()));
    }

    public record RepositoryRepresentation(String namespace, String name, String type) {}
}
