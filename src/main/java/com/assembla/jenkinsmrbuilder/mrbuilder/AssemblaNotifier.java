package com.assembla.jenkins.mrbuilder;

import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import java.util.logging.Logger;

/**
 * Created by pavel on 11/2/16.
 */
public class AssemblaNotifier extends Notifier {

    private static final Logger logger = Logger.getLogger(AssemblaNotifier.class.getName());

    private boolean commentMergeRequestEnabled;
    private boolean voteMergeRequestEnabled;

    @DataBoundConstructor
    public AssemblaNotifier(final boolean commentMergeRequestEnabled, final boolean voteMergeRequestEnabled) {
        super();
        this.commentMergeRequestEnabled = commentMergeRequestEnabled;
        this.voteMergeRequestEnabled = voteMergeRequestEnabled;
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }


    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    @Extension
    public static class DescriptorImpl extends BuildStepDescriptor<Publisher> {

        private boolean commentMergeRequestEnabled;
        private boolean voteMergeRequestEnabled;

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return "Assembla Notifications";
        }

        public boolean isCommentMergeRequestEnabled() {
            return commentMergeRequestEnabled;
        }

        public boolean isVoteMergeRequestEnabled() {
            return voteMergeRequestEnabled;
        }

        @Override
        public AssemblaNotifier newInstance(StaplerRequest sr, JSONObject json) {
            boolean commentMergeRequestEnabled = "true".equals(sr.getParameter("commentMergeRequestEnabled"));
            boolean voteMergeRequestEnabled = "true".equals(sr.getParameter("voteMergeRequestEnabled"));

            return new AssemblaNotifier(commentMergeRequestEnabled, voteMergeRequestEnabled);
        }
    }
}
