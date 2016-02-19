package org.jenkinsci.plugins.assembla;

import hudson.Extension;
import hudson.model.AbstractBuild;
import hudson.model.TaskListener;
import hudson.model.listeners.RunListener;

/**
 * Created by pavel on 18/2/16.
 */
@Extension
public class AssemblaBuildListener extends RunListener<AbstractBuild> {

    @Override
    public void onStarted(AbstractBuild abstractBuild, TaskListener listener) {
        AssemblaBuildTrigger trigger = AssemblaBuildTrigger.getTrigger(abstractBuild.getProject());

        if (trigger == null) {
            return;
        }

        trigger.getBuilder().onStarted(abstractBuild);

    }

    @Override
    public void onCompleted(AbstractBuild abstractBuild, TaskListener listener) {
        AssemblaBuildTrigger trigger = AssemblaBuildTrigger.getTrigger(abstractBuild.getProject());

        if (trigger == null) {
            return;
        }

        trigger.getBuilder().onCompleted(abstractBuild);
    }
}