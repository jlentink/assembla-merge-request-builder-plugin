# Assembla Merge Request Builder Plugin

A plugin that allows Jenkins to build merge requests.

This plugin fetches the source and target branches of a Assembla merge request and makes them available
to your build via build parameters. Once the build completes, Jenkins will leave a comment on the merge
request and related tickets indicating whether the merge request was successful.

## Prerequisites

* Whilst there is no explicit dependency on the [Git plugin](https://wiki.jenkins-ci.org/display/JENKINS/Git+Plugin),
  it's strongly recommended that you install it since Jenkins will be unable to fetch the source code for your project.
* It's also recommended to install [Assembla Auth plugin](https://wiki.jenkins-ci.org/display/JENKINS/Assembla+Auth+Plugin)

## Installation

* Ensure that a Jenkins user exists within your Assembla space, has access to the repository and has permissions to leave comments. Ensure that the user
  has **Member** level access to the project.
* Install the plugin in Jenkins. `Only manual installation for now. TODO: Fix readme after release`
    * The plugin is hosted on the [Jenkins Plugin repository](https://wiki.jenkins-ci.org/display/JENKINS/Assembla+Merge+Request+Builder+Plugin) 
    * Go to ``Jenkins`` -> ``Manage Plugins`` -> ``Available``
    * Search for ``Assembla Merge Request Builder``
    * And install it
    * Ensure you restart Jenkins
* Go to ``Manage Jenkins`` -> ``Configure System`` -> ``Assembla Merge Request Builder``
* Set your ``User API key`` key for the Jenkins user. This can be found by logging into Assembla as Jenkins and going to the [account page](https://www.assembla.com/user/edit/manage_clients) 
* Set your ``User API secret`` secret for the Jenkins user. This can be found the same way as API key.
* Click `Test credentials` button 
* Set/change any of the other available parameters as necessary.
* ``Save`` to preserve your changes.
* Go to `Manage Jenkins` -> `Configure Global Security` and set `Markup Formatter` to *Safe HTML*. It will make Jenkins display links in build history properly.

## Webhooks
* Make sure that you have webhook installed and you have a git repository in your space. If no, go to Admin -> Tools -> More -> Webhook section and click "Add" button
* Set Assembla webhook to trigger you jenkins server. Go to https://www.assembla.com/spaces/`your_space_name`/webhooks. Create new webhook, select "Assembla Jenkins plugin" from template (Make sure "Code comments" and "Code commits" is checked in Post updates about section).

## Creating a Job

* Create a new job by going to ``New Job``
* Set the ``Project Name``
* In the ``Source Code Management`` section:
    * Click ``Git`` and enter your Repository URL and in Advanced set its Name to ``origin``
    * For merge requests from forked repositories add another repository with Repository URL ``${assemblaSourceRepository}``.
    * In ``Branch Specifier`` enter ``${assemblaRefName}``
    * You configure jenkins to merge source with target branch before build. In the ``Additional Behaviours`` section:
        * Click the ``Add`` drop down button and the ``Merge before build`` item
        * Specify the name of the repository as ``origin`` (if origin corresponds to Assembla) and enter the
          ``Branch to merge to`` as ``${assemblaTargetBranch}``
        * **Ensure ``Prune stale remote-tracking branches`` is not added**
* In the ``Build Triggers`` section:
    * Check the ``Assembla Triggers``
    
* Configure any other pre build, build or post build actions as necessary
* ``Save`` to preserve your changes

## Manual Triggers

You can trigger a job a manually by clicking ``This build is parameterized`` and adding the relevant build parameters.
These include:

* $assemblaRefName - build git revision
* $assemblaSourceRepositoryUrl - assembla repository url
* $assemblaTargetBranch - merge request target branch
* $assemblaSourceBranch - merge request source branch

__Note:__  a manually triggered build will not add build triggered/succeeded/failed comments to the merge request.
__Note:__  You should ensure that the 'Global Config user.name Value' and 'Global Config user.email Value' are both set for your git plugin.  In some cases, you will get an error indicating that a branch cannot be merged if these are not set.

## Message templates

Plugin allows to customize build messages using templates (``Manage Jenkins`` -> ``Configure System`` -> ``Assembla Merge Request Builder``). 
You can use any env variables or build parameters in templates. For example, you can mention your QA team to test changes
or mention merge request author about build result etc. Additionally plugin provides:

* `$mrTitle` - merge request title
* `$mrAbbrTitle` - merge request title truncated to 30 chars
* `$mrUrl` - merge request url
* `$mrId` - merge request id
* `$jobName` - jenkins job name
* `$buildStatus` - build status (available only if build is completed)
* `$buildUrl` - build url
* `$assemblaSourceSpaceId` - assembla space id
* `$assemblaDescription` - merge request description
* `$assemblaSourceRepositoryUrl` - assembla repository url
* `$assemblaTargetBranch` - merge request target branch
* `$assemblaSourceBranch` - merge request source branch
* `$assemblaMergeRequestId` - assembla merge request id
* `$assemblaRefName` - build git revision

## Contributing

* Check out the latest master to make sure the feature hasn't been implemented or the bug hasn't been fixed yet
* Check out the issue tracker to make sure someone already hasn't requested it and/or contributed it
* Fork the project
* Start a feature/bugfix branch
* Commit and push until you are happy with your contribution
* Make sure to add tests for it. This is important so I don't break it in a future version unintentionally.
* Please try not to mess with the version, or history. If you want to have your own version, or is otherwise necessary, that is fine,
  but please isolate to its own commit so I can cherry-pick around it.

## Copyright

Copyright (c) 2016 Pavel Dotsulenko. See LICENSE for further details.