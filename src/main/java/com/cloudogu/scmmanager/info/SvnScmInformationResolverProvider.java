package com.cloudogu.scmmanager.info;

import hudson.Extension;

@Extension(optional = true)
public class SvnScmInformationResolverProvider extends AbstractScmInformationResolverProvider {

    public SvnScmInformationResolverProvider() {
        super("subversion");
    }

    @Override
    public ScmInformationResolver create() {
        return new SvnScmInformationResolver();
    }
}
