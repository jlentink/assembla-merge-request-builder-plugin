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
import org.jenkinsci.plugins.assembla.api.models.User;
import org.jenkinsci.plugins.assembla.cause.AssemblaMergeRequestCause;
import org.jenkinsci.plugins.assembla.cause.AssemblaPushCause;
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

    private boolean buildOnMergeRequestEnabled;
    private boolean mergeRequestCommentsEnabled;
    private boolean ticketCommentsEnabled;
    private boolean notifyOnStartEnabled;

    private boolean triggerOnPushEnabled;

    private String branchesToBuild;

    private transient AssemblaBuildReporter builder;

    @DataBoundConstructor
    public AssemblaBuildTrigger(String spaceName, String repoName,
                                boolean buildOnMergeRequestEnabled,
                                boolean mergeRequestCommentsEnabled,
                                boolean ticketCommentsEnabled,
                                boolean notifyOnStartEnabled,
                                boolean triggerOnPushEnabled,
                                String branchesToBuild) {
        this.spaceName = spaceName.trim();
        this.repoName = repoName.trim();
        this.buildOnMergeRequestEnabled = buildOnMergeRequestEnabled;
        this.mergeRequestCommentsEnabled = mergeRequestCommentsEnabled;
        this.ticketCommentsEnabled = ticketCommentsEnabled;
        this.notifyOnStartEnabled = notifyOnStartEnabled;
        this.triggerOnPushEnabled = triggerOnPushEnabled;
        this.branchesToBuild = branchesToBuild.trim();
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
        if (!StringUtils.isEmpty(repoName)) {
            DESCRIPTOR.removeRepoTrigger(this, super.job);
        }
        super.stop();
    }

    public QueueTaskFuture<?> handleMergeRequest(AssemblaMergeRequestCause cause) {
        if (!buildOnMergeRequestEnabled) {
            return null;
        }
        Map<String, ParameterValue> values = getDefaultParameters();

        values.put("assemblaMergeRequestId", new StringParameterValue("assemblaMergeRequestId", String.valueOf(cause.getMergeRequestId())));
        values.put("assemblaRefName", new StringParameterValue("assemblaRefName", cause.getCommitId()));
        values.put("assemblaSourceSpaceId", new StringParameterValue("assemblaSourceSpaceId", cause.getSourceSpaceId()));
        values.put("assemblaSourceRepositoryUrl", new StringParameterValue("assemblaSourceRepositoryUrl", cause.getSourceRepositoryUrl()));
        values.put("assemblaSourceBranch", new StringParameterValue("assemblaSourceBranch", cause.getSourceBranch()));
        values.put("assemblaTargetBranch", new StringParameterValue("assemblaTargetBranch", cause.getTargetBranch()));
        values.put("assemblaDescription", new StringParameterValue("assemblaDescription", cause.getDescription()));
        values.put("assemblaAuthorName", new StringParameterValue("assemblaAuthorName", cause.getAuthorName()));

        List<ParameterValue> listValues = new ArrayList<>(values.values());
        return job.scheduleBuild2(0, cause, new ParametersAction(listValues));
    }

    public QueueTaskFuture<?> handlePush(AssemblaPushCause cause) {
        List<String> buildableBranches = Arrays.asList(branchesToBuild.split(","));

        if (!(triggerOnPushEnabled && (buildableBranches.contains(cause.getSourceBranch()) || branchesToBuild.isEmpty()))) {
            return null;
        }

        Map<String, ParameterValue> values = getDefaultParameters();

        values.put("assemblaRefName", new StringParameterValue("assemblaRefName", cause.getCommitId()));
        values.put("assemblaSourceSpaceId", new StringParameterValue("assemblaSourceSpaceId", cause.getSourceSpaceId()));
        values.put("assemblaSourceRepositoryUrl", new StringParameterValue("assemblaSourceRepositoryUrl", cause.getSourceRepositoryUrl()));
        values.put("assemblaSourceBranch", new StringParameterValue("assemblaSourceBranch", cause.getSourceBranch()));
        values.put("assemblaDescription", new StringParameterValue("assemblaDescription", cause.getDescription()));
        values.put("assemblaAuthorName", new StringParameterValue("assemblaAuthorName", cause.getAuthorName()));

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

    public AssemblaBuildReporter getBuilder() {
        if (builder == null) {
            builder = new AssemblaBuildReporter(this);
        }
        return builder;
    }

    public static AssemblaBuildTriggerDescriptor getDesc() {
        return DESCRIPTOR;
    }

    public static AssemblaClient getAssembla() {
        return getAssembla(DESCRIPTOR.getBotApiKey(), DESCRIPTOR.getBotApiSecret());
    }

    public static AssemblaClient getAssembla(String key, String secret) {
        return new AssemblaClient(key, secret);
    }

    public static AssemblaBuildTrigger getTrigger(AbstractProject project) {
        Trigger trigger = project.getTrigger(AssemblaBuildTrigger.class);

        if (trigger == null || !(trigger instanceof AssemblaBuildTrigger)) {
            return null;
        }
        return (AssemblaBuildTrigger) trigger;
    }

    public boolean isMergeRequestCommentsEnabled() {
        return mergeRequestCommentsEnabled;
    }

    public boolean isTicketCommentsEnabled() {
        return ticketCommentsEnabled;
    }

    public boolean isNotifyOnStartEnabled() {
        return notifyOnStartEnabled;
    }

    public boolean isBuildOnMergeRequestEnabled() {
        return buildOnMergeRequestEnabled;
    }

    public boolean isTriggerOnPushEnabled() {
        return triggerOnPushEnabled;
    }

    public String getBranchesToBuild() {
        return branchesToBuild;
    }

    public static final class AssemblaBuildTriggerDescriptor extends TriggerDescriptor {
        private String botApiKey = "";
        private Secret botApiSecret;
        // private String skipBuildPhrase = "[skip ci]";
        private String buildDescriptionTemplate = "MR <a title=\"$mrTitle\" href=\"$mrUrl\">#$mrId</a>: $mrAbbrTitle";;
        private String buildStartedTemplate = "Build started, monitor at $buildUrl";
        private String buildResultTemplate  = "$jobName finished with status: $buildStatus\n"
                                            + "Source revision: $assemblaRefName\n"
                                            + "Build results available at: $buildUrl\n";

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
            return "Assembla Build Triggers";
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            botApiKey = formData.getString("botApiKey");
            botApiSecret = Secret.fromString(formData.getString("botApiSecret"));
            buildDescriptionTemplate = formData.getString("buildDescriptionTemplate");
            buildResultTemplate = formData.getString("buildResultTemplate");
            buildStartedTemplate = formData.getString("buildStartedTemplate");

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

        public FormValidation doTestCredentials(@QueryParameter("botApiKey") String key, @QueryParameter("botApiSecret") String secret) {
            User user;
            try {
                user = AssemblaBuildTrigger.getAssembla(key, secret).getUser();
            } catch (AssemblaClient.UnauthorizedError ex) {
                return FormValidation.error("Invalid credentials");
            }

            return FormValidation.ok("Successfully logged in as: " + user.getLogin() + " (" + user.getName() + ")");
        }


        public FormValidation doCheckSettings(@QueryParameter("spaceName") String spaceName, @QueryParameter("repoName") String repoName) {
            if (spaceName == null || spaceName.isEmpty()) {
                return FormValidation.error("You must provide a space name");
            }

            if (repoName == null || repoName.isEmpty()) {
                return FormValidation.error("You must provide a space name");
            }

            try {
                getAssembla().getSpace(spaceName);
            } catch (AssemblaClient.UnauthorizedError ex) {
                return FormValidation.error("Unable to authenticate. Please check API credentials on Jenkins system configuration page");
            } catch (AssemblaClient.ForbiddenError ex) {
                return FormValidation.error("You do not have permissions to access this space. Are you a member?");
            } catch (AssemblaClient.NotFoundError ex) {
                return FormValidation.error("Could not find space  " + spaceName);
            }

            try {
                getAssembla().getTool(spaceName, repoName);
            } catch (AssemblaClient.UnauthorizedError ex) {
                return FormValidation.error("Unable to authenticate. Please check API credentials on Jenkins system configuration page");
            } catch (AssemblaClient.ForbiddenError ex) {
                return FormValidation.error("You do not have permissions to access this tool");
            } catch (AssemblaClient.NotFoundError ex) {
                return FormValidation.error("Could not find repo with name " + repoName + " in space " + spaceName);
            }

            return FormValidation.ok("It's all good!");
        }

        public void addRepoTrigger(AssemblaBuildTrigger trigger, AbstractProject<?, ?> project) {
            String projectKey = getProjectKey(trigger);
            if (project == null || StringUtils.isEmpty(projectKey)) {
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

        public Set<AbstractProject<?, ?>> getRepoJobs(String spaceName, String repoName) {
            Set<AbstractProject<?, ?>> projects = repoJobs.get(getProjectKey(spaceName, repoName));

            if (projects == null) {
                projects = new HashSet<>();
            }

            return projects;
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

        private String getProjectKey(AssemblaBuildTrigger trigger) {
            return getProjectKey(trigger.getSpaceName(), trigger.getRepoName());
        }

        private String getProjectKey(String spaceName, String repoName) {
            return spaceName + ":" + repoName;
        }

        public String getBuildDescriptionTemplate() {
            return buildDescriptionTemplate;
        }

        public String getSkipBuildPhrase() {
            return skipBuildPhrase;
        }

        public String getBuildResultTemplate() {
            return buildResultTemplate;
        }

        public String getBuildStartedTemplate() {
            return buildStartedTemplate;
        }
    }
}
