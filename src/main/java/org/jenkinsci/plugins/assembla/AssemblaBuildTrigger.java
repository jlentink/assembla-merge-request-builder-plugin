package org.jenkinsci.plugins.assembla;

import hudson.Extension;
import hudson.model.*;
import hudson.model.queue.QueueTaskFuture;
import hudson.triggers.Trigger;
import hudson.triggers.TriggerDescriptor;
import hudson.util.FormValidation;
import hudson.util.Secret;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.assembla.api.AssemblaClient;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Created by pavel on 16/2/16.
 */
public class AssemblaBuildTrigger extends Trigger<AbstractProject<?, ?>> {
    @Extension
    public static final AssemblaBuildTriggerDescriptor DESCRIPTOR = new AssemblaBuildTriggerDescriptor();
    private static final Logger LOGGER = Logger.getLogger(AssemblaBuildTrigger.class.getName());

    private final String spaceName;
    private final String repoName;
    private boolean mergeRequestComments;
    private boolean ticketComments;
    private boolean notifyOnStart;
    private transient AssemblaBuilder builder;

    @DataBoundConstructor
    public AssemblaBuildTrigger(String spaceName, String repoName, boolean mergeRequestComments, boolean ticketComments, boolean notifyOnStart) {
        this.spaceName = spaceName;
        this.repoName = repoName;
        this.mergeRequestComments = mergeRequestComments;
        this.ticketComments = ticketComments;
        this.notifyOnStart = notifyOnStart;
    }

    @Override
    public void start(AbstractProject<?, ?> project, boolean newInstance) {
        super.start(project, newInstance);

        String name = project.getFullName();

        LOGGER.info("Trigger started for " + project.toString() + ". Repo name: " + repoName);

        if (project.isDisabled()) {
            LOGGER.info("Project is disabled, not starting trigger for job " + name);
            return;
        }

        DESCRIPTOR.addRepoTrigger(this, super.job);
    }

    @Override
    public void stop() {
        LOGGER.info("Trigger stopped. Repo name: " + repoName);
        if (!StringUtils.isEmpty(repoName)) {
            DESCRIPTOR.removeRepoTrigger(this, super.job);
        }
        super.stop();
    }

    public QueueTaskFuture<?> startJob(AssemblaCause cause) {
        Map<String, ParameterValue> values = getDefaultParameters();

        values.put("assemblaMergeRequestId", new StringParameterValue("assemblaMergeRequestId", String.valueOf(cause.getMergeRequestId())));
        values.put("assemblaSourceSpaceId", new StringParameterValue("assemblaSourceSpaceId", cause.getSourceSpaceId()));
        values.put("assemblaSourceRepositoryUrl", new StringParameterValue("assemblaSourceRepositoryUrl", cause.getSourceRepositoryUrl()));
        values.put("assemblaSourceBranch", new StringParameterValue("assemblaSourceBranch", cause.getSourceBranch()));
        values.put("assemblaTargetBranch", new StringParameterValue("assemblaTargetBranch", cause.getTargetBranch()));
        values.put("assemblaDescription", new StringParameterValue("assemblaDescription", cause.getDescription()));

        List<ParameterValue> listValues = new ArrayList<>(values.values());
        return job.scheduleBuild2(0, cause, new ParametersAction(listValues));
    }

    private Map<String, ParameterValue> getDefaultParameters() {
        Map<String, ParameterValue> values = new HashMap<>();
        ParametersDefinitionProperty definitionProperty = job.getProperty(ParametersDefinitionProperty.class);

        if (definitionProperty != null) {
            for (ParameterDefinition definition : definitionProperty.getParameterDefinitions()) {
                values.put(definition.getName(), definition.getDefaultParameterValue());
            }
        }

        return values;
    }

    @Override
    public AssemblaBuildTriggerDescriptor getDescriptor() {
        return DESCRIPTOR;
    }

    public String getSpaceName() {
        return spaceName;
    }

    public String getRepoName() {
        return repoName;
    }

    public AssemblaBuilder getBuilder() {
        if (builder == null) {
            builder = new AssemblaBuilder(this);
        }
        return builder;
    }

    public void handleMergeRequest(AssemblaCause cause) {
        LOGGER.info("Handling merge request");
        LOGGER.info("Space name: " + spaceName);
        LOGGER.info("Repo name: " + repoName);
        LOGGER.info("Job name: " + job.getFullDisplayName());
        startJob(cause);
    }

    public static AssemblaBuildTriggerDescriptor getDesc() {
        return DESCRIPTOR;
    }

    public static AssemblaClient getAssembla() {
        return new AssemblaClient(
                DESCRIPTOR.getBotApiKey(),
                DESCRIPTOR.getBotApiSecret()
        );
    }

    public static AssemblaBuildTrigger getTrigger(AbstractProject project) {
        Trigger trigger = project.getTrigger(AssemblaBuildTrigger.class);

        if (trigger == null || !(trigger instanceof AssemblaBuildTrigger)) {
            return null;
        }
        return (AssemblaBuildTrigger) trigger;
    }

    public boolean isMergeRequestComments() {
        return mergeRequestComments;
    }

    public boolean isTicketComments() {
        return ticketComments;
    }

    public boolean isNotifyOnStart() {
        return notifyOnStart;
    }

    public static final class AssemblaBuildTriggerDescriptor extends TriggerDescriptor {
        private String botApiKey = "";
        private Secret botApiSecret;
        private String successMessage = "Build finished.  Tests PASSED.";
        private String unstableMessage = "Build finished.  Tests FAILED.";
        private String failureMessage = "Build finished.  Tests FAILED.";

        private transient final Map<String, Set<AbstractProject<?, ?>>> repoJobs;

        public AssemblaBuildTriggerDescriptor() {
            load();
            repoJobs = new ConcurrentHashMap<>();
        }

        @Override
        public boolean isApplicable(Item item) {
            return item instanceof AbstractProject;
        }

        @Override
        public String getDisplayName() {
            return "Assembla Merge Requests Builder";
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            botApiKey = formData.getString("botApiKey");
            botApiSecret = Secret.fromString(formData.getString("botApiSecret"));
            successMessage = formData.getString("successMessage");
            unstableMessage = formData.getString("unstableMessage");
            failureMessage = formData.getString("failureMessage");

            save();

            return super.configure(req, formData);
        }

        public FormValidation doCheckBotApiKey(@QueryParameter String value) {
            if (value == null || value.isEmpty()) {
                return FormValidation.error("You must provide an API key for the Jenkins user");
            }

            return FormValidation.ok();
        }

        public FormValidation doCheckBotApiSecret(@QueryParameter String value) {
            if (value == null || value.isEmpty()) {
                return FormValidation.error("You must provide an API secret for the Jenkins user");
            }

            return FormValidation.ok();
        }

        public String getBotApiKey() {
            return botApiKey;
        }

        public String getBotApiSecret() {
            if (botApiSecret == null) {
                return "";
            }
            return botApiSecret.getPlainText();
        }

        public String getSuccessMessage() {
            if (successMessage == null) {
                successMessage = "Build finished.  Tests PASSED.";
            }
            return successMessage;
        }

        public String getUnstableMessage() {
            if (unstableMessage == null) {
                unstableMessage = "Build finished.  Tests FAILED.";
            }
            return unstableMessage;
        }

        public String getFailureMessage() {
            if (failureMessage == null) {
                failureMessage = "Build finished.  Tests FAILED.";
            }
            return failureMessage;
        }

        public void addRepoTrigger(AssemblaBuildTrigger trigger, AbstractProject<?, ?> project) {
            String projectKey = getProjectKey(trigger);
            if (project == null || StringUtils.isEmpty(projectKey)) {
                LOGGER.info("Not adding a trigger");
                LOGGER.info("project is: " + project);
                LOGGER.info("repo name is: " + projectKey);
                return;
            }
            LOGGER.info("Adding trigger for repo: " + projectKey);

            synchronized (repoJobs) {
                Set<AbstractProject<?, ?>> projects = repoJobs.get(projectKey);

                if (projects == null) {
                    projects = new HashSet<>();
                    repoJobs.put(projectKey, projects);
                }

                 projects.add(project);
            }
        }

        public void removeRepoTrigger(AssemblaBuildTrigger trigger, AbstractProject<?, ?> project) {
            String projectKey = getProjectKey(trigger);
            Set<AbstractProject<?, ?>> projects = repoJobs.get(projectKey);
            if (project == null || projects == null || StringUtils.isEmpty(projectKey)) {
                return;
            }
            LOGGER.info("Removing trigger for repo: " + projectKey);
            projects.remove(project);
        }

        public Set<AbstractProject<?, ?>> getRepoTriggers(String spaceName, String repoName) {
            Set<AbstractProject<?, ?>> projects = repoJobs.get(getProjectKey(spaceName, repoName));

            if (projects == null) {
                projects = new HashSet<>();
            }

            return projects;
        }

        private String getProjectKey(AssemblaBuildTrigger trigger) {
            return getProjectKey(trigger.getSpaceName(), trigger.getRepoName());
        }

        private String getProjectKey(String spaceName, String repoName) {
            return spaceName + ":" + repoName;
        }
    }

}
