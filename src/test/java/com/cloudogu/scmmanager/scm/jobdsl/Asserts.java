package com.cloudogu.scmmanager.scm.jobdsl;

import jenkins.scm.api.trait.SCMTrait;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class Asserts {

    private Asserts() {
    }

    public static void assertContainsOnlyInstancesOf(Collection<?> instances, Class<?>... expected) {
        Set<Class<?>> classes = instances.stream()
            .map(Object::getClass)
            .collect(Collectors.toSet());
        assertThat(classes).containsOnly(expected);
    }

}
