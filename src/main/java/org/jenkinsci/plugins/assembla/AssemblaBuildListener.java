package org.jenkinsci.plugins.assembla;

import hudson.Extension;
import hudson.model.AbstractBuild;
import hudson.model.TaskListener;
import hudson.model.listeners.RunListener;

import java.util.logging.Logger;

/**
 * Created by pavel on 18/2/16.
 */
@Extension
public class AssemblaBuildListener extends RunListener<AbstractBuild> {

    private static final Logger LOGGER = Logger.getLogger(AssemblaBuildListener.class.getName());

    @Override
    public void onStarted(AbstractBuild abstractBuild, TaskListener listener) {
        AssemblaBuildTrigger trigger = AssemblaBuildTrigger.getTrigger(abstractBuild.getProject());

        if (trigger == null) {
            return;
        }

        trigger.getBuildReporter().onStarted(abstractBuild, listener);
    }

    @Override
    public void onCompleted(AbstractBuild abstractBuild, TaskListener listener) {
        AssemblaBuildTrigger trigger = AssemblaBuildTrigger.getTrigger(abstractBuild.getProject());

        if (trigger == null) {
            return;
        }

        trigger.getBuildReporter().onCompleted(abstractBuild, listener);
    }
}