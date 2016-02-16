package org.jenkinsci.plugins.assembla;

import hudson.Extension;
import hudson.model.*;
import hudson.model.queue.QueueTaskFuture;
import hudson.triggers.Trigger;
import hudson.triggers.TriggerDescriptor;
import hudson.util.FormValidation;
import hudson.util.Secret;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by pavel on 16/2/16.
 */
public class AssemblaBuildTrigger extends Trigger<AbstractProject<?, ?>> {
    private static final Logger LOGGER = Logger.getLogger(AssemblaBuildTrigger.class.getName());

    private final String spaceName;
    private final String repoName;

    @DataBoundConstructor
    public AssemblaBuildTrigger(String spaceName, String repoName) {
        this.spaceName = spaceName;
        this.repoName = repoName;
    }

    @Override
    public void start(AbstractProject<?, ?> project, boolean newInstance) {
        AssemblaWebhook.setTrigger(this);
//        AssemblaBuilder.build(this);
        LOGGER.info("Build triggered!");
        super.start(project, newInstance);
    }

    public QueueTaskFuture<?> startJob(AssemblaCause cause) {
        Map<String, ParameterValue> values = getDefaultParameters();

        values.put("assemblaMergeRequestId", new StringParameterValue("assemblaMergeRequestId", String.valueOf(cause.getMergeRequestId())));
        values.put("assemblaSourceName", new StringParameterValue("assemblaSourceName", cause.getSourceName()));
        values.put("assemblaSourceRepository", new StringParameterValue("assemblaSourceRepository", cause.getSourceRepository()));
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

    @Extension
    public static final AssemblaBuildTriggerDescriptor DESCRIPTOR = new AssemblaBuildTriggerDescriptor();

    @Override
    public AssemblaBuildTriggerDescriptor getDescriptor() {
        return DESCRIPTOR;
    }

    public static final class AssemblaBuildTriggerDescriptor extends TriggerDescriptor {
        private String botApiKey = "";
        private Secret botApiSecret;
        private String successMessage = "Build finished.  Tests PASSED.";
        private String unstableMessage = "Build finished.  Tests FAILED.";
        private String failureMessage = "Build finished.  Tests FAILED.";

        public AssemblaBuildTriggerDescriptor() {
            load();
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

    }

}
