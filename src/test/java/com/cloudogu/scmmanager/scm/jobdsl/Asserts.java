package com.cloudogu.scmmanager.scm.jobdsl;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

public class Asserts {

    private Asserts() {}

    public static void assertContainsOnlyInstancesOf(Collection<?> instances, Class<?>... expected) {
        Set<Class<?>> classes = instances.stream().map(Object::getClass).collect(Collectors.toSet());
        assertThat(classes).containsOnly(expected);
    }
}
